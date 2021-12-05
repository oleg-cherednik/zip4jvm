package ru.olegcherednik.zip4jvm.io.zstd;

import lombok.RequiredArgsConstructor;
import ru.olegcherednik.zip4jvm.io.in.data.DataInput;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Oleg Cherednik
 * @since 05.12.2021
 */
@RequiredArgsConstructor
final class DataInputToInputStream extends InputStream {

    private final DataInput in;

    @Override
    public int read() throws IOException {
        return in.readByte();
    }

    @Override
    public int read(byte[] buf, int offs, int len) throws IOException {
        return in.read(buf, offs, len);
    }

    @Override
    public void close() throws IOException {
        in.close();
    }

}
