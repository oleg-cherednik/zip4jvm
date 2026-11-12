package io.airlift.compress.zstd.bis;

import io.airlift.compress.zstd.OutByteArrayWithOffs;
import lombok.Getter;
import lombok.Setter;

/**
 * <ol>
 * <li>setHeader()</li>
 * <li>getOutForData()</li>
 * <li>saveAndGetDataSize()</li>
 * <li>getOutForHeader()</li>
 * <li>close()</li>
 * </ol>
 *
 * @author Oleg Cherednik
 * @since 12.11.2026
 */
public final class HeaderDataOutDecorator {

    private final OutByteArrayWithOffs delegate;
    @Getter
    private final int startOffs;
    @Setter
    @Getter
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

    public void saveDataSize() {
        assert headerSize > 0;
        dataSize = delegate.getOffs() - startOffs - headerSize;
    }

    public int saveAndGetDataSize() {
        saveDataSize();
        return dataSize;
    }

    public int getOverallSize() {
        assert headerSize > 0;
        assert dataSize > 0;
        return headerSize + dataSize;
    }

    public void reset() {
        headerSize = -1;
        dataSize = -1;
        delegate.setOffs(startOffs);
    }

    public void close() {
        assert headerSize > 0;
        assert dataSize > 0;
        delegate.setOffs(startOffs + headerSize + dataSize);
    }

}
