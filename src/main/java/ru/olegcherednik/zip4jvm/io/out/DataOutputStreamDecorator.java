package ru.olegcherednik.zip4jvm.io.out;

import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author Oleg Cherednik
 * @since 03.08.2019
 */
@RequiredArgsConstructor
public final class DataOutputStreamDecorator extends OutputStream {

    private final DataOutput delegate;

    @Override
    public void write(int b) throws IOException {
        delegate.writeBytes((byte)b);
    }

    @Override
    public void write(byte... buf) throws IOException {
        delegate.writeBytes(buf);
    }

    @Override
    public void write(byte[] buf, int offs, int len) throws IOException {
        delegate.write(buf, offs, len);
    }

    @Override
    public void close() throws IOException {
        delegate.close();
    }
}
