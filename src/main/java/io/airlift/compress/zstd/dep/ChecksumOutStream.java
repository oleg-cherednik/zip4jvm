package io.airlift.compress.zstd.dep;

import io.airlift.compress.zstd.ByteArrayWithOffs;
import io.airlift.compress.zstd.compress.XXHash64;
import lombok.RequiredArgsConstructor;

/**
 * @author Oleg Cherednik
 * @since 27.10.2026
 */
@RequiredArgsConstructor
public final class ChecksumOutStream implements OutStream {

    private final OutStream delegate;
    private final XXHash64 checksum = new XXHash64();

    public long getChecksum() {
        return checksum.getValue();
    }

    @Override
    public int available() {
        return delegate.available();
    }

    @Override
    public int getOffs() {
        return delegate.getOffs();
    }

    @Override
    public void putByte(byte x) {
        checksum.update(x);
        delegate.putByte(x);
    }

    @Override
    public void copyMemory(ByteArrayWithOffs in, int bytes) {
        byte[] buf = new byte[bytes];
        int nowRead = in.read(buf, 0, buf.length);
        checksum.update(buf, 0, nowRead);
        delegate.copyMemory(new ByteArrayWithOffs(buf), nowRead);
    }

}
