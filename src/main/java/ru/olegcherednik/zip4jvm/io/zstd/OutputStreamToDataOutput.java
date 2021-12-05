package ru.olegcherednik.zip4jvm.io.zstd;

import lombok.RequiredArgsConstructor;
import ru.olegcherednik.zip4jvm.io.out.data.DataOutput;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author Oleg Cherednik
 * @since 05.12.2021
 */
@RequiredArgsConstructor
final class OutputStreamToDataOutput extends OutputStream {

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
    public void close() throws IOException {
        out.close();
    }

}
