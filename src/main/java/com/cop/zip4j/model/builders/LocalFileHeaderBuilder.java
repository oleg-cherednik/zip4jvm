package com.cop.zip4j.model.builders;

import com.cop.zip4j.model.CentralDirectory;
import com.cop.zip4j.model.Encryption;
import com.cop.zip4j.model.LocalFileHeader;
import lombok.RequiredArgsConstructor;

/**
 * @author Oleg Cherednik
 * @since 04.08.2019
 */
@RequiredArgsConstructor
public final class LocalFileHeaderBuilder {

    private final CentralDirectory.FileHeader fileHeader;

    public LocalFileHeader create() {
        Encryption encryption = fileHeader.getEncryption();
        LocalFileHeader localFileHeader = new LocalFileHeader();

        localFileHeader.setVersionToExtract(fileHeader.getVersionToExtract());
        localFileHeader.setGeneralPurposeFlag(fileHeader.getGeneralPurposeFlag().getAsInt());
        localFileHeader.setCompressionMethod(encryption.getCompressionMethod(fileHeader));
        localFileHeader.setLastModifiedTime(fileHeader.getLastModifiedTime());
        localFileHeader.setUncompressedSize(fileHeader.getUncompressedSize());
        localFileHeader.setFileName(fileHeader.getFileName());
        localFileHeader.setExtraField(fileHeader.getExtraField().deepCopy());
        localFileHeader.setCompressedSize(fileHeader.getCompressedSize());
        localFileHeader.setCrc32(encryption.getChecksum(fileHeader));

        return localFileHeader;
    }

}
