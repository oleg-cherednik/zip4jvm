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
import net.lingala.zip4j.util.ZipUtils;
import org.apache.commons.io.FilenameUtils;
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

    private final Path sourceFile;
    private final String fileNameStream;
    @NonNull
    private final ZipParameters parameters;
    @NonNull
    private final ZipModel zipModel;
    private final int currSplitFileCounter;

    public CentralDirectory.FileHeader createFileHeader() throws IOException {
        CentralDirectory.FileHeader fileHeader = new CentralDirectory.FileHeader(getFileName(), parameters.getEncryption());

        fileHeader.setVersionMadeBy(20);
        fileHeader.setVersionToExtract(20);
        updateGeneralPurposeFlag(fileHeader.getGeneralPurposeFlag());
        fileHeader.setCompressionMethod(getCompressionMethod());
        fileHeader.setLastModifiedTime(getLastModFileTime());
        fileHeader.setCrc32(getCrc32());
        fileHeader.setCompressedSize(getCompressedSize(fileHeader));
        fileHeader.setUncompressedSize(getUncompressedSize(fileHeader));
        fileHeader.setFileNameLength(ZipUtils.getEncodedStringLength(fileHeader.getFileName(), zipModel.getCharset()));
        fileHeader.setExtraFieldLength(0);
        fileHeader.setFileCommentLength(0);
        fileHeader.setDiskNumber(currSplitFileCounter);
        fileHeader.setInternalFileAttributes(null);
        fileHeader.setExternalFileAttributes(getExternalFileAttr());
        fileHeader.setOffsLocalFileHeader(0);
        fileHeader.setExtraDataRecords(Collections.emptyMap());
        fileHeader.setZip64ExtendedInfo(null);
        fileHeader.setAesExtraDataRecord(getAesExtraDataRecord(fileHeader.getEncryption()));
        fileHeader.setFileComment(null);

        return fileHeader;
    }

    public LocalFileHeader createLocalFileHeader(@NonNull CentralDirectory.FileHeader fileHeader) throws ZipException {
        LocalFileHeader localFileHeader = new LocalFileHeader();
        localFileHeader.setVersionToExtract(fileHeader.getVersionToExtract());
        localFileHeader.setGeneralPurposeFlag(fileHeader.getGeneralPurposeFlag().getData());
        localFileHeader.setCompressionMethod(fileHeader.getCompressionMethod());
        localFileHeader.setLastModifiedTime(fileHeader.getLastModifiedTime());
        localFileHeader.setUncompressedSize(fileHeader.getUncompressedSize());
        localFileHeader.setFileNameLength(fileHeader.getFileNameLength());
        localFileHeader.setFileName(fileHeader.getFileName());
        localFileHeader.setEncryption(fileHeader.getEncryption());
        localFileHeader.setAesExtraDataRecord(fileHeader.getAesExtraDataRecord());
        localFileHeader.setCrc32(fileHeader.getCrc32());
        localFileHeader.setCompressedSize(fileHeader.getCompressedSize());
        return localFileHeader;
    }

    @NonNull
    private String getFileName() throws IOException {
        String fileName = fileNameStream;

        if (!parameters.isSourceExternalStream())
            fileName = parameters.getRelativeEntryName(sourceFile);

        if (StringUtils.isBlank(fileName))
            throw new IOException("fileName is null or empty. unable to create file header");

        if (!parameters.isSourceExternalStream() && Files.isDirectory(sourceFile) && !ZipUtils.isDirectory(fileName))
            fileName += "/";

        return FilenameUtils.normalize(fileName, true);
    }

    private void updateGeneralPurposeFlag(@NonNull GeneralPurposeFlag generalPurposeFlag) {
        generalPurposeFlag.setCompressionLevel(parameters.getCompressionLevel());
        generalPurposeFlag.setDataDescriptorExists(parameters.getCompressionMethod() == CompressionMethod.DEFLATE);
        generalPurposeFlag.setUtf8Encoding(zipModel.getCharset() == StandardCharsets.UTF_8);
        generalPurposeFlag.setEncrypted(parameters.getEncryption() != Encryption.OFF);
        generalPurposeFlag.setStrongEncryption(parameters.getEncryption() == Encryption.STRONG);
    }

    private CompressionMethod getCompressionMethod() {
        return parameters.getEncryption() == Encryption.AES ? CompressionMethod.AES_ENC : parameters.getCompressionMethod();
    }

    private int getLastModFileTime() throws IOException {
        long time = parameters.isSourceExternalStream() ? System.currentTimeMillis() : Files.getLastModifiedTime(sourceFile).toMillis();
        return (int)ZipUtils.javaToDosTime(time);
    }

    private long getCrc32() {
        return parameters.getEncryption() == Encryption.STANDARD ? parameters.getSourceFileCRC() : 0;
    }

    private long getCompressedSize(CentralDirectory.FileHeader fileHeader) throws IOException {
        if (fileHeader.isDirectory())
            return 0;
        if (parameters.isSourceExternalStream())
            return 0;
        if (parameters.getCompressionMethod() != CompressionMethod.STORE)
            return 0;
        if (parameters.getEncryption() != Encryption.AES)
            return 0;

        long fileSize = Files.size(sourceFile);

        if (parameters.getEncryption() == Encryption.STANDARD)
            return fileSize + InternalZipConstants.STD_DEC_HDR_SIZE;

        return fileSize + parameters.getAesKeyStrength().getSaltLength() + InternalZipConstants.AES_AUTH_LENGTH + 2; //2 is password verifier
    }

    private long getUncompressedSize(CentralDirectory.FileHeader fileHeader) throws IOException {
        if (fileHeader.isDirectory())
            return 0;
        if (parameters.isSourceExternalStream())
            return 0;
        return Files.size(sourceFile);
    }

    private byte[] getExternalFileAttr() throws IOException {
        if (parameters.isSourceExternalStream())
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
        aesDataRecord.setAesStrength(parameters.getAesKeyStrength());
        aesDataRecord.setCompressionMethod(parameters.getCompressionMethod());

        return aesDataRecord;
    }

}
