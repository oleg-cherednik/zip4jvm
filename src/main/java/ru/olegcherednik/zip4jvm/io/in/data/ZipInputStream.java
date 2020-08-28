package ru.olegcherednik.zip4jvm.io.in.data;

import lombok.Getter;
import lombok.Setter;
import ru.olegcherednik.zip4jvm.io.in.file.DataInputFile;
import ru.olegcherednik.zip4jvm.io.in.file.LittleEndianDataInputFile;
import ru.olegcherednik.zip4jvm.model.ZipModel;
import ru.olegcherednik.zip4jvm.model.src.SrcZip;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 12.12.2019
 */
@Getter
public class ZipInputStream extends BaseDataInput implements ZipDataInput {

    private final ZipModel zipModel;
    private final DataInputFile delegate;
    @Setter
    private String fileName;

    public ZipInputStream(SrcZip srcZip) throws IOException {
        this(null, srcZip);
    }

    public ZipInputStream(ZipModel zipModel) throws IOException {
        this(zipModel, zipModel.getSrcZip());
    }

    private ZipInputStream(ZipModel zipModel, SrcZip srcZip) throws IOException {
        this.zipModel = zipModel;
        delegate = new LittleEndianDataInputFile(srcZip);
        fileName = srcZip.getPath().getFileName().toString();
    }

    @Override
    public long getAbsoluteOffs() {
        return delegate.getAbsoluteOffs();
    }

    @Override
    public long convertToAbsoluteOffs(int disk, long relOffs) {
        return delegate.toAbsOffs(disk, relOffs);
    }

    @Override
    public long getDiskRelativeOffs() {
        return delegate.getDiskRelativeOffs();
    }

    @Override
    public SrcZip.Disk getDisk() {
        return delegate.getDisk();
    }

    @Override
    public long length() throws IOException {
        return delegate.length();
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
    public void seek(int disk, long relativeOffs) throws IOException {
        delegate.seek(disk, relativeOffs);
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
    public SrcZip getSrcFile() {
        return delegate.getSrcZip();
    }

    @Override
    public String getFileName() {
        return delegate.getDisk().getFile().getFileName().toString();
    }

    @Override
    public String toString() {
        return delegate.toString();
    }

}