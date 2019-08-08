package com.cop.zip4j.io.out;

import com.cop.zip4j.model.ZipModel;
import lombok.NonNull;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Oleg Cherednik
 * @since 03.08.2019
 */
abstract class BaseMarkDataOutput implements MarkDataOutput {

    private final Map<String, Long> map = new HashMap<>();

    private long tic;

    @NonNull
    protected final ZipModel zipModel;
    @NonNull
    private DataOutput delegate;

    protected BaseMarkDataOutput(@NonNull ZipModel zipModel) throws FileNotFoundException {
        this.zipModel = zipModel;
    }

    protected void createFile(Path zipFile) throws FileNotFoundException {
        delegate = new LittleEndianWriteFile(zipFile);
    }

    @Override
    public final void seek(long pos) throws IOException {
        delegate.seek(pos);
    }

    @Override
    public final long getOffs() {
        return delegate.getOffs();
    }

    @Override
    public final void writeWord(int val) throws IOException {
        doWithTic(() -> delegate.writeWord(val));
    }

    @Override
    public final void writeDword(long val) throws IOException {
        doWithTic(() -> delegate.writeDword(val));
    }

    @Override
    public void writeQword(long val) throws IOException {
        doWithTic(() -> delegate.writeQword(val));
    }

    @Override
    public void write(byte[] buf, int offs, int len) throws IOException {
        doWithTic(() -> delegate.write(buf, offs, len));
    }

    private void doWithTic(Task task) throws IOException {
        long offs = getOffs();
        task.apply();
        tic += getOffs() - offs;
    }

    @Override
    public final void mark(String id) {
        map.put(id, tic);
    }

    @Override
    public final long getWrittenBytesAmount(String id) {
        return tic - map.getOrDefault(id, 0L);
    }

    @Override
    public void close() throws IOException {
        delegate.close();
    }

    @Override
    public String toString() {
        return "offs: " + getOffs();
    }

    @FunctionalInterface
    private interface Task {

        void apply() throws IOException;
    }

}
