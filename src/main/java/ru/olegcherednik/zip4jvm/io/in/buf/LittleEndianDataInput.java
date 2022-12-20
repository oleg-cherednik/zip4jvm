package ru.olegcherednik.zip4jvm.io.in.buf;

import lombok.RequiredArgsConstructor;
import ru.olegcherednik.zip4jvm.io.in.data.BaseDataInputNew;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 09.12.2022
 */
@RequiredArgsConstructor
public class LittleEndianDataInput extends BaseDataInputNew {

    private final byte[] src;
    private int srcOffs;

    @Override
    public long getAbsoluteOffs() {
        return srcOffs;
    }

    @Override
    public int read(byte[] buf, int offs, int len) throws IOException {
        int res = 0;

        for (int i = 0; i < len && srcOffs < src.length; i++, srcOffs++) {
            buf[offs + i] = src[srcOffs];
            res++;
        }

        return res;
    }

    @Override
    public long toLong(byte[] buf, int offs, int len) {
        long res = 0;

        for (int i = offs + len - 1; i >= offs; i--)
            res = res << 8 | buf[i] & 0xFF;

        return res;
    }

    @Override
    public long skip(long bytes) throws IOException {
        return srcOffs += bytes;
    }

    @Override
    public void seek(long absoluteOffs) throws IOException {
        srcOffs = (int)absoluteOffs;
    }

}
