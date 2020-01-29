package ru.olegcherednik.zip4jvm.io.in.entry;

import org.apache.commons.io.IOUtils;
import ru.olegcherednik.zip4jvm.crypto.Decoder;
import ru.olegcherednik.zip4jvm.exception.Zip4jvmException;
import ru.olegcherednik.zip4jvm.io.in.data.DataInput;
import ru.olegcherednik.zip4jvm.io.readers.DataDescriptorReader;
import ru.olegcherednik.zip4jvm.io.readers.LocalFileHeaderReader;
import ru.olegcherednik.zip4jvm.model.Compression;
import ru.olegcherednik.zip4jvm.model.DataDescriptor;
import ru.olegcherednik.zip4jvm.model.LocalFileHeader;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.function.Function;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

/**
 * @author Oleg Cherednik
 * @since 04.08.2019
 */
public abstract class EntryInputStream extends InputStream {

    protected final ZipEntry zipEntry;
    protected final DataInput in;
    protected final Decoder decoder;

    protected final long compressedSize;
    protected final long uncompressedSize;

    private final Checksum checksum = new CRC32();
    private final byte[] buf = new byte[1];

    protected long readCompressedBytes;
    protected long writtenUncompressedBytes;

    public static InputStream create(ZipEntry zipEntry, Function<Charset, Charset> charsetCustomizer, DataInput in) throws IOException {
        LocalFileHeader localFileHeader = new LocalFileHeaderReader(zipEntry.getLocalFileHeaderOffs(), charsetCustomizer).read(in);
        // TODO check why do I use Supplier here
        zipEntry.setDataDescriptorAvailable(() -> localFileHeader.getGeneralPurposeFlag().isDataDescriptorAvailable());
        // TODO check that localFileHeader matches fileHeader
        Decoder decoder = zipEntry.getEncryption().getCreateDecoder().apply(zipEntry, in);
        Compression compression = zipEntry.getCompression();

        if (compression == Compression.STORE)
            return new StoreEntryInputStream(zipEntry, in, decoder);
        if (compression == Compression.DEFLATE)
            return new InflateEntryInputStream(zipEntry, in, decoder);

        throw new Zip4jvmException("Compression is not supported: " + compression);
    }

    protected EntryInputStream(ZipEntry zipEntry, DataInput in, Decoder decoder) {
        this.zipEntry = zipEntry;
        this.in = in;
        this.decoder = decoder;
        compressedSize = Math.max(0, decoder.getDataCompressedSize(zipEntry.getCompressedSize()));
        uncompressedSize = Math.max(0, zipEntry.getUncompressedSize());
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
        in.close();
    }

    private void checkChecksum() {
        long expected = zipEntry.getChecksum();
        long actual = checksum.getValue();

        if (expected > 0 && expected != actual)
            throw new Zip4jvmException("Checksum is not matched: " + zipEntry.getFileName());
    }

    private void checkUncompressedSize() {
        if (uncompressedSize != writtenUncompressedBytes)
            throw new Zip4jvmException("UncompressedSize is not matched: " + zipEntry.getFileName());
    }

    /** Just read {@link DataDescriptor} and ignore it's value. We got it from {@link ru.olegcherednik.zip4jvm.model.CentralDirectory.FileHeader} */
    @SuppressWarnings("UnnecessaryFullyQualifiedName")
    private void readDataDescriptor() throws IOException {
        if (zipEntry.isDataDescriptorAvailable())
            DataDescriptorReader.get(zipEntry.isZip64()).read(in);
    }

    @Override
    public String toString() {
        return in.toString();
    }

}
