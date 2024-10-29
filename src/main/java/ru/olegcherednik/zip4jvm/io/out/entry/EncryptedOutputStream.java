package ru.olegcherednik.zip4jvm.io.out.entry;

import ru.olegcherednik.zip4jvm.io.out.data.EncoderDataOutput;

import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.io.OutputStream;

/**
 * This is an output stream decorator, that is responsible for data encryption.
 *
 * @author Oleg Cherednik
 * @since 29.10.2024
 */
@RequiredArgsConstructor
public class EncryptedOutputStream extends OutputStream {

    private final EncoderDataOutput encoderDataOutput;
    private final OutputStream delegate;

    @Override
    public final void write(int b) throws IOException {
        delegate.write(b);
    }

    @Override
    public void write(byte[] buf, int offs, int len) throws IOException {
        delegate.write(buf, offs, len);
    }

    @Override
    public void close() throws IOException {
        delegate.close();
        encoderDataOutput.encodingAccomplished();
    }

    @Override
    public String toString() {
        return delegate.toString();
    }

}
