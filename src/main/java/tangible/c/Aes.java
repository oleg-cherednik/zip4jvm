package tangible.c;

import static tangible.c.CpuArch.GetUi32;
import static tangible.c.CpuArch.SetUi32;

// Aes.h
public class Aes {

    public static final byte[] Sbox = {
            (byte)99, (byte)124, (byte)119, (byte)123, (byte)242, (byte)107, (byte)111, (byte)197,
            (byte)48, (byte)1, (byte)103, (byte)43, (byte)254, (byte)215, (byte)171, (byte)118,
            (byte)202, (byte)130, (byte)201, (byte)125, (byte)250, (byte)89, (byte)71, (byte)240,
            (byte)173, (byte)212, (byte)162, (byte)175, (byte)156, (byte)164, (byte)114, (byte)192,
            (byte)183, (byte)253, (byte)147, (byte)38, (byte)54, (byte)63, (byte)247, (byte)204,
            (byte)52, (byte)165, (byte)229, (byte)241, (byte)113, (byte)216, (byte)49, (byte)21,
            (byte)4, (byte)199, (byte)35, (byte)195, (byte)24, (byte)150, (byte)5, (byte)154,
            (byte)7, (byte)18, (byte)128, (byte)226, (byte)235, (byte)39, (byte)178, (byte)117,
            (byte)9, (byte)131, (byte)44, (byte)26, (byte)27, (byte)110, (byte)90, (byte)160,
            (byte)82, (byte)59, (byte)214, (byte)179, (byte)41, (byte)227, (byte)47, (byte)132,
            (byte)83, (byte)209, (byte)0, (byte)237, (byte)32, (byte)252, (byte)177, (byte)91,
            (byte)106, (byte)203, (byte)190, (byte)57, (byte)74, (byte)76, (byte)88, (byte)207,
            (byte)208, (byte)239, (byte)170, (byte)251, (byte)67, (byte)77, (byte)51, (byte)133,
            (byte)69, (byte)249, (byte)2, (byte)127, (byte)80, (byte)60, (byte)159, (byte)168,
            (byte)81, (byte)163, (byte)64, (byte)143, (byte)146, (byte)157, (byte)56, (byte)245,
            (byte)188, (byte)182, (byte)218, (byte)33, (byte)16, (byte)255, (byte)243, (byte)210,
            (byte)205, (byte)12, (byte)19, (byte)236, (byte)95, (byte)151, (byte)68, (byte)23,
            (byte)196, (byte)167, (byte)126, (byte)61, (byte)100, (byte)93, (byte)25, (byte)115,
            (byte)96, (byte)129, (byte)79, (byte)220, (byte)34, (byte)42, (byte)144, (byte)136,
            (byte)70, (byte)238, (byte)184, (byte)20, (byte)222, (byte)94, (byte)11, (byte)219,
            (byte)224, (byte)50, (byte)58, (byte)10, (byte)73, (byte)6, (byte)36, (byte)92,
            (byte)194, (byte)211, (byte)172, (byte)98, (byte)145, (byte)149, (byte)228, (byte)121,
            (byte)231, (byte)200, (byte)55, (byte)109, (byte)141, (byte)213, (byte)78, (byte)169,
            (byte)108, (byte)86, (byte)244, (byte)234, (byte)101, (byte)122, (byte)174, (byte)8,
            (byte)186, (byte)120, (byte)37, (byte)46, (byte)28, (byte)166, (byte)180, (byte)198,
            (byte)232, (byte)221, (byte)116, (byte)31, (byte)75, (byte)189, (byte)139, (byte)138,
            (byte)112, (byte)62, (byte)181, (byte)102, (byte)72, (byte)3, (byte)246, (byte)14,
            (byte)97, (byte)53, (byte)87, (byte)185, (byte)134, (byte)193, (byte)29, (byte)158,
            (byte)225, (byte)248, (byte)152, (byte)17, (byte)105, (byte)217, (byte)142, (byte)148,
            (byte)155, (byte)30, (byte)135, (byte)233, (byte)206, (byte)85, (byte)40, (byte)223,
            (byte)140, (byte)161, (byte)137, (byte)13, (byte)191, (byte)230, (byte)66, (byte)104,
            (byte)65, (byte)153, (byte)45, (byte)15, (byte)176, (byte)84, (byte)187, (byte)22,
    };

    public static final long[] T = new long[256 * 4];
    public static final long[] D = new long[256 * 4];

    public static final byte[] InvS = new byte[256];


    static {
        AesGenTables();
    }

