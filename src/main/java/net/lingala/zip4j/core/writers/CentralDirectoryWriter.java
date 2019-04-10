package net.lingala.zip4j.core.writers;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.lingala.zip4j.io.OutputStreamDecorator;
import net.lingala.zip4j.model.AESExtraDataRecord;
import net.lingala.zip4j.model.CentralDirectory;
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
public final class CentralDirectoryWriter {

    @NonNull
    private final ZipModel zipModel;
    @NonNull
    private final OutputStreamDecorator out;
    @NonNull
    private final LittleEndianBuffer bytes;

    public void write() throws IOException {
        if (zipModel.isEmpty())
            return;

        long offs = out.getOffs();
        int i = 0;

        for (CentralDirectory.FileHeader fileHeader : zipModel.getFileHeaders())
            write(fileHeader, i++);

        int delta = (int)(out.getOffs() - offs);
        int expected = bytes.size();
        int a = 0;
        a++;
    }

    private void write(CentralDirectory.FileHeader fileHeader, int i) throws IOException {
        byte[] shortByte = new byte[2];
        byte[] intByte = new byte[4];
        byte[] longByte = new byte[8];
        final byte[] emptyShortByte = { 0, 0 };
        final byte[] emptyIntByte = { 0, 0, 0, 0 };


        final boolean writeZip64FileSize = fileHeader.getCompressedSize() >= InternalZipConstants.ZIP_64_LIMIT ||
                fileHeader.getUncompressedSize() + HeaderWriter.ZIP64_EXTRA_BUF >= InternalZipConstants.ZIP_64_LIMIT;
        final boolean writeZip64OffsetLocalHeader = fileHeader.getOffsLocalFileHeader() > InternalZipConstants.ZIP_64_LIMIT;

        if (i == 0)
            out.writeDword(fileHeader.getSignature());
        else {
            Raw.writeIntLittleEndian(intByte, 0, fileHeader.getSignature());
            bytes.copyByteArrayToArrayList(intByte);
        }

        if (i == 0)
            out.writeWord((short)fileHeader.getVersionMadeBy());
        else {
            Raw.writeShortLittleEndian(shortByte, 0, (short)fileHeader.getVersionMadeBy());
            bytes.copyByteArrayToArrayList(shortByte);
        }

        if (i == 0)
            out.writeWord((short)fileHeader.getVersionToExtract());
        else {
            Raw.writeShortLittleEndian(shortByte, 0, (short)fileHeader.getVersionToExtract());
            bytes.copyByteArrayToArrayList(shortByte);
        }

        if (i == 0)
            out.writeWord(fileHeader.getGeneralPurposeFlag().getData());
        else {
            Raw.writeShortLittleEndian(shortByte, 0, fileHeader.getGeneralPurposeFlag().getData());
            bytes.copyByteArrayToArrayList(shortByte);
        }

        if (i == 0)
            out.writeShort(fileHeader.getCompressionMethod().getValue());
        else {
            Raw.writeShortLittleEndian(shortByte, 0, fileHeader.getCompressionMethod().getValue());
            bytes.copyByteArrayToArrayList(shortByte);
        }

        if (i == 0)
            out.writeDword(fileHeader.getLastModifiedTime());
        else {
            int dateTime = fileHeader.getLastModifiedTime();
            Raw.writeIntLittleEndian(intByte, 0, dateTime);
            bytes.copyByteArrayToArrayList(intByte);
        }

        if (i == 0)
            out.writeDword((int)fileHeader.getCrc32());
        else {
            Raw.writeIntLittleEndian(intByte, 0, (int)fileHeader.getCrc32());
            bytes.copyByteArrayToArrayList(intByte);
        }

        if (i == 0) {
            out.writeDword(getCompressedSize(fileHeader, writeZip64FileSize));
            out.writeDword(getUncompressedSize(fileHeader, writeZip64FileSize));
        } else {
            if (writeZip64FileSize) {
                Raw.writeLongLittleEndian(longByte, 0, InternalZipConstants.ZIP_64_LIMIT);
                System.arraycopy(longByte, 0, intByte, 0, 4);

                bytes.copyByteArrayToArrayList(intByte);

                bytes.copyByteArrayToArrayList(intByte);
            } else {
                Raw.writeLongLittleEndian(longByte, 0, fileHeader.getCompressedSize());
                System.arraycopy(longByte, 0, intByte, 0, 4);
//				Raw.writeIntLittleEndian(intByte, 0, (int)fileHeader.getCompressedSize());
                bytes.copyByteArrayToArrayList(intByte);

                Raw.writeLongLittleEndian(longByte, 0, fileHeader.getUncompressedSize());
                System.arraycopy(longByte, 0, intByte, 0, 4);
//				Raw.writeIntLittleEndian(intByte, 0, (int)fileHeader.getUncompressedSize());
                bytes.copyByteArrayToArrayList(intByte);
            }
        }

        if (i == 0)
            out.writeShort((short)fileHeader.getFileNameLength());
        else {
            Raw.writeShortLittleEndian(shortByte, 0, (short)fileHeader.getFileNameLength());
            bytes.copyByteArrayToArrayList(shortByte);
        }

        if (i == 0)
            out.writeWord(getExtraFieldLength(fileHeader, writeZip64FileSize, writeZip64OffsetLocalHeader));
        else {
            Raw.writeShortLittleEndian(shortByte, 0, getExtraFieldLength(fileHeader, writeZip64FileSize, writeZip64OffsetLocalHeader));
            bytes.copyByteArrayToArrayList(shortByte);
        }

        //Skip file comment length for now
        if (i == 0)
            out.writeShort((short)0);
        else
            bytes.copyByteArrayToArrayList(emptyShortByte);

        if (i == 0)
            out.writeShort((short)fileHeader.getDiskNumber());
        else {
            //Skip disk number start for now
            Raw.writeShortLittleEndian(shortByte, 0, (short)fileHeader.getDiskNumber());
            bytes.copyByteArrayToArrayList(shortByte);
        }

        if (i == -1)
            out.writeShort((short)0);
        else {
            //Skip internal file attributes for now
            bytes.copyByteArrayToArrayList(emptyShortByte);
        }

        if (i == -1)
            out.writeBytes(fileHeader.getExternalFileAttributes());
        else {
            if (fileHeader.getExternalFileAttributes() != null) {
                bytes.copyByteArrayToArrayList(fileHeader.getExternalFileAttributes());
            } else {
                bytes.copyByteArrayToArrayList(emptyIntByte);
            }
        }

        //Compute offset bytes before extra field is written for Zip64 compatibility
        //NOTE: this data is not written now, but written at a later point
        byte[] offsetLocalHeaderBytes = new byte[4];
        if (writeZip64OffsetLocalHeader) {
            Raw.writeLongLittleEndian(longByte, 0, InternalZipConstants.ZIP_64_LIMIT);
            System.arraycopy(longByte, 0, offsetLocalHeaderBytes, 0, 4);
        } else {
            Raw.writeLongLittleEndian(longByte, 0, fileHeader.getOffsLocalFileHeader());
            System.arraycopy(longByte, 0, offsetLocalHeaderBytes, 0, 4);
        }

        //offset local header
        //this data is computed above
        bytes.copyByteArrayToArrayList(offsetLocalHeaderBytes);

        byte[] fileNameBytes = fileHeader.getFileName().getBytes(zipModel.getCharset());
        bytes.copyByteArrayToArrayList(fileNameBytes);

        if (writeZip64FileSize || writeZip64OffsetLocalHeader)
            zipModel.zip64();

        if (zipModel.isZip64()) {
            //Zip64 header
            Raw.writeShortLittleEndian(shortByte, 0, (short)InternalZipConstants.EXTRAFIELDZIP64LENGTH);
            bytes.copyByteArrayToArrayList(shortByte);

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

            if (writeZip64FileSize) {
                Raw.writeLongLittleEndian(longByte, 0, fileHeader.getUncompressedSize());
                bytes.copyByteArrayToArrayList(longByte);

                Raw.writeLongLittleEndian(longByte, 0, fileHeader.getCompressedSize());
                bytes.copyByteArrayToArrayList(longByte);
            }

            if (writeZip64OffsetLocalHeader) {
                Raw.writeLongLittleEndian(longByte, 0, fileHeader.getOffsLocalFileHeader());
                bytes.copyByteArrayToArrayList(longByte);
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

            bytes.copyByteArrayToArrayList(aesExtraDataRecord.getVendor().getBytes());

            byte[] aesStrengthBytes = new byte[1];
            aesStrengthBytes[0] = aesExtraDataRecord.getAesStrength().getValue();
            bytes.copyByteArrayToArrayList(aesStrengthBytes);

            Raw.writeShortLittleEndian(shortByte, 0, aesExtraDataRecord.getCompressionMethod().getValue());
            bytes.copyByteArrayToArrayList(shortByte);
        }
    }

    private static long getCompressedSize(CentralDirectory.FileHeader fileHeader, boolean writeZip64FileSize) {
        if (writeZip64FileSize)
            return InternalZipConstants.ZIP_64_LIMIT;
        return fileHeader.getCompressedSize();
    }

    private static long getUncompressedSize(CentralDirectory.FileHeader fileHeader, boolean writeZip64FileSize) {
        if (writeZip64FileSize)
            return InternalZipConstants.ZIP_64_LIMIT;
        return fileHeader.getUncompressedSize();
    }

    private static short getExtraFieldLength(CentralDirectory.FileHeader fileHeader, boolean writeZip64FileSize,
            boolean writeZip64OffsetLocalHeader) {
        int extraFieldLength = 0;
        if (writeZip64FileSize || writeZip64OffsetLocalHeader) {
            extraFieldLength += 4;
            if (writeZip64FileSize)
                extraFieldLength += 16;
            if (writeZip64OffsetLocalHeader)
                extraFieldLength += 8;
        }
        if (fileHeader.getAesExtraDataRecord() != null)
            extraFieldLength += 11;
        return (short)extraFieldLength;
    }

}
