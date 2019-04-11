package net.lingala.zip4j.core.writers;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.lingala.zip4j.io.OutputStreamDecorator;
import net.lingala.zip4j.model.AESExtraDataRecord;
import net.lingala.zip4j.model.CentralDirectory;
import net.lingala.zip4j.model.ZipModel;
import net.lingala.zip4j.util.InternalZipConstants;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 10.03.2019
 */
@RequiredArgsConstructor
public final class CentralDirectoryWriter {

    @NonNull
    private final OutputStreamDecorator out;
    @NonNull
    private final ZipModel zipModel;

    public void write() throws IOException {
        for (CentralDirectory.FileHeader fileHeader : zipModel.getFileHeaders())
            write(fileHeader);
    }

    private void write(CentralDirectory.FileHeader fileHeader) throws IOException {
        final byte[] emptyShortByte = { 0, 0 };
        final byte[] emptyIntByte = { 0, 0, 0, 0 };


        final boolean writeZip64FileSize = fileHeader.getCompressedSize() >= InternalZipConstants.ZIP_64_LIMIT ||
                fileHeader.getUncompressedSize() + HeaderWriter.ZIP64_EXTRA_BUF >= InternalZipConstants.ZIP_64_LIMIT;
        final boolean writeZip64OffsetLocalHeader = fileHeader.getOffsLocalFileHeader() > InternalZipConstants.ZIP_64_LIMIT;

        out.writeDword(fileHeader.getSignature());

        out.writeWord(fileHeader.getVersionMadeBy());

        out.writeWord(fileHeader.getVersionToExtract());

        out.writeWord(fileHeader.getGeneralPurposeFlag().getData());

        out.writeShort(fileHeader.getCompressionMethod().getValue());

        out.writeDword(fileHeader.getLastModifiedTime());

        out.writeDword((int)fileHeader.getCrc32());

        out.writeDword(getCompressedSize(fileHeader, writeZip64FileSize));
        out.writeDword(getUncompressedSize(fileHeader, writeZip64FileSize));

        out.writeShort((short)fileHeader.getFileNameLength());

        out.writeWord(getExtraFieldLength(fileHeader, writeZip64FileSize, writeZip64OffsetLocalHeader));

        //Skip file comment length for now
        out.writeShort((short)0);

        out.writeShort((short)fileHeader.getDiskNumber());

        out.writeBytes(emptyShortByte);

        if (fileHeader.getExternalFileAttributes() != null)
            out.writeBytes(fileHeader.getExternalFileAttributes());
        else
            out.writeBytes(emptyIntByte);

        //Compute offset bytes before extra field is written for Zip64 compatibility
        //NOTE: this data is not written now, but written at a later point


        //offset local header
        //this data is computed above


        out.writeLongAsInt(getOffsLocalFileHeader(fileHeader, writeZip64OffsetLocalHeader));

        out.writeBytes(fileHeader.getFileName().getBytes(zipModel.getCharset()));

        if (writeZip64FileSize || writeZip64OffsetLocalHeader)
            zipModel.zip64();

        writeZip64ExtendedInfo(fileHeader, writeZip64FileSize, writeZip64OffsetLocalHeader);
        writeAesExtraDataRecord(fileHeader.getAesExtraDataRecord());
    }

    private void writeZip64ExtendedInfo(CentralDirectory.FileHeader fileHeader, boolean writeZip64FileSize,
            boolean writeZip64OffsetLocalHeader) throws IOException {
        if (!zipModel.isZip64())
            return;


        //Zip64 header
        out.writeWord((short)InternalZipConstants.EXTRAFIELDZIP64LENGTH);

        //Zip64 extra data record size
        short dataSize = 0;

        if (writeZip64FileSize)
            dataSize += 16;
        if (writeZip64OffsetLocalHeader)
            dataSize += 8;

        out.writeWord(dataSize);

        if (writeZip64FileSize) {
            out.writeDword(fileHeader.getUncompressedSize());

            out.writeDword(fileHeader.getCompressedSize());
        }

        if (writeZip64OffsetLocalHeader)
            out.writeDword(fileHeader.getOffsLocalFileHeader());
    }

    private void writeAesExtraDataRecord(AESExtraDataRecord record) throws IOException {
        if (record == null)
            return;

        out.writeWord((short)record.getSignature());
        out.writeWord((short)record.getDataSize());
        out.writeShort((short)record.getVersionNumber());
        out.writeBytes(record.getVendor().getBytes(zipModel.getCharset()));
        out.writeBytes(record.getAesStrength().getValue(), (byte)0);
        out.writeWord(record.getCompressionMethod().getValue());
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
