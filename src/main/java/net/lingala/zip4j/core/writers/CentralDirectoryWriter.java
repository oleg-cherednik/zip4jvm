package net.lingala.zip4j.core.writers;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.lingala.zip4j.io.OutputStreamDecorator;
import net.lingala.zip4j.model.CentralDirectory;
import net.lingala.zip4j.util.InternalZipConstants;

import java.io.IOException;
import java.nio.charset.Charset;

/**
 * @author Oleg Cherednik
 * @since 10.03.2019
 */
@RequiredArgsConstructor
public final class CentralDirectoryWriter {

    @NonNull
    private final CentralDirectory dir;
    @NonNull
    private final Charset charset;

    public void write(@NonNull OutputStreamDecorator out) throws IOException {
        writeFileHeaders(out);
        new DigitalSignatureWriter(dir.getDigitalSignature()).write(out);
    }

    private void writeFileHeaders(OutputStreamDecorator out) throws IOException {
        for (CentralDirectory.FileHeader fileHeader : dir.getFileHeaders())
            writeFileHeader(fileHeader, out);
    }

    private void writeFileHeader(CentralDirectory.FileHeader fileHeader, OutputStreamDecorator out) throws IOException {
        byte[] fileName = fileHeader.getFileName(charset);
        byte[] fileComment = fileHeader.getFileComment(charset);

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
        out.writeWord((short)fileHeader.getExtraField().getLength());
        out.writeShort((short)fileComment.length);
        out.writeShort((short)fileHeader.getDiskNumber());
        out.writeBytes(fileHeader.getInternalFileAttributes() != null ? fileHeader.getInternalFileAttributes() : new byte[2]);
        out.writeBytes(fileHeader.getExternalFileAttributes() != null ? fileHeader.getExternalFileAttributes() : new byte[4]);
        out.writeLongAsInt(fileHeader.isWriteZip64OffsetLocalHeader() ? InternalZipConstants.ZIP_64_LIMIT : fileHeader.getOffsLocalFileHeader());
        out.writeBytes(fileName);
        new ExtraFieldWriter(fileHeader.getExtraField(), charset).write(out);
        out.writeBytes(fileComment);
    }

}
