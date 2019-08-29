package com.cop.zip4j.model.builders;

import com.cop.zip4j.model.CentralDirectory;
import com.cop.zip4j.model.Encryption;
import com.cop.zip4j.model.GeneralPurposeFlag;
import com.cop.zip4j.model.LocalFileHeader;
import com.cop.zip4j.model.Zip64;
import com.cop.zip4j.model.aes.AesExtraDataRecord;
import com.cop.zip4j.model.entry.PathZipEntry;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

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
        LocalFileHeader localFileHeader = new LocalFileHeader();

        localFileHeader.setVersionToExtract(CentralDirectory.FileHeader.VERSION);
        localFileHeader.setGeneralPurposeFlag(getGeneralPurposeFlag());
        localFileHeader.setCompressionMethod(entry.getEncryption().getCompressionMethod(entry));
        localFileHeader.setLastModifiedTime(entry.getLastModifiedTime());
        localFileHeader.setCrc32(getCrc32(localFileHeader).getAsLong());
        localFileHeader.setCompressedSize(getCompressedSize(localFileHeader).getAsLong());
        localFileHeader.setUncompressedSize(getUncompressedSize(localFileHeader).getAsLong());
        localFileHeader.setFileName(entry.getName());
        localFileHeader.getExtraField().setAesExtraDataRecord(getAesExtraDataRecord(entry.getEncryption()));

        return localFileHeader;
    }

    private GeneralPurposeFlag getGeneralPurposeFlag() {
        GeneralPurposeFlag generalPurposeFlag = new GeneralPurposeFlag();
        generalPurposeFlag.setCompressionLevel(entry.getCompressionLevel());
        generalPurposeFlag.setDataDescriptorExists(entry.isRegularFile());
        generalPurposeFlag.setUtf8(charset == StandardCharsets.UTF_8);
        generalPurposeFlag.setEncrypted(entry.getEncryption() != Encryption.OFF);
//        generalPurposeFlag.setStrongEncryption(entry.getEncryption() == Encryption.STRONG);
        generalPurposeFlag.setStrongEncryption(false);

        return generalPurposeFlag;
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
                                 .versionNumber((short)2)
                                 .strength(entry.getStrength())
                                 .compressionMethod(entry.getCompression().getMethod())
                                 .build();
    }

}
