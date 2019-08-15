package com.cop.zip4j.io.in.entry;

import com.cop.zip4j.crypto.Decoder;
import com.cop.zip4j.exception.Zip4jException;
import com.cop.zip4j.io.in.DataInput;
import com.cop.zip4j.io.readers.DataDescriptorReader;
import com.cop.zip4j.io.readers.LocalFileHeaderReader;
import com.cop.zip4j.model.CentralDirectory;
import com.cop.zip4j.model.Compression;
import com.cop.zip4j.model.DataDescriptor;
import com.cop.zip4j.model.LocalFileHeader;
import lombok.NonNull;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

/**
 * @author Oleg Cherednik
 * @since 04.08.2019
 */
public abstract class EntryInputStream extends InputStream {

    protected final DataInput in;
    protected final LocalFileHeader localFileHeader;
    protected final Decoder decoder;

    protected final long compressedSize;
    protected final long uncompressedSize;

    private final Checksum checksum = new CRC32();
    private final byte[] buf = new byte[1];

    protected int readCompressedBytes;
    protected long writtenUncompressedBytes;

    public static InputStream create(@NonNull CentralDirectory.FileHeader fileHeader, char[] password, DataInput in) throws IOException {
        LocalFileHeader localFileHeader = new LocalFileHeaderReader(fileHeader).read(in);
        Decoder decoder = localFileHeader.getEncryption().decoder(in, localFileHeader, password);
        Compression compression = fileHeader.getCompression();

        if (compression == Compression.STORE)
            return new StoreEntryInputStream(in, localFileHeader, decoder);
        if (compression == Compression.DEFLATE)
            return new InflateEntryInputStream(in, localFileHeader, decoder);

        throw new Zip4jException("Compression is not supported: " + compression);
    }

    protected EntryInputStream(DataInput in, LocalFileHeader localFileHeader, Decoder decoder) {
        this.in = in;
        this.localFileHeader = localFileHeader;
        this.decoder = decoder;
        compressedSize = decoder.getCompressedSize(localFileHeader);
        uncompressedSize = localFileHeader.getUncompressedSize();
    }

    protected final void updateChecksum(byte[] buf, int offs, int len) {
        checksum.update(buf, offs, len);
    }

    protected long getAvailableCompressedBytes() {
        return Math.max(0, compressedSize - readCompressedBytes);
    }

    @Override
    public int available() {
        return (int)Math.max(0, uncompressedSize - writtenUncompressedBytes);
    }

    @Override
    public final int read() throws IOException {
        int len = read(buf, 0, 1);
        return len == IOUtils.EOF ? IOUtils.EOF : buf[0] & 0xFF;
    }

    @Override
    public final long skip(long n) throws IOException {
        return super.skip(n);
    }

    @Override
    public void close() throws IOException {
        decoder.close(in);
        readDataDescriptor();
        checkChecksum();
        checkUncompressedSize();
    }

    private void checkChecksum() {
        long expected = localFileHeader.getCrc32();
        long actual = checksum.getValue();

        if (expected > 0 && expected != actual)
            throw new Zip4jException("Checksum is not matched: " + localFileHeader.getFileName());
    }

    private void checkUncompressedSize() {
        long expected = localFileHeader.getUncompressedSize();
        long actual = writtenUncompressedBytes;

        if (expected != actual)
            throw new Zip4jException("UncompressedSize is not matched: " + localFileHeader.getFileName());
    }

    private void readDataDescriptor() throws IOException {
        if (localFileHeader.getGeneralPurposeFlag().isDataDescriptorExists()) {
            DataDescriptor dataDescriptor = new DataDescriptorReader(false).read(in);

            localFileHeader.setCrc32(dataDescriptor.getCrc32());
            localFileHeader.setCompressedSize(dataDescriptor.getCompressedSize());
            localFileHeader.setUncompressedSize(dataDescriptor.getUncompressedSize());
        }
    }

}
