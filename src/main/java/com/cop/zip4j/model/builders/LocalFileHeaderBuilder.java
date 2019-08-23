package com.cop.zip4j.model.builders;

import com.cop.zip4j.model.CentralDirectory;
import com.cop.zip4j.model.ExtraField;
import com.cop.zip4j.model.LocalFileHeader;
import com.cop.zip4j.model.Zip64;
import com.cop.zip4j.model.ZipModel;
import com.cop.zip4j.model.aes.AesExtraDataRecord;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.function.LongSupplier;
import java.util.function.Supplier;

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
        LocalFileHeader localFileHeader = new LocalFileHeader();

        localFileHeader.setVersionToExtract(fileHeader.getVersionToExtract());
        localFileHeader.setGeneralPurposeFlag(fileHeader.getGeneralPurposeFlag().getAsInt());
        localFileHeader.setCompressionMethod(fileHeader.getEncryption().getCompressionMethod(fileHeader));
        localFileHeader.setLastModifiedTime(fileHeader.getLastModifiedTime());
        localFileHeader.setCrc32(getCrc32().getAsLong());
        localFileHeader.setCompressedSize(getCompressedSize().getAsLong());
        localFileHeader.setUncompressedSize(getUncompressedSize().getAsLong());
        localFileHeader.setFileName(fileHeader.getFileName());
        updateExtraField(localFileHeader.getExtraField());

        return localFileHeader;
    }

    private LongSupplier getCrc32() {
        if (fileHeader.getGeneralPurposeFlag().isDataDescriptorExists())
            return () -> LOOK_IN_DATA_DESCRIPTOR;
        return zipModel.getActivity().getCrc32LocalFileHeader(fileHeader::getCrc32);
    }

    private LongSupplier getCompressedSize() {
        if (fileHeader.getGeneralPurposeFlag().isDataDescriptorExists())
            return () -> LOOK_IN_DATA_DESCRIPTOR;
        return zipModel.getActivity().getCompressedSizeLocalFileHeader(fileHeader::getOriginalCompressedSize);
    }

    private LongSupplier getUncompressedSize() {
        if (fileHeader.getGeneralPurposeFlag().isDataDescriptorExists())
            return () -> LOOK_IN_DATA_DESCRIPTOR;
        return zipModel.getActivity().getUncompressedSizeLocalFileHeader(fileHeader::getOriginalUncompressedSize);
    }

    private void updateExtraField(ExtraField extraField) {
        extraField.setExtendedInfo(getExtendedInfo().get());
        extraField.setAesExtraDataRecord(getAesExtraDataRecord());
    }

    private Supplier<Zip64.ExtendedInfo> getExtendedInfo() {
        if (fileHeader.getGeneralPurposeFlag().isDataDescriptorExists())
            return () -> Zip64.ExtendedInfo.NULL;
        return zipModel.getActivity().getExtendedInfoLocalFileHeader(fileHeader);
    }

    private AesExtraDataRecord getAesExtraDataRecord() {
        return fileHeader.getExtraField().getAesExtraDataRecord();
    }

}
