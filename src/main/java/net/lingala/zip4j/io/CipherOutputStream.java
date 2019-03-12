/*
 * Copyright 2010 Srikanth Reddy Lingala
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.lingala.zip4j.io;

import lombok.NonNull;
import net.lingala.zip4j.core.HeaderWriter;
import net.lingala.zip4j.core.writers.LocalFileHeaderWriter;
import net.lingala.zip4j.crypto.AESEncryptor;
import net.lingala.zip4j.crypto.Encryptor;
import net.lingala.zip4j.crypto.StandardEncryptor;
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
import net.lingala.zip4j.util.Raw;
import net.lingala.zip4j.util.Zip4jUtil;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.zip.CRC32;

public abstract class CipherOutputStream extends OutputStream {

    protected final OutputStreamDecorator out;
    private Path sourceFile;
    @NonNull
    protected CentralDirectory.FileHeader fileHeader;
    private LocalFileHeader localFileHeader;
    @NonNull
    private Encryptor encryptor = Encryptor.NULL;
    protected ZipParameters zipParameters;
    protected ZipModel zipModel;

    protected final CRC32 crc = new CRC32();
    private long bytesWrittenForThisFile;
    private final byte[] pendingBuffer = new byte[InternalZipConstants.AES_BLOCK_SIZE];
    private int pendingBufferLength;
    private long totalBytesRead;

    protected CipherOutputStream(OutputStream out, ZipModel zipModel) {
        this.out = new OutputStreamDecorator(out);
        initZipModel(zipModel);
    }

    public void putNextEntry(Path file, ZipParameters parameters) throws ZipException {
        if (!parameters.isSourceExternalStream() && file == null)
            throw new ZipException("input file is null");

        if (!parameters.isSourceExternalStream() && !Files.exists(file))
            throw new ZipException("input file does not exist");

        try {
            sourceFile = file;
            zipParameters = parameters.toBuilder().build();

            if (parameters.isSourceExternalStream()) {
                if (StringUtils.isBlank(zipParameters.getFileNameInZip()))
                    throw new ZipException("file name is empty for external stream");

                if (Zip4jUtil.isDirectory(parameters.getFileNameInZip())) {
                    zipParameters.setEncryption(Encryption.OFF);
                    zipParameters.setCompressionMethod(CompressionMethod.STORE);
                }
            } else if (Files.isDirectory(sourceFile)) {
                zipParameters.setEncryption(Encryption.OFF);
                zipParameters.setCompressionMethod(CompressionMethod.STORE);
            }

            fileHeader = createFileHeader();
            localFileHeader = createLocalFileHeader(fileHeader);

            if (zipModel.isSplitArchive() && zipModel.isEmpty()) {
                byte[] intByte = new byte[4];
                Raw.writeIntLittleEndian(intByte, 0, (int)InternalZipConstants.SPLITSIG);
                out.getDelegate().write(intByte);
                out.addTotalBytesWritten(4);
            }

            fileHeader.setOffLocalHeaderRelative(out.getOffsLocalHeaderRelative());
            out.addTotalBytesWritten(new LocalFileHeaderWriter().write(localFileHeader, zipModel, out.getDelegate()));

            encryptor = createEncryptor();
            int length = encryptor.write(out.getDelegate());
            out.addTotalBytesWritten(length);
            bytesWrittenForThisFile += length;

            crc.reset();
        } catch(ZipException e) {
            throw e;
        } catch(Exception e) {
            throw new ZipException(e);
        }
    }

    private Encryptor createEncryptor() throws ZipException {
        if (zipParameters.getEncryption() == Encryption.OFF)
            return Encryptor.NULL;
        if (zipParameters.getEncryption() == Encryption.STANDARD)
            // Since we do not know the crc here, we use the modification time for encrypting.
            return new StandardEncryptor(zipParameters.getPassword(), (localFileHeader.getLastModFileTime() & 0x0000ffff) << 16);
        if (zipParameters.getEncryption() == Encryption.AES)
            return new AESEncryptor(zipParameters.getPassword(), zipParameters.getAesKeyStrength());

        throw new ZipException("invalid encryption method");
    }

    private void initZipModel(ZipModel zipModel) {
        this.zipModel = zipModel == null ? new ZipModel() : zipModel;
        this.zipModel.createEndCentralDirectoryIfNotExist();

        if (this.zipModel.getCentralDirectory() == null)
            this.zipModel.setCentralDirectory(new CentralDirectory());

        if (this.zipModel.getCentralDirectory().getFileHeaders() == null)
            this.zipModel.getCentralDirectory().setFileHeaders(new ArrayList<>());

        if (this.zipModel.getLocalFileHeaderList() == null)
            this.zipModel.setLocalFileHeaderList(new ArrayList<>());

        this.zipModel.setSplitLength(out.getSplitLength());
    }

    public void write(int bval) throws IOException {
        byte[] b = new byte[1];
        b[0] = (byte)bval;
        write(b, 0, 1);
    }

    public void write(byte[] b) throws IOException {
        if (b == null)
            throw new NullPointerException();

        if (b.length == 0) return;

        write(b, 0, b.length);
    }

    public void write(byte[] b, int off, int len) throws IOException {
        if (len == 0) return;

        if (zipParameters.getEncryption() == Encryption.AES) {
            if (pendingBufferLength != 0) {
                if (len >= (InternalZipConstants.AES_BLOCK_SIZE - pendingBufferLength)) {
                    System.arraycopy(b, off, pendingBuffer, pendingBufferLength,
                            InternalZipConstants.AES_BLOCK_SIZE - pendingBufferLength);
                    encryptAndWrite(pendingBuffer, 0, pendingBuffer.length);
                    off = InternalZipConstants.AES_BLOCK_SIZE - pendingBufferLength;
                    len -= off;
                    pendingBufferLength = 0;
                } else {
                    System.arraycopy(b, off, pendingBuffer, pendingBufferLength,
                            len);
                    pendingBufferLength += len;
                    return;
                }
            }
            if (len != 0 && len % 16 != 0) {
                System.arraycopy(b, (len + off) - (len % 16), pendingBuffer, 0, len % 16);
                pendingBufferLength = len % 16;
                len -= pendingBufferLength;
            }
        }
        if (len != 0)
            encryptAndWrite(b, off, len);
    }

    private void encryptAndWrite(byte[] b, int off, int len) throws IOException {
        try {
            encryptor.encrypt(b, off, len);
            out.getDelegate().write(b, off, len);
            out.addTotalBytesWritten(len);
            bytesWrittenForThisFile += len;
        } catch(ZipException e) {
            throw new IOException(e);
        }
    }

    public void closeEntry() throws IOException, ZipException {

        if (this.pendingBufferLength != 0) {
            encryptAndWrite(pendingBuffer, 0, pendingBufferLength);
            pendingBufferLength = 0;
        }

        if (zipParameters.getEncryption() == Encryption.AES) {
            if (encryptor instanceof AESEncryptor) {
                out.getDelegate().write(((AESEncryptor)encryptor).getFinalMac());
                bytesWrittenForThisFile += 10;
                out.addTotalBytesWritten(10);
            } else {
                throw new ZipException("invalid encryptor for AES encrypted file");
            }
        }
        fileHeader.setCompressedSize(bytesWrittenForThisFile);
        localFileHeader.setCompressedSize(bytesWrittenForThisFile);

        if (zipParameters.isSourceExternalStream()) {
            fileHeader.setUncompressedSize(totalBytesRead);
            if (localFileHeader.getUncompressedSize() != totalBytesRead) {
                localFileHeader.setUncompressedSize(totalBytesRead);
            }
        }

        long crc32 = fileHeader.getEncryption() == Encryption.AES ? 0 : crc.getValue();

        if (zipParameters.getEncryption() == Encryption.AES) {
            fileHeader.setCrc32(0);
            localFileHeader.setCrc32(0);
        } else {
            fileHeader.setCrc32(crc32);
            localFileHeader.setCrc32(crc32);
        }

        zipModel.addLocalFileHeader(localFileHeader);
        zipModel.getCentralDirectory().addFileHeader(fileHeader);

        out.addTotalBytesWritten(new LocalFileHeaderWriter().writeExtended(localFileHeader, out.getDelegate()));

        crc.reset();
        bytesWrittenForThisFile = 0;
        encryptor = Encryptor.NULL;
        totalBytesRead = 0;
    }

    public void finish() throws IOException, ZipException {
        zipModel.getEndCentralDirectory().setOffOfStartOfCentralDir(out.getTotalBytesWritten());

        HeaderWriter headerWriter = new HeaderWriter();
        headerWriter.finalizeZipFile(zipModel, out.getDelegate());
    }

    public void close() throws IOException {
        if (out != null)
            out.getDelegate().close();
    }

    private CentralDirectory.FileHeader createFileHeader() throws ZipException, IOException {
        CentralDirectory.FileHeader fileHeader = new CentralDirectory.FileHeader();

        fileHeader.setVersionMadeBy(20);
        fileHeader.setVersionNeededToExtract(20);
        updateGeneralPurposeFlag(fileHeader.getGeneralPurposeFlag());
        fileHeader.setCompressionMethod(getCompressionMethod());
        fileHeader.setLastModFileTime(getLastModFileTime());
        fileHeader.setCrc32(getCrc32());
        fileHeader.setCompressedSize(getCompressedSize(fileHeader));
        fileHeader.setUncompressedSize(getUncompressedSize(fileHeader));

        fileHeader.setAesExtraDataRecord(getAesExtraDataRecord(zipParameters.getEncryption()));


        String fileName = null;
        if (zipParameters.isSourceExternalStream()) {
            if (StringUtils.isBlank(zipParameters.getFileNameInZip()))
                throw new ZipException("fileNameInZip is null or empty");
            fileName = zipParameters.getFileNameInZip();
        } else {
            fileHeader.setUncompressedSize(Files.size(sourceFile));
            fileName = zipParameters.getRelativeFileName(sourceFile);

        }

        if (StringUtils.isBlank(fileName))
            throw new ZipException("fileName is null or empty. unable to create file header");

        if (!zipParameters.isSourceExternalStream() && Files.isDirectory(sourceFile) && !Zip4jUtil.isDirectory(fileName))
            fileName += InternalZipConstants.FILE_SEPARATOR;

        fileHeader.setFileName(fileName);
        fileHeader.setFileNameLength(Zip4jUtil.getEncodedStringLength(fileName, zipModel.getCharset()));
        fileHeader.setDiskNumberStart(out.getCurrSplitFileCounter());

        int fileAttrs = 0;
        if (!zipParameters.isSourceExternalStream())
            fileAttrs = getFileAttributes(sourceFile);
        byte[] externalFileAttrs = { (byte)fileAttrs, 0, 0, 0 };

        fileHeader.setExternalFileAttr(externalFileAttrs);

        return fileHeader;
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

    private long getUncompressedSize(CentralDirectory.FileHeader fileHeader) throws IOException, ZipException {
        if (fileHeader.isDirectory())
            return 0;
        if (zipParameters.isSourceExternalStream())
            return 0;
        return Files.size(sourceFile);
    }

    private static LocalFileHeader createLocalFileHeader(@NonNull CentralDirectory.FileHeader fileHeader) throws ZipException {
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

    /**
     * Checks the file attributes and returns an integer
     *
     * @param file
     * @return
     * @throws ZipException
     */
    private static int getFileAttributes(@NonNull Path file) throws IOException {
        if (!Files.exists(file))
            return 0;

        if (Files.isDirectory(file))
            return Files.isHidden(file) ? InternalZipConstants.FOLDER_MODE_HIDDEN : InternalZipConstants.FOLDER_MODE_NONE;
        if (!Files.isWritable(file) && Files.isHidden(file))
            return InternalZipConstants.FILE_MODE_READ_ONLY_HIDDEN;
        if (!Files.isWritable(file))
            return InternalZipConstants.FILE_MODE_READ_ONLY;
        return Files.isHidden(file) ? InternalZipConstants.FILE_MODE_HIDDEN : InternalZipConstants.FILE_MODE_NONE;
    }

    private int[] generateGeneralPurposeBitArray(CentralDirectory.FileHeader fileHeader) {
        int[] generalPurposeBits = new int[8];
//        generalPurposeBits[0] = fileHeader.getEncryption() != Encryption.OFF ? 1 : 0;

        fileHeader.getGeneralPurposeFlag().setCompressionLevel(zipParameters.getCompressionLevel());
        fileHeader.getGeneralPurposeFlag().setDataDescriptorExists(true);

        return generalPurposeBits;
    }

    public void decrementCompressedFileSize(int value) {
        if (value <= 0) return;

        if (value <= this.bytesWrittenForThisFile) {
            this.bytesWrittenForThisFile -= value;
        }
    }

    protected void updateTotalBytesRead(int toUpdate) {
        if (toUpdate > 0) {
            totalBytesRead += toUpdate;
        }
    }
}
