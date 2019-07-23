package net.lingala.zip4j.io;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.lingala.zip4j.crypto.pkware.StandardEncoder;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.AesExtraDataRecord;
import net.lingala.zip4j.model.CentralDirectory;
import net.lingala.zip4j.model.CompressionMethod;
import net.lingala.zip4j.model.Encryption;
import net.lingala.zip4j.model.GeneralPurposeFlag;
import net.lingala.zip4j.model.LocalFileHeader;
import net.lingala.zip4j.model.Zip64;
import net.lingala.zip4j.model.ZipModel;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.utils.InternalZipConstants;
import net.lingala.zip4j.utils.ZipUtils;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

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
        CentralDirectory.FileHeader fileHeader = new CentralDirectory.FileHeader(getFileName());

        fileHeader.setVersionMadeBy(CentralDirectory.FileHeader.DEF_VERSION);
        fileHeader.setVersionToExtract(CentralDirectory.FileHeader.DEF_VERSION);
        updateGeneralPurposeFlag(fileHeader.getGeneralPurposeFlag());
        fileHeader.setCompressionMethod(parameters.getActualCompressionMethod());
        fileHeader.setLastModifiedTime(getLastModFileTime());
        fileHeader.setCrc32(getCrc32());
        fileHeader.setCompressedSize(getCompressedSize(fileHeader));
        fileHeader.setUncompressedSize(getUncompressedSize(fileHeader));
        fileHeader.setFileCommentLength(0);
        fileHeader.setDiskNumber(currSplitFileCounter);
        fileHeader.setInternalFileAttributes(null);
        fileHeader.setExternalFileAttributes(getExternalFileAttr());
        fileHeader.setOffsLocalFileHeader(0);
        fileHeader.setZip64ExtendedInfo(Zip64.ExtendedInfo.NULL);
        fileHeader.setAesExtraDataRecord(getAesExtraDataRecord(parameters.getEncryption()));
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
        localFileHeader.setFileName(fileHeader.getFileName());
        localFileHeader.setExtraField(fileHeader.getExtraField().deepCopy());
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

        return ZipUtils.normalizeFileName.apply(fileName);
    }

    private void updateGeneralPurposeFlag(@NonNull GeneralPurposeFlag generalPurposeFlag) {
        generalPurposeFlag.setCompressionLevel(parameters.getCompressionLevel());
        generalPurposeFlag.setDataDescriptorExists(parameters.getCompressionMethod() == CompressionMethod.DEFLATE);
        generalPurposeFlag.setUtf8Encoding(zipModel.getCharset() == StandardCharsets.UTF_8);
        generalPurposeFlag.setEncrypted(parameters.getEncryption() != Encryption.OFF);
        generalPurposeFlag.setStrongEncryption(parameters.getEncryption() == Encryption.STRONG);
    }

    private int getLastModFileTime() throws IOException {
        long time = parameters.isSourceExternalStream() ? System.currentTimeMillis() : Files.getLastModifiedTime(sourceFile).toMillis();
        return (int)ZipUtils.javaToDosTime(time);
    }

    private long getCrc32() {
        return parameters.getCrc32();
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
            return fileSize + StandardEncoder.SIZE_RND_HEADER;

        return fileSize + parameters.getAesStrength().getSaltLength() + InternalZipConstants.AES_AUTH_LENGTH + 2; //2 is password verifier
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
        aesDataRecord.setAesStrength(parameters.getAesStrength());
        aesDataRecord.setCompressionMethod(parameters.getCompressionMethod());

        return aesDataRecord;
    }

}
