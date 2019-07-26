package com.cop.zip4j.io.delegate;

import com.cop.zip4j.core.writers.DataDescriptorWriter;
import com.cop.zip4j.core.writers.LocalFileHeaderWriter;
import com.cop.zip4j.crypto.Encoder;
import com.cop.zip4j.crypto.aes.AesEngine;
import com.cop.zip4j.exception.ZipException;
import com.cop.zip4j.io.CentralDirectoryBuilder;
import com.cop.zip4j.io.CipherOutputStream;
import com.cop.zip4j.model.DataDescriptor;
import com.cop.zip4j.model.Encryption;
import com.cop.zip4j.model.ZipParameters;
import com.cop.zip4j.model.entry.PathZipEntry;
import com.cop.zip4j.utils.InternalZipConstants;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 26.07.2019
 */
@RequiredArgsConstructor
public class CommonOutputDelegate implements OutputDelegate {

    protected final CipherOutputStream cipherOutputStream;

    @Override
    public void putNextEntry(@NonNull PathZipEntry entry, @NonNull ZipParameters parameters) {
        try {
            int currSplitFileCounter = cipherOutputStream.out.getCurrSplitFileCounter();
            CentralDirectoryBuilder centralDirectoryBuilder = new CentralDirectoryBuilder(entry, parameters, cipherOutputStream.zipModel,
                    currSplitFileCounter);
            cipherOutputStream.fileHeader = centralDirectoryBuilder.createFileHeader();
            cipherOutputStream.localFileHeader = centralDirectoryBuilder.createLocalFileHeader(cipherOutputStream.fileHeader);

            if (cipherOutputStream.zipModel.isSplitArchive() && cipherOutputStream.zipModel.isEmpty())
                cipherOutputStream.out.writeDword(InternalZipConstants.SPLITSIG);

            cipherOutputStream.fileHeader.setOffsLocalFileHeader(cipherOutputStream.out.getFilePointer());
            new LocalFileHeaderWriter(cipherOutputStream.localFileHeader, cipherOutputStream.zipModel).write(cipherOutputStream.out);

            cipherOutputStream.encoder = parameters.getEncryption().encoder(cipherOutputStream.localFileHeader, parameters);
            cipherOutputStream.encryption = parameters.getEncryption();

            cipherOutputStream.out.mark(CipherOutputStream.MARK);

            cipherOutputStream.encoder.write(cipherOutputStream.out);
            cipherOutputStream.crc.reset();
        } catch(ZipException e) {
            throw e;
        } catch(Exception e) {
            throw new ZipException(e);
        }
    }

    @Override
    public void write(byte[] buf, int offs, int len) throws IOException {
        cipherOutputStream.crc.update(buf, offs, len);
        cipherOutputStream.totalBytesRead += len;
        _write(buf, offs, len);
    }

    protected final void _write(byte[] buf, int offs, int len) throws IOException {
        if (len == 0)
            return;

        if (cipherOutputStream.encryption == Encryption.AES) {
            if (cipherOutputStream.pendingBufferLength != 0) {
                if (len >= (AesEngine.AES_BLOCK_SIZE - cipherOutputStream.pendingBufferLength)) {
                    System.arraycopy(buf, offs, cipherOutputStream.pendingBuffer, cipherOutputStream.pendingBufferLength,
                            AesEngine.AES_BLOCK_SIZE - cipherOutputStream.pendingBufferLength);
                    encryptAndWrite(cipherOutputStream.pendingBuffer, 0, cipherOutputStream.pendingBuffer.length);
                    offs = AesEngine.AES_BLOCK_SIZE - cipherOutputStream.pendingBufferLength;
                    len -= offs;
                    cipherOutputStream.pendingBufferLength = 0;
                } else {
                    System.arraycopy(buf, offs, cipherOutputStream.pendingBuffer, cipherOutputStream.pendingBufferLength,
                            len);
                    cipherOutputStream.pendingBufferLength += len;
                    return;
                }
            }

            if (len % 16 != 0) {
                System.arraycopy(buf, (len + offs) - (len % 16), cipherOutputStream.pendingBuffer, 0, len % 16);
                cipherOutputStream.pendingBufferLength = len % 16;
                len -= cipherOutputStream.pendingBufferLength;
            }
        }

        if (len != 0)
            encryptAndWrite(buf, offs, len);
    }

    private void encryptAndWrite(byte[] buf, int offs, int len) throws IOException {
        cipherOutputStream.encoder.encode(buf, offs, len);
        cipherOutputStream.out.writeBytes(buf, offs, len);
    }

    @Override
    public void closeEntry() throws IOException {
        if (cipherOutputStream.pendingBufferLength != 0) {
            encryptAndWrite(cipherOutputStream.pendingBuffer, 0, cipherOutputStream.pendingBufferLength);
            cipherOutputStream.pendingBufferLength = 0;
        }

        cipherOutputStream.encoder.closeEntry(cipherOutputStream.out);

        cipherOutputStream.fileHeader.setCrc32(
                cipherOutputStream.fileHeader.getEncryption() == Encryption.AES ? 0 : cipherOutputStream.crc.getValue());
        cipherOutputStream.fileHeader.setCompressedSize(cipherOutputStream.out.getWrittenBytesAmount(CipherOutputStream.MARK));
        cipherOutputStream.fileHeader.setUncompressedSize(cipherOutputStream.totalBytesRead);
        cipherOutputStream.zipModel.addFileHeader(cipherOutputStream.fileHeader);

        writeDataDescriptor();

        cipherOutputStream.crc.reset();
        cipherOutputStream.out.mark(CipherOutputStream.MARK);
        cipherOutputStream.encoder = Encoder.NULL;
        cipherOutputStream.totalBytesRead = 0;
    }

    private void writeDataDescriptor() throws IOException {
        // TODO should be isDataDescriptorExists == true only when parameters.getCompressionMethod() == CompressionMethod.DEFLATE
        if (!cipherOutputStream.localFileHeader.getGeneralPurposeFlag().isDataDescriptorExists())
            return;

        DataDescriptor dataDescriptor = new DataDescriptor();
        dataDescriptor.setCrc32(cipherOutputStream.fileHeader.getCrc32());
        dataDescriptor.setCompressedSize(cipherOutputStream.fileHeader.getCompressedSize());
        dataDescriptor.setUncompressedSize(cipherOutputStream.fileHeader.getUncompressedSize());

        new DataDescriptorWriter(dataDescriptor).write(cipherOutputStream.out);
    }


}
