package com.cop.zip4j.io.entry;

import com.cop.zip4j.core.writers.DataDescriptorWriter;
import com.cop.zip4j.core.writers.LocalFileHeaderWriter;
import com.cop.zip4j.crypto.Encoder;
import com.cop.zip4j.crypto.aes.AesEngine;
import com.cop.zip4j.exception.ZipException;
import com.cop.zip4j.io.CentralDirectoryBuilder;
import com.cop.zip4j.io.ZipOutputStream;
import com.cop.zip4j.model.CentralDirectory;
import com.cop.zip4j.model.DataDescriptor;
import com.cop.zip4j.model.Encryption;
import com.cop.zip4j.model.LocalFileHeader;
import com.cop.zip4j.model.ZipParameters;
import com.cop.zip4j.model.entry.PathZipEntry;
import com.cop.zip4j.utils.InternalZipConstants;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.CRC32;

/**
 * @author Oleg Cherednik
 * @since 26.07.2019
 */
@RequiredArgsConstructor
public class CommonEntryOutputDelegate extends OutputStream {

    private static final String MARK = "entry";

    protected final ZipOutputStream zipOutputStream;
    protected final CRC32 crc32 = new CRC32();

    private CentralDirectory.FileHeader fileHeader;
    private LocalFileHeader localFileHeader;

    private final byte[] pendingBuffer = new byte[AesEngine.AES_BLOCK_SIZE];
    private int pendingBufferLength;
    protected long totalBytesRead;

    @NonNull
    private Encoder encoder = Encoder.NULL;
    @NonNull
    private Encryption encryption = Encryption.OFF;

    public void putNextEntry(@NonNull PathZipEntry entry, @NonNull ZipParameters parameters) {
        try {
            int currSplitFileCounter = zipOutputStream.out.getCurrSplitFileCounter();
            CentralDirectoryBuilder centralDirectoryBuilder = new CentralDirectoryBuilder(entry, parameters, zipOutputStream.zipModel,
                    currSplitFileCounter);
            fileHeader = centralDirectoryBuilder.createFileHeader();
            localFileHeader = centralDirectoryBuilder.createLocalFileHeader(fileHeader);

            if (zipOutputStream.zipModel.isSplitArchive() && zipOutputStream.zipModel.isEmpty())
                zipOutputStream.out.writeDword(InternalZipConstants.SPLITSIG);

            fileHeader.setOffsLocalFileHeader(zipOutputStream.out.getFilePointer());
            new LocalFileHeaderWriter(localFileHeader, zipOutputStream.zipModel).write(zipOutputStream.out);

            encoder = parameters.getEncryption().encoder(localFileHeader, parameters);
            encryption = parameters.getEncryption();

            zipOutputStream.out.mark(MARK);

            encoder.write(zipOutputStream.out);
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
        crc32.update(buf, offs, len);
        totalBytesRead += len;
        _write(buf, offs, len);
    }

    protected final void _write(byte[] buf, int offs, int len) throws IOException {
        if (len == 0)
            return;

        if (encryption == Encryption.AES) {
            if (pendingBufferLength != 0) {
                if (len >= (AesEngine.AES_BLOCK_SIZE - pendingBufferLength)) {
                    System.arraycopy(buf, offs, pendingBuffer, pendingBufferLength, AesEngine.AES_BLOCK_SIZE - pendingBufferLength);
                    encryptAndWrite(pendingBuffer, 0, pendingBuffer.length);
                    offs = AesEngine.AES_BLOCK_SIZE - pendingBufferLength;
                    len -= offs;
                    pendingBufferLength = 0;
                } else {
                    System.arraycopy(buf, offs, pendingBuffer, pendingBufferLength, len);
                    pendingBufferLength += len;
                    return;
                }
            }

            if (len % 16 != 0) {
                System.arraycopy(buf, (len + offs) - (len % 16), pendingBuffer, 0, len % 16);
                pendingBufferLength = len % 16;
                len -= pendingBufferLength;
            }
        }

        if (len != 0)
            encryptAndWrite(buf, offs, len);
    }

    private void encryptAndWrite(byte[] buf, int offs, int len) throws IOException {
        encoder.encode(buf, offs, len);
        zipOutputStream.out.writeBytes(buf, offs, len);
    }

    @Override
    public void close() throws IOException {
        if (pendingBufferLength != 0) {
            encryptAndWrite(pendingBuffer, 0, pendingBufferLength);
            pendingBufferLength = 0;
        }

        encoder.close(zipOutputStream.out);

        fileHeader.setCrc32(fileHeader.getEncryption() == Encryption.AES ? 0 : crc32.getValue());
        fileHeader.setCompressedSize(zipOutputStream.out.getWrittenBytesAmount(MARK));
        fileHeader.setUncompressedSize(totalBytesRead);
        zipOutputStream.zipModel.addFileHeader(fileHeader);

        writeDataDescriptor();

        zipOutputStream.out.mark(MARK);
        totalBytesRead = 0;
    }

    private void writeDataDescriptor() throws IOException {
        // TODO should be isDataDescriptorExists == true only when parameters.getCompressionMethod() == CompressionMethod.DEFLATE
        if (!localFileHeader.getGeneralPurposeFlag().isDataDescriptorExists())
            return;

        DataDescriptor dataDescriptor = new DataDescriptor();
        dataDescriptor.setCrc32(fileHeader.getCrc32());
        dataDescriptor.setCompressedSize(fileHeader.getCompressedSize());
        dataDescriptor.setUncompressedSize(fileHeader.getUncompressedSize());

        new DataDescriptorWriter(dataDescriptor).write(zipOutputStream.out);
    }

}
