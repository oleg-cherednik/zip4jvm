package net.lingala.zip4j.io;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.AESExtraDataRecord;
import net.lingala.zip4j.model.CentralDirectory;
import net.lingala.zip4j.model.CompressionMethod;
import net.lingala.zip4j.model.Encryption;
import net.lingala.zip4j.model.GeneralPurposeFlag;
import net.lingala.zip4j.model.LocalFileHeader;
import net.lingala.zip4j.model.ZipModel;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.InternalZipConstants;
import net.lingala.zip4j.util.Zip4jUtil;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;

/**
 * @author Oleg Cherednik
 * @since 12.03.2019
 */
@RequiredArgsConstructor
public class CentralDirectoryBuilder {

    @NonNull
    private final Path sourceFile;
    @NonNull
    private final ZipParameters zipParameters;
    @NonNull
    private final ZipModel zipModel;
    private final int currSplitFileCounter;

    public CentralDirectory.FileHeader createFileHeader() throws IOException {
        String fileName = getFileName();

        CentralDirectory.FileHeader fileHeader = new CentralDirectory.FileHeader();
        fileHeader.setVersionMadeBy(20);
        fileHeader.setVersionNeededToExtract(20);
        updateGeneralPurposeFlag(fileHeader.getGeneralPurposeFlag());
        fileHeader.setCompressionMethod(getCompressionMethod());
        fileHeader.setLastModFileTime(getLastModFileTime());
        fileHeader.setCrc32(getCrc32());
        fileHeader.setCompressedSize(getCompressedSize(fileHeader));
        fileHeader.setUncompressedSize(getUncompressedSize(fileHeader));
        fileHeader.setFileNameLength(Zip4jUtil.getEncodedStringLength(fileName, zipModel.getCharset()));
        fileHeader.setExtraFieldLength(0);
        fileHeader.setFileCommentLength(0);
        fileHeader.setDiskNumberStart(currSplitFileCounter);
        fileHeader.setInternalFileAttr(null);
        fileHeader.setExternalFileAttr(getExternalFileAttr());
        fileHeader.setOffLocalHeaderRelative(0);
        fileHeader.setFileName(fileName);
        fileHeader.setExtraDataRecords(Collections.emptyMap());
        fileHeader.setZip64ExtendedInfo(null);
        fileHeader.setAesExtraDataRecord(getAesExtraDataRecord(zipParameters.getEncryption()));
        fileHeader.setFileComment(null);

        return fileHeader;
    }

    public LocalFileHeader createLocalFileHeader(@NonNull CentralDirectory.FileHeader fileHeader) throws ZipException {
        LocalFileHeader localFileHeader = new LocalFileHeader();
        localFileHeader.setVersionNeededToExtract(fileHeader.getVersionNeededToExtract());
        localFileHeader.setGeneralPurposeFlag(fileHeader.getGeneralPurposeFlag().getData());
        localFileHeader.setCompressionMethod(fileHeader.getCompressionMethod());
        localFileHeader.setLastModFileTime(fileHeader.getLastModFileTime());
        localFileHeader.setUncompressedSize(fileHeader.getUncompressedSize());
        localFileHeader.setFileNameLength(fileHeader.getFileNameLength());
        localFileHeader.setFileName(fileHeader.getFileName());
        localFileHeader.setEncryption(fileHeader.getEncryption());
        localFileHeader.setAesExtraDataRecord(fileHeader.getAesExtraDataRecord());
        localFileHeader.setCrc32(fileHeader.getCrc32());
        localFileHeader.setCompressedSize(fileHeader.getCompressedSize());
        return localFileHeader;
    }

    private String getFileName() throws IOException {
        String fileName = zipParameters.getFileName(sourceFile);

        if (StringUtils.isBlank(fileName))
            throw new IOException("fileName is null or empty. unable to create file header");

        if (!zipParameters.isSourceExternalStream() && Files.isDirectory(sourceFile) && !Zip4jUtil.isDirectory(fileName))
            fileName += InternalZipConstants.FILE_SEPARATOR;

        return fileName;
    }

