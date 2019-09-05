package ru.olegcherednik.zip4jvm.io.writers;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import ru.olegcherednik.zip4jvm.io.out.DataOutput;
import ru.olegcherednik.zip4jvm.model.LocalFileHeader;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 10.03.2019
 */
@RequiredArgsConstructor
public final class LocalFileHeaderWriter {

    @NonNull
    private final LocalFileHeader localFileHeader;

    public void write(@NonNull DataOutput out) throws IOException {
        byte[] fileName = localFileHeader.getFileName();

        out.writeDwordSignature(LocalFileHeader.SIGNATURE);
        out.writeWord(localFileHeader.getVersionToExtract());
        out.writeWord(localFileHeader.getGeneralPurposeFlag().getAsInt());
        out.writeWord(localFileHeader.getCompressionMethod().getCode());
        out.writeDword(localFileHeader.getLastModifiedTime());
        out.writeDword(localFileHeader.getCrc32());
        out.writeDword(localFileHeader.getCompressedSize());
        out.writeDword(localFileHeader.getUncompressedSize());
        out.writeWord(fileName.length);
        out.writeWord(localFileHeader.getExtraField().getSize());
        out.writeBytes(fileName);

        new ExtraFieldWriter(localFileHeader.getExtraField(), localFileHeader.getGeneralPurposeFlag().getCharset()).write(out);
    }

}
