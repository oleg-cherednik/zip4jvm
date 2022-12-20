package ru.olegcherednik.zip4jvm.io.in.data;

import lombok.RequiredArgsConstructor;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 20.12.2022
 */
@RequiredArgsConstructor
public class DataInputNewDecorator extends BaseDataInputNew {

    private final DataInput in;

    @Override
    public long skip(long bytes) throws IOException {
        return in.skip(bytes);
    }

    @Override
    public void seek(long absoluteOffs) throws IOException {
        in.seek(absoluteOffs);
    }

    @Override
    public long getAbsoluteOffs() {
        return in.getAbsoluteOffs();
    }

    @Override
    public int read(byte[] buf, int offs, int len) throws IOException {
        return in.read(buf, offs, len);
    }

    @Override
    public long toLong(byte[] buf, int offs, int len) {
        return in.toLong(buf, offs, len);
    }
}
