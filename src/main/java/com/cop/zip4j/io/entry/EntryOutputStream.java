package com.cop.zip4j.io.entry;

import com.cop.zip4j.core.writers.DataDescriptorWriter;
import com.cop.zip4j.core.writers.LocalFileHeaderWriter;
import com.cop.zip4j.crypto.Encoder;
import com.cop.zip4j.crypto.aes.AesEngine;
import com.cop.zip4j.exception.Zip4jException;
import com.cop.zip4j.io.CentralDirectoryBuilder;
import com.cop.zip4j.io.out.MarkDataOutput;
import com.cop.zip4j.model.CentralDirectory;
import com.cop.zip4j.model.CompressionMethod;
import com.cop.zip4j.model.DataDescriptor;
import com.cop.zip4j.model.Encryption;
import com.cop.zip4j.model.LocalFileHeader;
import com.cop.zip4j.model.ZipModel;
import com.cop.zip4j.model.entry.PathZipEntry;
import com.cop.zip4j.utils.InternalZipConstants;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

/**
 * @author Oleg Cherednik
 * @since 26.07.2019
 */
@RequiredArgsConstructor
public class EntryOutputStream extends OutputStream {

    private static final String MARK = "entry";

    @NonNull
    private final ZipModel zipModel;
    protected final MarkDataOutput out;
    protected final Checksum crc32 = new CRC32();

    private CentralDirectory.FileHeader fileHeader;
    private LocalFileHeader localFileHeader;

    private final byte[] pendingBuffer = new byte[AesEngine.AES_BLOCK_SIZE];
    private int pendingBufferLength;
    protected long total;

    @NonNull
    private Encoder encoder = Encoder.NULL;
    @NonNull
    private Encryption encryption = Encryption.OFF;

    public static EntryOutputStream create(@NonNull PathZipEntry entry, @NonNull ZipModel zipModel, @NonNull MarkDataOutput out) {
        EntryOutputStream stream;

        if (entry.getCompressionMethod() == CompressionMethod.DEFLATE)
            stream = new DeflateEntryOutputStream(zipModel, out, entry.getCompressionLevel());
        else
            stream = new EntryOutputStream(zipModel, out);

        stream.putNextEntry(entry);

        return stream;
    }

    private void putNextEntry(@NonNull PathZipEntry entry) {
        try {
            CentralDirectoryBuilder centralDirectoryBuilder = new CentralDirectoryBuilder(entry, zipModel, out.getCounter());

            fileHeader = centralDirectoryBuilder.createFileHeader();
            localFileHeader = centralDirectoryBuilder.createLocalFileHeader(fileHeader);

            if (zipModel.isSplitArchive() && zipModel.isEmpty())
                out.writeDword(InternalZipConstants.SPLITSIG);

            fileHeader.setOffsLocalFileHeader(out.getFilePointer());
            new LocalFileHeaderWriter(zipModel, localFileHeader).write(out);

            encoder = entry.getEncryption().encoder(localFileHeader, entry);
            encryption = entry.getEncryption();

            out.mark(MARK);
            encoder.writeHeader(out);
        } catch(IOException e) {
            throw new Zip4jException(e);
        }
    }

    @Override
    public void write(int b) throws IOException {
        write(new byte[] { (byte)b }, 0, 1);
    }

    @Override
    public final void write(byte[] buf, int offs, int len) throws IOException {
        crc32.update(buf, offs, len);
        total += len;
        writeImpl(buf, offs, len);
    }

    protected void writeImpl(byte[] buf, int offs, int len) throws IOException {
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
        encoder.encrypt(buf, offs, len);
        out.write(buf, offs, len);
    }

    @Override
    public void close() throws IOException {
        if (pendingBufferLength != 0) {
            encryptAndWrite(pendingBuffer, 0, pendingBufferLength);
            pendingBufferLength = 0;
        }

        encoder.close(out);

        fileHeader.setCrc32(fileHeader.getEncryption() == Encryption.AES ? 0 : crc32.getValue());
        fileHeader.setCompressedSize(out.getWrittenBytesAmount(MARK));
        fileHeader.setUncompressedSize(total);
        zipModel.addFileHeader(fileHeader);

        writeDataDescriptor();

        out.mark(MARK);
    }

    private void writeDataDescriptor() throws IOException {
        // TODO should be isDataDescriptorExists == true only when parameters.getCompressionMethod() == CompressionMethod.DEFLATE
        if (!localFileHeader.getGeneralPurposeFlag().isDataDescriptorExists())
            return;

        DataDescriptor dataDescriptor = new DataDescriptor();
        dataDescriptor.setCrc32(fileHeader.getCompressionMethod() == CompressionMethod.AES_ENC ? 0 : fileHeader.getCrc32());
        dataDescriptor.setCompressedSize(fileHeader.getCompressedSize());
        dataDescriptor.setUncompressedSize(fileHeader.getUncompressedSize());

        new DataDescriptorWriter(dataDescriptor).write(out);
    }

}
