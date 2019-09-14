package ru.olegcherednik.zip4jvm.io.out;

import lombok.NonNull;
import ru.olegcherednik.zip4jvm.exception.Zip4jException;
import ru.olegcherednik.zip4jvm.model.ZipModel;
import ru.olegcherednik.zip4jvm.utils.ZipUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Oleg Cherednik
 * @since 03.08.2019
 */
abstract class BaseDataOutput implements DataOutput {

    private final Map<String, Long> map = new HashMap<>();

    private long tic;

    @NonNull
    protected final ZipModel zipModel;
    @NonNull
    private DataOutputFile delegate;

    protected BaseDataOutput(@NonNull ZipModel zipModel) throws FileNotFoundException {
        this.zipModel = zipModel;
    }

    protected void createFile(Path zip) throws FileNotFoundException {
        delegate = new LittleEndianWriteFile(zip);
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
    public void writeWord(int val) throws IOException {
        doWithTic(() -> delegate.write(delegate.convertWord(val)));
    }

    @Override
    public void writeDword(long val) throws IOException {
        doWithTic(() -> delegate.write(delegate.convertDword(val)));
    }

    @Override
    public void writeQword(long val) throws IOException {
        doWithTic(() -> delegate.write(delegate.convertQword(val)));
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
    public long getMark(String id) {
        if (map.containsKey(id))
            return map.get(id);

        throw new Zip4jException("Cannot find mark: " + id);
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
        return ZipUtils.toString(getOffs());
    }

    @FunctionalInterface
    private interface Task {

        void apply() throws IOException;
    }

}
