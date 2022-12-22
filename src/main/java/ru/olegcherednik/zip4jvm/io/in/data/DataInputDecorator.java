package ru.olegcherednik.zip4jvm.io.in.data;

import lombok.RequiredArgsConstructor;
import ru.olegcherednik.zip4jvm.model.src.SrcZip;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 20.12.2022
 */
@RequiredArgsConstructor
public class DataInputDecorator extends BaseDataInput {

    private final DataInputNew in;

    @Override
    public long getAbsoluteOffs() {
        return in.getAbsoluteOffs();
    }

    @Override
    public long convertToAbsoluteOffs(int diskNo, long relativeOffs) {
        return relativeOffs;
    }

    @Override
    public long getDiskRelativeOffs() {
        return in.getAbsoluteOffs();
    }

    @Override
    public SrcZip getSrcZip() {
        return null;
    }

    @Override
    public SrcZip.Disk getDisk() {
        return null;
    }

    @Override
    public long size() throws IOException {
        return 0;
    }

    @Override
    public int read(byte[] buf, int offs, int len) throws IOException {
        return in.read(buf, offs, len);
    }

    @Override
    public long toLong(byte[] buf, int offs, int len) {
        return in.toLong(buf, offs, len);
    }

    @Override
    public void seek(int diskNo, long relativeOffs) throws IOException {
    }

    @Override
    public long skip(long bytes) throws IOException {
        return in.skip(bytes);
    }

    @Override
    public void seek(long absoluteOffs) throws IOException {
        in.seek(absoluteOffs);
    }
}
