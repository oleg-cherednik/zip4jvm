package com.cop.zip4j.io.in.entry;

import com.cop.zip4j.core.readers.LocalFileHeaderReader;
import com.cop.zip4j.crypto.Decoder;
import com.cop.zip4j.exception.Zip4jException;
import com.cop.zip4j.io.in.DataInput;
import com.cop.zip4j.model.CentralDirectory;
import com.cop.zip4j.model.Compression;
import com.cop.zip4j.model.LocalFileHeader;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

/**
 * @author Oleg Cherednik
 * @since 04.08.2019
 */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class EntryInputStream extends InputStream {

    protected final DataInput in;
    protected final LocalFileHeader localFileHeader;
    protected final Decoder decoder;

    private final Checksum checksum = new CRC32();
    private final byte[] buf = new byte[1];

    protected int readBytes;

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

    protected final void updateChecksum(byte[] buf, int offs, int len) {
        checksum.update(buf, offs, len);
    }

    @Override
    public final int read() throws IOException {
        if (available() == 0)
            return IOUtils.EOF;

        int len = read(buf, 0, 1);

        if (len == IOUtils.EOF)
            return IOUtils.EOF;

        return buf[0] & 0xFF;
    }

    @Override
    public final long skip(long n) throws IOException {
        return super.skip(n);
    }

    @Override
    public void close() throws IOException {
        checkChecksum();
        checkUncompressedSize();
    }

    private void checkChecksum() {
        long expected = localFileHeader.getCrc32();
        long actual = checksum.getValue();

        if (expected != 0 && expected != actual)
            throw new Zip4jException("Checksum is not matched: " + localFileHeader.getFileName());
    }

    private void checkUncompressedSize() {
        long expected = localFileHeader.getUncompressedSize();
        long actual = readBytes;

        if (expected != 0 && expected != actual)
            throw new Zip4jException("UncompressedSize is not matched: " + localFileHeader.getFileName());
    }

}
