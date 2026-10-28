package ru.olegcherednik.zip4jvm.io.in;

import ru.olegcherednik.zip4jvm.io.in.file.random.RandomAccessDataInput;

import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

/**
 * {@link InputStream} to {@link DataInput} decorator.
 *
 * @author Oleg Cherednik
 * @since 28.10.2026
 */
@RequiredArgsConstructor
public class DataInputToInputStreamDecorator extends InputStream {

    private static final String MARKER = "DataInputToInputStreamDecorator_" + UUID.randomUUID();

    private final DataInput in;

    // ---------- InputStream ----------

    @Override
    public int read() throws IOException {
        return in.readByte();
    }

    @Override
    public int available() throws IOException {
        return (int) in.available();
    }

    @Override
    public boolean markSupported() {
        return in instanceof RandomAccessDataInput;
    }

    @Override
    public void mark(int readLimit) {
        if (markSupported())
            in.mark(MARKER);
        else
            super.mark(readLimit);
    }

    @Override
    public void reset() throws IOException {
        if (markSupported())
            ((RandomAccessDataInput) in).seek(MARKER);
        else
            super.reset();
    }

    // ---------- AutoCloseable ----------

    @Override
    public void close() throws IOException {
        in.close();
    }

}
