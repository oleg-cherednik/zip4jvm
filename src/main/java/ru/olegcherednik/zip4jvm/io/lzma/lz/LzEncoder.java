package ru.olegcherednik.zip4jvm.io.lzma.lz;

import ru.olegcherednik.zip4jvm.io.lzma.LzmaInputStream;

import java.io.IOException;
import java.io.OutputStream;

import static ru.olegcherednik.zip4jvm.io.lzma.LzmaCoder.MATCH_LEN_MAX;

public abstract class LzEncoder {

    /**
     * Number of bytes to keep available before the current byte
     * when moving the LZ window.
     */
    private final int keepSizeBefore;

    /**
     * Number of bytes that must be available, the current byte included,
     * to make hasEnoughData return true. Flushing and finishing are
     * naturally exceptions to this since there cannot be any data after
     * the end of the uncompressed input.
     */
    private final int keepSizeAfter;

    final int niceLen;

    final byte[] buf;

    int readPos = -1;
    private int readLimit = -1;
    private boolean finishing = false;
    private int writePos = 0;
    private int pendingSize = 0;

    public static void normalize(int[] arr, int size, int offs) {
        for (int i = 0; i < size; i++)
            arr[i] = arr[i] <= offs ? 0 : arr[i] - offs;
    }

    /**
     * Gets the size of the LZ window buffer that needs to be allocated.
     */
    private static int getBufSize(int dictSize, int extraSizeAfter) {
        int keepSizeBefore = dictSize;
        int keepSizeAfter = extraSizeAfter + MATCH_LEN_MAX;
        int reserveSize = Math.min(dictSize / 2 + (256 << 10), 512 << 20);
        return keepSizeBefore + keepSizeAfter + reserveSize;
    }

    /**
     * Creates a new LZEncoder. See <code>getInstance</code>.
     */
    protected LzEncoder(LzmaInputStream.Properties properties, int extraSizeAfter) {
        int size = getBufSize(properties.getDictionarySize(), extraSizeAfter);
        buf = new byte[size];

        keepSizeBefore = properties.getDictionarySize();
        keepSizeAfter = extraSizeAfter + MATCH_LEN_MAX;

        niceLen = properties.getNiceLength();
    }

    /**
     * Moves data from the end of the buffer to the beginning, discarding
     * old data and making space for new input.
     */
    private void moveWindow() {
        // Align the move to a multiple of 16 bytes. LZMA2 needs this
        // because it uses the lowest bits from readPos to get the
        // alignment of the uncompressed data.
        int moveOffset = (readPos + 1 - keepSizeBefore) & ~15;
        int moveSize = writePos - moveOffset;
        System.arraycopy(buf, moveOffset, buf, 0, moveSize);

        readPos -= moveOffset;
        readLimit -= moveOffset;
        writePos -= moveOffset;
    }

    /**
     * Copies new data into the LZEncoder's buffer.
     */
    public int fillWindow(byte[] in, int off, int len) {
        assert !finishing;

        // Move the sliding window if needed.
        if (readPos >= buf.length - keepSizeAfter)
            moveWindow();

        // Try to fill the dictionary buffer. If it becomes full,
        // some of the input bytes may be left unused.
        if (len > buf.length - writePos)
            len = buf.length - writePos;

        System.arraycopy(in, off, buf, writePos, len);
        writePos += len;

        // Set the new readLimit but only if there's enough data to allow
        // encoding of at least one more byte.
        if (writePos >= keepSizeAfter)
            readLimit = writePos - keepSizeAfter;

        processPendingBytes();

        // Tell the caller how much input we actually copied into
        // the dictionary.
        return len;
    }

    /**
     * Process pending bytes remaining from preset dictionary initialization
     * or encoder flush operation.
     */
    private void processPendingBytes() {
        // After flushing or setting a preset dictionary there will be
        // pending data that hasn't been ran through the match finder yet.
        // Run it through the match finder now if there is enough new data
        // available (readPos < readLimit) that the encoder may encode at
        // least one more input byte. This way we don't waste any time
        // looping in the match finder (and marking the same bytes as
        // pending again) if the application provides very little new data
        // per write call.
        if (pendingSize > 0 && readPos < readLimit) {
            readPos -= pendingSize;
            int oldPendingSize = pendingSize;
            pendingSize = 0;
            skip(oldPendingSize);
            assert pendingSize < oldPendingSize;
        }
    }

    /**
     * Returns true if at least one byte has already been run through
     * the match finder.
     */
    public boolean isStarted() {
        return readPos != -1;
    }

