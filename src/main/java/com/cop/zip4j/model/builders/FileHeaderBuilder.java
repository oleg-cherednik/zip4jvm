package com.cop.zip4j.model.builders;

import com.cop.zip4j.model.CentralDirectory;
import com.cop.zip4j.model.ExternalFileAttributes;
import com.cop.zip4j.model.ExtraField;
import com.cop.zip4j.model.InternalFileAttributes;
import com.cop.zip4j.model.Zip64;
import com.cop.zip4j.model.entry.PathZipEntry;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 30.08.2019
 */
@RequiredArgsConstructor
final class FileHeaderBuilder {

    @NonNull
    private final PathZipEntry entry;

    @NonNull
    public CentralDirectory.FileHeader create() throws IOException {
        CentralDirectory.FileHeader fileHeader = new CentralDirectory.FileHeader(entry.getName());

        fileHeader.setVersionMadeBy(CentralDirectory.FileHeader.VERSION);
        fileHeader.setVersionToExtract(CentralDirectory.FileHeader.VERSION);
        fileHeader.setGeneralPurposeFlag(entry.createGeneralPurposeFlag());
        fileHeader.setCompressionMethod(entry.getEncryption().getCompressionMethod(entry));
        fileHeader.setLastModifiedTime(entry.getLastModifiedTime());
        fileHeader.setCrc32(entry.getEncryption().getChecksumFileHeader().apply(entry.checksum()));
        fileHeader.setCompressedSize(entry.getCompressedSizeNew());
        fileHeader.setUncompressedSize(entry.size());
        fileHeader.setFileCommentLength(0);
        fileHeader.setDiskNumber(entry.getDisc());
        fileHeader.setInternalFileAttributes(InternalFileAttributes.of(entry.getPath()));
        fileHeader.setExternalFileAttributes(ExternalFileAttributes.of(entry.getPath()));
        fileHeader.setOffsLocalFileHeader(entry.getLocalFileHeaderOffs());
        fileHeader.setExtraField(createExtraField());
        fileHeader.setFileComment(null);

        return fileHeader;
    }

    private ExtraField createExtraField() {
        ExtraField extraField = new ExtraField();
        extraField.setExtendedInfo(createExtendedInfo());
        extraField.setAesExtraDataRecord(new AesExtraDataRecordBuilder(entry).create());
        return extraField;
    }

    private Zip64.ExtendedInfo createExtendedInfo() {
//        if (entry.isDataDescriptorAvailable())
//            return Zip64.ExtendedInfo.NULL;
        if (entry.isZip64())
            return Zip64.ExtendedInfo.builder()
                                     .compressedSize(entry.getCompressedSizeNew())
                                     .uncompressedSize(entry.size())
//                                     .offsLocalHeaderRelative(entry.getLocalFileHeaderOffs())
                                     .build();
        return Zip64.ExtendedInfo.NULL;
    }

}
