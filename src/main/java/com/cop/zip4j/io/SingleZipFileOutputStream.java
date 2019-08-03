package com.cop.zip4j.io;

import com.cop.zip4j.core.writers.ZipModelWriter;
import com.cop.zip4j.model.ZipModel;
import lombok.NonNull;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;

/**
 * This class is responsible for write and correctly close ons single zip file or single part of split zip file.
 *
 * @author Oleg Cherednik
 * @since 08.03.2019
 */
public final class SingleZipFileOutputStream extends OutputStream implements MarkDataOutput {

    @NonNull
    private final ZipModel zipModel;
    @NonNull
    private final MarkDataOutput delegate;

    @NonNull
    public static SingleZipFileOutputStream create(@NonNull ZipModel zipModel) throws FileNotFoundException {
        return new SingleZipFileOutputStream(zipModel.getZipFile(), zipModel);
    }

    @NonNull
    public static SingleZipFileOutputStream create(@NonNull Path zipFile, @NonNull ZipModel zipModel) throws FileNotFoundException {
        return new SingleZipFileOutputStream(zipFile, zipModel);
    }

    private SingleZipFileOutputStream(@NonNull Path zipFile, @NonNull ZipModel zipModel) throws FileNotFoundException {
        delegate = new MarkDataOutputDecorator(new LittleEndianWriteFile(zipFile));
        this.zipModel = zipModel;
    }

    @Override
    public void close() throws IOException {
        zipModel.getEndCentralDirectory().setOffs(getOffs());
        new ZipModelWriter(zipModel).finalizeZipFile(this, true);
        delegate.close();
    }


    // -----------------------------------

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
    public void write(int b) throws IOException {
        write(new byte[] { (byte)b }, 0, 1);
    }

    @Override
    public void write(byte[] buf, int offs, int len) throws IOException {
        delegate.write(buf, offs, len);
    }

    @Override
    public int getCounter() {
        return 0;
    }

    @Override
    public void mark(String id) {
        delegate.mark(id);
    }

    @Override
    public long getWrittenBytesAmount(String id) {
        return delegate.getWrittenBytesAmount(id);
    }
}
