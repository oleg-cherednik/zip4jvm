package com.cop.zip4j.model.builders;

import com.cop.zip4j.exception.Zip4jException;
import com.cop.zip4j.model.CentralDirectory;
import com.cop.zip4j.model.Encryption;
import com.cop.zip4j.model.GeneralPurposeFlag;
import com.cop.zip4j.model.LocalFileHeader;
import com.cop.zip4j.model.Zip64;
import com.cop.zip4j.model.ZipModel;
import com.cop.zip4j.model.entry.PathZipEntry;
import com.cop.zip4j.utils.ZipUtils;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.StringUtils;

import java.nio.charset.StandardCharsets;

/**
 * @author Oleg Cherednik
 * @since 27.08.2019
 */
@RequiredArgsConstructor
public final class LocalFileHeaderEntryBuilder {

    public static final long LOOK_IN_DATA_DESCRIPTOR = 0;
    public static final long LOOK_IN_EXTRA_FIELD = Zip64.LIMIT;

    @NonNull
    private final PathZipEntry entry;
    @NonNull
    private final ZipModel zipModel;

    public LocalFileHeader create() {
        LocalFileHeader localFileHeader = new LocalFileHeader();

        localFileHeader.setVersionToExtract(CentralDirectory.FileHeader.VERSION);
        updateGeneralPurposeFlag(localFileHeader);
        localFileHeader.setCompressionMethod(entry.getEncryption().getCompressionMethod(entry));
        localFileHeader.setLastModifiedTime(entry.getLastModifiedTime());
        localFileHeader.setCrc32(entry.checksum());
        localFileHeader.setCompressedSize(entry.getCompressedSize());
        localFileHeader.setUncompressedSize(entry.size());
        localFileHeader.setFileName(getFileName());

        return localFileHeader;
    }

    private void updateGeneralPurposeFlag(LocalFileHeader localFileHeader) {
        GeneralPurposeFlag generalPurposeFlag = localFileHeader.getGeneralPurposeFlag();

        generalPurposeFlag.setCompressionLevel(entry.getCompressionLevel());
        generalPurposeFlag.setDataDescriptorExists(!ZipUtils.isDirectory(localFileHeader.getFileName()));
        generalPurposeFlag.setUtf8(zipModel.getCharset() == StandardCharsets.UTF_8);
        generalPurposeFlag.setEncrypted(entry.getEncryption() != Encryption.OFF);
//        generalPurposeFlag.setStrongEncryption(entry.getEncryption() == Encryption.STRONG);
        generalPurposeFlag.setStrongEncryption(false);
    }

    // TODO move to ZipEntry
    private String getFileName() {
        String fileName = entry.getName();

        if (StringUtils.isBlank(fileName))
            throw new Zip4jException("fileName is null or empty. unable to create file header");

        if (entry.isDirectory())
            fileName += "/";

        return ZipUtils.normalizeFileName.apply(fileName);
    }

}
