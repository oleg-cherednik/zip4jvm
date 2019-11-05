package ru.olegcherednik.zip4jvm.io.in;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;

/**
 * @author Oleg Cherednik
 * @since 04.08.2019
 */
public class SingleZipInputStream extends BaseDataInput {

    public SingleZipInputStream(Path zip) throws FileNotFoundException {
        delegate = new LittleEndianReadFile(zip);
    }

    @Override
    public int read(byte[] buf, int offs, int len) throws IOException {
        int res = delegate.read(buf, offs, len);
        cycleBuffer.write(buf, offs, len);
        return res;
    }

    @Override
    public void skip(long bytes) throws IOException {
        while (bytes > 0) {
            int actual = delegate.skip((int)Math.min(Integer.MAX_VALUE, bytes));
            bytes -= actual;

            if (actual == 0)
                break;
        }
    }

    @Override
    public void skip(long bytes) throws IOException {
        while (bytes > 0) {
            int actual = delegate.skip((int)Math.min(Integer.MAX_VALUE, bytes));
            bytes -= actual;

            if (actual == 0)
                break;
        }
    }

}
