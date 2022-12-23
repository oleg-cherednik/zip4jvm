package ru.olegcherednik.zip4jvm.io.in.data;

import lombok.RequiredArgsConstructor;
import ru.olegcherednik.zip4jvm.io.Endianness;

/**
 * @author Oleg Cherednik
 * @since 20.12.2022
 */
@RequiredArgsConstructor
public class DataInputNewDecorator extends BaseDataInputNew {

    private final DataInputFile in;

    @Override
    public long skip(long bytes) {
        return in.skip(bytes);
    }

    @Override
    public void seek(long absoluteOffs) {
        in.seek(absoluteOffs);
    }

    @Override
    public long getAbsoluteOffs() {
        return in.getAbsoluteOffs();
    }

    @Override
    public long size() {
        return in.size();
    }

    @Override
    public int read(byte[] buf, int offs, int len) {
        return in.read(buf, offs, len);
    }

    @Override
    public Endianness getEndianness() {
        return in.getEndianness();
    }
}
