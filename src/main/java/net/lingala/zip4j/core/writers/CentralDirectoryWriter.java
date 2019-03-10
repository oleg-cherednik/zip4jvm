package net.lingala.zip4j.core.writers;

import net.lingala.zip4j.core.HeaderWriter;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.AESExtraDataRecord;
import net.lingala.zip4j.model.CentralDirectory;
import net.lingala.zip4j.model.ZipModel;
import net.lingala.zip4j.util.InternalZipConstants;
import net.lingala.zip4j.util.LittleEndianBuffer;
import net.lingala.zip4j.util.Raw;

import java.io.OutputStream;

/**
 * @author Oleg Cherednik
 * @since 10.03.2019
 */
public final class CentralDirectoryWriter {

    public int write(ZipModel zipModel, CentralDirectory.FileHeader fileHeader,
            OutputStream outputStream, LittleEndianBuffer bytes) throws ZipException {
        try {
            int sizeOfFileHeader = 0;

            byte[] shortByte = new byte[2];
            byte[] intByte = new byte[4];
            byte[] longByte = new byte[8];
            final byte[] emptyShortByte = { 0, 0 };
            final byte[] emptyIntByte = { 0, 0, 0, 0 };

            boolean writeZip64FileSize = false;
            boolean writeZip64OffsetLocalHeader = false;

            Raw.writeIntLittleEndian(intByte, 0, fileHeader.getSignature());
            bytes.copyByteArrayToArrayList(intByte);
            sizeOfFileHeader += 4;

            Raw.writeShortLittleEndian(shortByte, 0, (short)fileHeader.getVersionMadeBy());
            bytes.copyByteArrayToArrayList(shortByte);
            sizeOfFileHeader += 2;

            Raw.writeShortLittleEndian(shortByte, 0, (short)fileHeader.getVersionNeededToExtract());
            bytes.copyByteArrayToArrayList(shortByte);
            sizeOfFileHeader += 2;

            bytes.copyByteArrayToArrayList(fileHeader.getGeneralPurposeFlag());
            sizeOfFileHeader += 2;

            Raw.writeShortLittleEndian(shortByte, 0, fileHeader.getCompressionMethod().getValue());
            bytes.copyByteArrayToArrayList(shortByte);
            sizeOfFileHeader += 2;

            int dateTime = fileHeader.getLastModFileTime();
            Raw.writeIntLittleEndian(intByte, 0, dateTime);
            bytes.copyByteArrayToArrayList(intByte);
            sizeOfFileHeader += 4;

            Raw.writeIntLittleEndian(intByte, 0, (int)(fileHeader.getCrc32()));
            bytes.copyByteArrayToArrayList(intByte);
            sizeOfFileHeader += 4;

            if (fileHeader.getCompressedSize() >= InternalZipConstants.ZIP_64_LIMIT ||
                    fileHeader.getUncompressedSize() + HeaderWriter.ZIP64_EXTRA_BUF >= InternalZipConstants.ZIP_64_LIMIT) {
                Raw.writeLongLittleEndian(longByte, 0, InternalZipConstants.ZIP_64_LIMIT);
                System.arraycopy(longByte, 0, intByte, 0, 4);

                bytes.copyByteArrayToArrayList(intByte);
                sizeOfFileHeader += 4;

                bytes.copyByteArrayToArrayList(intByte);
                sizeOfFileHeader += 4;

                writeZip64FileSize = true;
            } else {
                Raw.writeLongLittleEndian(longByte, 0, fileHeader.getCompressedSize());
                System.arraycopy(longByte, 0, intByte, 0, 4);
//				Raw.writeIntLittleEndian(intByte, 0, (int)fileHeader.getCompressedSize());
                bytes.copyByteArrayToArrayList(intByte);
                sizeOfFileHeader += 4;

                Raw.writeLongLittleEndian(longByte, 0, fileHeader.getUncompressedSize());
                System.arraycopy(longByte, 0, intByte, 0, 4);
//				Raw.writeIntLittleEndian(intByte, 0, (int)fileHeader.getUncompressedSize());
                bytes.copyByteArrayToArrayList(intByte);
                sizeOfFileHeader += 4;
            }

            Raw.writeShortLittleEndian(shortByte, 0, (short)fileHeader.getFileNameLength());
            bytes.copyByteArrayToArrayList(shortByte);
            sizeOfFileHeader += 2;

            //Compute offset bytes before extra field is written for Zip64 compatibility
            //NOTE: this data is not written now, but written at a later point
            byte[] offsetLocalHeaderBytes = new byte[4];
            if (fileHeader.getOffLocalHeaderRelative() > InternalZipConstants.ZIP_64_LIMIT) {
                Raw.writeLongLittleEndian(longByte, 0, InternalZipConstants.ZIP_64_LIMIT);
                System.arraycopy(longByte, 0, offsetLocalHeaderBytes, 0, 4);
                writeZip64OffsetLocalHeader = true;
            } else {
                Raw.writeLongLittleEndian(longByte, 0, fileHeader.getOffLocalHeaderRelative());
                System.arraycopy(longByte, 0, offsetLocalHeaderBytes, 0, 4);
            }

            // extra field length
            int extraFieldLength = 0;
            if (writeZip64FileSize || writeZip64OffsetLocalHeader) {
                extraFieldLength += 4;
                if (writeZip64FileSize)
                    extraFieldLength += 16;
                if (writeZip64OffsetLocalHeader)
                    extraFieldLength += 8;
            }
            if (fileHeader.getAesExtraDataRecord() != null) {
                extraFieldLength += 11;
            }
            Raw.writeShortLittleEndian(shortByte, 0, (short)(extraFieldLength));
            bytes.copyByteArrayToArrayList(shortByte);
            sizeOfFileHeader += 2;

            //Skip file comment length for now
            bytes.copyByteArrayToArrayList(emptyShortByte);
            sizeOfFileHeader += 2;

            //Skip disk number start for now
            Raw.writeShortLittleEndian(shortByte, 0, (short)(fileHeader.getDiskNumberStart()));
            bytes.copyByteArrayToArrayList(shortByte);
            sizeOfFileHeader += 2;

            //Skip internal file attributes for now
            bytes.copyByteArrayToArrayList(emptyShortByte);
            sizeOfFileHeader += 2;

            //External file attributes
            if (fileHeader.getExternalFileAttr() != null) {
                bytes.copyByteArrayToArrayList(fileHeader.getExternalFileAttr());
            } else {
                bytes.copyByteArrayToArrayList(emptyIntByte);
            }
            sizeOfFileHeader += 4;

            //offset local header
            //this data is computed above
            bytes.copyByteArrayToArrayList(offsetLocalHeaderBytes);
            sizeOfFileHeader += 4;

            byte[] fileNameBytes = fileHeader.getFileName().getBytes(zipModel.getCharset());
            bytes.copyByteArrayToArrayList(fileNameBytes);
            sizeOfFileHeader += fileNameBytes.length;

            if (writeZip64FileSize || writeZip64OffsetLocalHeader) {
                zipModel.setZip64Format(true);

                //Zip64 header
                Raw.writeShortLittleEndian(shortByte, 0, (short)InternalZipConstants.EXTRAFIELDZIP64LENGTH);
                bytes.copyByteArrayToArrayList(shortByte);
                sizeOfFileHeader += 2;

                //Zip64 extra data record size
                int dataSize = 0;

                if (writeZip64FileSize) {
                    dataSize += 16;
                }
                if (writeZip64OffsetLocalHeader) {
                    dataSize += 8;
                }

                Raw.writeShortLittleEndian(shortByte, 0, (short)dataSize);
                bytes.copyByteArrayToArrayList(shortByte);
                sizeOfFileHeader += 2;

                if (writeZip64FileSize) {
                    Raw.writeLongLittleEndian(longByte, 0, fileHeader.getUncompressedSize());
                    bytes.copyByteArrayToArrayList(longByte);
                    sizeOfFileHeader += 8;

                    Raw.writeLongLittleEndian(longByte, 0, fileHeader.getCompressedSize());
                    bytes.copyByteArrayToArrayList(longByte);
                    sizeOfFileHeader += 8;
                }

                if (writeZip64OffsetLocalHeader) {
                    Raw.writeLongLittleEndian(longByte, 0, fileHeader.getOffLocalHeaderRelative());
                    bytes.copyByteArrayToArrayList(longByte);
                    sizeOfFileHeader += 8;
                }
            }

            if (fileHeader.getAesExtraDataRecord() != null) {
                AESExtraDataRecord aesExtraDataRecord = fileHeader.getAesExtraDataRecord();

                Raw.writeShortLittleEndian(shortByte, 0, (short)aesExtraDataRecord.getSignature());
                bytes.copyByteArrayToArrayList(shortByte);

                Raw.writeShortLittleEndian(shortByte, 0, (short)aesExtraDataRecord.getDataSize());
                bytes.copyByteArrayToArrayList(shortByte);

                Raw.writeShortLittleEndian(shortByte, 0, (short)aesExtraDataRecord.getVersionNumber());
                bytes.copyByteArrayToArrayList(shortByte);

                bytes.copyByteArrayToArrayList(aesExtraDataRecord.getVendorID().getBytes());

                byte[] aesStrengthBytes = new byte[1];
                aesStrengthBytes[0] = aesExtraDataRecord.getAesStrength().getValue();
                bytes.copyByteArrayToArrayList(aesStrengthBytes);

                Raw.writeShortLittleEndian(shortByte, 0, aesExtraDataRecord.getCompressionMethod().getValue());
                bytes.copyByteArrayToArrayList(shortByte);

                sizeOfFileHeader += 11;
            }

//			out.write(byteArrayListToByteArray(headerBytesList));

            return sizeOfFileHeader;
        } catch(Exception e) {
            throw new ZipException(e);
        }
    }
}
