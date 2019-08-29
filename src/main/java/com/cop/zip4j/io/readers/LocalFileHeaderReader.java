package com.cop.zip4j.io.readers;

import com.cop.zip4j.exception.Zip4jException;
import com.cop.zip4j.io.in.DataInput;
import com.cop.zip4j.model.CentralDirectory;
import com.cop.zip4j.model.CompressionMethod;
import com.cop.zip4j.model.DataDescriptor;
import com.cop.zip4j.model.GeneralPurposeFlag;
import com.cop.zip4j.model.LocalFileHeader;
import com.cop.zip4j.model.entry.PathZipEntry;
import com.cop.zip4j.utils.ZipUtils;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.util.function.LongSupplier;

import static com.cop.zip4j.model.builders.LocalFileHeaderBuilder.LOOK_IN_DATA_DESCRIPTOR;
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
        updateExtraField(localFileHeader, extraFieldLength, in);
        localFileHeader.setOffs(in.getOffs());

        return readDataDescriptor(localFileHeader);
    }

    private static void updateExtraField(LocalFileHeader localFileHeader, int extraFieldLength, DataInput in) throws IOException {
        boolean uncompressedSize = localFileHeader.getUncompressedSize() == LOOK_IN_EXTRA_FIELD;
        boolean compressedSize = localFileHeader.getCompressedSize() == LOOK_IN_EXTRA_FIELD;
        new ExtraFieldReader(extraFieldLength, uncompressedSize, compressedSize, false, false, localFileHeader.getExtraField()).read(in);
    }

    /**
     * In case of value is {@literal 0} it means that real value is in {@link DataDescriptor} that place after the data.
     * We do not use it while reading, because we could get this size from {@link CentralDirectory.FileHeader}.
     */
    private LocalFileHeader readDataDescriptor(LocalFileHeader localFileHeader) {
        localFileHeader.setCrc32(getFromFileHeader(localFileHeader::getCrc32, entry::checksum));
        localFileHeader.setCompressedSize(getFromFileHeader(localFileHeader::getCompressedSize, entry::getCompressedSizeNew));
        localFileHeader.setUncompressedSize(getFromFileHeader(localFileHeader::getUncompressedSize, entry::size));
        return localFileHeader;
    }

    private static long getFromFileHeader(LongSupplier actual, LongSupplier real) {
        return actual.getAsLong() == LOOK_IN_DATA_DESCRIPTOR ? real.getAsLong() : actual.getAsLong();
    }

    private void findHead(DataInput in) throws IOException {
        in.seek(entry.getLocalFileHeaderOffs());

        if (in.readSignature() != LocalFileHeader.SIGNATURE)
            throw new Zip4jException("invalid local file header signature");
    }

}
