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
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.CentralDirectory;
import net.lingala.zip4j.model.CompressionMethod;
import net.lingala.zip4j.model.Encryption;
import net.lingala.zip4j.model.LocalFileHeader;
import net.lingala.zip4j.model.ZipModel;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.InternalZipConstants;
import net.lingala.zip4j.util.ZipUtils;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.CRC32;

public abstract class CipherOutputStream extends OutputStream {

    protected final OutputStreamDecorator out;
    protected final ZipModel zipModel;

    private Path sourceFile;
    @NonNull
    protected CentralDirectory.FileHeader fileHeader;
    private LocalFileHeader localFileHeader;
    @NonNull
    private Encryptor encryptor = Encryptor.NULL;
    protected ZipParameters parameters;


    protected final CRC32 crc = new CRC32();
    private long bytesWrittenForThisFile;
    private final byte[] pendingBuffer = new byte[InternalZipConstants.AES_BLOCK_SIZE];
    private int pendingBufferLength;
    private long totalBytesRead;

    protected CipherOutputStream(@NonNull OutputStream out, ZipModel zipModel) {
        this.out = new OutputStreamDecorator(out);
        this.zipModel = initZipModel(zipModel, this.out);
    }

    public final void putNextEntry(String fileNameStream, ZipParameters parameters) {
        putNextEntry(null, fileNameStream, parameters);
    }

    public final void putNextEntry(Path file, ZipParameters parameters) {
        putNextEntry(file, null, parameters);
    }

    protected void putNextEntry(Path file, String fileNameStream, ZipParameters parameters) {
        if (!parameters.isSourceExternalStream() && file == null)
            throw new ZipException("input file is null");
        if (!parameters.isSourceExternalStream() && !Files.exists(file))
            throw new ZipException("input file does not exist");

        try {
            sourceFile = file;
            this.parameters = parameters = parameters.toBuilder().build();

            if (parameters.isSourceExternalStream()) {
                if (StringUtils.isBlank(fileNameStream))
                    throw new ZipException("file name is empty for external stream");

                if (ZipUtils.isDirectory(fileNameStream)) {
                    parameters.setEncryption(Encryption.OFF);
                    parameters.setCompressionMethod(CompressionMethod.STORE);
                }
            } else if (Files.isDirectory(sourceFile)) {
                parameters.setEncryption(Encryption.OFF);
                parameters.setCompressionMethod(CompressionMethod.STORE);
            }

            int currSplitFileCounter = out.getCurrSplitFileCounter();
            CentralDirectoryBuilder centralDirectoryBuilder = new CentralDirectoryBuilder(sourceFile, fileNameStream, parameters, zipModel,
                    currSplitFileCounter);
            fileHeader = centralDirectoryBuilder.createFileHeader();
            localFileHeader = centralDirectoryBuilder.createLocalFileHeader(fileHeader);

            if (zipModel.isSplitArchive() && zipModel.isEmpty())
                out.writeInt(InternalZipConstants.SPLITSIG);

            fileHeader.setOffsLocalFileHeader(out.getOffsLocalHeaderRelative());
            out.addTotalBytesWritten(new LocalFileHeaderWriter().write(localFileHeader, zipModel, out));

            encryptor = parameters.getEncryption().createEncryptor(parameters, localFileHeader);
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

    private static ZipModel initZipModel(ZipModel zipModel, @NonNull OutputStreamDecorator out) {
        zipModel = zipModel == null ? new ZipModel() : zipModel;
        zipModel.setSplitLength(out.getSplitLength());
        return zipModel;
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

        if (parameters.getEncryption() == Encryption.AES) {
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

        if (parameters.getEncryption() == Encryption.AES) {
            if (encryptor instanceof AESEncryptor) {
                out.getDelegate().write(((AESEncryptor)encryptor).getFinalMac());
                bytesWrittenForThisFile += 10;
                out.addTotalBytesWritten(10);
            } else
                throw new ZipException("invalid encryptor for AES encrypted file");
        }

        fileHeader.setCompressedSize(bytesWrittenForThisFile);
        localFileHeader.setCompressedSize(bytesWrittenForThisFile);

        if (parameters.isSourceExternalStream()) {
            fileHeader.setUncompressedSize(totalBytesRead);

            if (localFileHeader.getUncompressedSize() != totalBytesRead)
                localFileHeader.setUncompressedSize(totalBytesRead);
        }

        long crc32 = fileHeader.getEncryption() == Encryption.AES ? 0 : crc.getValue();

        if (parameters.getEncryption() == Encryption.AES) {
            fileHeader.setCrc32(0);
            localFileHeader.setCrc32(0);
        } else {
            fileHeader.setCrc32(crc32);
            localFileHeader.setCrc32(crc32);
        }

        zipModel.addLocalFileHeader(localFileHeader);
        zipModel.addFileHeader(fileHeader);

        out.addTotalBytesWritten(new LocalFileHeaderWriter().writeExtended(localFileHeader, out));

        crc.reset();
        bytesWrittenForThisFile = 0;
        encryptor = Encryptor.NULL;
        totalBytesRead = 0;
    }

    public void finish() throws IOException, ZipException {
        zipModel.getEndCentralDirectory().setOffsCentralDirectory(out.getTotalBytesWritten());
        new HeaderWriter().finalizeZipFile(zipModel, out.getDelegate());
    }

    @Override
    public void close() throws IOException {
        finish();
        out.close();
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

    public void seek(long pos) throws IOException {
        out.seek(pos);
    }
}
