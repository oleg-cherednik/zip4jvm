package com.cop.zip4j.io.readers;

import com.cop.zip4j.exception.Zip4jException;
import com.cop.zip4j.io.in.DataInput;
import com.cop.zip4j.model.CentralDirectory;
import com.cop.zip4j.model.CompressionMethod;
import com.cop.zip4j.model.ExternalFileAttributes;
import com.cop.zip4j.model.InternalFileAttributes;
import com.cop.zip4j.utils.ZipUtils;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import static com.cop.zip4j.model.Zip64.LIMIT;

/**
 * @author Oleg Cherednik
 * @since 26.04.2019
 */
@RequiredArgsConstructor
final class FileHeaderReader {

    private final long totalEntries;

    public List<CentralDirectory.FileHeader> read(@NonNull DataInput in) throws IOException {
        List<CentralDirectory.FileHeader> fileHeaders = new LinkedList<>();

        for (int i = 0; i < totalEntries; i++)
            fileHeaders.add(readFileHeader(in));

        return fileHeaders;
    }

    private static CentralDirectory.FileHeader readFileHeader(DataInput in) throws IOException {
        long offs = in.getOffs();
        CentralDirectory.FileHeader fileHeader = new CentralDirectory.FileHeader();

        if (in.readSignature() != CentralDirectory.FileHeader.SIGNATURE)
            throw new Zip4jException("Expected central directory entry not found offs=" + offs);

        fileHeader.setVersionMadeBy(in.readWord());
        fileHeader.setVersionToExtract(in.readWord());
        fileHeader.setGeneralPurposeFlagData(in.readWord());
        fileHeader.setCompressionMethod(CompressionMethod.parseValue(in.readWord()));
        fileHeader.setLastModifiedTime((int)in.readDword());
        fileHeader.setCrc32(in.readDword());
        fileHeader.setCompressedSize(in.readDword());
        fileHeader.setUncompressedSize(in.readDword());
        int fileNameLength = in.readWord();
        int extraFieldLength = in.readWord();
        int fileCommentLength = in.readWord();
        fileHeader.setDiskNumber(in.readWord());
        fileHeader.setInternalFileAttributes(InternalFileAttributes.read(in));
        fileHeader.setExternalFileAttributes(ExternalFileAttributes.read(in));
        fileHeader.setOffsLocalFileHeader(in.readDword());
        fileHeader.setFileName(ZipUtils.normalizeFileName.apply(in.readString(fileNameLength)));
        updateExtraField(fileHeader, extraFieldLength, in);
        fileHeader.setFileComment(in.readString(fileCommentLength));

        return fileHeader;
    }

    private static void updateExtraField(CentralDirectory.FileHeader fileHeader, int extraFieldLength, DataInput in) throws IOException {
        boolean uncompressedSize = fileHeader.getUncompressedSize() == LIMIT;
        boolean compressedSize = fileHeader.getCompressedSize() == LIMIT;
        boolean offsHeader = fileHeader.getOffsLocalFileHeader() == LIMIT;
        boolean diskNumber = fileHeader.getDiskNumber() == 0xFFFF;
        new ExtraFieldReader(extraFieldLength, uncompressedSize, compressedSize, offsHeader, diskNumber, fileHeader.getExtraField()).read(in);
    }

}
