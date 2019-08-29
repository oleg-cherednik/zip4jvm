package com.cop.zip4j.model.builders;

import com.cop.zip4j.model.CentralDirectory;
import com.cop.zip4j.model.Encryption;
import com.cop.zip4j.model.ExternalFileAttributes;
import com.cop.zip4j.model.GeneralPurposeFlag;
import com.cop.zip4j.model.InternalFileAttributes;
import com.cop.zip4j.model.Zip64;
import com.cop.zip4j.model.ZipModel;
import com.cop.zip4j.model.aes.AesExtraDataRecord;
import com.cop.zip4j.model.entry.PathZipEntry;
import com.cop.zip4j.utils.ZipUtils;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * @author Oleg Cherednik
 * @since 12.03.2019
 */
@RequiredArgsConstructor
public class CentralDirectoryBuilder {

    @NonNull
    private final PathZipEntry entry;
    @NonNull
    private final ZipModel zipModel;
    private final int currSplitFileCounter;

    @NonNull
    public CentralDirectory.FileHeader create() throws IOException {
        CentralDirectory.FileHeader fileHeader = new CentralDirectory.FileHeader(getFileName());

        fileHeader.setVersionMadeBy(CentralDirectory.FileHeader.VERSION);
        fileHeader.setVersionToExtract(CentralDirectory.FileHeader.VERSION);
        updateGeneralPurposeFlag(fileHeader);
        fileHeader.setCompressionMethod(entry.getEncryption().getCompressionMethod(entry));
        fileHeader.setLastModifiedTime(entry.getLastModifiedTime());
        fileHeader.setCrc32(entry.checksum());
        fileHeader.setCompressedSize(entry.getCompressedSizeNew());
        fileHeader.setUncompressedSize(entry.size());
        fileHeader.setFileCommentLength(0);
        fileHeader.setDiskNumber(currSplitFileCounter);
        fileHeader.setInternalFileAttributes(InternalFileAttributes.of(entry.getPath()));
        fileHeader.setExternalFileAttributes(ExternalFileAttributes.of(entry.getPath()));
        fileHeader.setOffsLocalFileHeader(entry.getLocalFileHeaderOffs());
        fileHeader.setZip64ExtendedInfo(Zip64.ExtendedInfo.NULL);
        fileHeader.setAesExtraDataRecord(getAesExtraDataRecord(entry.getEncryption()));
        fileHeader.setFileComment(null);

        return fileHeader;
    }

    @NonNull
    private String getFileName() throws IOException {
        String fileName = entry.getName();

        if (StringUtils.isBlank(fileName))
            throw new IOException("fileName is null or empty. unable to create file header");

        if (entry.isDirectory() && !ZipUtils.isDirectory(fileName))
            fileName += "/";

        return ZipUtils.normalizeFileName.apply(fileName);
    }

    private void updateGeneralPurposeFlag(CentralDirectory.FileHeader fileHeader) {
        GeneralPurposeFlag generalPurposeFlag = fileHeader.getGeneralPurposeFlag();

        generalPurposeFlag.setCompressionLevel(entry.getCompressionLevel());
        generalPurposeFlag.setDataDescriptorExists(!ZipUtils.isDirectory(fileHeader.getFileName()));
        generalPurposeFlag.setUtf8(zipModel.getCharset() == StandardCharsets.UTF_8);
        generalPurposeFlag.setEncrypted(entry.getEncryption() != Encryption.OFF);
//        generalPurposeFlag.setStrongEncryption(entry.getEncryption() == Encryption.STRONG);
        generalPurposeFlag.setStrongEncryption(false);
    }

    @NonNull
    private AesExtraDataRecord getAesExtraDataRecord(@NonNull Encryption encryption) {
        if (encryption != Encryption.AES)
            return AesExtraDataRecord.NULL;

        return AesExtraDataRecord.builder()
                                 .size(7)
                                 .vendor("AE")
                                 // Always set the version number to 2 as we do not store CRC for any AES encrypted files
                                 // only MAC is stored and as per the specification, if version number is 2, then MAC is read
                                 // and CRC is ignored
                                 .versionNumber((short)2)
                                 .strength(entry.getStrength())
                                 .compressionMethod(entry.getCompression().getMethod())
                                 .build();
    }

}
