package ru.olegcherednik.zip4jvm.io.in.data;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import ru.olegcherednik.zip4jvm.io.Endianness;
import ru.olegcherednik.zip4jvm.model.src.SrcZip;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 18.12.2022
 */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class CommonBaseDataInput extends BaseDataInput {

    protected final DataInput in;

    @Override
    public long getAbsoluteOffs() {
        return in.getAbsoluteOffs();
    }

    @Override
    public long convertToAbsoluteOffs(int diskNo, long relativeOffs) {
        return in.convertToAbsoluteOffs(diskNo, relativeOffs);
    }

    @Override
    public long getDiskRelativeOffs() {
        return in.getDiskRelativeOffs();
    }

    @Override
    public SrcZip getSrcZip() {
        return in.getSrcZip();
    }

    @Override
    public SrcZip.Disk getDisk() {
        return in.getDisk();
    }

    @Override
    public long size() throws IOException {
        return in.size();
    }

    @Override
    public int read(byte[] buf, int offs, int len) throws IOException {
        return in.read(buf, offs, len);
    }

    @Override
    public Endianness getEndianness() {
        return in.getEndianness();
    }

    @Override
    public void seek(int diskNo, long relativeOffs) throws IOException {
        in.seek(diskNo, relativeOffs);
    }

    @Override
    public void close() throws IOException {
        in.close();
    }

    @Override
    public long skip(long bytes) throws IOException {
        return in.skip(bytes);
    }

    @Override
    public void seek(long absoluteOffs) throws IOException {
        in.seek(absoluteOffs);
    }

    @Override
    public String toString() {
        return in.toString();
    }

}
