package com.cop.zip4j.io.delegate;

import com.cop.zip4j.core.writers.DataDescriptorWriter;
import com.cop.zip4j.core.writers.LocalFileHeaderWriter;
import com.cop.zip4j.crypto.Encoder;
import com.cop.zip4j.crypto.aes.AesEngine;
import com.cop.zip4j.exception.ZipException;
import com.cop.zip4j.io.CentralDirectoryBuilder;
import com.cop.zip4j.io.ZipOutputStream;
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
public class CommonEntryOutputDelegate extends EntryOutputDelegate {

    protected final ZipOutputStream zipOutputStream;

    @Override
    public void putNextEntry(@NonNull PathZipEntry entry, @NonNull ZipParameters parameters) {
        try {
            int currSplitFileCounter = zipOutputStream.out.getCurrSplitFileCounter();
            CentralDirectoryBuilder centralDirectoryBuilder = new CentralDirectoryBuilder(entry, parameters, zipOutputStream.zipModel,
                    currSplitFileCounter);
            zipOutputStream.fileHeader = centralDirectoryBuilder.createFileHeader();
            zipOutputStream.localFileHeader = centralDirectoryBuilder.createLocalFileHeader(zipOutputStream.fileHeader);

            if (zipOutputStream.zipModel.isSplitArchive() && zipOutputStream.zipModel.isEmpty())
                zipOutputStream.out.writeDword(InternalZipConstants.SPLITSIG);

            zipOutputStream.fileHeader.setOffsLocalFileHeader(zipOutputStream.out.getFilePointer());
            new LocalFileHeaderWriter(zipOutputStream.localFileHeader, zipOutputStream.zipModel).write(zipOutputStream.out);

            zipOutputStream.encoder = parameters.getEncryption().encoder(zipOutputStream.localFileHeader, parameters);
            zipOutputStream.encryption = parameters.getEncryption();

            zipOutputStream.out.mark(ZipOutputStream.MARK);

            zipOutputStream.encoder.write(zipOutputStream.out);
            zipOutputStream.crc.reset();
        } catch(ZipException e) {
            throw e;
        } catch(Exception e) {
            throw new ZipException(e);
        }
    }

    @Override
    public void write(int b) throws IOException {
        write(new byte[] { (byte)b }, 0, 1);
    }

    @Override
    public void write(byte[] buf, int offs, int len) throws IOException {
        zipOutputStream.crc.update(buf, offs, len);
        zipOutputStream.totalBytesRead += len;
        _write(buf, offs, len);
    }

    protected final void _write(byte[] buf, int offs, int len) throws IOException {
        if (len == 0)
            return;

        if (zipOutputStream.encryption == Encryption.AES) {
            if (zipOutputStream.pendingBufferLength != 0) {
                if (len >= (AesEngine.AES_BLOCK_SIZE - zipOutputStream.pendingBufferLength)) {
                    System.arraycopy(buf, offs, zipOutputStream.pendingBuffer, zipOutputStream.pendingBufferLength,
                            AesEngine.AES_BLOCK_SIZE - zipOutputStream.pendingBufferLength);
                    encryptAndWrite(zipOutputStream.pendingBuffer, 0, zipOutputStream.pendingBuffer.length);
                    offs = AesEngine.AES_BLOCK_SIZE - zipOutputStream.pendingBufferLength;
                    len -= offs;
                    zipOutputStream.pendingBufferLength = 0;
                } else {
                    System.arraycopy(buf, offs, zipOutputStream.pendingBuffer, zipOutputStream.pendingBufferLength,
                            len);
                    zipOutputStream.pendingBufferLength += len;
                    return;
                }
            }

            if (len % 16 != 0) {
                System.arraycopy(buf, (len + offs) - (len % 16), zipOutputStream.pendingBuffer, 0, len % 16);
                zipOutputStream.pendingBufferLength = len % 16;
                len -= zipOutputStream.pendingBufferLength;
            }
        }

        if (len != 0)
            encryptAndWrite(buf, offs, len);
    }

    private void encryptAndWrite(byte[] buf, int offs, int len) throws IOException {
        zipOutputStream.encoder.encode(buf, offs, len);
        zipOutputStream.out.writeBytes(buf, offs, len);
    }

    @Override
    public void close() throws IOException {
        if (zipOutputStream.pendingBufferLength != 0) {
            encryptAndWrite(zipOutputStream.pendingBuffer, 0, zipOutputStream.pendingBufferLength);
            zipOutputStream.pendingBufferLength = 0;
        }

        zipOutputStream.encoder.closeEntry(zipOutputStream.out);

        zipOutputStream.fileHeader.setCrc32(
                zipOutputStream.fileHeader.getEncryption() == Encryption.AES ? 0 : zipOutputStream.crc.getValue());
        zipOutputStream.fileHeader.setCompressedSize(zipOutputStream.out.getWrittenBytesAmount(ZipOutputStream.MARK));
        zipOutputStream.fileHeader.setUncompressedSize(zipOutputStream.totalBytesRead);
        zipOutputStream.zipModel.addFileHeader(zipOutputStream.fileHeader);

        writeDataDescriptor();

        zipOutputStream.crc.reset();
        zipOutputStream.out.mark(ZipOutputStream.MARK);
        zipOutputStream.encoder = Encoder.NULL;
        zipOutputStream.totalBytesRead = 0;
    }

    private void writeDataDescriptor() throws IOException {
        // TODO should be isDataDescriptorExists == true only when parameters.getCompressionMethod() == CompressionMethod.DEFLATE
        if (!zipOutputStream.localFileHeader.getGeneralPurposeFlag().isDataDescriptorExists())
            return;

        DataDescriptor dataDescriptor = new DataDescriptor();
        dataDescriptor.setCrc32(zipOutputStream.fileHeader.getCrc32());
        dataDescriptor.setCompressedSize(zipOutputStream.fileHeader.getCompressedSize());
        dataDescriptor.setUncompressedSize(zipOutputStream.fileHeader.getUncompressedSize());

        new DataDescriptorWriter(dataDescriptor).write(zipOutputStream.out);
    }

}
