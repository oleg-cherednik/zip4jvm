package ru.olegcherednik.zip4jvm.io.out;

import ru.olegcherednik.zip4jvm.io.AbstractMarker;
import ru.olegcherednik.zip4jvm.model.ZipModel;

import java.io.IOException;
import java.nio.file.Path;

/**
 * @author Oleg Cherednik
 * @since 03.08.2019
 */
abstract class BaseDataOutput extends AbstractMarker implements DataOutput {

    private static final int OFFS_WORD = 1;
    private static final int OFFS_DWORD = 3;
    private static final int OFFS_QWORD = 7;

    private static final ThreadLocal<byte[]> THREAD_LOCAL_BUF = ThreadLocal.withInitial(() -> new byte[15]);

    protected final ZipModel zipModel;
    private DataOutputFile delegate;

    protected BaseDataOutput(ZipModel zipModel) throws IOException {
        this.zipModel = zipModel;
        createFile(zipModel.getFile());
    }

    protected void createFile(Path zip) throws IOException {
        delegate = new LittleEndianWriteFile(zip);
    }

    @Override
    public final long getOffs() {
        return delegate.getOffs();
    }

    @Override
    public void writeWord(int val) throws IOException {
        convertAndWrite(val, OFFS_WORD, 2);
    }

    @Override
    public void writeDword(long val) throws IOException {
        convertAndWrite(val, OFFS_DWORD, 4);
    }

    @Override
    public void writeQword(long val) throws IOException {
        convertAndWrite(val, OFFS_QWORD, 8);
    }

    private void convertAndWrite(long val, int offs, int len) throws IOException {
        byte[] buf = THREAD_LOCAL_BUF.get();
        delegate.convert(val, buf, offs, len);
        write(buf, offs, len);
    }

    @Override
    public void write(byte[] buf, int offs, int len) throws IOException {
        long offsFrom = getOffs();
        delegate.write(buf, offs, len);
        incTic(getOffs() - offsFrom);
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
