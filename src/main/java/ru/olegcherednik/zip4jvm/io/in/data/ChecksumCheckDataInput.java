package ru.olegcherednik.zip4jvm.io.in.data;

import ru.olegcherednik.zip4jvm.exception.Zip4jvmException;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;

import org.apache.commons.codec.digest.PureJavaCrc32;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.util.zip.Checksum;

/**
 * @author Oleg Cherednik
 * @since 15.11.2024
 */
public class ChecksumCheckDataInput extends BaseDataInput {

    private final long expectedCrc32;
    private final String fileName;
    private final Checksum crc32 = new PureJavaCrc32();

    public static ChecksumCheckDataInput checksum(ZipEntry zipEntry, DataInput in) {
        return new ChecksumCheckDataInput(zipEntry.getChecksum(), zipEntry.getFileName(), in);
    }

    protected ChecksumCheckDataInput(long expectedCrc32, String fileName, DataInput in) {
        super(in);
        this.expectedCrc32 = expectedCrc32;
        this.fileName = fileName;
    }

    // ---------- InputStream ----------

    @Override
    public int read(byte[] buf, int offs, int len) throws IOException {
        int readNow = super.read(buf, offs, len);

        if (readNow != IOUtils.EOF)
            crc32.update(buf, offs, readNow);

        return readNow;
    }

    // ---------- AutoCloseable ----------

    @Override
    public void close() throws IOException {
        long actual = crc32.getValue();

        if (expectedCrc32 > 0 && expectedCrc32 != actual)
            throw new Zip4jvmException("Checksum is not matched: " + fileName);

        super.close();
    }

}