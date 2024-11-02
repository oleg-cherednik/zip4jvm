package ru.olegcherednik.zip4jvm.io;

/**
 * @author Oleg Cherednik
 * @since 01.11.2024
 */
public enum ByteOrder {

    LITTLE_ENDIAN {
        @Override
        public long getLong(byte[] buf, int offs, int len) {
            long res = 0;

            for (int i = offs + len - 1; i >= offs; i--)
                res = res << 8 | buf[i] & 0xFF;

            return res;
        }

        @Override
        public void fromLong(long val, byte[] buf, int offs, int len) {
            for (int i = 0; i < len; i++) {
                buf[offs + i] = (byte) val;
                val >>= 8;
            }
        }
    };

    public abstract long getLong(byte[] buf, int offs, int len);

    public abstract void fromLong(long val, byte[] buf, int offs, int len);

}
