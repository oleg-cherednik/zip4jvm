package ru.olegcherednik.zip4jvm.io.in.data;

import ru.olegcherednik.zip4jvm.io.ByteOrder;
import ru.olegcherednik.zip4jvm.utils.quitely.Quietly;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 15.11.2024
 */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class FooDataInput extends OldBaseDataInput {

    protected final DataInput in;

    // ---------- DataInput ----------

    @Override
    public long getAbsoluteOffs() {
        return in.getAbsoluteOffs();
    }

    @Override
    public long size() {
        return in.size();
    }

    @Override
    public ByteOrder getByteOrder() {
        return in.getByteOrder();
    }

    @Override
    public void seek(long absoluteOffs) {
        in.seek(absoluteOffs);
    }

    @Override
    public int readByte() {
        return Quietly.doQuietly(() -> getByteOrder().readByte(this));
    }

    @Override
    public int readWord() {
        return Quietly.doQuietly(() -> getByteOrder().readWord(this));
    }

    @Override
    public long readDword() {
        return Quietly.doQuietly(() -> getByteOrder().readDword(this));
    }

    @Override
    public long readQword() {
        return Quietly.doQuietly(() -> getByteOrder().readQword(this));
    }

    // ---------- AutoCloseable ----------

    @Override
    public void close() throws IOException {
        in.close();
    }

}
