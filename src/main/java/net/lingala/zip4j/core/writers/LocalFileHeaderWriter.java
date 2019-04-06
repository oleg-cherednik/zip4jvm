package net.lingala.zip4j.core.writers;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.lingala.zip4j.core.HeaderWriter;
import net.lingala.zip4j.io.OutputStreamDecorator;
import net.lingala.zip4j.model.AESExtraDataRecord;
import net.lingala.zip4j.model.LocalFileHeader;
import net.lingala.zip4j.model.Zip64EndCentralDirectoryLocator;
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

    public void write(@NonNull ZipModel zipModel, @NonNull OutputStreamDecorator out) throws IOException {
        out.writeDword(localFileHeader.getSignature());
        out.writeWord((short)localFileHeader.getVersionToExtract());
        out.writeShort(localFileHeader.getGeneralPurposeFlag().getData());
        out.writeWord(localFileHeader.getCompressionMethod().getValue());
        out.writeDword(localFileHeader.getLastModifiedTime());
        out.writeDword((int)localFileHeader.getCrc32());

        //compressed & uncompressed size
        if (localFileHeader.getUncompressedSize() + HeaderWriter.ZIP64_EXTRA_BUF >= InternalZipConstants.ZIP_64_LIMIT) {
            out.writeDword((int)InternalZipConstants.ZIP_64_LIMIT);
            out.writeDword(0);
            zipModel.setZip64EndCentralDirectoryLocator(new Zip64EndCentralDirectoryLocator());
            localFileHeader.setWriteComprSizeInZip64ExtraRecord(true);
        } else {
            out.writeDword(localFileHeader.getCompressedSize());
            out.writeDword(localFileHeader.getUncompressedSize());
            localFileHeader.setWriteComprSizeInZip64ExtraRecord(false);
        }

        out.writeWord((short)localFileHeader.getFileNameLength());
        out.writeWord(localFileHeader.getExtraFileLength(zipModel));
        out.writeBytes(localFileHeader.getFileName().getBytes(zipModel.getCharset()));

        if (zipModel.isZip64Format()) {
            out.writeWord((short)InternalZipConstants.EXTRAFIELDZIP64LENGTH);
            out.writeWord((short)16);
            out.writeLong(localFileHeader.getUncompressedSize());
            out.writeBytes(new byte[8]);
        }

        if (localFileHeader.getAesExtraDataRecord() != null) {
            AESExtraDataRecord aesExtraDataRecord = localFileHeader.getAesExtraDataRecord();

            out.writeWord((short)aesExtraDataRecord.getSignature());
            out.writeWord((short)aesExtraDataRecord.getDataSize());
            out.writeWord((short)aesExtraDataRecord.getVersionNumber());
            out.writeBytes(aesExtraDataRecord.getVendor().getBytes());
            out.writeBytes(aesExtraDataRecord.getAesStrength().getValue());
            out.writeWord(aesExtraDataRecord.getCompressionMethod().getValue());
        }
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
