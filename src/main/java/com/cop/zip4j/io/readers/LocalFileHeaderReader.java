package com.cop.zip4j.io.readers;

import com.cop.zip4j.exception.Zip4jException;
import com.cop.zip4j.io.in.DataInput;
import com.cop.zip4j.model.CentralDirectory;
import com.cop.zip4j.model.CompressionMethod;
import com.cop.zip4j.model.LocalFileHeader;
import com.cop.zip4j.utils.ZipUtils;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.io.IOException;

import static com.cop.zip4j.model.ZipModel.ZIP_64_LIMIT;

/**
 * @author Oleg Cherednik
 * @since 08.03.2019
 */
@RequiredArgsConstructor
public final class LocalFileHeaderReader {

    private final CentralDirectory.FileHeader fileHeader;

    @NonNull
    public LocalFileHeader read(@NonNull DataInput in) throws IOException {
        findHead(in);

        LocalFileHeader localFileHeader = new LocalFileHeader();

        localFileHeader.setVersionToExtract(in.readWord());
        localFileHeader.setGeneralPurposeFlag(in.readWord());
        localFileHeader.setCompressionMethod(CompressionMethod.parseValue(in.readWord()));
        localFileHeader.setLastModifiedTime((int)in.readDword());
        localFileHeader.setCrc32(in.readDword());
        localFileHeader.setCompressedSize(in.readDword());
        localFileHeader.setUncompressedSize(in.readDword());
        int fileNameLength = in.readWord();
        int extraFieldLength = in.readWord();
        localFileHeader.setFileName(ZipUtils.normalizeFileName.apply(in.readString(fileNameLength)));

        boolean uncompressedSize = localFileHeader.getUncompressedSize() == ZIP_64_LIMIT;
        boolean compressedSize = localFileHeader.getCompressedSize() == ZIP_64_LIMIT;
        localFileHeader.setExtraField(new ExtraFieldReader(extraFieldLength, uncompressedSize, compressedSize, false, false).read(in));

        localFileHeader.setOffs(in.getOffs());

        return localFileHeader;
    }

    private void findHead(DataInput in) throws IOException {
        in.seek(fileHeader.getOffsLocalFileHeader());

        if (in.readSignature() != LocalFileHeader.SIGNATURE)
            throw new Zip4jException("invalid local file header signature");
    }

}
