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

/**
 * @author Oleg Cherednik
 * @since 22.03.2019
 */
public abstract class CipherOutputStream extends OutputStream {

    protected final OutputStreamDecorator out;
    protected final ZipModel zipModel;

    @NonNull
    protected CentralDirectory.FileHeader fileHeader;
    private LocalFileHeader localFileHeader;
    @NonNull
    private Encryptor encryptor = Encryptor.NULL;
    protected ZipParameters parameters;


    protected final CRC32 crc = new CRC32();
    private final byte[] pendingBuffer = new byte[InternalZipConstants.AES_BLOCK_SIZE];
    private int pendingBufferLength;
    protected long totalBytesRead;

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
            this.parameters = parameters = parameters.toBuilder().build();

            if (parameters.isSourceExternalStream()) {
                if (StringUtils.isBlank(fileNameStream))
                    throw new ZipException("file name is empty for external stream");

                if (ZipUtils.isDirectory(fileNameStream)) {
                    parameters.setEncryption(Encryption.OFF);
                    parameters.setCompressionMethod(CompressionMethod.STORE);
                }
            } else if (Files.isDirectory(file)) {
                parameters.setEncryption(Encryption.OFF);
                parameters.setCompressionMethod(CompressionMethod.STORE);
            }

            int currSplitFileCounter = out.getCurrSplitFileCounter();
            CentralDirectoryBuilder builder = new CentralDirectoryBuilder(file, fileNameStream, parameters, zipModel, currSplitFileCounter);
            fileHeader = builder.createFileHeader();
            localFileHeader = builder.createLocalFileHeader(fileHeader);

            if (zipModel.isSplitArchive() && zipModel.isEmpty())
                out.writeInt(InternalZipConstants.SPLITSIG);

            fileHeader.setOffsLocalFileHeader(out.getOffsLocalHeaderRelative());
            new LocalFileHeaderWriter(localFileHeader).write(zipModel, out);

            (encryptor = parameters.getEncryption().createEncryptor(parameters, localFileHeader)).write(out);
            out.mark();
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

    @Override
    public void write(int val) throws IOException {
        write(new byte[] { (byte)val }, 0, 1);
    }

    @Override
    public void write(byte[] buf, int offs, int len) throws IOException {
        if (len == 0)
            return;

        if (parameters.getEncryption() == Encryption.AES) {
            if (pendingBufferLength != 0) {
                if (len >= (InternalZipConstants.AES_BLOCK_SIZE - pendingBufferLength)) {
                    System.arraycopy(buf, offs, pendingBuffer, pendingBufferLength, InternalZipConstants.AES_BLOCK_SIZE - pendingBufferLength);
                    encryptAndWrite(pendingBuffer, 0, pendingBuffer.length);
                    offs = InternalZipConstants.AES_BLOCK_SIZE - pendingBufferLength;
                    len -= offs;
                    pendingBufferLength = 0;
                } else {
                    System.arraycopy(buf, offs, pendingBuffer, pendingBufferLength, len);
                    pendingBufferLength += len;
                    return;
                }
            }
            if (len != 0 && len % 16 != 0) {
                System.arraycopy(buf, (len + offs) - (len % 16), pendingBuffer, 0, len % 16);
                pendingBufferLength = len % 16;
                len -= pendingBufferLength;
            }
        }
        if (len != 0)
            encryptAndWrite(buf, offs, len);
    }

    private void encryptAndWrite(byte[] buf, int offs, int len) throws IOException {
        encryptor.encrypt(buf, offs, len);
        out.writeBytes(buf, offs, len);
    }

    public void closeEntry() throws IOException, ZipException {
        if (pendingBufferLength != 0) {
            encryptAndWrite(pendingBuffer, 0, pendingBufferLength);
            pendingBufferLength = 0;
        }

        if (parameters.getEncryption() == Encryption.AES) {
            if (encryptor instanceof AESEncryptor)
                out.writeBytes(((AESEncryptor)encryptor).getFinalMac());
            else
                throw new ZipException("invalid encryptor for AES encrypted file");
        }

        fileHeader.setCompressedSize(out.getWrittenBytes());
        localFileHeader.setCompressedSize(out.getWrittenBytes());

        if (parameters.isSourceExternalStream()) {
            fileHeader.setUncompressedSize(totalBytesRead);

            if (localFileHeader.getUncompressedSize() != totalBytesRead)
                localFileHeader.setUncompressedSize(totalBytesRead);
        }

        fileHeader.setCrc32(parameters.getEncryption() == Encryption.AES ? 0 : crc.getValue());
        localFileHeader.setCrc32(parameters.getEncryption() == Encryption.AES ? 0 : crc.getValue());

        zipModel.addLocalFileHeader(localFileHeader);
        zipModel.addFileHeader(fileHeader);

        new LocalFileHeaderWriter(localFileHeader).writeExtended(out);

        crc.reset();
        encryptor = Encryptor.NULL;
        totalBytesRead = 0;
    }

    public void finish() throws IOException, ZipException {
        zipModel.getEndCentralDirectory().setOffsCentralDirectory(out.getOffs());
        new HeaderWriter().finalizeZipFile(zipModel, out.getDelegate());
    }

    @Override
    public void close() throws IOException {
        finish();
        out.close();
    }

    public void seek(long pos) throws IOException {
        out.seek(pos);
    }
}
