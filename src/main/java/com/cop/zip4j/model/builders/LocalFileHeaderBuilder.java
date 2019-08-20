package com.cop.zip4j.model.builders;

import com.cop.zip4j.model.CentralDirectory;
import com.cop.zip4j.model.Encryption;
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

        if(fileHeader.getGeneralPurposeFlag().isDataDescriptorExists()) {
            localFileHeader.setCompressedSize(0);
            localFileHeader.setUncompressedSize(0);
        } else {
            localFileHeader.setCompressedSize(zipModel.isZip64() ? ZipModel.ZIP_64_LIMIT : fileHeader.getCompressedSize());
            localFileHeader.setUncompressedSize(zipModel.isZip64() ? ZipModel.ZIP_64_LIMIT : fileHeader.getUncompressedSize());
        }

        localFileHeader.setFileName(fileHeader.getFileName());
        localFileHeader.setExtraField(fileHeader.getExtraField().deepCopy());
        localFileHeader.setCrc32(encryption.getChecksum(fileHeader));

        if(fileHeader.getGeneralPurposeFlag().isDataDescriptorExists())
            localFileHeader.getExtraField().setExtendedInfo(Zip64.ExtendedInfo.NULL);

        return localFileHeader;
    }

}
