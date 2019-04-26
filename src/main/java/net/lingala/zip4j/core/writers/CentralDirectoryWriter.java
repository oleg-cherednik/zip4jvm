package net.lingala.zip4j.core.writers;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.lingala.zip4j.io.OutputStreamDecorator;
import net.lingala.zip4j.model.CentralDirectory;
import net.lingala.zip4j.model.ExtraField;
import net.lingala.zip4j.model.Zip64ExtendedInfo;
import net.lingala.zip4j.model.ZipModel;
import net.lingala.zip4j.util.InternalZipConstants;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 10.03.2019
 */
@RequiredArgsConstructor
public final class CentralDirectoryWriter {

    @NonNull
    private final CentralDirectory dir;
    @NonNull
    private final ZipModel zipModel;

    public void write(@NonNull OutputStreamDecorator out) throws IOException {
        writeFileHeaders(out);
        new DigitalSignatureWriter(dir.getDigitalSignature()).write(out);
    }

    private void writeFileHeaders(OutputStreamDecorator out) throws IOException {
        for (CentralDirectory.FileHeader fileHeader : dir.getFileHeaders())
            writeFileHeader(fileHeader, out);
    }

    private void writeFileHeader(CentralDirectory.FileHeader fileHeader, OutputStreamDecorator out) throws IOException {
        updateZip64(fileHeader);

        byte[] fileName = fileHeader.getFileName(zipModel.getCharset());
        byte[] fileComment = fileHeader.getFileComment(zipModel.getCharset());

        out.writeDword(fileHeader.getSignature());
        out.writeWord(fileHeader.getVersionMadeBy());
        out.writeWord(fileHeader.getVersionToExtract());
        out.writeWord(fileHeader.getGeneralPurposeFlag().getData());
        out.writeShort(fileHeader.getCompressionMethod().getValue());
        out.writeDword(fileHeader.getLastModifiedTime());
        out.writeDword((int)fileHeader.getCrc32());
        out.writeDword(fileHeader.isWriteZip64FileSize() ? InternalZipConstants.ZIP_64_LIMIT : fileHeader.getCompressedSize());
        out.writeDword(fileHeader.isWriteZip64FileSize() ? InternalZipConstants.ZIP_64_LIMIT : fileHeader.getUncompressedSize());
        out.writeShort((short)fileName.length);
        out.writeWord(ExtraField.getExtraFieldLength(fileHeader));
        out.writeShort((short)fileComment.length);
        out.writeShort((short)fileHeader.getDiskNumber());
        out.writeBytes(fileHeader.getInternalFileAttributes() != null ? fileHeader.getInternalFileAttributes() : new byte[2]);
        out.writeBytes(fileHeader.getExternalFileAttributes() != null ? fileHeader.getExternalFileAttributes() : new byte[4]);
        out.writeLongAsInt(fileHeader.isWriteZip64OffsetLocalHeader() ? InternalZipConstants.ZIP_64_LIMIT : fileHeader.getOffsLocalFileHeader());
        out.writeBytes(fileName);
        new ExtraFieldWriter(fileHeader.getExtraField(), zipModel.getCharset()).write(out);
        out.writeBytes(fileComment);
    }

    // TODO should be updated on the fly
    @Deprecated
    private void updateZip64(CentralDirectory.FileHeader fileHeader) {
        if (fileHeader.isWriteZip64FileSize() || fileHeader.isWriteZip64OffsetLocalHeader())
            zipModel.zip64();

//        if (fileHeader.getExtraField() == null)
//            fileHeader.setExtraField(new ExtraField());
//        if (fileHeader.getExtraField().getZip64ExtendedInfo() == null)
//            fileHeader.getExtraField().setZip64ExtendedInfo(new Zip64ExtendedInfo());

        // TODO move it before
        Zip64ExtendedInfo info = fileHeader.getExtraField().getZip64ExtendedInfo();

        if (info != Zip64ExtendedInfo.NULL) {
            short dataSize = 0;

            if (fileHeader.isWriteZip64FileSize())
                dataSize += 16;
            if (fileHeader.isWriteZip64OffsetLocalHeader())
                dataSize += 8;

            info.setSize(dataSize);
            info.setUncompressedSize(fileHeader.isWriteZip64FileSize() ? fileHeader.getUncompressedSize() : -1);
            info.setCompressedSize(fileHeader.isWriteZip64FileSize() ? fileHeader.getCompressedSize() : -1);
            info.setOffsLocalHeaderRelative(fileHeader.isWriteZip64OffsetLocalHeader() ? fileHeader.getOffsLocalFileHeader() : -1);
        }
    }

}
