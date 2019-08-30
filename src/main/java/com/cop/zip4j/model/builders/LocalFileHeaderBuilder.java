package com.cop.zip4j.model.builders;

import com.cop.zip4j.model.CentralDirectory;
import com.cop.zip4j.model.Encryption;
import com.cop.zip4j.model.ExtraField;
import com.cop.zip4j.model.LocalFileHeader;
import com.cop.zip4j.model.Zip64;
import com.cop.zip4j.model.aes.AesExtraDataRecord;
import com.cop.zip4j.model.entry.PathZipEntry;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

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

    public LocalFileHeader create() {
        LocalFileHeader localFileHeader = new LocalFileHeader();

        localFileHeader.setVersionToExtract(CentralDirectory.FileHeader.VERSION);
        localFileHeader.setGeneralPurposeFlag(entry.createGeneralPurposeFlag());
        localFileHeader.setCompressionMethod(entry.getEncryption().getCompressionMethod(entry));
        localFileHeader.setLastModifiedTime(entry.getLastModifiedTime());
        localFileHeader.setCrc32(getValue(entry.checksum()));
        localFileHeader.setCompressedSize(getValue(entry.getCompressedSize()));
        localFileHeader.setUncompressedSize(getValue(entry.size()));
        localFileHeader.setFileName(entry.getName());
        localFileHeader.setExtraField(createExtraField());

        return localFileHeader;
    }

    private ExtraField createExtraField() {
        ExtraField extraField = new ExtraField();
        extraField.setExtendedInfo(createExtendedInfo());
        extraField.setAesExtraDataRecord(createAesExtraDataRecord());
        return extraField;
    }

    private Zip64.ExtendedInfo createExtendedInfo() {
        if (entry.isDataDescriptorAvailable())
            return Zip64.ExtendedInfo.NULL;
        if (entry.isZip64())
            return Zip64.ExtendedInfo.builder()
                                     .compressedSize(entry.getCompressedSizeNew())
                                     .uncompressedSize(entry.size())
//                                     .offsLocalHeaderRelative(entry.getLocalFileHeaderOffs())
                                     .build();
        return Zip64.ExtendedInfo.NULL;
    }

    private AesExtraDataRecord createAesExtraDataRecord() {
        if (entry.getEncryption() != Encryption.AES)
            return AesExtraDataRecord.NULL;

        return AesExtraDataRecord.builder()
                                 .size(7)
                                 .vendor("AE")
                                 .versionNumber((short)2)
                                 .strength(entry.getStrength())
                                 .compressionMethod(entry.getCompression().getMethod())
                                 .build();
    }

    private long getValue(long value) {
        if (entry.isDataDescriptorAvailable())
            return LOOK_IN_DATA_DESCRIPTOR;
        if (entry.isZip64())
            return LOOK_IN_EXTRA_FIELD;
        return value;
    }

}
