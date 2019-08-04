package com.cop.zip4j.core.builders;

import com.cop.zip4j.model.CentralDirectory;
import com.cop.zip4j.model.CompressionMethod;
import com.cop.zip4j.model.Encryption;
import com.cop.zip4j.model.LocalFileHeader;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * @author Oleg Cherednik
 * @since 04.08.2019
 */
@RequiredArgsConstructor
public final class LocalFileHeaderBuilder {

    private final CentralDirectory.FileHeader fileHeader;

    public LocalFileHeader create() {
        LocalFileHeader localFileHeader = new LocalFileHeader();

        localFileHeader.setVersionToExtract(fileHeader.getVersionToExtract());
        localFileHeader.setGeneralPurposeFlag(fileHeader.getGeneralPurposeFlag().getData());
        localFileHeader.setCompressionMethod(getCompressionMethod(fileHeader));
        localFileHeader.setLastModifiedTime(fileHeader.getLastModifiedTime());
        localFileHeader.setUncompressedSize(fileHeader.getUncompressedSize());
        localFileHeader.setFileName(fileHeader.getFileName());
        localFileHeader.setExtraField(fileHeader.getExtraField().deepCopy());
        localFileHeader.setCompressedSize(fileHeader.getCompressedSize());
        localFileHeader.setCrc32(getChecksum(fileHeader));

        return localFileHeader;
    }

    @NonNull
    private static CompressionMethod getCompressionMethod(CentralDirectory.FileHeader fileHeader) {
        if (fileHeader.getEncryption() == Encryption.AES || fileHeader.getEncryption() == Encryption.AES_NEW)
            return CompressionMethod.AES_ENC;
        return fileHeader.getCompressionMethod();
    }

    private static long getChecksum(CentralDirectory.FileHeader fileHeader) {
        if (fileHeader.getEncryption() == Encryption.AES || fileHeader.getEncryption() == Encryption.AES_NEW)
            return 0;
        return fileHeader.getCrc32();
    }

}