    private static byte xtime(byte a) {
        return (byte)(((a << 1) ^ ((a & 0x80) != 0 ? 0x1B : 0)) & 0xFF);
    }

    private static long Ui32(byte a0, byte a1, byte a2, byte a3) {
        return (a0 | (a1 << 8) | (a2 << 16) | (a3 << 24)) & 0xFFFFFFFFL;
    }

    private static int offs(int x) {
        return x << 8;
    }

    private static byte gb(int n, long x) {
        if (n == 0)
            return (byte)(x & 0xFF);
        if (n == 1)
            return (byte)((x >> 8) & 0xFF);
        if (n == 2)
            return (byte)((x >> 16) & 0xFF);
        if (n == 3)
            return (byte)((x >> 24) & 0xFF);

        throw new RuntimeException();
    }

    private static long DD(int x) {
        return D[x << 8];
    }

    private static long HD(int i, int x, long[] s) {
        return DD(gb(x, s[(i - x) & 3]) & 0xFF);
    }

    private static void HD4(long[] m, int i, long[] s, int p, long[] w) {
        m[i] = HD(i, 0, s) ^ HD(i, 1, s) ^ HD(i, 2, s) ^ HD(i, 3, s) ^ w[p + i];
    }

    private static void HD16(long[] m, long[] s, int p, long[] w) {
        HD4(m, 0, s, p, w);
        HD4(m, 1, s, p, w);
        HD4(m, 2, s, p, w);
        HD4(m, 3, s, p, w);
    }

    private static byte FD(int i, long[] m, int x) {
        return InvS[gb(x, m[(i - x) & 3])];
    }

    private static void FD4(long[] dest, int i, long[] m, long[] w) {
        dest[i] = Ui32(FD(i, m, 0), FD(i, m, 1), FD(i, m, 2), FD(i, m, 3)) ^ w[i];
    }

    private static void TT(int x, int i, long v) {
        T[(x << 8) + i] = v;
    }

    private static void DD(int x, int i, long v) {
        D[(x << 8) + i] = v;
    }

    private static void AesGenTables() {
        for (int i = 0; i < 256; i++)
            InvS[Sbox[i] & 0xFF] = (byte)i;

        for (int i = 0; i < 256; i++) {
            {
                byte a1 = Sbox[i];
                byte a2 = xtime(a1);
                byte a3 = (byte)(a2 ^ a1);
                TT(0, i, Ui32(a2, a1, a1, a3));
                TT(1, i, Ui32(a3, a2, a1, a1));
                TT(2, i, Ui32(a1, a3, a2, a1));
                TT(3, i, Ui32(a1, a1, a3, a2));
            }

            {
                byte a1 = InvS[i];
                byte a2 = xtime(a1);
                byte a4 = xtime(a2);
                byte a8 = xtime(a4);
                byte a9 = (byte)(a8 ^ a1);
                byte aB = (byte)(a8 ^ a2 ^ a1);
                byte aD = (byte)(a8 ^ a4 ^ a1);
                byte aE = (byte)(a8 ^ a4 ^ a2);
                DD(0, i, Ui32(aE, a9, aD, aB));
                DD(1, i, Ui32(aB, aE, a9, aD));
                DD(2, i, Ui32(aD, aB, aE, a9));
                DD(3, i, Ui32(a9, aD, aB, aE));
            }
        }
    }

    public static final int AES_BLOCK_SIZE = 16;
    /* 16-byte (4 * 32-bit words) blocks: 1 (IV) + 1 (keyMode) + 15 (AES-256 roundKeys) */
    public static final int AES_NUM_IVMRK_WORDS = (1 + 1 + 15) * 4;

    // Aes.c:143
    public static void Aes_SetKey_Enc(long[] w, int offs, byte[] key, int keySize) {
//        int i, wSize;
//        wSize = keySize + 28;
//        keySize /= 4;
//        w[0] = ((UInt32)keySize / 2) + 3;
//        w += 4;
//
//        for (i = 0; i < keySize; i++, key += 4)
//            w[i] = GetUi32(key);
//
//        for (; i < wSize; i++) {
//            UInt32 t = w[(size_t)i - 1];
//            unsigned rem = i % keySize;
//            if (rem == 0)
//                t = Ui32(Sbox[gb1(t)] ^ Rcon[i / keySize], Sbox[gb2(t)], Sbox[gb3(t)], Sbox[gb0(t)]);
//            else if (keySize > 6 && rem == 4)
//                t = Ui32(Sbox[gb0(t)], Sbox[gb1(t)], Sbox[gb2(t)], Sbox[gb3(t)]);
//            w[i] = w[i - keySize] ^ t;
//        }

    }

