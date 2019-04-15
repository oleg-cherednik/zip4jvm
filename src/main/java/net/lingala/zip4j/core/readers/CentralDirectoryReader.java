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
import net.lingala.zip4j.util.LittleEndianDecorator;
import net.lingala.zip4j.util.LittleEndianRandomAccessFile;
import org.apache.commons.io.FilenameUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Oleg Cherednik
 * @since 05.03.2019
 */
@RequiredArgsConstructor
final class CentralDirectoryReader {

    private final EndCentralDirectory dir;
    private final Zip64EndCentralDirectory zip64Dir;

    @NonNull
    public CentralDirectory read(@NonNull LittleEndianRandomAccessFile in) throws IOException {
        findHead(in);

        CentralDirectory dir = new CentralDirectory();
        dir.setFileHeaders(readFileHeaders(in));
        dir.setDigitalSignature(new DigitalSignatureReader().read(in));

        return dir;
    }

    private List<CentralDirectory.FileHeader> readFileHeaders(LittleEndianRandomAccessFile in) throws IOException {
        int total = isZip64() ? (int)zip64Dir.getTotalEntries() : dir.getTotalEntries();
        List<CentralDirectory.FileHeader> fileHeaders = new ArrayList<>(total);

        for (int i = 0; i < total; i++)
            fileHeaders.add(readFileHeader(in));

        return fileHeaders;
    }

    private static CentralDirectory.FileHeader readFileHeader(LittleEndianRandomAccessFile in) throws IOException {
        CentralDirectory.FileHeader fileHeader = new CentralDirectory.FileHeader();

        int signature = in.readInt();

        if (signature != CentralDirectory.FileHeader.SIGNATURE)
            throw new IOException("Expected central directory entry not found (offs:" + (in.getFilePointer() - 4) + ')');

        fileHeader.setVersionMadeBy(in.readShort());
        fileHeader.setVersionToExtract(in.readShort());
        fileHeader.setGeneralPurposeFlag(in.readShort());
        fileHeader.setCompressionMethod(CompressionMethod.parseValue(in.readShort()));
        fileHeader.setLastModifiedTime(in.readInt());
        fileHeader.setCrc32(in.readInt());
        fileHeader.setCompressedSize(in.readIntAsLong());
        fileHeader.setUncompressedSize(in.readIntAsLong());
        short fileNameLength = in.readShort();
        fileHeader.setExtraFieldLength(in.readShort());
        short fileCommentLength = in.readShort();
        fileHeader.setDiskNumber(in.readShort());
        fileHeader.setInternalFileAttributes(in.readBytes(2));
        fileHeader.setExternalFileAttributes(in.readBytes(4));
        fileHeader.setOffsLocalFileHeader(in.readIntAsLong());
        fileHeader.setFileName(FilenameUtils.normalize(in.readString(fileNameLength), true));
        new ExtraFieldReader(fileHeader.getExtraFieldLength()).read(in, fileHeader);
        fileHeader.setFileComment(in.readString(fileCommentLength));

        return fileHeader;
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

    private void findHead(LittleEndianRandomAccessFile in) throws IOException {
        in.seek(isZip64() ? zip64Dir.getOffsetStartCenDirWRTStartDiskNo() : dir.getOffs());
    }

    private boolean isZip64() {
        return zip64Dir != null;
    }
}
