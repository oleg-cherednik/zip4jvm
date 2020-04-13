package ru.olegcherednik.zip4jvm.io.in.entry;

import org.apache.commons.io.IOUtils;
import ru.olegcherednik.zip4jvm.exception.Zip4jvmException;
import ru.olegcherednik.zip4jvm.io.bzip2.Bzip2InputStream;
import ru.olegcherednik.zip4jvm.io.in.data.DataInput;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 12.04.2020
 */
final class Bzip2EntryInputStream extends EntryInputStream {

    private final Bzip2InputStream bzip;

    public Bzip2EntryInputStream(ZipEntry zipEntry, DataInput in) throws IOException {
        super(zipEntry, in);
        bzip = createInputStream();
    }

    private Bzip2InputStream createInputStream() throws IOException {
        int magicHi = in.readByte();
        int magicLo = in.readByte();
        int version = in.readByte();
        int blockSize100k = in.readByte();

        if (magicHi != 'B' || magicLo != 'Z')
            throw new Zip4jvmException(String.format("BZIP2 magic number is not correct: actual is '%c%c' (expected is 'BZ')",
                    magicHi, magicLo));
        if (version != 'h')
            throw new Zip4jvmException(String.format("BZIP2 version '%c' is not supported: only 'h' is supported", version));
        if (blockSize100k < '1' || blockSize100k > '9')
            throw new Zip4jvmException(String.format("BZIP2 block size is invalid: actual is '%c' (expected between '1' and '9')", blockSize100k));

        return new Bzip2InputStream(in, blockSize100k);
    }

    @Override
    public int read(byte[] buf, int offs, int len) throws IOException {
        len = bzip.read(buf, offs, len);

        if (len == 0 || len == IOUtils.EOF)
            return IOUtils.EOF;

        readCompressedBytes += len;
        writtenUncompressedBytes += len;
        updateChecksum(buf, offs, len);
        return len;
    }
}