    // Aes.c:166
    public static void Aes_SetKey_Dec(long[] w, int offs, byte[] key, int keySize) {
        int a = 0;
        a++;
//        unsigned i, num;
//        Aes_SetKey_Enc(w, key, keySize);
//        num = keySize + 20;
//        w += 8;
//        for (i = 0; i < num; i++) {
//            UInt32 r = w[i];
//            w[i] =
//                    DD(0)[Sbox[gb0(r)]] ^
//                            DD(1)[Sbox[gb1(r)]] ^
//                            DD(2)[Sbox[gb2(r)]] ^
//                            DD(3)[Sbox[gb3(r)]];
//        }
    }

    // Aes.c:238
    public static void AesCbc_Encode(long[] p, int p_offs, byte[] data, int d_offs, int numBlocks) {
//        for (; numBlocks != 0; numBlocks--, data += AES_BLOCK_SIZE) {
//            p[0] ^= GetUi32(data);
//            p[1] ^= GetUi32(data + 4);
//            p[2] ^= GetUi32(data + 8);
//            p[3] ^= GetUi32(data + 12);
//
//            Aes_Encode(p, p_offs + 4, p, p);
//
//            SetUi32(data, p[0]);
//            SetUi32(data + 4, p[1]);
//            SetUi32(data + 8, p[2]);
//            SetUi32(data + 12, p[3]);
//        }
    }

    // Aes.c:256
    public static void AesCbc_Decode(long[] p, int p_offs, byte[] data, int d_offs, int numBlocks) {
        long[] in = new long[4];
        long[] out = new long[4];

        for (; numBlocks != 0; numBlocks--, d_offs += AES_BLOCK_SIZE) {
            in[0] = GetUi32(data, d_offs);
            in[1] = GetUi32(data, d_offs + 4);
            in[2] = GetUi32(data, d_offs + 8);
            in[3] = GetUi32(data, d_offs + 12);

            Aes_Decode(p, 4, out, in);

            SetUi32(data, d_offs, p[0] ^ out[0]);
            SetUi32(data, d_offs + 4, p[1] ^ out[1]);
            SetUi32(data, d_offs + 8, p[2] ^ out[2]);
            SetUi32(data, d_offs + 12, p[3] ^ out[3]);

            p[0] = in[0];
            p[1] = in[1];
            p[2] = in[2];
            p[3] = in[3];
        }
    }

    // Aes.c:187
    public static void Aes_Encode(int[] w, int offs, long[] dest, int d_offs, long[] src) {
//        UInt32 s[ 4];
//        UInt32 m[ 4];
//        UInt32 numRounds2 = w[0];
//        w += 4;
//        s[0] = src[0] ^ w[0];
//        s[1] = src[1] ^ w[1];
//        s[2] = src[2] ^ w[2];
//        s[3] = src[3] ^ w[3];
//        w += 4;
//        for (; ; ) {
//            HT16(m, s, 0);
//            if (--numRounds2 == 0)
//                break;
//            HT16(s, m, 4);
//            w += 8;
//        }
//        w += 4;
//        FT4(0);
//        FT4(1);
//        FT4(2);
//        FT4(3);
    }

    // Aes.c:210
    public static void Aes_Decode(long[] w, int w_offs, long[] dest, long[] src) {
        long[] s = new long[4];
        long[] m = new long[4];
        long numRounds2 = w[w_offs];
        w_offs += 4 + numRounds2 * 8;

        s[0] = src[0] ^ w[w_offs];
        s[1] = src[1] ^ w[w_offs + 1];
        s[2] = src[2] ^ w[w_offs + 2];
        s[3] = src[3] ^ w[w_offs + 3];

        for (; ; ) {
            w_offs -= 8;
            HD16(m, s, 4, w);
            if (--numRounds2 == 0)
                break;
            HD16(s, m, 0, w);
        }

        FD4(dest, 0, m, w);
        FD4(dest, 1, m, w);
        FD4(dest, 2, m, w);
        FD4(dest, 3, m, w);
    }

    public static void AesCbc_Init(long[] p, byte[] iv) {
        for (int i = 0; i < 4; i++)
            p[i] = GetUi32(iv, i * 4);
    }

    public interface AES_CODE_FUNC {

        void apply(long[] p, int p_offs, byte[] data, int d_offs, int numBlocks);
    }


}
