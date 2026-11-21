package io.airlift.compress.zstd.io;

import io.airlift.compress.zstd.In;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;

import static io.airlift.compress.zstd.model.Constants.SIZE_OF_BYTE;
import static io.airlift.compress.zstd.model.Constants.SIZE_OF_DWORD;
import static io.airlift.compress.zstd.model.Constants.SIZE_OF_QWORD;

/**
 * Sliding input window for block compression: history ({@code <= windowSize}) followed by the current block
 * ({@code <= blockMaxSize}). Filled sequentially from {@link In}; match finders use random access inside
 * {@link #buf}.
 *
 * <pre>
 * buf = [ history (&lt;= Window_Size) | current block (&lt;= Block_Maximum_Size) ]
 *                                   ^ blockStart                         ^ pos
 * </pre>
 *
 * @author Oleg Cherednik
 * @since 17.11.2026
 */
@Getter
public final class ByteArrayRandomAccessIn {

    private final byte[] buf;
    private final int windowSize;
    private final int blockMaxSize;

    /** End of valid data / next fill index. */
    private int pos;
    /** Start of the current block inside {@link #buf}. */
    private int blockStart;

    /** Cursor for {@link #getByte()} / {@link #getInt()} / {@link #getLong()}. */
    @Setter
    private int offs;
    /** Bytes in the current block ({@code limit - blockStart}). */
    @Setter
    private int size;
    /** End of the current block ({@code blockStart + size}). */
    @Setter
    private int limit;

    public ByteArrayRandomAccessIn(int windowSize, int blockMaxSize) {
        this.windowSize = windowSize;
        this.blockMaxSize = blockMaxSize;
        buf = new byte[windowSize + blockMaxSize];
    }

    /**
     * When there is no room for the next block, keep only the last {@code windowSize} bytes. Returns the slide
     * delta (0 if no slide), so hash tables can be rebased.
     */
    public int slide() {
        if (pos + blockMaxSize <= buf.length || pos <= windowSize)
            return 0;

        int delta = pos - windowSize;
        System.arraycopy(buf, delta, buf, 0, windowSize);
        pos = windowSize;
        blockStart = Math.max(0, blockStart - delta);
        offs = Math.max(0, offs - delta);
        limit = Math.max(0, limit - delta);
        return delta;
    }

    /**
     * Appends up to {@code want} bytes from {@code in} at {@link #pos}. Sets the current block view to the newly
     * read range. Returns the number of bytes read (0 at EOF).
     */
    public int fill(In in, int want) throws IOException {
        int toRead = Math.min(want, buf.length - pos);
        if (toRead <= 0)
            return 0;

        int read = in.read(buf, pos, toRead);
        if (read <= 0)
            return 0;

        blockStart = pos;
        pos += read;
        offs = blockStart;
        size = read;
        limit = blockStart + read;
        return read;
    }

    public int available() {
        return limit - offs;
    }

    public int getByte() {
        int res = buf[offs] & 0xFF;
        offs += SIZE_OF_BYTE;
        return res;
    }

    public int getInt() {
        long val = 0;

        for (int i = 0; i < SIZE_OF_DWORD; i++)
            val = ((long) (buf[offs + i] & 0xFF) << 8 * i) | val;

        offs += SIZE_OF_DWORD;
        return (int) val;
    }

    public long getLong() {
        long val = 0;

        for (int i = 0; i < SIZE_OF_QWORD; i++)
            val = ((long) (buf[offs + i] & 0xFF) << 8 * i) | val;

        offs += SIZE_OF_QWORD;
        return val;
    }

    @Override
    public String toString() {
        return String.format("pos: %s, blockStart: %s, offs: %s, size: %s, limit: %s",
                             pos, blockStart, offs, size, limit);
    }

}
