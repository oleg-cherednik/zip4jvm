package com.cop.zip4j.io.writers;

import com.cop.zip4j.io.out.DataOutput;
import com.cop.zip4j.model.LocalFileHeader;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.nio.charset.Charset;

/**
 * @author Oleg Cherednik
 * @since 10.03.2019
 */
@RequiredArgsConstructor
public final class LocalFileHeaderWriter {

    @NonNull
    private final LocalFileHeader localFileHeader;
    @NonNull
    private final Charset charset;

    public void write(@NonNull DataOutput out) throws IOException {
        byte[] fileName = localFileHeader.getFileName(charset);

        out.writeDwordSignature(LocalFileHeader.SIGNATURE);
        out.writeWord(localFileHeader.getVersionToExtract());
        out.writeWord(localFileHeader.getGeneralPurposeFlag().getAsInt());
        out.writeWord(localFileHeader.getCompressionMethod().getCode());
        out.writeDword(localFileHeader.getLastModifiedTime());
        out.writeDword(localFileHeader.getCrc32());
        out.writeDword(localFileHeader.getCompressedSize());
        out.writeDword(localFileHeader.getUncompressedSize());
        out.writeWord(fileName.length);
        out.writeWord(localFileHeader.getExtraField().getLength());
        out.writeBytes(fileName);

        new ExtraFieldWriter(localFileHeader.getExtraField(), charset).write(out);
    }

}
