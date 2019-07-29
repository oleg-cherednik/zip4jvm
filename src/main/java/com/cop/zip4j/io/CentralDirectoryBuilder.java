package com.cop.zip4j.io;

import com.cop.zip4j.crypto.aes.AesEngine;
import com.cop.zip4j.crypto.pkware.PkwareHeader;
import com.cop.zip4j.exception.Zip4jException;
import com.cop.zip4j.model.AesExtraDataRecord;
import com.cop.zip4j.model.CentralDirectory;
import com.cop.zip4j.model.CompressionMethod;
import com.cop.zip4j.model.Encryption;
import com.cop.zip4j.model.GeneralPurposeFlag;
import com.cop.zip4j.model.LocalFileHeader;
import com.cop.zip4j.model.Zip64;
import com.cop.zip4j.model.ZipModel;
import com.cop.zip4j.model.entry.PathZipEntry;
import com.cop.zip4j.utils.InternalZipConstants;
import com.cop.zip4j.utils.ZipUtils;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/**
 * @author Oleg Cherednik
 * @since 12.03.2019
 */
@RequiredArgsConstructor
public class CentralDirectoryBuilder {

    private final PathZipEntry entry;
    @NonNull
    private final ZipModel zipModel;
    private final int currSplitFileCounter;

    public CentralDirectory.FileHeader createFileHeader() throws IOException {
        CentralDirectory.FileHeader fileHeader = new CentralDirectory.FileHeader(getFileName());

        fileHeader.setVersionMadeBy(CentralDirectory.FileHeader.DEF_VERSION);
        fileHeader.setVersionToExtract(CentralDirectory.FileHeader.DEF_VERSION);
        updateGeneralPurposeFlag(fileHeader.getGeneralPurposeFlag());
        fileHeader.setCompressionMethod(entry.getCompressionMethod());
        fileHeader.setLastModifiedTime(getLastModFileTime());
        fileHeader.setCrc32(entry.crc32());
        fileHeader.setCompressedSize(getCompressedSize());
        fileHeader.setUncompressedSize(getUncompressedSize());
        fileHeader.setFileCommentLength(0);
        fileHeader.setDiskNumber(currSplitFileCounter);
        fileHeader.setInternalFileAttributes(null);
        fileHeader.setExternalFileAttributes(getExternalFileAttr());
        fileHeader.setOffsLocalFileHeader(0);
        fileHeader.setZip64ExtendedInfo(Zip64.ExtendedInfo.NULL);
        fileHeader.setAesExtraDataRecord(getAesExtraDataRecord(entry.getEncryption()));
        fileHeader.setFileComment(null);

        return fileHeader;
    }

    public LocalFileHeader createLocalFileHeader(@NonNull CentralDirectory.FileHeader fileHeader) throws Zip4jException {
        LocalFileHeader localFileHeader = new LocalFileHeader();

        localFileHeader.setVersionToExtract(fileHeader.getVersionToExtract());
        localFileHeader.setGeneralPurposeFlag(fileHeader.getGeneralPurposeFlag().getData());
        localFileHeader.setCompressionMethod(fileHeader.getCompressionMethod());
        localFileHeader.setLastModifiedTime(fileHeader.getLastModifiedTime());
        localFileHeader.setUncompressedSize(fileHeader.getUncompressedSize());
        localFileHeader.setFileName(fileHeader.getFileName());
        localFileHeader.setExtraField(fileHeader.getExtraField().deepCopy());
        localFileHeader.setCrc32(fileHeader.getCrc32());
        localFileHeader.setCompressedSize(fileHeader.getCompressedSize());

        return localFileHeader;
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

    private void updateGeneralPurposeFlag(@NonNull GeneralPurposeFlag generalPurposeFlag) {
        generalPurposeFlag.setCompressionLevel(entry.getCompressionLevel());
//        generalPurposeFlag.setDataDescriptorExists(entry.getCompressionMethod() == CompressionMethod.DEFLATE);
        generalPurposeFlag.setDataDescriptorExists(true);
        generalPurposeFlag.setUtf8Encoding(zipModel.getCharset() == StandardCharsets.UTF_8);
        generalPurposeFlag.setEncrypted(entry.getEncryption() != Encryption.OFF);
        generalPurposeFlag.setStrongEncryption(entry.getEncryption() == Encryption.STRONG);
    }

    private int getLastModFileTime() throws IOException {
        long time = Files.getLastModifiedTime(entry.getPath()).toMillis();
        return (int)ZipUtils.javaToDosTime(time);
    }

    private long getCompressedSize() {
        if (entry.isDirectory())
            return 0;
        if (entry.getCompressionMethod() != CompressionMethod.STORE)
            return 0;
        if (entry.getEncryption() != Encryption.AES)
            return 0;

        long fileSize = entry.size();

        if (entry.getEncryption() == Encryption.PKWARE)
            return fileSize + PkwareHeader.SIZE;

        return fileSize + entry.getAesStrength().getSaltLength() + AesEngine.AES_AUTH_LENGTH + 2; //2 is password verifier
    }

    private long getUncompressedSize() {
        return entry.size();
    }

    private byte[] getExternalFileAttr() throws IOException {
        int attr = InternalZipConstants.FILE_MODE_READ_ONLY;

        if (entry.isDirectory())
            attr = Files.isHidden(entry.getPath()) ? InternalZipConstants.FOLDER_MODE_HIDDEN : InternalZipConstants.FOLDER_MODE_NONE;
        if (!Files.isWritable(entry.getPath()) && Files.isHidden(entry.getPath()))
            attr = InternalZipConstants.FILE_MODE_READ_ONLY_HIDDEN;
        if (Files.isWritable(entry.getPath()))
            attr = Files.isHidden(entry.getPath()) ? InternalZipConstants.FILE_MODE_HIDDEN : InternalZipConstants.FILE_MODE_NONE;

        return new byte[] { (byte)attr, 0, 0, 0 };
    }

    @NonNull
    private AesExtraDataRecord getAesExtraDataRecord(@NonNull Encryption encryption) {
        if (encryption != Encryption.AES)
            return AesExtraDataRecord.NULL;

        AesExtraDataRecord aesDataRecord = new AesExtraDataRecord();
        aesDataRecord.setDataSize(7);
        aesDataRecord.setVendor("AE");
        // Always set the version number to 2 as we do not store CRC for any AES encrypted files
        // only MAC is stored and as per the specification, if version number is 2, then MAC is read
        // and CRC is ignored
        aesDataRecord.setVersionNumber((short)2);
        aesDataRecord.setAesStrength(entry.getAesStrength());
        aesDataRecord.setCompressionMethod(entry.getCompressionMethod());

        return aesDataRecord;
    }

}
