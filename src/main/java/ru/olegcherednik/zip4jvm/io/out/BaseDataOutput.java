package ru.olegcherednik.zip4jvm.io.out;

import ru.olegcherednik.zip4jvm.exception.Zip4jvmException;
import ru.olegcherednik.zip4jvm.model.ZipModel;

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

    private static final int OFFS_WORD = 1;
    private static final int OFFS_DWORD = 3;
    private static final int OFFS_QWORD = 7;

    private static final ThreadLocal<byte[]> THREAD_LOCAL_BUF = ThreadLocal.withInitial(() -> new byte[15]);

    private final Map<String, Long> map = new HashMap<>();

    private long tic;

    protected final ZipModel zipModel;
    private DataOutputFile delegate;

    protected BaseDataOutput(ZipModel zipModel) throws FileNotFoundException {
        this.zipModel = zipModel;
        createFile(zipModel.getFile());
    }

    protected void createFile(Path zip) throws FileNotFoundException {
        delegate = new LittleEndianWriteFile(zip);
    }

    @Override
    public final long getOffs() {
        return delegate.getOffs();
    }

    @Override
    public void writeWord(int val) throws IOException {
        doWithTic(() -> convertAndWrite(val, OFFS_WORD, 2));
    }

    @Override
    public void writeDword(long val) throws IOException {
        doWithTic(() -> convertAndWrite(val, OFFS_DWORD, 4));
    }

    @Override
    public void writeQword(long val) throws IOException {
        doWithTic(() -> convertAndWrite(val, OFFS_QWORD, 8));
    }

    private void convertAndWrite(long val, int offs, int len) throws IOException {
        byte[] buf = THREAD_LOCAL_BUF.get();
        delegate.convert(val, buf, offs, len);
        delegate.write(buf, offs, len);
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

        throw new Zip4jvmException("Cannot find mark: " + id);
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
    public final String toString() {
        return delegate.toString();
    }

    @FunctionalInterface
    private interface Task {

        void apply() throws IOException;
    }

}
