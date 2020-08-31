package ru.olegcherednik.zip4jvm.io.in.data;

import lombok.Getter;
import ru.olegcherednik.zip4jvm.io.in.file.DataInputFile;
import ru.olegcherednik.zip4jvm.io.in.file.LittleEndianDataInputFile;
import ru.olegcherednik.zip4jvm.model.src.SrcZip;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 12.12.2019
 */
@Getter
public class ZipInputStream extends BaseDataInput {

    private final DataInputFile delegate;

    public ZipInputStream(SrcZip srcZip) throws IOException {
        delegate = new LittleEndianDataInputFile(srcZip);
    }

    @Override
    public long getAbsoluteOffs() {
        return delegate.getAbsoluteOffs();
    }

    @Override
    public long convertToAbsoluteOffs(int diskNo, long relativeOffs) {
        return delegate.convertToAbsoluteOffs(diskNo, relativeOffs);
    }

    @Override
    public long getDiskRelativeOffs() {
        return delegate.getDiskRelativeOffs();
    }

    @Override
    public SrcZip getSrcZip() {
        return delegate.getSrcZip();
    }

    @Override
    public SrcZip.Disk getDisk() {
        return delegate.getDisk();
    }

    @Override
    public long size() throws IOException {
        return delegate.size();
    }

    @Override
    public int read(byte[] buf, int offs, int len) throws IOException {
        return delegate.read(buf, offs, len);
    }

    @Override
    public void seek(long absoluteOffs) throws IOException {
        delegate.seek(absoluteOffs);
    }

    @Override
    public void seek(int diskNo, long relativeOffs) throws IOException {
        delegate.seek(diskNo, relativeOffs);
    }

    @Override
    public long skip(long bytes) throws IOException {
        return delegate.skip(bytes);
    }

    @Override
    public void close() throws IOException {
        delegate.close();
    }

    @Override
    public long toLong(byte[] buf, int offs, int len) {
        return delegate.toLong(buf, offs, len);
    }

    @Override
    public String toString() {
        return delegate.toString();
    }

}
