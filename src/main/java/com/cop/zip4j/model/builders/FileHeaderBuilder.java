package com.cop.zip4j.model.builders;

import com.cop.zip4j.model.CentralDirectory;
import com.cop.zip4j.model.Encryption;
import com.cop.zip4j.model.ExternalFileAttributes;
import com.cop.zip4j.model.InternalFileAttributes;
import com.cop.zip4j.model.Zip64;
import com.cop.zip4j.model.aes.AesExtraDataRecord;
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
        fileHeader.setCrc32(entry.checksum());
        fileHeader.setCompressedSize(entry.getCompressedSizeNew());
        fileHeader.setUncompressedSize(entry.size());
        fileHeader.setFileCommentLength(0);
        fileHeader.setDiskNumber(entry.getDisc());
        fileHeader.setInternalFileAttributes(InternalFileAttributes.of(entry.getPath()));
        fileHeader.setExternalFileAttributes(ExternalFileAttributes.of(entry.getPath()));
        fileHeader.setOffsLocalFileHeader(entry.getLocalFileHeaderOffs());
        fileHeader.setZip64ExtendedInfo(Zip64.ExtendedInfo.NULL);
        fileHeader.setAesExtraDataRecord(getAesExtraDataRecord());
        fileHeader.setFileComment(null);

        return fileHeader;
    }

    @NonNull
    private AesExtraDataRecord getAesExtraDataRecord() {
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
}
