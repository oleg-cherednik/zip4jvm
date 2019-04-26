package net.lingala.zip4j.core.readers;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.lingala.zip4j.model.CentralDirectory;
import net.lingala.zip4j.model.CompressionMethod;
import net.lingala.zip4j.util.LittleEndianRandomAccessFile;
import org.apache.commons.io.FilenameUtils;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Oleg Cherednik
 * @since 05.03.2019
 */
@RequiredArgsConstructor
final class CentralDirectoryReader {

    private final long offs;
    private final long totalEntries;

    @NonNull
    public CentralDirectory read(@NonNull LittleEndianRandomAccessFile in) throws IOException {
        findHead(in);

        CentralDirectory dir = new CentralDirectory();
        dir.setFileHeaders(readFileHeaders(in));
        dir.setDigitalSignature(new DigitalSignatureReader().read(in));

        return dir;
    }

    private List<CentralDirectory.FileHeader> readFileHeaders(LittleEndianRandomAccessFile in) throws IOException {
        List<CentralDirectory.FileHeader> fileHeaders = new LinkedList<>();

        for (int i = 0; i < totalEntries; i++)
            fileHeaders.add(readFileHeader(in));

        return fileHeaders;
    }

    private static CentralDirectory.FileHeader readFileHeader(LittleEndianRandomAccessFile in) throws IOException {
        CentralDirectory.FileHeader fileHeader = new CentralDirectory.FileHeader();

        if (in.readInt() != CentralDirectory.FileHeader.SIGNATURE)
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
        short extraFieldLength = in.readShort();
        short fileCommentLength = in.readShort();
        fileHeader.setDiskNumber(in.readShort());
        fileHeader.setInternalFileAttributes(in.readBytes(2));
        fileHeader.setExternalFileAttributes(in.readBytes(4));
        fileHeader.setOffsLocalFileHeader(in.readIntAsLong());
        fileHeader.setFileName(FilenameUtils.normalize(in.readString(fileNameLength), true));

        boolean uncompressedSize = (fileHeader.getUncompressedSize() & 0xFFFF) == 0xFFFF;
        boolean compressedSize = (fileHeader.getCompressedSize() & 0xFFFF) == 0xFFFF;
        boolean offs = (fileHeader.getOffsLocalFileHeader() & 0xFFFF) == 0xFFFF;
        boolean diskNumber = (fileHeader.getDiskNumber() & 0xFFFF) == 0xFFFF;
        fileHeader.setExtraField(new ExtraFieldReader(extraFieldLength, uncompressedSize, compressedSize, offs, diskNumber).read(in));
        fileHeader.setFileComment(in.readString(fileCommentLength));

        return fileHeader;
    }

    private void findHead(LittleEndianRandomAccessFile in) throws IOException {
        in.seek(offs);
    }
}
