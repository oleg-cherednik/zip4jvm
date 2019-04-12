package net.lingala.zip4j.core.readers;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.lingala.zip4j.model.AESExtraDataRecord;
import net.lingala.zip4j.model.AESStrength;
import net.lingala.zip4j.model.CentralDirectory;
import net.lingala.zip4j.model.CompressionMethod;
import net.lingala.zip4j.model.EndCentralDirectory;
import net.lingala.zip4j.model.ExtraDataRecord;
import net.lingala.zip4j.model.Zip64EndCentralDirectory;
import net.lingala.zip4j.model.Zip64ExtendedInfo;
import net.lingala.zip4j.util.InternalZipConstants;
import net.lingala.zip4j.util.LittleEndianDecorator;
import net.lingala.zip4j.util.LittleEndianRandomAccessFile;
import org.apache.commons.io.FilenameUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Oleg Cherednik
 * @since 05.03.2019
 */
@RequiredArgsConstructor
final class CentralDirectoryReader {

    private final LittleEndianRandomAccessFile in;
    private final EndCentralDirectory dir;
    private final Zip64EndCentralDirectory zip64Dir;

    public CentralDirectory read() throws IOException {
        findHead();

        CentralDirectory dir = new CentralDirectory();
        dir.setFileHeaders(readFileHeaders());
        dir.setDigitalSignature(readDigitalSignature());

        return dir;
    }

    private List<CentralDirectory.FileHeader> readFileHeaders() throws IOException {
        int total = isZip64() ? (int)zip64Dir.getTotalEntries() : dir.getTotalEntries();
        List<CentralDirectory.FileHeader> fileHeaders = new ArrayList<>(total);

        for (int i = 0; i < total; i++)
            fileHeaders.add(readFileHeader());

        return fileHeaders;
    }

    private CentralDirectory.FileHeader readFileHeader() throws IOException {
        CentralDirectory.FileHeader fileHeader = new CentralDirectory.FileHeader();

        int signature = in.readInt();

        if (signature != InternalZipConstants.CENSIG)
            throw new IOException("Expected central directory entry not found (offs:" + (in.getFilePointer() - 4) + ')');

        fileHeader.setVersionMadeBy(in.readShort());
        fileHeader.setVersionToExtract(in.readShort());
        fileHeader.setGeneralPurposeFlag(in.readShort());
        fileHeader.setCompressionMethod(CompressionMethod.parseValue(in.readShort()));
        fileHeader.setLastModifiedTime(in.readInt());
        fileHeader.setCrc32(in.readInt());
        fileHeader.setCompressedSize(in.readIntAsLong());
        fileHeader.setUncompressedSize(in.readIntAsLong());
        fileHeader.setFileNameLength(in.readShort());
        fileHeader.setExtraFieldLength(in.readShort());
        fileHeader.setFileCommentLength(in.readShort());
        fileHeader.setDiskNumber(in.readShort());
        fileHeader.setInternalFileAttributes(in.readBytes(2));
        fileHeader.setExternalFileAttributes(in.readBytes(4));
        fileHeader.setOffsLocalFileHeader(in.readIntAsLong());
        fileHeader.setFileName(FilenameUtils.normalize(in.readString(fileHeader.getFileNameLength()), true));
        fileHeader.setExtraDataRecords(readExtraDataRecords(in, fileHeader.getExtraFieldLength()));

        fileHeader.setZip64ExtendedInfo(readZip64ExtendedInfo(fileHeader));
        fileHeader.setAesExtraDataRecord(readAESExtraDataRecord(fileHeader.getExtraDataRecords()));
        fileHeader.setFileComment(in.readString(fileHeader.getFileCommentLength()));

        return fileHeader;
    }

    // TODO same logic for extra field for FileHeader and LocalFileHeader
    @NonNull
    public static Map<Short, ExtraDataRecord> readExtraDataRecords(LittleEndianRandomAccessFile in, int length) throws IOException {
        if (length <= 0)
            return Collections.emptyMap();

        final long offsMax = in.getFilePointer() + length;
        Map<Short, ExtraDataRecord> map = new HashMap<>();

        while (in.getFilePointer() < offsMax) {
            ExtraDataRecord record = new ExtraDataRecord();
            record.setHeader(in.readShort());
            record.setSizeOfData(in.readShort());

            if (record.getSizeOfData() == 0)
                continue;
            if (in.getFilePointer() + record.getSizeOfData() > offsMax)
                // extra data record is corrupt; skip reading any further extra data
                break;

            record.setData(in.readBytes(record.getSizeOfData()));
            map.put(record.getHeader(), record);
        }

        return map.isEmpty() ? Collections.emptyMap() : map;
    }

    private CentralDirectory.DigitalSignature readDigitalSignature() throws IOException {
        if (in.readInt() != InternalZipConstants.DIGSIG)
            return null;

        CentralDirectory.DigitalSignature digitalSignature = new CentralDirectory.DigitalSignature();
        digitalSignature.setSizeOfData(in.readShort());
        digitalSignature.setSignatureData(in.readBytes(digitalSignature.getSizeOfData()));

        return digitalSignature;
    }

    public static AESExtraDataRecord readAESExtraDataRecord(@NonNull Map<Short, ExtraDataRecord> records) throws IOException {
        ExtraDataRecord record = records.get(ExtraDataRecord.HEADER_AESSIG);

        if (record == null)
            return null;

        LittleEndianDecorator in = new LittleEndianDecorator(record.getData());

        AESExtraDataRecord res = new AESExtraDataRecord();
        res.setDataSize(record.getSizeOfData());
        res.setVersionNumber(in.readShort());
        res.setVendor(in.readString(2));
        res.setAesStrength(AESStrength.parseByte(in.readByte()));
        res.setCompressionMethod(CompressionMethod.parseValue(in.readShort()));

        return res;
    }

    // TODO pretty similar to LocalFileHeader
    private static Zip64ExtendedInfo readZip64ExtendedInfo(@NonNull CentralDirectory.FileHeader fileHeader) throws IOException {
        ExtraDataRecord record = fileHeader.getExtraDataRecordByHeader(ExtraDataRecord.HEADER_ZIP64);

        if (record == null)
            return null;

        LittleEndianDecorator in = new LittleEndianDecorator(record.getData());

        Zip64ExtendedInfo res = new Zip64ExtendedInfo();
        res.setSize(record.getSizeOfData());
        res.setUnCompressedSize((fileHeader.getUncompressedSize() & 0xFFFF) == 0xFFFF ? in.readLong() : -1);
        res.setCompressedSize((fileHeader.getCompressedSize() & 0xFFFF) == 0xFFFF ? in.readLong() : -1);
        res.setOffsLocalHeaderRelative((fileHeader.getOffsLocalFileHeader() & 0xFFFF) == 0xFFFF ? in.readLong() : -1);
        res.setDiskNumberStart((fileHeader.getDiskNumber() & 0xFFFF) == 0xFFFF ? in.readInt() : -1);

        if (res.getUnCompressedSize() != -1 || res.getCompressedSize() != -1
                || res.getOffsLocalHeaderRelative() != -1 || res.getDiskNumberStart() != -1)
            return res;

        return null;
    }

    private void findHead() throws IOException {
        in.seek(isZip64() ? zip64Dir.getOffsetStartCenDirWRTStartDiskNo() : dir.getOffs());
    }

    private boolean isZip64() {
        return zip64Dir != null;
    }
}
