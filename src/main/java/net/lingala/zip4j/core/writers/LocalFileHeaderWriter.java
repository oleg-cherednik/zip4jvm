package net.lingala.zip4j.core.writers;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.lingala.zip4j.io.OutputStreamDecorator;
import net.lingala.zip4j.model.LocalFileHeader;
import net.lingala.zip4j.model.Zip64ExtendedInfo;
import net.lingala.zip4j.model.ZipModel;
import net.lingala.zip4j.util.InternalZipConstants;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 10.03.2019
 */
@RequiredArgsConstructor
public final class LocalFileHeaderWriter {

    @NonNull
    private final LocalFileHeader localFileHeader;
    @NonNull
    private final ZipModel zipModel;

    public void write(@NonNull OutputStreamDecorator out) throws IOException {
        out.writeDword(localFileHeader.getSignature());
        out.writeWord((short)localFileHeader.getVersionToExtract());
        out.writeShort(localFileHeader.getGeneralPurposeFlag().getData());
        out.writeWord(localFileHeader.getCompressionMethod().getValue());
        out.writeDword(localFileHeader.getLastModifiedTime());
        out.writeDword((int)localFileHeader.getCrc32());

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

        out.writeWord((short)fileName.length);
        out.writeWord((short)localFileHeader.getExtraField().getLength());
        out.writeBytes(fileName);

        if (zipModel.isZip64()) {
            out.writeWord(Zip64ExtendedInfo.SIGNATURE);
            out.writeWord((short)16);
            out.writeLong(localFileHeader.getUncompressedSize());
            out.writeBytes(new byte[8]);
        }

        new ExtraFieldWriter(localFileHeader.getExtraField(), zipModel.getCharset()).write(out);
    }

    public void writeExtended(@NonNull OutputStreamDecorator out) throws IOException {
        //Extended local file header signature
        out.writeDword((int)InternalZipConstants.EXTSIG);

        //CRC
        out.writeDword((int)localFileHeader.getCrc32());

        //compressed size
        long compressedSize = localFileHeader.getCompressedSize();
        if (compressedSize >= Integer.MAX_VALUE) {
            compressedSize = Integer.MAX_VALUE;
        }
        out.writeDword((int)compressedSize);

        //uncompressed size
        long uncompressedSize = localFileHeader.getUncompressedSize();
        if (uncompressedSize >= Integer.MAX_VALUE) {
            uncompressedSize = Integer.MAX_VALUE;
        }
        out.writeDword((int)uncompressedSize);
    }

}
