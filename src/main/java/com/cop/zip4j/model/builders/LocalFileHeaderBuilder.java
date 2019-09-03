package com.cop.zip4j.model.builders;

import com.cop.zip4j.model.CentralDirectory;
import com.cop.zip4j.model.Encryption;
import com.cop.zip4j.model.ExtraField;
import com.cop.zip4j.model.GeneralPurposeFlag;
import com.cop.zip4j.model.LocalFileHeader;
import com.cop.zip4j.model.Zip64;
import com.cop.zip4j.model.entry.PathZipEntry;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.nio.charset.StandardCharsets;

/**
 * @author Oleg Cherednik
 * @since 27.08.2019
 */
@RequiredArgsConstructor
public final class LocalFileHeaderBuilder {

    public static final long LOOK_IN_DATA_DESCRIPTOR = 0;
    public static final long LOOK_IN_EXTRA_FIELD = Zip64.LIMIT;

    @NonNull
    private final PathZipEntry entry;

    @NonNull
    public LocalFileHeader create() {
        LocalFileHeader localFileHeader = new LocalFileHeader();
        localFileHeader.setVersionToExtract(CentralDirectory.FileHeader.VERSION);
        localFileHeader.setGeneralPurposeFlag(createGeneralPurposeFlag());
        localFileHeader.setCompressionMethod(entry.getEncryption().getCompressionMethod().apply(entry.getCompression()));
        localFileHeader.setLastModifiedTime(entry.getLastModifiedTime());
        localFileHeader.setCrc32(getCrc32());
        localFileHeader.setCompressedSize(getSize(entry.getCompressedSize()));
        localFileHeader.setUncompressedSize(getSize(entry.getUncompressedSize()));
        localFileHeader.setFileName(entry.getFileName());
        localFileHeader.setExtraField(createExtraField());
        return localFileHeader;
    }

    private GeneralPurposeFlag createGeneralPurposeFlag() {
        GeneralPurposeFlag generalPurposeFlag = new GeneralPurposeFlag();
        generalPurposeFlag.setCompressionLevel(entry.getCompressionLevel());
        generalPurposeFlag.setDataDescriptorAvailable(entry.isDataDescriptorAvailable());
        generalPurposeFlag.setUtf8(entry.getCharset() == StandardCharsets.UTF_8);
        generalPurposeFlag.setEncrypted(entry.getEncryption() != Encryption.OFF);
//        generalPurposeFlag.setStrongEncryption(entry.getEncryption() == Encryption.STRONG);
        generalPurposeFlag.setStrongEncryption(false);

        return generalPurposeFlag;
    }

    private ExtraField createExtraField() {
        ExtraField extraField = new ExtraField();
        extraField.setExtendedInfo(createExtendedInfo());
        extraField.setAesExtraDataRecord(new AesExtraDataRecordBuilder(entry).create());
        return extraField;
    }

    private Zip64.ExtendedInfo createExtendedInfo() {
        if (entry.isDataDescriptorAvailable())
            return Zip64.ExtendedInfo.NULL;
        if (entry.isZip64())
            return Zip64.ExtendedInfo.builder()
                                     .compressedSize(entry.getCompressedSize())
                                     .uncompressedSize(entry.getUncompressedSize())
//                                     .offsLocalHeaderRelative(entry.getLocalFileHeaderOffs())
                                     .build();
        return Zip64.ExtendedInfo.NULL;
    }

    private long getCrc32() {
        if (entry.isDataDescriptorAvailable())
            return LOOK_IN_DATA_DESCRIPTOR;
        return entry.getEncryption().getChecksum().apply(entry);
    }

    private long getSize(long size) {
        if (entry.isDataDescriptorAvailable())
            return LOOK_IN_DATA_DESCRIPTOR;
        if (entry.isZip64())
            return LOOK_IN_EXTRA_FIELD;
        return size;
    }

}
