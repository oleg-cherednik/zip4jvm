package com.cop.zip4j.io;

import lombok.NonNull;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;

/**
 * @author Oleg Cherednik
 * @since 03.08.2019
 */
public abstract class BaseMarkDataOutput implements MarkDataOutput {

    @NonNull
    protected MarkDataOutput delegate;
    @NonNull
    protected Path zipFile;

    protected BaseMarkDataOutput(@NonNull Path zipFile) throws FileNotFoundException {
        createDelegate(zipFile);
    }

    protected final void createDelegate(Path zipFile) throws FileNotFoundException {
        this.zipFile = zipFile;
        delegate = new MarkDataOutputDecorator(new LittleEndianWriteFile(zipFile));
    }

    @Override
    public void seek(long pos) throws IOException {
        delegate.seek(pos);
    }

    @Override
    public long getFilePointer() throws IOException {
        return delegate.getFilePointer();
    }

    @Override
    public void writeDword(int val) throws IOException {
        delegate.writeDword(val);
    }

    @Override
    public void writeDword(long val) throws IOException {
        delegate.writeDword(val);
    }

    @Override
    public void writeWord(int val) throws IOException {
        delegate.writeWord(val);
    }

    @Override
    public void writeQword(long val) throws IOException {
        delegate.writeQword(val);
    }

    @Override
    public void write(byte[] buf, int offs, int len) throws IOException {
        delegate.write(buf, offs, len);
    }

    @Override
    public int getCounter() {
        return delegate.getCounter();
    }

    @Override
    public void mark(String id) {
        delegate.mark(id);
    }

    @Override
    public long getWrittenBytesAmount(String id) {
        return delegate.getWrittenBytesAmount(id);
    }

    @Override
    public void close() throws IOException {
        delegate.close();
    }

    @Override
    public String toString() {
        return "offs: " + getOffs();
    }

}
