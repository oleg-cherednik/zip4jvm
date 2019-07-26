package com.cop.zip4j.io;

import com.cop.zip4j.core.writers.DataDescriptorWriter;
import com.cop.zip4j.core.writers.LocalFileHeaderWriter;
import com.cop.zip4j.core.writers.ZipModelWriter;
import com.cop.zip4j.crypto.Encoder;
import com.cop.zip4j.crypto.aes.AesEngine;
import com.cop.zip4j.exception.ZipException;
import com.cop.zip4j.model.CentralDirectory;
import com.cop.zip4j.model.CompressionMethod;
import com.cop.zip4j.model.DataDescriptor;
import com.cop.zip4j.model.Encryption;
import com.cop.zip4j.model.LocalFileHeader;
import com.cop.zip4j.model.ZipModel;
import com.cop.zip4j.model.ZipParameters;
import com.cop.zip4j.model.entry.PathZipEntry;
import com.cop.zip4j.utils.InternalZipConstants;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.ArrayUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.CRC32;

/**
 * @author Oleg Cherednik
 * @since 26.07.2019
 */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public final class ZipOutputStream extends OutputStream {

    private static final String MARK = "entry";

    @NonNull
    protected final SplitOutputStream out;
    @NonNull
    protected final ZipModel zipModel;

    protected CentralDirectory.FileHeader fileHeader;
    private LocalFileHeader localFileHeader;
    @NonNull
    private Encoder encoder = Encoder.NULL;
    @NonNull
    private Encryption encryption = Encryption.OFF;
    @NonNull
    protected CompressionMethod compressionMethod = CompressionMethod.DEFLATE;

    protected final CRC32 crc = new CRC32();
    private final byte[] pendingBuffer = new byte[AesEngine.AES_BLOCK_SIZE];
    private int pendingBufferLength;
    protected long totalBytesRead;

    public void putNextEntry(@NonNull PathZipEntry entry, @NonNull ZipParameters parameters) {
        try {
            int currSplitFileCounter = out.getCurrSplitFileCounter();
            CentralDirectoryBuilder centralDirectoryBuilder = new CentralDirectoryBuilder(entry, parameters, zipModel, currSplitFileCounter);
            fileHeader = centralDirectoryBuilder.createFileHeader();
            localFileHeader = centralDirectoryBuilder.createLocalFileHeader(fileHeader);

            if (zipModel.isSplitArchive() && zipModel.isEmpty())
                out.writeDword(InternalZipConstants.SPLITSIG);

            fileHeader.setOffsLocalFileHeader(out.getFilePointer());
            new LocalFileHeaderWriter(localFileHeader, zipModel).write(out);

            encoder = parameters.getEncryption().encoder(localFileHeader, parameters);
            encryption = parameters.getEncryption();
            compressionMethod = parameters.getCompressionMethod();

            out.mark(MARK);

            encoder.write(out);
            crc.reset();
        } catch(ZipException e) {
            throw e;
        } catch(Exception e) {
            throw new ZipException(e);
        }
    }

    @Override
    public void write(int bval) throws IOException {
        byte[] b = new byte[1];
        b[0] = (byte)bval;
        write(b, 0, 1);
    }

    @Override
    public void write(byte[] b) throws IOException {
        if (ArrayUtils.isNotEmpty(b))
            write(b, 0, b.length);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        if (len == 0)
            return;

        if (encryption == Encryption.AES) {
            if (pendingBufferLength != 0) {
                if (len >= (AesEngine.AES_BLOCK_SIZE - pendingBufferLength)) {
                    System.arraycopy(b, off, pendingBuffer, pendingBufferLength,
                            AesEngine.AES_BLOCK_SIZE - pendingBufferLength);
                    encryptAndWrite(pendingBuffer, 0, pendingBuffer.length);
                    off = AesEngine.AES_BLOCK_SIZE - pendingBufferLength;
                    len -= off;
                    pendingBufferLength = 0;
                } else {
                    System.arraycopy(b, off, pendingBuffer, pendingBufferLength,
                            len);
                    pendingBufferLength += len;
                    return;
                }
            }

            if (len % 16 != 0) {
                System.arraycopy(b, (len + off) - (len % 16), pendingBuffer, 0, len % 16);
                pendingBufferLength = len % 16;
                len -= pendingBufferLength;
            }
        }

        if (len != 0)
            encryptAndWrite(b, off, len);
    }

    private void encryptAndWrite(byte[] buf, int offs, int len) throws IOException {
        encoder.encode(buf, offs, len);
        out.writeBytes(buf, offs, len);
    }

    public void closeEntry() throws IOException {
        if (pendingBufferLength != 0) {
            encryptAndWrite(pendingBuffer, 0, pendingBufferLength);
            pendingBufferLength = 0;
        }

        encoder.closeEntry(out);

        fileHeader.setCrc32(fileHeader.getEncryption() == Encryption.AES ? 0 : crc.getValue());
        fileHeader.setCompressedSize(out.getWrittenBytesAmount(MARK));
        fileHeader.setUncompressedSize(totalBytesRead);
        zipModel.addFileHeader(fileHeader);

        writeDataDescriptor();

        crc.reset();
        out.mark(MARK);
        encoder = Encoder.NULL;
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

        new DataDescriptorWriter(dataDescriptor).write(out);
    }

    @Override
    public void close() throws IOException {
        zipModel.getEndCentralDirectory().setOffs(out.getOffs());
        new ZipModelWriter(zipModel).finalizeZipFile(out, true);
        out.close();
    }

    public void seek(long pos) throws IOException {
        out.seek(pos);
    }
}
