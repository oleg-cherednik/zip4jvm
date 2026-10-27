package io.airlift.compress.zstd.compress;

import io.airlift.compress.zstd.FrameHeader;
import org.apache.commons.lang3.ArrayUtils;

import static io.airlift.compress.zstd.Constants.MAX_BLOCK_SIZE;

/**
 * @author Oleg Cherednik
 * @since 27.10.2026
 */
public class SlideWindow {

    private int windowSize;
    private int blockMaxSize;
    private int windowCapacity;
    private int windowPos;
    private byte[] window = ArrayUtils.EMPTY_BYTE_ARRAY;

    public void init(FrameHeader frameHeader) {
        windowSize = frameHeader.getWindowSize();
        blockMaxSize = Math.min(windowSize, MAX_BLOCK_SIZE);

        long contentSize = frameHeader.getContentSize();

        // the whole frame fits into the window - it is never slid;
        // otherwise there is a room for one more window to
        // slide it not after every block

        if (contentSize == FrameHeader.SIZE_UNKNOWN || contentSize > windowSize)
            windowCapacity = 2 * windowSize + blockMaxSize;
        else
            windowCapacity = (int) contentSize;

        if (window.length < windowCapacity)
            window = new byte[windowCapacity];

        windowPos = 0;
    }

    /**
     * Copies {@code length} bytes from {@code src} to {@code dst} within the {@link #window} (LZ77 match). The source
     * may overlap the destination ({@code dst - src < length}): then the output repeats with period
     * {@code dst - src}.
     */
    public void copyMatch(int src, int dst, int length) {
        int remaining = length;

        while (remaining > 0) {
            // [src, dst) is already written and never overlaps [dst, dst + bytes); for overlapped match the
            // distance doubles on every iteration as the copied part is a repetition of the source pattern
            int bytes = Math.min(dst - src, remaining);
            System.arraycopy(window, src, window, dst, bytes);
            dst += bytes;
            remaining -= bytes;
        }
    }

}
