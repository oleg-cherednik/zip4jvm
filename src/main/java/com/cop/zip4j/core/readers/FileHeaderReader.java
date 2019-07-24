package com.cop.zip4j.core.readers;

import lombok.RequiredArgsConstructor;
import com.cop.zip4j.exception.ZipException;
import com.cop.zip4j.io.LittleEndianRandomAccessFile;
import com.cop.zip4j.model.CentralDirectory;
import com.cop.zip4j.model.CompressionMethod;
import com.cop.zip4j.utils.ZipUtils;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Oleg Cherednik
 * @since 26.04.2019
 */
@RequiredArgsConstructor
final class FileHeaderReader {

    private final long totalEntries;

    public List<CentralDirectory.FileHeader> read(LittleEndianRandomAccessFile in) throws IOException {
        List<CentralDirectory.FileHeader> fileHeaders = new LinkedList<>();

        for (int i = 0; i < totalEntries; i++)
            fileHeaders.add(readFileHeader(in));

        return fileHeaders;
    }

    private static CentralDirectory.FileHeader readFileHeader(LittleEndianRandomAccessFile in) throws IOException {
        CentralDirectory.FileHeader fileHeader = new CentralDirectory.FileHeader();

        if (in.readDword() != CentralDirectory.FileHeader.SIGNATURE)
            throw new ZipException("Expected central directory entry not found (offs:" + (in.getFilePointer() - 4) + ')');

        fileHeader.setVersionMadeBy(in.readWord());
        fileHeader.setVersionToExtract(in.readWord());
        fileHeader.setGeneralPurposeFlag(in.readWord());
        fileHeader.setCompressionMethod(CompressionMethod.parseValue(in.readWord()));
        fileHeader.setLastModifiedTime(in.readDword());
        fileHeader.setCrc32(in.readDword());
        fileHeader.setCompressedSize(in.readDwordLong());
        fileHeader.setUncompressedSize(in.readDwordLong());
        int fileNameLength = in.readWord();
        int extraFieldLength = in.readWord();
        int fileCommentLength = in.readWord();
        fileHeader.setDiskNumber(in.readWord());
        fileHeader.setInternalFileAttributes(in.readBytes(2));
        fileHeader.setExternalFileAttributes(in.readBytes(4));
        fileHeader.setOffsLocalFileHeader(in.readDwordLong());
        fileHeader.setFileName(ZipUtils.normalizeFileName.apply(in.readString(fileNameLength)));

        boolean uncompressedSize = fileHeader.getUncompressedSize() == 0xFFFF;
        boolean compressedSize = fileHeader.getCompressedSize() == 0xFFFF;
        boolean offs = fileHeader.getOffsLocalFileHeader() == 0xFFFF;
        boolean diskNumber = fileHeader.getDiskNumber() == 0xFFFF;
        fileHeader.setExtraField(new ExtraFieldReader(extraFieldLength, uncompressedSize, compressedSize, offs, diskNumber).read(in));
        fileHeader.setFileComment(in.readString(fileCommentLength));

        return fileHeader;
    }

}
