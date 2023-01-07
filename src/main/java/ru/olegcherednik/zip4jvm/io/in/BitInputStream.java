package ru.olegcherednik.zip4jvm.io.in;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.IOUtils;
import ru.olegcherednik.zip4jvm.io.in.data.DataInput;

import java.io.IOException;
import java.nio.ByteOrder;

/**
 * @author Oleg Cherednik
 * @since 08.01.2023
 */
@RequiredArgsConstructor
public final class BitInputStream {

    private static final long[] MASKS = createMasks();

    private final DataInput in;
    private final ByteOrder byteOrder;

    private long bitsCache;
    private int bitsCacheSize;

    private static long[] createMasks() {
        long[] masks = new long[64];

        for (int i = 1; i < masks.length; i++)
            masks[i] = (masks[i - 1] << 1) + 1;

        return masks;
    }

    public long readBits(int totalBits) throws IOException {
        if (ensureCache(totalBits))
            return IOUtils.EOF;
        if (bitsCacheSize < totalBits)
            return processBitsGreater57(totalBits);
        return readCacheBits(totalBits);
    }

    public boolean readBit() throws IOException {
        return readBits(1) != 0;
    }

    private long readCacheBits(int totalBits) {
        long res;

        if (byteOrder == ByteOrder.LITTLE_ENDIAN) {
            res = bitsCache & MASKS[totalBits];
            bitsCache >>>= totalBits;
        } else
            res = (bitsCache >> (bitsCacheSize - totalBits)) & MASKS[totalBits];

        bitsCacheSize -= totalBits;
        return res;
    }

    private boolean ensureCache(int count) throws IOException {
        while (bitsCacheSize < count && bitsCacheSize < 57) {
            long nextByte = in.readByte();

            if (nextByte < 0)
                return true;

            if (byteOrder == ByteOrder.LITTLE_ENDIAN) {
                bitsCache |= nextByte << bitsCacheSize;
            } else {
                bitsCache <<= Byte.SIZE;
                bitsCache |= nextByte;
            }
            bitsCacheSize += Byte.SIZE;
        }
        return false;
    }

    private long processBitsGreater57(int totalBits) throws IOException {
        int bitsToAddCount = totalBits - bitsCacheSize;
        int overflowBits = Byte.SIZE - bitsToAddCount;
        int nextByte = in.readByte();
        long overflow;

        if (nextByte < 0)
            return nextByte;

        if (byteOrder == ByteOrder.LITTLE_ENDIAN) {
            bitsCache |= (nextByte & MASKS[bitsToAddCount]) << bitsCacheSize;
            overflow = (nextByte >>> bitsToAddCount) & MASKS[overflowBits];
        } else {
            bitsCache <<= bitsToAddCount;
            bitsCache |= (nextByte >>> overflowBits) & MASKS[bitsToAddCount];
            overflow = nextByte & MASKS[overflowBits];
        }

        long res = bitsCache & MASKS[totalBits];
        bitsCache = overflow;
        bitsCacheSize = overflowBits;
        return res;
    }

    /**
     * Returns an estimate of the number of bits that can be read from
     * this input stream without blocking by the next invocation of a
     * method for this input stream.
     *
     * @return estimate of the number of bits that can be read without blocking
     * @throws IOException if the underlying stream throws one when calling available
     * @since 1.16
     */
    public long bitsAvailable() throws IOException {
        return bitsCacheSize;
    }

    public void alignWithByteBoundary() {
        int skip = bitsCacheSize % Byte.SIZE;

        if (skip > 0)
            readCacheBits(skip);
    }

    @Override
    public String toString() {
        return in.toString();
    }

}
