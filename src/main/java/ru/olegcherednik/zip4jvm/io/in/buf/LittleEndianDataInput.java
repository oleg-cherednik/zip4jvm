package ru.olegcherednik.zip4jvm.io.in.buf;

import lombok.RequiredArgsConstructor;
import ru.olegcherednik.zip4jvm.io.in.data.BaseDataInput;
import ru.olegcherednik.zip4jvm.model.src.SrcZip;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 09.12.2022
 */
@RequiredArgsConstructor
public class LittleEndianDataInput extends BaseDataInput {

    private final byte[] buf;
    private final SrcZip srcZip;
    private int offs;

    @Override
    public long getAbsoluteOffs() {
        return offs;
    }

    @Override
    public long convertToAbsoluteOffs(int diskNo, long relativeOffs) {
        return offs;
    }

    @Override
    public long getDiskRelativeOffs() {
        return offs;
    }

    @Override
    public SrcZip getSrcZip() {
        return null;
    }

    @Override
    public SrcZip.Disk getDisk() {
        return srcZip.getDiskByNo(0);
    }

    @Override
    public long size() throws IOException {
        return buf.length - offs;
    }

    @Override
    public int read(byte[] buf, int offs, int len) throws IOException {
        int res = 0;

        for (int i = 0; i < len && this.offs < this.buf.length; i++, this.offs++) {
            buf[offs + i] = this.buf[this.offs];
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
    public void seek(int diskNo, long relativeOffs) throws IOException {
    }

    @Override
    public long skip(long bytes) throws IOException {
        return offs += bytes;
    }

    @Override
    public void seek(long absoluteOffs) throws IOException {
        offs = (int)absoluteOffs;
    }

    @Override
    public void close() throws IOException {
    }
}
