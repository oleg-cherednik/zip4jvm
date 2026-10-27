package io.airlift.compress.zstd.dep;

import io.airlift.compress.zstd.compress.XXHash64;
import lombok.RequiredArgsConstructor;

/**
 * @author Oleg Cherednik
 * @since 27.10.2026
 */
@RequiredArgsConstructor
public final class ChecksumOutStream implements OutStream {

    private final OutStream delegate;
    private final XXHash64 checksum;

    @Override
    public int available() {
        return delegate.available();
    }

    @Override
    public int getOffs() {
        return delegate.getOffs();
    }

    @Override
    public void copyMemory(byte[] buf, int offs, int bytes) {
        checksum.update(buf, offs, bytes);
        delegate.copyMemory(buf, offs, bytes);
    }

}
