package ru.olegcherednik.zip4jvm.io.in;

import lombok.Getter;
import lombok.Setter;
import ru.olegcherednik.zip4jvm.model.ZipModel;

import java.io.IOException;
import java.nio.file.Path;

/**
 * @author Oleg Cherednik
 * @since 12.12.2019
 */
@Getter
public abstract class BaseZipDataInput extends BaseDataInput implements ZipDataInput {

    protected final ZipModel zipModel;
    @Setter
    protected String fileName;
    protected DataInputFile delegate;

    protected BaseZipDataInput(ZipModel zipModel, Path file) throws IOException {
        this.zipModel = zipModel;
        createDelegate(file);
    }

    protected final void createDelegate(Path file) throws IOException {
        if (delegate != null)
            delegate.close();

        delegate = new LittleEndianReadFile(file);
        fileName = file.getFileName().toString();
    }

    @Override
    public long getTotalDisks() {
        return zipModel.getTotalDisks();
    }

    @Override
    public long getOffs() {
        return delegate.getOffs();
    }

    @Override
    protected final long convert(byte[] buf, int offs, int len) {
        return delegate.convert(buf, offs, len);
    }

    @Override
    public long length() throws IOException {
        return delegate.length();
    }

    @Override
    public void seek(long pos) throws IOException {
        delegate.seek(pos);
    }

    @Override
    public void close() throws IOException {
        delegate.close();
    }

    @Override
    public String toString() {
        return delegate.toString();
    }

}
