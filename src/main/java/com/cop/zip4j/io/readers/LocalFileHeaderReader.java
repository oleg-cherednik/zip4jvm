package com.cop.zip4j.io.readers;

import com.cop.zip4j.exception.Zip4jException;
import com.cop.zip4j.io.in.DataInput;
import com.cop.zip4j.model.CentralDirectory;
import com.cop.zip4j.model.CompressionMethod;
import com.cop.zip4j.model.DataDescriptor;
import com.cop.zip4j.model.LocalFileHeader;
import com.cop.zip4j.utils.ZipUtils;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.util.function.LongSupplier;

import static com.cop.zip4j.model.ZipModel.ZIP_64_LIMIT;
import static com.cop.zip4j.model.builders.LocalFileHeaderBuilder.LOOK_IN_DATA_DESCRIPTOR;

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
        localFileHeader.setCrc32(getFromFileHeader(in, fileHeader::getCrc32));
        localFileHeader.setCompressedSize(getFromFileHeader(in, fileHeader::getCompressedSize));
        localFileHeader.setUncompressedSize(getFromFileHeader(in, fileHeader::getUncompressedSize));
        int fileNameLength = in.readWord();
        int extraFieldLength = in.readWord();
        localFileHeader.setFileName(ZipUtils.normalizeFileName.apply(in.readString(fileNameLength)));

        boolean uncompressedSize = localFileHeader.getUncompressedSize() == ZIP_64_LIMIT;
        boolean compressedSize = localFileHeader.getCompressedSize() == ZIP_64_LIMIT;
        localFileHeader.setExtraField(new ExtraFieldReader(extraFieldLength, uncompressedSize, compressedSize, false, false).read(in));

        localFileHeader.setOffs(in.getOffs());

        return localFileHeader;
    }

    /**
     * In case of value is {@literal 0} it means that real value is in {@link DataDescriptor} that place after the data.
     * We do not use it while reading, because we could get this size from {@link CentralDirectory.FileHeader}.
     */
    private static long getFromFileHeader(DataInput in, LongSupplier supplier) throws IOException {
        long size = in.readDword();
        return size == LOOK_IN_DATA_DESCRIPTOR ? supplier.getAsLong() : size;
    }

    private void findHead(DataInput in) throws IOException {
        in.seek(fileHeader.getOffsLocalFileHeader());

        if (in.readSignature() != LocalFileHeader.SIGNATURE)
            throw new Zip4jException("invalid local file header signature");
    }

}
