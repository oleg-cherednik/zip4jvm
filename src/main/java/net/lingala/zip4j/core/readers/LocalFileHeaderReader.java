package net.lingala.zip4j.core.readers;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.AESExtraDataRecord;
import net.lingala.zip4j.model.AESStrength;
import net.lingala.zip4j.model.CentralDirectory;
import net.lingala.zip4j.model.CompressionMethod;
import net.lingala.zip4j.model.ExtraDataRecord;
import net.lingala.zip4j.model.ExtraField;
import net.lingala.zip4j.model.LocalFileHeader;
import net.lingala.zip4j.model.Zip64ExtendedInfo;
import net.lingala.zip4j.util.InternalZipConstants;
import net.lingala.zip4j.util.LittleEndianDecorator;
import net.lingala.zip4j.util.LittleEndianRandomAccessFile;
import org.apache.commons.io.FilenameUtils;

import java.io.IOException;
import java.util.Map;

/**
 * @author Oleg Cherednik
 * @since 08.03.2019
 */
@RequiredArgsConstructor
public final class LocalFileHeaderReader {

    private final CentralDirectory.FileHeader fileHeader;

    @NonNull
    public LocalFileHeader read(@NonNull LittleEndianRandomAccessFile in) throws IOException, ZipException {
        findHead(in);

        LocalFileHeader localFileHeader = new LocalFileHeader();

        localFileHeader.setVersionToExtract(in.readShort());
        localFileHeader.setGeneralPurposeFlag(in.readShort());
        localFileHeader.setCompressionMethod(CompressionMethod.parseValue(in.readShort()));
        localFileHeader.setLastModifiedTime(in.readInt());
        localFileHeader.setCrc32(in.readInt());
        localFileHeader.setCompressedSize(in.readIntAsLong());
        localFileHeader.setUncompressedSize(in.readIntAsLong());

        short fileNameLength = in.readShort();

        localFileHeader.setExtraFieldLength(in.readShort());
        localFileHeader.setFileName(FilenameUtils.normalize(in.readString(fileNameLength)));
        localFileHeader.setExtraDataRecords(new ExtraFieldReader(localFileHeader.getExtraFieldLength(), false, false, false, false).read(in));

        localFileHeader.setOffsetStartOfData(in.getFilePointer());
        localFileHeader.setPassword(fileHeader.getPassword());
        localFileHeader.setZip64ExtendedInfo(readZip64ExtendedInfo(localFileHeader));
        localFileHeader.setAesExtraDataRecord(readAESExtraDataRecord(localFileHeader.getExtraDataRecords()));

        if (localFileHeader.getCrc32() <= 0)
            localFileHeader.setCrc32(fileHeader.getCrc32());

        if (localFileHeader.getCompressedSize() <= 0)
            localFileHeader.setCompressedSize(fileHeader.getCompressedSize());

        if (localFileHeader.getUncompressedSize() <= 0)
            localFileHeader.setUncompressedSize(fileHeader.getUncompressedSize());

        return localFileHeader;
    }

    private void findHead(LittleEndianRandomAccessFile in) throws IOException {
        in.seek(fileHeader.getOffsLocalFileHeader());

        if (in.readInt() == InternalZipConstants.LOCSIG)
            return;

        throw new IOException("invalid local file header signature");
    }

    // TODO pretty similar to FileHeader
    @NonNull
    private static Zip64ExtendedInfo readZip64ExtendedInfo(@NonNull LocalFileHeader localFileHeader) throws IOException {
        ExtraDataRecord record = localFileHeader.getExtraDataRecordByHeader(Zip64ExtendedInfo.SIGNATURE);

        if (record == null)
            return Zip64ExtendedInfo.NULL;

        LittleEndianDecorator in = new LittleEndianDecorator(record.getData());

        Zip64ExtendedInfo res = new Zip64ExtendedInfo();
        res.setSize(record.getSizeOfData());
        res.setUncompressedSize((localFileHeader.getUncompressedSize() & 0xFFFF) == 0xFFFF ? in.readLong() : ExtraField.NO_DATA);
        res.setCompressedSize((localFileHeader.getCompressedSize() & 0xFFFF) == 0xFFFF ? in.readLong() : ExtraField.NO_DATA);
        // TODO why it throws exception
//        res.setOffsLocalHeaderRelative(in.readLong());
//        res.setDiskNumberStart(in.readInt());

        if (res.getUncompressedSize() != ExtraField.NO_DATA || res.getCompressedSize() != ExtraField.NO_DATA
                || res.getOffsLocalHeaderRelative() != ExtraField.NO_DATA || res.getDiskNumber() != ExtraField.NO_DATA)
            return res;

        return Zip64ExtendedInfo.NULL;
    }

    @NonNull
    private static AESExtraDataRecord readAESExtraDataRecord(@NonNull Map<Short, ExtraDataRecord> records) throws IOException {
        ExtraDataRecord record = records.get(AESExtraDataRecord.SIGNATURE);

        if (record == null)
            return AESExtraDataRecord.NULL;

        LittleEndianDecorator in = new LittleEndianDecorator(record.getData());

        AESExtraDataRecord res = new AESExtraDataRecord();
        res.setDataSize(record.getSizeOfData());
        res.setVersionNumber(in.readShort());
        res.setVendor(in.readString(2));
        res.setAesStrength(AESStrength.parseByte(in.readByte()));
        res.setCompressionMethod(CompressionMethod.parseValue(in.readShort()));

        return res;
    }
}
