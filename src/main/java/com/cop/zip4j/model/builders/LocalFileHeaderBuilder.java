package com.cop.zip4j.model.builders;

import com.cop.zip4j.model.CentralDirectory;
import com.cop.zip4j.model.Encryption;
import com.cop.zip4j.model.ExtraField;
import com.cop.zip4j.model.LocalFileHeader;
import com.cop.zip4j.model.Zip64;
import com.cop.zip4j.model.ZipModel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * @author Oleg Cherednik
 * @since 04.08.2019
 */
@RequiredArgsConstructor
public final class LocalFileHeaderBuilder {

    public static final long LOOK_IN_DATA_DESCRIPTOR = 0;
    public static final long LOOK_IN_EXTRA_FIELD = ZipModel.ZIP_64_LIMIT;

    @NonNull
    private final ZipModel zipModel;
    @NonNull
    private final CentralDirectory.FileHeader fileHeader;

    public LocalFileHeader create() {
        Encryption encryption = fileHeader.getEncryption();
        LocalFileHeader localFileHeader = new LocalFileHeader();

        localFileHeader.setVersionToExtract(fileHeader.getVersionToExtract());
        localFileHeader.setGeneralPurposeFlag(fileHeader.getGeneralPurposeFlag().getAsInt());
        localFileHeader.setCompressionMethod(encryption.getCompressionMethod(fileHeader));
        localFileHeader.setLastModifiedTime(fileHeader.getLastModifiedTime());
        localFileHeader.setCrc32(encryption.getChecksum(fileHeader));
        localFileHeader.setCompressedSize(getCompressedSize());
        localFileHeader.setUncompressedSize(getUncompressedSize());
        localFileHeader.setFileName(fileHeader.getFileName());
        localFileHeader.setExtraField(getExtraField());

        return localFileHeader;
    }

    private long getCompressedSize() {
        if (fileHeader.getGeneralPurposeFlag().isDataDescriptorExists())
            return LOOK_IN_DATA_DESCRIPTOR;
        if (zipModel.isZip64())
            return LOOK_IN_EXTRA_FIELD;
        return fileHeader.getCompressedSize();
    }

    private long getUncompressedSize() {
        if (fileHeader.getGeneralPurposeFlag().isDataDescriptorExists())
            return LOOK_IN_DATA_DESCRIPTOR;
        if (zipModel.isZip64())
            return LOOK_IN_EXTRA_FIELD;
        return fileHeader.getUncompressedSize();
    }

    private ExtraField getExtraField() {
        ExtraField extraField = fileHeader.getExtraField().deepCopy();

        if (fileHeader.getGeneralPurposeFlag().isDataDescriptorExists())
            extraField.setExtendedInfo(Zip64.ExtendedInfo.NULL);

        return extraField;
    }

}
