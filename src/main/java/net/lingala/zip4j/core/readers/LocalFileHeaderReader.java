package net.lingala.zip4j.core.readers;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.CentralDirectory;
import net.lingala.zip4j.model.CompressionMethod;
import net.lingala.zip4j.model.ExtraDataRecord;
import net.lingala.zip4j.model.LocalFileHeader;
import net.lingala.zip4j.model.Zip64ExtendedInfo;
import net.lingala.zip4j.util.InternalZipConstants;
import net.lingala.zip4j.util.LittleEndianDecorator;
import net.lingala.zip4j.util.LittleEndianRandomAccessFile;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 08.03.2019
 */
@RequiredArgsConstructor
public final class LocalFileHeaderReader {

    private final LittleEndianRandomAccessFile in;
    private final CentralDirectory.FileHeader fileHeader;

    @NonNull
    public LocalFileHeader read() throws IOException, ZipException {
        findHead();

        LocalFileHeader localFileHeader = new LocalFileHeader();

        localFileHeader.setVersionToExtract(in.readShort());
        localFileHeader.setGeneralPurposeFlag(in.readShort());
        localFileHeader.setCompressionMethod(CompressionMethod.parseValue(in.readShort()));
        localFileHeader.setLastModifiedTime(in.readInt());
        localFileHeader.setCrc32(in.readInt());
        localFileHeader.setCompressedSize(in.readIntAsLong());
        localFileHeader.setUncompressedSize(in.readIntAsLong());
        localFileHeader.setFileNameLength(in.readShort());
        localFileHeader.setExtraFieldLength(in.readShort());
        localFileHeader.setFileName(in.readString(localFileHeader.getFileNameLength()));
        localFileHeader.setExtraDataRecords(CentralDirectoryReader.readExtraDataRecords(in, localFileHeader.getExtraFieldLength()));

        localFileHeader.setOffsetStartOfData(in.getFilePointer());
        localFileHeader.setPassword(fileHeader.getPassword());
        localFileHeader.setZip64ExtendedInfo(readZip64ExtendedInfo(localFileHeader));
        localFileHeader.setAesExtraDataRecord(CentralDirectoryReader.readAESExtraDataRecord(localFileHeader.getExtraDataRecords()));

        if (localFileHeader.getCrc32() <= 0)
            localFileHeader.setCrc32(fileHeader.getCrc32());

        if (localFileHeader.getCompressedSize() <= 0)
            localFileHeader.setCompressedSize(fileHeader.getCompressedSize());

        if (localFileHeader.getUncompressedSize() <= 0)
            localFileHeader.setUncompressedSize(fileHeader.getUncompressedSize());

        return localFileHeader;
    }

    private void findHead() throws IOException {
        in.seek(fileHeader.getOffsLocalFileHeader());

        if (in.readInt() == InternalZipConstants.LOCSIG)
            return;

        throw new IOException("invalid local file header signature");
    }

    // TODO pretty similar to FileHeader
    private static Zip64ExtendedInfo readZip64ExtendedInfo(@NonNull LocalFileHeader localFileHeader) throws IOException {
        ExtraDataRecord record = localFileHeader.getExtraDataRecordByHeader(ExtraDataRecord.HEADER_ZIP64);

        if (record == null)
            return null;

        LittleEndianDecorator in = new LittleEndianDecorator(record.getData());

        Zip64ExtendedInfo res = new Zip64ExtendedInfo();
        res.setSize(record.getSizeOfData());
        res.setUnCompressedSize((localFileHeader.getUncompressedSize() & 0xFFFF) == 0xFFFF ? in.readLong() : -1);
        res.setCompressedSize((localFileHeader.getCompressedSize() & 0xFFFF) == 0xFFFF ? in.readLong() : -1);
        res.setOffsLocalHeaderRelative(in.readLong());
        res.setDiskNumberStart(in.readInt());

        if (res.getUnCompressedSize() != -1 || res.getCompressedSize() != -1
                || res.getOffsLocalHeaderRelative() != -1 || res.getDiskNumberStart() != -1)
            return res;

        return null;
    }
}
