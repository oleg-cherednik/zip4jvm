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
import net.lingala.zip4j.util.LittleEndianBuffer;
import net.lingala.zip4j.util.Raw;

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
        LittleEndianBuffer bytes = new LittleEndianBuffer();

        bytes.writeDword(localFileHeader.getSignature());
        bytes.writeWord((short)localFileHeader.getVersionToExtract());
        bytes.writeShort(localFileHeader.getGeneralPurposeFlag().getData());
        bytes.writeWord(localFileHeader.getCompressionMethod().getValue());
        bytes.writeDword(localFileHeader.getLastModifiedTime());
        bytes.writeDword((int)localFileHeader.getCrc32());

        //compressed & uncompressed size
        if (localFileHeader.getUncompressedSize() + HeaderWriter.ZIP64_EXTRA_BUF >= InternalZipConstants.ZIP_64_LIMIT) {
            bytes.writeDword((int)InternalZipConstants.ZIP_64_LIMIT);
            bytes.writeDword(0);
            zipModel.setZip64EndCentralDirectoryLocator(new Zip64EndCentralDirectoryLocator());
            localFileHeader.setWriteComprSizeInZip64ExtraRecord(true);
        } else {
            bytes.writeDword(localFileHeader.getCompressedSize());
            bytes.writeDword(localFileHeader.getUncompressedSize());
            localFileHeader.setWriteComprSizeInZip64ExtraRecord(false);
        }

        bytes.writeWord((short)localFileHeader.getFileNameLength());
        bytes.writeWord(localFileHeader.getExtraFileLength(zipModel));
        bytes.writeBytes(localFileHeader.getFileName().getBytes(zipModel.getCharset()));

        if (zipModel.isZip64Format()) {
            bytes.writeWord((short)InternalZipConstants.EXTRAFIELDZIP64LENGTH);
            bytes.writeWord((short)16);
            bytes.writeLong(localFileHeader.getUncompressedSize());
            bytes.writeBytes(new byte[8]);
        }

        if (localFileHeader.getAesExtraDataRecord() != null) {
            AESExtraDataRecord aesExtraDataRecord = localFileHeader.getAesExtraDataRecord();

            bytes.writeWord((short)aesExtraDataRecord.getSignature());
            bytes.writeWord((short)aesExtraDataRecord.getDataSize());
            bytes.writeWord((short)aesExtraDataRecord.getVersionNumber());
            bytes.writeBytes(aesExtraDataRecord.getVendor().getBytes());
            bytes.writeBytes(aesExtraDataRecord.getAesStrength().getValue());
            bytes.writeWord(aesExtraDataRecord.getCompressionMethod().getValue());
        }

        bytes.flushInto(out);
    }

    public void writeExtended(@NonNull OutputStreamDecorator out) throws IOException {
        LittleEndianBuffer bytes = new LittleEndianBuffer();
        byte[] intByte = new byte[4];

        //Extended local file header signature
        Raw.writeIntLittleEndian(intByte, 0, (int)InternalZipConstants.EXTSIG);
        bytes.copyByteArrayToArrayList(intByte);

        //CRC
        Raw.writeIntLittleEndian(intByte, 0, (int)localFileHeader.getCrc32());
        bytes.copyByteArrayToArrayList(intByte);

        //compressed size
        long compressedSize = localFileHeader.getCompressedSize();
        if (compressedSize >= Integer.MAX_VALUE) {
            compressedSize = Integer.MAX_VALUE;
        }
        Raw.writeIntLittleEndian(intByte, 0, (int)compressedSize);
        bytes.copyByteArrayToArrayList(intByte);

        //uncompressed size
        long uncompressedSize = localFileHeader.getUncompressedSize();
        if (uncompressedSize >= Integer.MAX_VALUE) {
            uncompressedSize = Integer.MAX_VALUE;
        }
        Raw.writeIntLittleEndian(intByte, 0, (int)uncompressedSize);
        bytes.copyByteArrayToArrayList(intByte);

        bytes.flushInto(out);
    }

}
