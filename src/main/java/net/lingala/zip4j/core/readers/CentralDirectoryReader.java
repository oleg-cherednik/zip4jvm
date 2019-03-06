package net.lingala.zip4j.core.readers;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.AESExtraDataRecord;
import net.lingala.zip4j.model.CentralDirectory;
import net.lingala.zip4j.model.DigitalSignature;
import net.lingala.zip4j.model.EndCentralDirectory;
import net.lingala.zip4j.model.ExtraDataRecord;
import net.lingala.zip4j.model.Zip64EndCentralDirectory;
import net.lingala.zip4j.model.Zip64ExtendedInfo;
import net.lingala.zip4j.util.InternalZipConstants;
import net.lingala.zip4j.util.LittleEndianDecorator;
import net.lingala.zip4j.util.LittleEndianRandomAccessFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Oleg Cherednik
 * @since 05.03.2019
 */
@RequiredArgsConstructor
final class CentralDirectoryReader {

    private final LittleEndianRandomAccessFile in;
    private final EndCentralDirectory dir;
    private final Zip64EndCentralDirectory zip64Dir;
    private final boolean zip64;

    public CentralDirectory read() throws IOException, ZipException {
        findHead();

        CentralDirectory centralDirectory = new CentralDirectory();
        centralDirectory.setFileHeaders(readFileHeaders());
        centralDirectory.setDigitalSignature(readDigitalSignature());

        return centralDirectory;
    }

    @NonNull
    private List<CentralDirectory.FileHeader> readFileHeaders() throws IOException {
        int total = zip64 ? (int)zip64Dir.getTotNoOfEntriesInCentralDir() : dir.getTotNoOfEntriesInCentralDir();
        List<CentralDirectory.FileHeader> fileHeaders = new ArrayList<>(total);

        for (int i = 0; i < total; i++)
            fileHeaders.add(readFileHeader());

        return fileHeaders;
    }

    @NonNull
    private CentralDirectory.FileHeader readFileHeader() throws IOException {
        CentralDirectory.FileHeader fileHeader = new CentralDirectory.FileHeader();

        int signature = in.readInt();

        if (signature != InternalZipConstants.CENSIG)
            throw new IOException("Expected central directory entry not found (offs:" + in.getFilePointer() + ')');

        fileHeader.setVersionMadeBy(in.readShort());
        fileHeader.setVersionNeededToExtract(in.readShort());
        fileHeader.setGeneralPurposeFlag(in.readBytes(2));
        fileHeader.setCompressionMethod(in.readShort());
        fileHeader.setLastModFileTime(in.readInt());
        fileHeader.setCrc32(in.readInt());
        fileHeader.setCompressedSize(in.readIntAsLong());
        fileHeader.setUncompressedSize(in.readIntAsLong());
        fileHeader.setFileNameLength(in.readShort());
        fileHeader.setExtraFieldLength(in.readShort());
        fileHeader.setFileCommentLength(in.readShort());
        fileHeader.setDiskNumberStart(in.readShort());
        fileHeader.setInternalFileAttr(in.readBytes(2));
        fileHeader.setExternalFileAttr(in.readBytes(4));
        fileHeader.setOffLocalHeaderRelative(in.readIntAsLong());
        fileHeader.setFileName(in.readString(fileHeader.getFileNameLength()));

        fileHeader.setExtraDataRecords(readExtraDataRecords(fileHeader.getExtraFieldLength()));
        fileHeader.setZip64ExtendedInfo(readZip64ExtendedInfo(fileHeader));
        fileHeader.setAesExtraDataRecord(readAESExtraDataRecord(fileHeader.getExtraDataRecords()));
        fileHeader.setFileComment(in.readString(fileHeader.getFileCommentLength()));

        return fileHeader;
    }

    // TODO same logic for extra field for FileHeader and LocalFileHeader
    @NonNull
    private List<ExtraDataRecord> readExtraDataRecords(int length) throws IOException {
        if (length <= 0)
            return Collections.emptyList();

        final long offsMax = in.getFilePointer() + length;
        List<ExtraDataRecord> records = new ArrayList<>();

        while (in.getFilePointer() < offsMax) {
            ExtraDataRecord record = new ExtraDataRecord();
            record.setHeader(in.readShort());
            record.setSizeOfData(in.readShort());

            if (in.getFilePointer() + record.getSizeOfData() > offsMax)
                // extra data record is corrupt; skil reading any further extra data
                break;

            record.setData(in.readBytes(record.getSizeOfData()));
            records.add(record);
        }

        return records.isEmpty() ? Collections.emptyList() : records;
    }

    private DigitalSignature readDigitalSignature() throws IOException {
        if (in.readInt() != InternalZipConstants.DIGSIG)
            return null;

        DigitalSignature digitalSignature = new DigitalSignature();
        digitalSignature.setSizeOfData(in.readShort());
        digitalSignature.setSignatureData(in.readBytes(digitalSignature.getSizeOfData()));

        return digitalSignature;
    }

    private static AESExtraDataRecord readAESExtraDataRecord(@NonNull List<ExtraDataRecord> records) throws IOException {
        for (ExtraDataRecord record : records) {
            if (record.getHeader() != ExtraDataRecord.HEADER_AESSIG)
                continue;
            if (record.getSizeOfData() == 0)
                return null;

            LittleEndianDecorator in = new LittleEndianDecorator(record.getData());

            AESExtraDataRecord res = new AESExtraDataRecord();
            res.setDataSize(record.getSizeOfData());
            res.setVersionNumber(in.readShort());
            res.setVendorID(in.readString(2));
            res.setAesStrength(in.readByte());
            res.setCompressionMethod(in.readShort());

            return res;
        }

        return null;
    }

    private static Zip64ExtendedInfo readZip64ExtendedInfo(@NonNull CentralDirectory.FileHeader fileHeader) throws IOException {
        for (ExtraDataRecord record : fileHeader.getExtraDataRecords()) {
            if (record.getHeader() != ExtraDataRecord.HEADER_ZIP64)
                continue;
            if (record.getSizeOfData() == 0)
                return null;

            LittleEndianDecorator in = new LittleEndianDecorator(record.getData());

            Zip64ExtendedInfo res = new Zip64ExtendedInfo();
            res.setSize(record.getSizeOfData());
            res.setUnCompressedSize((fileHeader.getUncompressedSize() & 0xFFFF) == 0xFFFF ? in.readLong() : -1);
            res.setCompressedSize((fileHeader.getCompressedSize() & 0xFFFF) == 0xFFFF ? in.readLong() : -1);
            res.setOffsLocalHeaderRelative((fileHeader.getOffLocalHeaderRelative() & 0xFFFF) == 0xFFFF ? in.readLong() : -1);
            res.setDiskNumberStart((fileHeader.getDiskNumberStart() & 0xFFFF) == 0xFFFF ? in.readInt() : -1);

            if (res.getUnCompressedSize() != -1 || res.getCompressedSize() != -1
                    || res.getOffsLocalHeaderRelative() != -1 || res.getDiskNumberStart() != -1)
                return res;

            return null;
        }

        return null;
    }

    private void findHead() throws IOException {
        in.seek(zip64 ? zip64Dir.getOffsetStartCenDirWRTStartDiskNo() : dir.getOffOfStartOfCentralDir());
    }
}
