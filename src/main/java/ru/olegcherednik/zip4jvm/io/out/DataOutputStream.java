package ru.olegcherednik.zip4jvm.io.out;

import ru.olegcherednik.zip4jvm.io.out.data.DataOutput;

import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author Oleg Cherednik
 * @since 29.10.2024
 */
@RequiredArgsConstructor
public class DataOutputStream extends OutputStream {

    private final DataOutput out;

    @Override
    public void write(int val) throws IOException {
        out.writeByte(val);
    }

    @Override
    public void write(byte[] buf, int offs, int len) throws IOException {
        out.write(buf, offs, len);
    }

    @Override
    public void flush() throws IOException {
        out.flush();
    }

    @Override
    public void close() throws IOException {
        out.close();
    }

}
