package ru.olegcherednik.zip4jvm.io.in;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.nio.file.Path;

/**
 * @author Oleg Cherednik
 * @since 22.01.2020
 */
public abstract class BaseSplitZipInputStream extends BaseZipDataInput {

    protected String fileName;
    protected long disk;

    protected BaseSplitZipInputStream(Zip zip) throws IOException {
        super(zip);
        fileName = zip.getPath().getFileName().toString();
    }

    @Override
    @SuppressWarnings("PMD.AvoidReassigningParameters")
    public int read(byte[] buf, int offs, final int len) throws IOException {
        int res = 0;
        int size = len;

        while (res < len) {
            int total = delegate.read(buf, offs, size);

            if (total > 0)
                res += total;

            if (total == IOUtils.EOF || total < size) {
                openNextDisk();
                offs += Math.max(0, total);
                size -= Math.max(0, total);
            }
        }

        return res;
    }

    @Override
    public void skip(long bytes) throws IOException {
        while (bytes > 0) {
            int expected = (int)Math.min(bytes, Integer.MAX_VALUE);
            int actual = delegate.skip(expected);

            bytes -= actual;

            if (actual < expected)
                openNextDisk();
        }
    }

    protected abstract Path getNextDiskPath();

    private void openNextDisk() throws IOException {
        Path splitFile = getNextDiskPath();
        delegate.close();
        delegate = new LittleEndianReadFile(splitFile);
        fileName = splitFile.getFileName().toString();
    }

    @Override
    public final String toString() {
        return "disk: " + disk + ", " + super.toString();
    }

    @Override
    public long getDisk() {
        return disk;
    }

}
