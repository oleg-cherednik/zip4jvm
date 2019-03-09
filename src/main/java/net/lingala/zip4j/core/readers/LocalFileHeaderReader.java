package net.lingala.zip4j.core.readers;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.CentralDirectory;
import net.lingala.zip4j.model.CompressionMethod;
import net.lingala.zip4j.model.Encryption;
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

    public LocalFileHeader read() throws IOException, ZipException {
        findHead();

        LocalFileHeader localFileHeader = new LocalFileHeader();

        localFileHeader.setVersionNeededToExtract(in.readShort());
        localFileHeader.setGeneralPurposeFlag(in.readBytes(2));
        localFileHeader.setCompressionMethod(CompressionMethod.parseValue(in.readShort()));
        localFileHeader.setLastModFileTime(in.readInt());
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

        if (localFileHeader.isEncrypted()) {

            if (localFileHeader.getEncryption() == Encryption.AES) {
                //Do nothing
            } else {
                if ((localFileHeader.getGeneralPurposeFlag()[0] & 64) == 64) {
                    //hardcoded for now
                    localFileHeader.setEncryption(Encryption.STRONG);
                } else {
                    localFileHeader.setEncryption(Encryption.STANDARD);
//						localFileHeader.setCompressedSize(localFileHeader.getCompressedSize()
//								- ZipConstants.STD_DEC_HDR_SIZE);
                }
            }

        }

        if (localFileHeader.getCrc32() <= 0)
            localFileHeader.setCrc32(fileHeader.getCrc32());

        if (localFileHeader.getCompressedSize() <= 0)
            localFileHeader.setCompressedSize(fileHeader.getCompressedSize());

        if (localFileHeader.getUncompressedSize() <= 0)
            localFileHeader.setUncompressedSize(fileHeader.getUncompressedSize());

        return localFileHeader;
    }

    private void findHead() throws IOException {
        in.seek(fileHeader.getOffLocalHeaderRelative());

        if (in.readInt() == InternalZipConstants.LOCSIG)
            return;

        throw new IOException("invalid local file header signature");
    }

    // TODO pretty similar to FileHeader
    private static Zip64ExtendedInfo readZip64ExtendedInfo(@NonNull LocalFileHeader localFileHeader) throws IOException {
        for (ExtraDataRecord record : localFileHeader.getExtraDataRecords()) {
            if (record.getHeader() != ExtraDataRecord.HEADER_ZIP64)
                continue;
            if (record.getSizeOfData() == 0)
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

        return null;
    }
}
