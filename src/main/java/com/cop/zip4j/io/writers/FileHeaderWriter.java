package com.cop.zip4j.io.writers;

import com.cop.zip4j.io.out.DataOutput;
import com.cop.zip4j.model.CentralDirectory;
import com.cop.zip4j.model.Zip64;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

/**
 * @author Oleg Cherednik
 * @since 26.04.2019
 */
@RequiredArgsConstructor
final class FileHeaderWriter {

    @NonNull
    private final List<CentralDirectory.FileHeader> fileHeaders;
    @NonNull
    private final Charset charset;

    public void write(@NonNull DataOutput out) throws IOException {
        for (CentralDirectory.FileHeader fileHeader : fileHeaders)
            writeFileHeader(fileHeader, out);
    }

    private void writeFileHeader(CentralDirectory.FileHeader fileHeader, DataOutput out) throws IOException {
        byte[] fileName = fileHeader.getFileName(charset);
        byte[] fileComment = fileHeader.getFileComment(charset);

        if(fileHeader.getExtraField().getExtendedInfo() == Zip64.ExtendedInfo.NULL)
            System.out.println(fileHeader.getFileName() + ": " + fileHeader.getCompressedSize());
        else
            System.out.println(fileHeader.getFileName() + ": " + fileHeader.getExtraField().getExtendedInfo().getCompressedSize());

        out.writeDwordSignature(CentralDirectory.FileHeader.SIGNATURE);
        out.writeWord(fileHeader.getVersionMadeBy());
        out.writeWord(fileHeader.getVersionToExtract());
        out.writeWord(fileHeader.getGeneralPurposeFlag().getAsInt());
        out.writeWord(fileHeader.getCompressionMethod().getCode());
        out.writeDword(fileHeader.getLastModifiedTime());
        out.writeDword(fileHeader.getCrc32());
        out.writeDword(fileHeader.getCompressedSize());
        out.writeDword(fileHeader.getUncompressedSize());
        out.writeWord(fileName.length);
        out.writeWord(fileHeader.getExtraField().getSize());
        out.writeWord(fileComment.length);
        out.writeWord(fileHeader.getDiskNumber());
        out.writeBytes(fileHeader.getInternalFileAttributes().get());
        out.writeBytes(fileHeader.getExternalFileAttributes().get());
        out.writeDword(fileHeader.isWriteZip64OffsetLocalHeader() ? Zip64.LIMIT : fileHeader.getOffsLocalFileHeader());
        out.writeBytes(fileName);
        new ExtraFieldWriter(fileHeader.getExtraField(), charset).write(out);
        out.writeBytes(fileComment);
    }
}
