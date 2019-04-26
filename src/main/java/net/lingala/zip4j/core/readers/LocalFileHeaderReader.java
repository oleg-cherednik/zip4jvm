package net.lingala.zip4j.core.readers;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.CentralDirectory;
import net.lingala.zip4j.model.CompressionMethod;
import net.lingala.zip4j.model.LocalFileHeader;
import net.lingala.zip4j.util.LittleEndianRandomAccessFile;
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
        short extraFieldLength = in.readShort();
        localFileHeader.setFileName(FilenameUtils.normalize(in.readString(fileNameLength)));
        localFileHeader.setExtraField(new ExtraFieldReader(extraFieldLength).read(in));
        localFileHeader.setOffs(in.getFilePointer());
        localFileHeader.setPassword(fileHeader.getPassword());

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

        if (in.readInt() == LocalFileHeader.SIGNATURE)
            return;

        throw new IOException("invalid local file header signature");
    }

}
