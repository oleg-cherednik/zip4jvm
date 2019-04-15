package net.lingala.zip4j.core.writers;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.lingala.zip4j.io.OutputStreamDecorator;
import net.lingala.zip4j.model.CentralDirectory;
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
        final boolean writeZip64FileSize = fileHeader.getCompressedSize() >= InternalZipConstants.ZIP_64_LIMIT ||
                fileHeader.getUncompressedSize() + HeaderWriter.ZIP64_EXTRA_BUF >= InternalZipConstants.ZIP_64_LIMIT;
        final boolean writeZip64OffsetLocalHeader = fileHeader.getOffsLocalFileHeader() > InternalZipConstants.ZIP_64_LIMIT;

        byte[] fileName = fileHeader.getFileName(zipModel.getCharset());
        byte[] fileComment = fileHeader.getFileComment(zipModel.getCharset());

        out.writeDword(fileHeader.getSignature());
        out.writeWord(fileHeader.getVersionMadeBy());
        out.writeWord(fileHeader.getVersionToExtract());
        out.writeWord(fileHeader.getGeneralPurposeFlag().getData());
        out.writeShort(fileHeader.getCompressionMethod().getValue());
        out.writeDword(fileHeader.getLastModifiedTime());
        out.writeDword((int)fileHeader.getCrc32());
        out.writeDword(getCompressedSize(fileHeader, writeZip64FileSize));
        out.writeDword(getUncompressedSize(fileHeader, writeZip64FileSize));
        out.writeShort((short)fileName.length);
        out.writeWord(getExtraFieldLength(fileHeader, writeZip64FileSize, writeZip64OffsetLocalHeader));
        out.writeShort((short)fileComment.length);
        out.writeShort((short)fileHeader.getDiskNumber());
        out.writeBytes(fileHeader.getInternalFileAttributes() != null ? fileHeader.getInternalFileAttributes() : new byte[2]);
        out.writeBytes(fileHeader.getExternalFileAttributes() != null ? fileHeader.getExternalFileAttributes() : new byte[4]);
        out.writeLongAsInt(getOffsLocalFileHeader(fileHeader, writeZip64OffsetLocalHeader));
        out.writeBytes(fileName);
        new ExtraFieldWriter(fileHeader, zipModel, writeZip64FileSize, writeZip64OffsetLocalHeader).write(out);
        out.writeBytes(fileComment);
    }

    private static long getCompressedSize(CentralDirectory.FileHeader fileHeader, boolean writeZip64FileSize) {
        if (writeZip64FileSize)
            return InternalZipConstants.ZIP_64_LIMIT;
        return fileHeader.getCompressedSize();
    }

    private static long getUncompressedSize(CentralDirectory.FileHeader fileHeader, boolean writeZip64FileSize) {
        if (writeZip64FileSize)
            return InternalZipConstants.ZIP_64_LIMIT;
        return fileHeader.getUncompressedSize();
    }

    private static short getExtraFieldLength(CentralDirectory.FileHeader fileHeader, boolean writeZip64FileSize,
            boolean writeZip64OffsetLocalHeader) {
        int extraFieldLength = 0;
        if (writeZip64FileSize || writeZip64OffsetLocalHeader) {
            extraFieldLength += 4;
            if (writeZip64FileSize)
                extraFieldLength += 16;
            if (writeZip64OffsetLocalHeader)
                extraFieldLength += 8;
        }
        if (fileHeader.getAesExtraDataRecord() != null)
            extraFieldLength += 11;
        return (short)extraFieldLength;
    }

    private long getOffsLocalFileHeader(CentralDirectory.FileHeader fileHeader, boolean writeZip64OffsetLocalHeader) {
        if (writeZip64OffsetLocalHeader)
            return InternalZipConstants.ZIP_64_LIMIT;
        return fileHeader.getOffsLocalFileHeader();
    }

}
