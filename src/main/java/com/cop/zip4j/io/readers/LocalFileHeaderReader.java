package com.cop.zip4j.io.readers;

import com.cop.zip4j.exception.Zip4jException;
import com.cop.zip4j.io.in.DataInput;
import com.cop.zip4j.model.CompressionMethod;
import com.cop.zip4j.model.GeneralPurposeFlag;
import com.cop.zip4j.model.LocalFileHeader;
import com.cop.zip4j.model.entry.PathZipEntry;
import com.cop.zip4j.utils.ZipUtils;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.io.IOException;

import static com.cop.zip4j.model.builders.LocalFileHeaderBuilder.LOOK_IN_EXTRA_FIELD;

/**
 * @author Oleg Cherednik
 * @since 08.03.2019
 */
@RequiredArgsConstructor
public final class LocalFileHeaderReader {

    @NonNull
    private final PathZipEntry entry;

    @NonNull
    public LocalFileHeader read(@NonNull DataInput in) throws IOException {
        findHead(in);

        LocalFileHeader localFileHeader = new LocalFileHeader();

        localFileHeader.setVersionToExtract(in.readWord());
        localFileHeader.setGeneralPurposeFlag(new GeneralPurposeFlag(in.readWord()));
        localFileHeader.setCompressionMethod(CompressionMethod.parseValue(in.readWord()));
        localFileHeader.setLastModifiedTime((int)in.readDword());
        localFileHeader.setCrc32(in.readDword());
        localFileHeader.setCompressedSize(in.readDword());
        localFileHeader.setUncompressedSize(in.readDword());
        int fileNameLength = in.readWord();
        int extraFieldLength = in.readWord();
        localFileHeader.setFileName(ZipUtils.normalizeFileName.apply(in.readString(fileNameLength)));
        localFileHeader.setExtraField(getExtraFieldReader(extraFieldLength, localFileHeader).read(in));

        return localFileHeader;
    }

    private void findHead(DataInput in) throws IOException {
        in.seek(entry.getLocalFileHeaderOffs());

        if (in.readSignature() != LocalFileHeader.SIGNATURE)
            throw new Zip4jException("invalid local file header signature");
    }

    private static ExtraFieldReader getExtraFieldReader(int size, LocalFileHeader localFileHeader) {
        boolean uncompressedSize = localFileHeader.getUncompressedSize() == LOOK_IN_EXTRA_FIELD;
        boolean compressedSize = localFileHeader.getCompressedSize() == LOOK_IN_EXTRA_FIELD;
        return new ExtraFieldReader(size, uncompressedSize, compressedSize, false, false);
    }

}
