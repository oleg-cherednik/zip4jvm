package ru.olegcherednik.zip4jvm.io.in;

import java.util.Arrays;

/**
 * @author Oleg Cherednik
 * @since 28.10.2019
 */
public class CycleBuffer {

    private final byte[] buf = new byte[512];
    private int cur;
    private boolean full;

    public void write(byte[] buf, int offs, int len) {
        for(int i = 0; i < len; i++) {
            this.buf[cur] = buf[offs + i];

            if(cur == this.buf.length - 1) {
                full = true;
                cur = 0;
            } else
                cur++;
        }
    }

    public void clear() {
        cur = 0;
        Arrays.fill(buf, (byte)0);
        full = false;
    }

    public byte[] getLastBytes(int bytes) {
        bytes = Math.min(buf.length, bytes);

        if(!full)
            bytes = Math.min(bytes, cur);

        byte[] res = new byte[bytes];

        for(int i = res.length - 1, j = cur - 1; i >= 0; i--, j--) {
            if(j < 0)
                j = buf.length - 1;

            res[i] = buf[j];
        }

        return res;
    }

}
