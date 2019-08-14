package com.cop.zip4j.io.writers;

import com.cop.zip4j.io.out.DataOutput;
import com.cop.zip4j.model.LocalFileHeader;
import com.cop.zip4j.model.Zip64;
import com.cop.zip4j.model.ZipModel;
import com.cop.zip4j.utils.InternalZipConstants;
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
        out.writeWord(localFileHeader.getGeneralPurposeFlag().getData());
        out.writeWord(localFileHeader.getCompressionMethod().getValue());
        out.writeDword(localFileHeader.getLastModifiedTime());
        out.writeDword(localFileHeader.getCrc32());

        //compressed & uncompressed size
        if (localFileHeader.getUncompressedSize() + ZipModelWriter.ZIP64_EXTRA_BUF >= InternalZipConstants.ZIP_64_LIMIT) {
            out.writeDword((int)InternalZipConstants.ZIP_64_LIMIT);
            out.writeDword(0);
            zipModel.zip64();
        } else {
            out.writeDword(localFileHeader.getCompressedSize());
            out.writeDword(localFileHeader.getUncompressedSize());
        }

        byte[] fileName = localFileHeader.getFileName(zipModel.getCharset());

        out.writeWord(fileName.length);
        out.writeWord(localFileHeader.getExtraField().getLength());
        out.writeBytes(fileName);

        if (zipModel.isZip64()) {
            out.writeDwordSignature(Zip64.ExtendedInfo.SIGNATURE);
            out.writeWord(16);
            out.writeQword(localFileHeader.getUncompressedSize());
            out.writeBytes(new byte[8]);
        }

        new ExtraFieldWriter(localFileHeader.getExtraField(), zipModel.getCharset()).write(out);
    }

}
