package io.airlift.compress.zstd;

import ru.olegcherednik.zip4jvm.io.in.DataInput;

import lombok.RequiredArgsConstructor;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 27.10.2026
 */
@RequiredArgsConstructor
public final class DataInputToIn implements In {

    private final DataInput in;

    @Override
    public int readByte() {
        return in.readByte();
    }

    @Override
    public int readWord() {
        return in.readWord();
    }

    @Override
    public long readDword() {
        return in.readDword();
    }

    @Override
    public long readQword() {
        return in.readQword();
    }

    @Override
    public long getAbsOffs() {
        return in.getAbsOffs();
    }

    @Override
    public int available() throws IOException {
        return (int) in.available();
    }

    @Override
    public int read(byte[] buf) {
        return in.read(buf, 0, buf.length);
    }

    @Override
    public int read(byte[] buf, int offs, int len) {
        return in.read(buf, offs, len);
    }

}
