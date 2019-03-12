package net.lingala.zip4j.core.writers;

import lombok.NonNull;
import net.lingala.zip4j.core.HeaderWriter;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.AESExtraDataRecord;
import net.lingala.zip4j.model.LocalFileHeader;
import net.lingala.zip4j.model.ZipModel;
import net.lingala.zip4j.util.InternalZipConstants;
import net.lingala.zip4j.util.LittleEndianBuffer;
import net.lingala.zip4j.util.Raw;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author Oleg Cherednik
 * @since 10.03.2019
 */
public final class LocalFileHeaderWriter {

    public int write(@NonNull LocalFileHeader localFileHeader, @NonNull ZipModel zipModel, @NonNull OutputStream out)
            throws ZipException, IOException {
        LittleEndianBuffer bytes = new LittleEndianBuffer();

        bytes.writeDword(localFileHeader.getSignature());
        bytes.writeWord((short)localFileHeader.getVersionNeededToExtract());
        bytes.writeShort(localFileHeader.getGeneralPurposeFlag().getData());
        bytes.writeWord(localFileHeader.getCompressionMethod().getValue());
        bytes.writeDword(localFileHeader.getLastModFileTime());
        bytes.writeDword((int)localFileHeader.getCrc32());

        //compressed & uncompressed size
        if (localFileHeader.getUncompressedSize() + HeaderWriter.ZIP64_EXTRA_BUF >= InternalZipConstants.ZIP_64_LIMIT) {
            bytes.writeDword((int)InternalZipConstants.ZIP_64_LIMIT);
            bytes.writeDword(0);
            zipModel.setZip64Format(true);
            localFileHeader.setWriteComprSizeInZip64ExtraRecord(true);
        } else {
            bytes.writeDword(localFileHeader.getCompressedSize());
            bytes.writeDword(localFileHeader.getUncompressedSize());
            localFileHeader.setWriteComprSizeInZip64ExtraRecord(false);
        }

        bytes.writeWord((short)localFileHeader.getFileNameLength());
        bytes.writeWord(localFileHeader.getExtraFileLength(zipModel));
        bytes.writeBytes(zipModel.convertFileNameToByteArr(localFileHeader.getFileName()));

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

        return bytes.flushInto(out);
    }

    public int writeExtended(@NonNull LocalFileHeader localFileHeader, @NonNull OutputStream out) throws ZipException, IOException {
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

        return bytes.flushInto(out);
    }

}
