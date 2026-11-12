package io.airlift.compress.zstd.bis;

import io.airlift.compress.zstd.OutByteArrayWithOffs;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Oleg Cherednik
 * @since 12.11.2026
 */
public final class HeaderDataOutDecorator {

    private final OutByteArrayWithOffs delegate;
    @Getter
    private final int startOffs;
    @Setter
    private int headerSize = -1;
    private int dataSize = -1;

    public HeaderDataOutDecorator(OutByteArrayWithOffs delegate) {
        this.delegate = delegate;
        startOffs = delegate.getOffs();
    }

    public OutByteArrayWithOffs getOutForData() {
        assert headerSize > 0;
        delegate.setOffs(startOffs + headerSize);
        return delegate;
    }

    public OutByteArrayWithOffs getOutForHeader() {
        delegate.setOffs(startOffs);
        return delegate;
    }

    public int saveAndGetDataSize() {
        assert headerSize > 0;
        dataSize = delegate.getOffs() - startOffs - headerSize;
        return dataSize;
    }

    public void close() {
        assert headerSize > 0;
        assert dataSize > 0;
        delegate.setOffs(startOffs + headerSize + dataSize);
    }

}