    private void updateGeneralPurposeFlag(@NonNull GeneralPurposeFlag generalPurposeFlag) {
        generalPurposeFlag.setCompressionLevel(zipParameters.getCompressionLevel());
        generalPurposeFlag.setDataDescriptorExists(true);
        generalPurposeFlag.setUtf8Enconding(zipModel.getCharset() == StandardCharsets.UTF_8);
    }

    private CompressionMethod getCompressionMethod() {
        return zipParameters.getEncryption() == Encryption.AES ? CompressionMethod.AES_ENC : zipParameters.getCompressionMethod();
    }

    private int getLastModFileTime() throws IOException {
        long time = zipParameters.isSourceExternalStream() ? System.currentTimeMillis() : Files.getLastModifiedTime(sourceFile).toMillis();
        return (int)Zip4jUtil.javaToDosTime(time);
    }

    private long getCrc32() {
        return zipParameters.getEncryption() == Encryption.STANDARD ? zipParameters.getSourceFileCRC() : 0;
    }

    private long getCompressedSize(CentralDirectory.FileHeader fileHeader) throws IOException {
        if (fileHeader.isDirectory())
            return 0;
        if (zipParameters.isSourceExternalStream())
            return 0;
        if (zipParameters.getCompressionMethod() != CompressionMethod.STORE)
            return 0;
        if (zipParameters.getEncryption() != Encryption.AES)
            return 0;

        long fileSize = Files.size(sourceFile);

        if (zipParameters.getEncryption() == Encryption.STANDARD)
            return fileSize + InternalZipConstants.STD_DEC_HDR_SIZE;

        return fileSize + zipParameters.getAesKeyStrength().getSaltLength() + InternalZipConstants.AES_AUTH_LENGTH + 2; //2 is password verifier
    }

    private long getUncompressedSize(CentralDirectory.FileHeader fileHeader) throws IOException {
        if (fileHeader.isDirectory())
            return 0;
        if (zipParameters.isSourceExternalStream())
            return 0;
        return Files.size(sourceFile);
    }

    private byte[] getExternalFileAttr() throws IOException {
        if (zipParameters.isSourceExternalStream())
            return null;
        if (!Files.exists(sourceFile))
            return null;

        int attr = InternalZipConstants.FILE_MODE_READ_ONLY;

        if (Files.isDirectory(sourceFile))
            attr = Files.isHidden(sourceFile) ? InternalZipConstants.FOLDER_MODE_HIDDEN : InternalZipConstants.FOLDER_MODE_NONE;
        if (!Files.isWritable(sourceFile) && Files.isHidden(sourceFile))
            attr = InternalZipConstants.FILE_MODE_READ_ONLY_HIDDEN;
        if (Files.isWritable(sourceFile))
            attr = Files.isHidden(sourceFile) ? InternalZipConstants.FILE_MODE_HIDDEN : InternalZipConstants.FILE_MODE_NONE;

        return new byte[] { (byte)attr, 0, 0, 0 };
    }

    private AESExtraDataRecord getAesExtraDataRecord(@NonNull Encryption encryption) {
        if (encryption != Encryption.AES)
            return null;

        AESExtraDataRecord aesDataRecord = new AESExtraDataRecord();
        aesDataRecord.setDataSize(7);
        aesDataRecord.setVendor("AE");
        // Always set the version number to 2 as we do not store CRC for any AES encrypted files
        // only MAC is stored and as per the specification, if version number is 2, then MAC is read
        // and CRC is ignored
        aesDataRecord.setVersionNumber(2);
        aesDataRecord.setAesStrength(zipParameters.getAesKeyStrength());
        aesDataRecord.setCompressionMethod(zipParameters.getCompressionMethod());

        return aesDataRecord;
    }

}
