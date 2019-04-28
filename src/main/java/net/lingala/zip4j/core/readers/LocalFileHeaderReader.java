package net.lingala.zip4j.core.readers;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.io.LittleEndianRandomAccessFile;
import net.lingala.zip4j.model.CentralDirectory;
import net.lingala.zip4j.model.CompressionMethod;
import net.lingala.zip4j.model.LocalFileHeader;
import org.apache.commons.io.FilenameUtils;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 08.03.2019
 */
@RequiredArgsConstructor
public final class LocalFileHeaderReader {

    private final CentralDirectory.FileHeader fileHeader;

    @NonNull
    public LocalFileHeader read(@NonNull LittleEndianRandomAccessFile in) throws IOException {
        findHead(in);

        LocalFileHeader localFileHeader = new LocalFileHeader();

        localFileHeader.setVersionToExtract(in.readWord());
        localFileHeader.setGeneralPurposeFlag(in.readWord());
        localFileHeader.setCompressionMethod(CompressionMethod.parseValue(in.readWord()));
        localFileHeader.setLastModifiedTime(in.readDword());
        localFileHeader.setCrc32(in.readDword());
        localFileHeader.setCompressedSize(in.readDwordLong());
        localFileHeader.setUncompressedSize(in.readDwordLong());
        int fileNameLength = in.readWord();
        int extraFieldLength = in.readWord();
        localFileHeader.setFileName(FilenameUtils.normalize(in.readString(fileNameLength)));
        localFileHeader.setExtraField(new ExtraFieldReader(extraFieldLength).read(in));

        localFileHeader.setOffs(in.getFilePointer());

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

        if (in.readDword() == LocalFileHeader.SIGNATURE)
            return;

        throw new ZipException("invalid local file header signature");
    }

}
