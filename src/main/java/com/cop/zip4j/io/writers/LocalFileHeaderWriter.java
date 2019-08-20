package com.cop.zip4j.io.writers;

import com.cop.zip4j.io.out.DataOutput;
import com.cop.zip4j.model.LocalFileHeader;
import com.cop.zip4j.model.ZipModel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 10.03.2019
 */
@RequiredArgsConstructor
public final class LocalFileHeaderWriter {

    @NonNull
    private final ZipModel zipModel;
    @NonNull
    private final LocalFileHeader localFileHeader;

    public void write(@NonNull DataOutput out) throws IOException {
        out.writeDwordSignature(LocalFileHeader.SIGNATURE);
        out.writeWord(localFileHeader.getVersionToExtract());
        out.writeWord(localFileHeader.getGeneralPurposeFlag().getAsInt());
        out.writeWord(localFileHeader.getCompressionMethod().getCode());
        out.writeDword(localFileHeader.getLastModifiedTime());
        out.writeDword(localFileHeader.getCrc32());

//        if(localFileHeader.getUncompressedSize() + ZipModelWriter.ZIP64_EXTRA_BUF >= ZipModel.ZIP_64_LIMIT)
//            zipModel.zip64();

        out.writeDword(localFileHeader.getCompressedSize());
        out.writeDword(localFileHeader.getUncompressedSize());

        byte[] fileName = localFileHeader.getFileName(zipModel.getCharset());

        out.writeWord(fileName.length);
        out.writeWord(localFileHeader.getExtraField().getLength());
        out.writeBytes(fileName);

        new ExtraFieldWriter(localFileHeader.getExtraField(), zipModel.getCharset()).write(out);
    }

}
