package com.cop.zip4j.model.builders;

import com.cop.zip4j.exception.Zip4jException;
import com.cop.zip4j.model.CentralDirectory;
import com.cop.zip4j.model.Encryption;
import com.cop.zip4j.model.GeneralPurposeFlag;
import com.cop.zip4j.model.LocalFileHeader;
import com.cop.zip4j.model.Zip64;
import com.cop.zip4j.model.aes.AesExtraDataRecord;
import com.cop.zip4j.model.entry.PathZipEntry;
import com.cop.zip4j.utils.ZipUtils;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.StringUtils;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.function.LongSupplier;

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
    private final Charset charset;

    public LocalFileHeader create() {
        String fileName = getFileName();
        LocalFileHeader localFileHeader = new LocalFileHeader();

        localFileHeader.setVersionToExtract(CentralDirectory.FileHeader.VERSION);
        localFileHeader.setGeneralPurposeFlag(getGeneralPurposeFlag(fileName));
        localFileHeader.setCompressionMethod(entry.getEncryption().getCompressionMethod(entry));
        localFileHeader.setLastModifiedTime(entry.getLastModifiedTime());
        localFileHeader.setCrc32(getCrc32(localFileHeader).getAsLong());
        localFileHeader.setCompressedSize(getCompressedSize(localFileHeader).getAsLong());
        localFileHeader.setUncompressedSize(getUncompressedSize(localFileHeader).getAsLong());
        localFileHeader.setFileName(fileName);
        localFileHeader.getExtraField().setAesExtraDataRecord(getAesExtraDataRecord(entry.getEncryption()));

        return localFileHeader;
    }

    private GeneralPurposeFlag getGeneralPurposeFlag(String fileName) {
        GeneralPurposeFlag generalPurposeFlag = new GeneralPurposeFlag();
        generalPurposeFlag.setCompressionLevel(entry.getCompressionLevel());
        generalPurposeFlag.setDataDescriptorExists(ZipUtils.isRegularFile(fileName));
        generalPurposeFlag.setUtf8(charset == StandardCharsets.UTF_8);
        generalPurposeFlag.setEncrypted(entry.getEncryption() != Encryption.OFF);
//        generalPurposeFlag.setStrongEncryption(entry.getEncryption() == Encryption.STRONG);
        generalPurposeFlag.setStrongEncryption(false);

        return generalPurposeFlag;
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

    private LongSupplier getCrc32(LocalFileHeader localFileHeader) {
        if (localFileHeader.getGeneralPurposeFlag().isDataDescriptorExists())
            return () -> LOOK_IN_DATA_DESCRIPTOR;
        return entry.getActivity().getCrc32LocalFileHeader(entry::checksum);
    }

    private LongSupplier getCompressedSize(LocalFileHeader localFileHeader) {
        if (localFileHeader.getGeneralPurposeFlag().isDataDescriptorExists())
            return () -> LOOK_IN_DATA_DESCRIPTOR;
        return entry.getActivity().getCompressedSizeLocalFileHeader(entry::getCompressedSize);
    }

    private LongSupplier getUncompressedSize(LocalFileHeader localFileHeader) {
        if (localFileHeader.getGeneralPurposeFlag().isDataDescriptorExists())
            return () -> LOOK_IN_DATA_DESCRIPTOR;
        return entry.getActivity().getUncompressedSizeLocalFileHeader(entry::size);
    }

    private AesExtraDataRecord getAesExtraDataRecord(Encryption encryption) {
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
