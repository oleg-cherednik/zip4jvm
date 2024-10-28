package ru.olegcherednik.zip4jvm.io.out.entry;

import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author Oleg Cherednik
 * @since 28.10.2024
 */
@RequiredArgsConstructor
public class SequenceOutputStream extends OutputStream {

    private final OutputStream os;

    @Override
    public void write(int b) throws IOException {
        os.write(b);
    }

    @Override
    public void write(byte[] buf, int offs, int len) throws IOException {
        os.write(buf, offs, len);
    }

    @Override
    public void close() throws IOException {
        os.close();
    }
}
