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

        if (i == 0)
            out.writeBytes(emptyShortByte);
        else {
            //Skip internal file attributes for now
            bytes.copyByteArrayToArrayList(emptyShortByte);
        }

        if (i == 0)
            if (fileHeader.getExternalFileAttributes() != null)
                out.writeBytes(fileHeader.getExternalFileAttributes());
            else
                out.writeBytes(emptyIntByte);
        else {
            if (fileHeader.getExternalFileAttributes() != null)
                bytes.copyByteArrayToArrayList(fileHeader.getExternalFileAttributes());
            else
                bytes.copyByteArrayToArrayList(emptyIntByte);
        }

        //Compute offset bytes before extra field is written for Zip64 compatibility
        //NOTE: this data is not written now, but written at a later point


        //offset local header
        //this data is computed above


        if (i == 0)
            out.writeLongAsInt(getOffsLocalFileHeader(fileHeader, writeZip64OffsetLocalHeader));
        else {
            byte[] offsetLocalHeaderBytes = new byte[4];
            if (writeZip64OffsetLocalHeader) {
                Raw.writeLongLittleEndian(longByte, 0, InternalZipConstants.ZIP_64_LIMIT);
                System.arraycopy(longByte, 0, offsetLocalHeaderBytes, 0, 4);
            } else {
                Raw.writeLongLittleEndian(longByte, 0, fileHeader.getOffsLocalFileHeader());
                System.arraycopy(longByte, 0, offsetLocalHeaderBytes, 0, 4);
            }
            bytes.copyByteArrayToArrayList(offsetLocalHeaderBytes);
        }

        if (i == 0)
            out.writeBytes(fileHeader.getFileName().getBytes(zipModel.getCharset()));
        else
            bytes.copyByteArrayToArrayList(fileHeader.getFileName().getBytes(zipModel.getCharset()));

        if (writeZip64FileSize || writeZip64OffsetLocalHeader)
            zipModel.zip64();

        writeZip64ExtendedInfo(fileHeader, writeZip64FileSize, writeZip64OffsetLocalHeader, i);
        writeAesExtraDataRecord(fileHeader.getAesExtraDataRecord(), i);

    }

    private void writeZip64ExtendedInfo(CentralDirectory.FileHeader fileHeader, boolean writeZip64FileSize,
            boolean writeZip64OffsetLocalHeader, int i) throws IOException {
        if (!zipModel.isZip64())
            return;

        byte[] shortByte = new byte[2];
        byte[] longByte = new byte[8];

        //Zip64 header
        if (i == 0)
            out.writeWord((short)InternalZipConstants.EXTRAFIELDZIP64LENGTH);
        else {
            Raw.writeShortLittleEndian(shortByte, 0, (short)InternalZipConstants.EXTRAFIELDZIP64LENGTH);
            bytes.copyByteArrayToArrayList(shortByte);
        }

        //Zip64 extra data record size
        short dataSize = 0;

        if (writeZip64FileSize)
            dataSize += 16;
        if (writeZip64OffsetLocalHeader)
            dataSize += 8;

        if (i == 0)
            out.writeWord(dataSize);
        else {
            Raw.writeShortLittleEndian(shortByte, 0, dataSize);
            bytes.copyByteArrayToArrayList(shortByte);
        }

        if (writeZip64FileSize) {
            if (i == 0)
                out.writeDword(fileHeader.getUncompressedSize());
            else {
                Raw.writeLongLittleEndian(longByte, 0, fileHeader.getUncompressedSize());
                bytes.copyByteArrayToArrayList(longByte);
            }

            if (i == 0)
                out.writeDword(fileHeader.getCompressedSize());
            else {
                Raw.writeLongLittleEndian(longByte, 0, fileHeader.getCompressedSize());
                bytes.copyByteArrayToArrayList(longByte);
            }
        }

        if (writeZip64OffsetLocalHeader) {
            if (i == 0)
                out.writeDword(fileHeader.getOffsLocalFileHeader());
            else {
                Raw.writeLongLittleEndian(longByte, 0, fileHeader.getOffsLocalFileHeader());
                bytes.copyByteArrayToArrayList(longByte);
            }
        }
    }

    private void writeAesExtraDataRecord(AESExtraDataRecord record, int i) throws IOException {
        if (record == null)
            return;

        byte[] shortByte = new byte[2];

        if (i == 0)
            out.writeWord((short)record.getSignature());
        else {
            Raw.writeShortLittleEndian(shortByte, 0, (short)record.getSignature());
            bytes.copyByteArrayToArrayList(shortByte);
        }

        if (i == 0)
            out.writeWord((short)record.getDataSize());
        else {
            Raw.writeShortLittleEndian(shortByte, 0, (short)record.getDataSize());
            bytes.copyByteArrayToArrayList(shortByte);
        }

        if (i == 0)
            out.writeShort((short)record.getVersionNumber());
        else {
            Raw.writeShortLittleEndian(shortByte, 0, (short)record.getVersionNumber());
            bytes.copyByteArrayToArrayList(shortByte);
        }

        if (i == 0)
            out.writeBytes(record.getVendor().getBytes(zipModel.getCharset()));
        else
            bytes.copyByteArrayToArrayList(record.getVendor().getBytes(zipModel.getCharset()));

        if (i == 0)
            out.writeBytes(record.getAesStrength().getValue(), (byte)0);
        else {
            byte[] aesStrengthBytes = new byte[1];
            aesStrengthBytes[0] = record.getAesStrength().getValue();
            bytes.copyByteArrayToArrayList(aesStrengthBytes);
        }

        if (i == 0)
            out.writeWord(record.getCompressionMethod().getValue());
        else {
            Raw.writeShortLittleEndian(shortByte, 0, record.getCompressionMethod().getValue());
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

    private long getOffsLocalFileHeader(CentralDirectory.FileHeader fileHeader, boolean writeZip64OffsetLocalHeader) {
        if (writeZip64OffsetLocalHeader)
            return InternalZipConstants.ZIP_64_LIMIT;
        return fileHeader.getOffsLocalFileHeader();
    }

}
