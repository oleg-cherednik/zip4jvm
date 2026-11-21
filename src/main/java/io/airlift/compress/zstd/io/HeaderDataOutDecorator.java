package io.airlift.compress.zstd.io;

import lombok.Getter;
import lombok.Setter;

/**
 * This decorator provides ability to write <tt>header</tt> and <tt>data</tt>
 * parts in the opposite way. E.g. <tt>data</tt> part first (that comes after
 * <tt>header</tt>) in the stream, and then <tt>header</tt> itself.
 * <p>
 * After all, the offs of the stream will be set after the <tt>data</tt> part.
 * <ol>
 * This is the sequence of using this decorator:
 * <li>setHeaderSize()</li>
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

    private final RandomAccess rao;
    @Getter
    private final int startOffs;
    @Setter
    @Getter
    private int headerSize = -1;
    private int dataSize = -1;

    public HeaderDataOutDecorator(RandomAccess rao) {
        this.rao = rao;
        startOffs = rao.getOffs();
    }

    public RandomAccess getOutForData() {
        assert headerSize > 0;
        rao.setOffs(startOffs + headerSize);
        return rao;
    }

    public RandomAccess getOutForHeader() {
        rao.setOffs(startOffs);
        return rao;
    }

    public void saveDataSize() {
        assert headerSize > 0;
        dataSize = Math.max(rao.getOffs() - startOffs - headerSize, 0);
    }

    public int saveAndGetDataSize() {
        saveDataSize();
        return dataSize;
    }

    public void reset() {
        headerSize = -1;
        dataSize = -1;
        rao.setOffs(startOffs);
    }

    public void close() {
        assert headerSize >= 0;
        assert dataSize >= 0;
        rao.setOffs(startOffs + headerSize + dataSize);
    }

}