    /**
     * Marks that all the input needs to be made available in
     * the encoded output.
     */
    public void setFlushing() {
        readLimit = writePos - 1;
        processPendingBytes();
    }

    /**
     * Marks that there is no more input remaining. The read position
     * can be advanced until the end of the data.
     */
    public void setFinishing() {
        readLimit = writePos - 1;
        finishing = true;
        processPendingBytes();
    }

    /**
     * Tests if there is enough input available to let the caller encode
     * at least one more byte.
     */
    public boolean hasEnoughData(int alreadyReadLen) {
        return readPos - alreadyReadLen < readLimit;
    }

    public void copyUncompressed(OutputStream out, int backward, int len)
            throws IOException {
        out.write(buf, readPos + 1 - backward, len);
    }

    /**
     * Get the number of bytes available, including the current byte.
     * <p>
     * Note that the result is undefined if <code>getMatches</code> or
     * <code>skip</code> hasn't been called yet and no preset dictionary
     * is being used.
     */
    public int getAvail() {
        assert isStarted();
        return writePos - readPos;
    }

    /**
     * Gets the lowest four bits of the absolute offset of the current byte.
     * Bits other than the lowest four are undefined.
     */
    public int getPos() {
        return readPos;
    }

    /**
     * Gets the byte from the given backward offset.
     * <p>
     * The current byte is at <code>0</code>, the previous byte
     * at <code>1</code> etc. To get a byte at zero-based distance,
     * use <code>getByte(dist + 1)<code>.
     * <p>
     * This function is equivalent to <code>getByte(0, backward)</code>.
     */
    public int getByte(int backward) {
        return buf[readPos - backward] & 0xFF;
    }

    /**
     * Gets the byte from the given forward minus backward offset.
     * The forward offset is added to the current position. This lets
     * one read bytes ahead of the current byte.
     */
    public int getByte(int forward, int backward) {
        return buf[readPos + forward - backward] & 0xFF;
    }

    /**
     * Get the length of a match at the given distance.
     *
     * @param dist     zero-based distance of the match to test
     * @param lenLimit don't test for a match longer than this
     * @return length of the match; it is in the range [0, lenLimit]
     */
    public int getMatchLen(int dist, int lenLimit) {
        int backPos = readPos - dist - 1;
        int len = 0;

        while (len < lenLimit && buf[readPos + len] == buf[backPos + len])
            ++len;

        return len;
    }

    /**
     * Get the length of a match at the given distance and forward offset.
     *
     * @param forward  forward offset
     * @param dist     zero-based distance of the match to test
     * @param lenLimit don't test for a match longer than this
     * @return length of the match; it is in the range [0, lenLimit]
     */
    public int getMatchLen(int forward, int dist, int lenLimit) {
        int curPos = readPos + forward;
        int backPos = curPos - dist - 1;
        int len = 0;

        while (len < lenLimit && buf[curPos + len] == buf[backPos + len])
            ++len;

        return len;
    }

    /**
     * Verifies that the matches returned by the match finder are valid.
     * This is meant to be used in an assert statement. This is totally
     * useless for actual encoding since match finder's results should
     * naturally always be valid if it isn't broken.
     *
     * @param matches return value from <code>getMatches</code>
     * @return true if matches are valid, false if match finder is broken
     */
    public boolean verifyMatches(Matches matches) {
        int lenLimit = Math.min(getAvail(), MATCH_LEN_MAX);

        for (int i = 0; i < matches.count; ++i)
            if (getMatchLen(matches.dist[i], lenLimit) != matches.len[i])
                return false;

        return true;
    }

    /**
     * Moves to the next byte, checks if there is enough input available,
     * and returns the amount of input available.
     *
     * @param requiredForFlushing  minimum number of available bytes when
     *                             flushing; encoding may be continued with
     *                             new input after flushing
     * @param requiredForFinishing minimum number of available bytes when
     *                             finishing; encoding must not be continued
     *                             after finishing or the match finder state
     *                             may be corrupt
     * @return the number of bytes available or zero if there
     * is not enough input available
     */
    int movePos(int requiredForFlushing, int requiredForFinishing) {
        assert requiredForFlushing >= requiredForFinishing;

        ++readPos;
        int avail = writePos - readPos;

        if (avail < requiredForFlushing) {
            if (avail < requiredForFinishing || !finishing) {
                ++pendingSize;
                avail = 0;
            }
        }

        return avail;
    }

    /**
     * Runs match finder for the next byte and returns the matches found.
     */
    public abstract Matches getMatches();

    /**
     * Skips the given number of bytes in the match finder.
     */
    public abstract void skip(int len);
}