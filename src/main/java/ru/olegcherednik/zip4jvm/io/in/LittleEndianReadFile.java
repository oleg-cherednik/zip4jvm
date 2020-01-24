package ru.olegcherednik.zip4jvm.io.in;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.nio.file.Path;

/**
 * @author Oleg Cherednik
 * @since 21.02.2019
 */
public class LittleEndianReadFile extends BaseDataInputFile {

    public LittleEndianReadFile(Path file) throws IOException {
        super(file);
    }

    @Override
    public int skip(int bytes) throws IOException {
        return in.skipBytes(bytes);
    }

    @Override
    public long length() throws IOException {
        return in.length();
    }

    @Override
    public void seek(long pos) throws IOException {
        in.seek(pos);
    }

    @Override
    public int read(byte[] buf, int offs, int len) throws IOException {
        return in.read(buf, offs, len);
    }

    @Override
    public int readSignature() throws IOException {
        // TODO probably it's better to use convert
        int b0 = in.read();
        int b1 = in.read();
        int b2 = in.read();
        int b3 = in.read();
        return b3 << 24 | b2 << 16 | b1 << 8 | b0;
    }

    @Override
    public long getBaseOffs() {
        try {
            return in.getFilePointer();
        } catch(IOException e) {
            return IOUtils.EOF;
        }
    }
}
