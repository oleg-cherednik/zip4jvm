package io.airlift.compress.zstd.compress;

import io.airlift.compress.zstd.FrameHeader;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.ArrayUtils;

import static io.airlift.compress.zstd.Constants.MAX_BLOCK_SIZE;

/**
 * Decoded data of the current frame: the history for matches (up to {@code Window_Size} bytes) followed by the
 * current block. The output is write-only, so matches are copied within this buffer and only finished blocks go to
 * the output.
 * <pre>
 * buf = [ history (<= Window_Size) | current block (<= Block_Maximum_Size) | free space ]
 *                                   ^ block start                         ^ pos         ^ capacity
 * </pre>
 *
 * @author Oleg Cherednik
 * @see <a href="https://www.rfc-editor.org/rfc/rfc8878.html#section-3.1.1.1.2">RFC 8878, 3.1.1.1.2.
 *         Window_Descriptor</a>
 * @since 26.10.2026
 */
public final class SlideWindow {

    @Getter
    private byte[] buf = ArrayUtils.EMPTY_BYTE_ARRAY;
    @Getter
    private int capacity;
    // next write position in buf
    @Getter
    @Setter
    private int pos;
    private int windowSize;
    // Block_Maximum_Size = min(Window_Size, 128 KB)
    // https://www.rfc-editor.org/rfc/rfc8878.html#section-3.1.1.2.3
    private int blockMaxSize;

    @Getter
    private int blockStart;

    public void init(FrameHeader frameHeader) {
        windowSize = frameHeader.getWindowSize();
        blockMaxSize = Math.min(windowSize, MAX_BLOCK_SIZE);

        long contentSize = frameHeader.getContentSize();
        // the whole frame fits into the window - it is never slid; otherwise there is a room for one more window to
        // slide it not after every block
        capacity = contentSize != FrameHeader.UNKNOWN_SIZE && contentSize <= windowSize
                   ? (int) contentSize
                   : 2 * windowSize + blockMaxSize;

        if (buf.length < capacity)
            buf = new byte[capacity];

        pos = 0;

        blockStart = 0;
    }

    // when there is no room for the next block, only the last Window_Size bytes are kept (matches cannot refer
    // further back)
    public void slide() {
        if (pos + blockMaxSize <= capacity || pos <= windowSize)
            return;

        System.arraycopy(buf, pos - windowSize, buf, 0, windowSize);
        pos = windowSize;
    }

    // the decoded size of the block started at pos cannot exceed Block_Maximum_Size and the window
    public int getBlockOutputLimit() {
        return Math.min(capacity, pos + blockMaxSize);
    }

    public void incPos(int bytes) {
        pos += bytes;
    }

    /**
     * Copies {@code length} bytes from {@code src} to {@code dst} within the {@link #buf} (LZ77 match). The source
     * may overlap the destination ({@code dst - src < length}): then the output repeats with period
     * {@code dst - src}.
     */
    public void copyMatch(int src, int dst, int length) {
        int remaining = length;

        while (remaining > 0) {
            // [src, dst) is already written and never overlaps [dst, dst + bytes); for overlapped match the
            // distance doubles on every iteration as the copied part is a repetition of the source pattern
            int bytes = Math.min(dst - src, remaining);
            System.arraycopy(buf, src, buf, dst, bytes);
            dst += bytes;
            remaining -= bytes;
        }
    }

    public void markBlockStart() {
        blockStart = pos;
    }

    public int getDecodedSize() {
        return pos - blockStart;
    }

}
