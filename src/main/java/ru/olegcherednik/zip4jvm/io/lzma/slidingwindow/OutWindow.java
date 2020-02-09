package ru.olegcherednik.zip4jvm.io.lzma.slidingwindow;

import java.io.IOException;

public class OutWindow {

    private final byte[] buf;

    private int streamPos;
    private int offs;

    public OutWindow(int windowSize) {
        buf = new byte[windowSize];
    }

    public void writeByte(byte b) throws IOException {
        buf[offs++] = b;
        offs = checkHigherBound(offs);
    }

    public void writeBlock(int distance, int len) throws IOException {
        int pos = checkLowerBound(offs - distance - 1);

        for (; len != 0; len--) {
            pos = checkHigherBound(pos);
            offs = checkHigherBound(offs);
            buf[offs++] = buf[pos++];
        }
    }

    public byte get(int distance) {
        int pos = offs - distance - 1;
        pos += pos < 0 ? buf.length : 0;
        return buf[pos];
    }

    public int flush(byte[] buf, int offs, int len) {
        int res = 0;

        if (this.offs > streamPos) {
            res = Math.min(this.offs - streamPos, len);
            System.arraycopy(this.buf, streamPos, buf, offs, res);
            streamPos += res;
        } else if (this.offs < streamPos) {
            int cur = Math.min(this.buf.length - streamPos, len - res);
            System.arraycopy(this.buf, streamPos, buf, offs, cur);
            streamPos = checkHigherBound(streamPos + cur);

            offs += cur;
            res += cur;

            cur = Math.min(offs, len - res);
            System.arraycopy(this.buf, 0, buf, offs, cur);

            res += cur;
        }

        return res;
    }

    private int checkLowerBound(int i) {
        return i < 0 ? i + buf.length : i;
    }

    private int checkHigherBound(int i) {
        return i >= buf.length ? i - buf.length : i;
    }
}
