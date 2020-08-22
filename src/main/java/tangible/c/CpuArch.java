package tangible.c;

public class CpuArch {

    public static long GetUi32(byte[] p, int offs) {
        long res = p[offs] & 0xFF;
        res |= (p[offs + 1] & 0xFF) << 8;
        res |= (p[offs + 2] & 0xFF) << 16;
        res |= (p[offs + 3] & 0xFF) << 24;
        return res & 0xFFFFFFFFL;
    }

    public static void SetUi32(byte[] p, int offs, long v) {
        p[offs] = (byte)(v & 0xFF);
        p[offs + 1] = (byte)((v >> 8) & 0xFF);
        p[offs + 2] = (byte)((v >> 16) & 0xFF);
        p[offs + 3] = (byte)((v >> 24) & 0xFF);
    }
}
