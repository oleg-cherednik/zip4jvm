package ru.olegcherednik.zip4jvm.crypto.aes;

import java.util.Arrays;

/**
 * @author Oleg Cherednik
 * @since 22.11.2022
 */
public class MyAes {

    public static final int AES_BLOCK_SIZE = 16;
    private static final int AES_NUM_IVMRK_WORDS = (1 + 1 + 15) * 4;

    private AesCodeFunc g_AesCbc_Encode;
    private AesCodeFunc g_AesCbc_Decode;
    private AesCodeFunc g_AesCtr_Code;
    private AesCodeFunc _codeFunc;

    private int _keySize;
    private boolean _keyIsSet;
    private boolean _encodeMode;
    private int _offset;
    private int[] _aes = new int[AES_NUM_IVMRK_WORDS + 3];
    private byte[] _iv = new byte[AES_BLOCK_SIZE];

    public MyAes() {
        AesGenTables();
    }

    private void AesGenTables() {
        for (int i = 0; i < 256; i++)
            InvS[Sbox[i]] = i;

        for (int i = 0; i < 256; i++) {
            {
                int a1 = Sbox[i];
                int a2 = xtime(a1);
                int a3 = a2 ^ a1;
                T[TT(0, i)] = Ui32(a2, a1, a1, a3);
                T[TT(1, i)] = Ui32(a3, a2, a1, a1);
                T[TT(2, i)] = Ui32(a1, a3, a2, a1);
                T[TT(3, i)] = Ui32(a1, a1, a3, a2);
            }
            {
                int a1 = InvS[i];
                int a2 = xtime(a1);
                int a4 = xtime(a2);
                int a8 = xtime(a4);
                int a9 = a8 ^ a1;
                int aB = a8 ^ a2 ^ a1;
                int aD = a8 ^ a4 ^ a1;
                int aE = a8 ^ a4 ^ a2;
                D[DD(0, i)] = Ui32(aE, a9, aD, aB);
                D[DD(1, i)] = Ui32(aB, aE, a9, aD);
                D[DD(2, i)] = Ui32(aD, aB, aE, a9);
                D[DD(3, i)] = Ui32(a9, aD, aB, aE);
            }
        }

        g_AesCbc_Encode = new AesCbcEncode();
        g_AesCbc_Decode = new AesCbcDecode();
        g_AesCtr_Code = new AesCtrCode();
    }

    public void init(boolean encodeMode, int keySize) {
//        Arrays.fill(_aes, 0xCDCDCDCD);

        _keySize = keySize;
        _keyIsSet = false;
        _encodeMode = encodeMode;
        _offset = 3;    // _offset = ((0 - (unsigned)(ptrdiff_t)_aes) & 0xF) / sizeof(UInt32);
        SetFunctions(1);
    }

    public void Init() {
        AesCbc_Init();

        if (!_keyIsSet)
            throw new RuntimeException();
    }

    private void AesCbc_Init() {
        for (int i = 0, j = 0; i < 4; i++) {
            int a = _iv[j++];
            int b = _iv[j++];
            int c = _iv[j++];
            int d = _iv[j++];
            _aes[_offset + i] = Ui32(a, b, c, d);
        }
    }

    private static int TT(int x, int i) {
        return (x << 8) + i;
    }

    private static int DD(int x, int i) {
        return (x << 8) + i;
    }

    private static int xtime(int x) {
        return ((x << 1) ^ ((x & 0x80) != 0 ? 0x1B : 0)) & 0xFF;
    }

    private static long Ui32(long a0, long a1, long a2, long a3) {
        return a0 | (a1 << 8) | (a2 << 16) | (a3 << 24);
    }

    private boolean SetFunctions(int algo) {
        _codeFunc = _encodeMode ? g_AesCbc_Encode : g_AesCbc_Decode;
        if (algo == 1) {
            _codeFunc = _encodeMode ? new AesCbcEncode() : new AesCbcDecode();
        }
        return true;
    }

    public void SetKey(byte[] data) {
        int size = data.length;

        if ((size & 0x7) != 0 || size < 16 || size > 32)
            throw new RuntimeException();
        if (_keySize != 0 && size != _keySize)
            throw new RuntimeException();

        if (_encodeMode)
            Aes_SetKey_Enc(_offset + 4, data);
        else
            Aes_SetKey_Dec(_offset + 4, data);

        _keyIsSet = true;
    }

    public void SetInitVector(byte[] data) {
        if (data.length != AES_BLOCK_SIZE)
            throw new RuntimeException();

        System.arraycopy(data, 0, _iv, 0, data.length);
        Init();
    }

    public byte[] filter(byte[] p) {
        if (!_keyIsSet)
            throw new RuntimeException();
        if (p == null || p.length == 0)
            return new byte[0];

        return filter(p, 0, p.length);
    }

    public byte[] filter(byte[] p, int offs, int len) {
        if (!_keyIsSet)
            throw new RuntimeException();
        if (p == null || p.length == 0)
            return new byte[0];

        int size = len >> 4;
        byte[] res = Arrays.copyOfRange(p, offs, len);
        _codeFunc.AesCbc_Encode(_aes, _offset, res, size);

        return res;
    }

    private final int[] InvS = new int[256];
    private final int[] T = new int[256 * 4];
    private final int[] D = new int[256 * 4];

    private final int[] Sbox = {
            0x63, 0x7c, 0x77, 0x7b, 0xf2, 0x6b, 0x6f, 0xc5, 0x30, 0x01, 0x67, 0x2b, 0xfe, 0xd7, 0xab, 0x76,
            0xca, 0x82, 0xc9, 0x7d, 0xfa, 0x59, 0x47, 0xf0, 0xad, 0xd4, 0xa2, 0xaf, 0x9c, 0xa4, 0x72, 0xc0,
            0xb7, 0xfd, 0x93, 0x26, 0x36, 0x3f, 0xf7, 0xcc, 0x34, 0xa5, 0xe5, 0xf1, 0x71, 0xd8, 0x31, 0x15,
            0x04, 0xc7, 0x23, 0xc3, 0x18, 0x96, 0x05, 0x9a, 0x07, 0x12, 0x80, 0xe2, 0xeb, 0x27, 0xb2, 0x75,
            0x09, 0x83, 0x2c, 0x1a, 0x1b, 0x6e, 0x5a, 0xa0, 0x52, 0x3b, 0xd6, 0xb3, 0x29, 0xe3, 0x2f, 0x84,
            0x53, 0xd1, 0x00, 0xed, 0x20, 0xfc, 0xb1, 0x5b, 0x6a, 0xcb, 0xbe, 0x39, 0x4a, 0x4c, 0x58, 0xcf,
            0xd0, 0xef, 0xaa, 0xfb, 0x43, 0x4d, 0x33, 0x85, 0x45, 0xf9, 0x02, 0x7f, 0x50, 0x3c, 0x9f, 0xa8,
            0x51, 0xa3, 0x40, 0x8f, 0x92, 0x9d, 0x38, 0xf5, 0xbc, 0xb6, 0xda, 0x21, 0x10, 0xff, 0xf3, 0xd2,
            0xcd, 0x0c, 0x13, 0xec, 0x5f, 0x97, 0x44, 0x17, 0xc4, 0xa7, 0x7e, 0x3d, 0x64, 0x5d, 0x19, 0x73,
            0x60, 0x81, 0x4f, 0xdc, 0x22, 0x2a, 0x90, 0x88, 0x46, 0xee, 0xb8, 0x14, 0xde, 0x5e, 0x0b, 0xdb,
            0xe0, 0x32, 0x3a, 0x0a, 0x49, 0x06, 0x24, 0x5c, 0xc2, 0xd3, 0xac, 0x62, 0x91, 0x95, 0xe4, 0x79,
            0xe7, 0xc8, 0x37, 0x6d, 0x8d, 0xd5, 0x4e, 0xa9, 0x6c, 0x56, 0xf4, 0xea, 0x65, 0x7a, 0xae, 0x08,
            0xba, 0x78, 0x25, 0x2e, 0x1c, 0xa6, 0xb4, 0xc6, 0xe8, 0xdd, 0x74, 0x1f, 0x4b, 0xbd, 0x8b, 0x8a,
            0x70, 0x3e, 0xb5, 0x66, 0x48, 0x03, 0xf6, 0x0e, 0x61, 0x35, 0x57, 0xb9, 0x86, 0xc1, 0x1d, 0x9e,
            0xe1, 0xf8, 0x98, 0x11, 0x69, 0xd9, 0x8e, 0x94, 0x9b, 0x1e, 0x87, 0xe9, 0xce, 0x55, 0x28, 0xdf,
            0x8c, 0xa1, 0x89, 0x0d, 0xbf, 0xe6, 0x42, 0x68, 0x41, 0x99, 0x2d, 0x0f, 0xb0, 0x54, 0xbb, 0x16 };

    private final int[] Rcon = { 0x00, 0x01, 0x02, 0x04, 0x08, 0x10, 0x20, 0x40, 0x80, 0x1b, 0x36 };

    private void Aes_SetKey_Dec(int offs, byte[] data) {
        Aes_SetKey_Enc(offs, data);
        offs += 8;

        for (int i = 0; i < data.length + 20; i++) {
            int r = _aes[offs + i];
            long a = D[DD(0, Sbox[gb0(r)])];
            long b = D[DD(1, Sbox[gb1(r)])];
            long c = D[DD(2, Sbox[gb2(r)])];
            long d = D[DD(3, Sbox[gb3(r)])];

            _aes[offs + i] = (int)(a ^ b ^ c ^ d);
        }
    }

    private void Aes_SetKey_Enc(int offs, byte[] data) {
        int wSize = data.length + 28;
        int keySize = data.length / 4;

        _aes[offs] = (keySize / 2) + 3;
        _aes[offs] <<= 8;
        _aes[offs] <<= 8;
        _aes[offs] <<= 8;
        offs += 4;

        int i = 0;

        for (int j = 0; i < keySize; i++, j += 4) {
            _aes[offs + i] = GetUi32(data, j);
        }

        for (; i < wSize; i++) {
            int t = _aes[offs + i - 1];
            int rem = i % keySize;

            if (rem == 0)
                t = Ui32(Sbox[gb1(t)] ^ Rcon[i / keySize], Sbox[gb2(t)], Sbox[gb3(t)], Sbox[gb0(t)]);
            else if (keySize > 6 && rem == 4)
                t = Ui32(Sbox[gb0(t)], Sbox[gb1(t)], Sbox[gb2(t)], Sbox[gb3(t)]);

            _aes[offs + i] = _aes[offs + i - keySize] ^ t;
        }
    }

    private static int gb(int x, int j) {
        if (j == 0)
            return gb0(x);
        if (j == 1)
            return gb1(x);
        if (j == 2)
            return gb2(x);
        if (j == 3)
            return gb3(x);

        throw new RuntimeException();
    }

    public static int gb0(int x) {
        return x & 0xFF;
    }

    public static int gb1(int x) {
        return (x >> 8) & 0xFF;
    }

    public static int gb2(int x) {
        return (x >> 16) & 0xFF;
    }

    public static int gb3(int x) {
        return (x >> 24) & 0xFF;
    }

    public static void SetUi32(byte[] data, int offs, int val) {
        data[offs] = (byte)gb0(val);
        data[offs + 1] = (byte)gb1(val);
        data[offs + 2] = (byte)gb2(val);
        data[offs + 3] = (byte)gb3(val);
    }

    private static int Ui32(int a0, int a1, int a2, int a3) {
        return a0 & 0xFF | ((a1 & 0xFF) << 8) | ((a2 & 0xFF) << 16) | ((a3 & 0xFF) << 24);
    }

    public static int GetUi32(byte[] arr, int offs) {
        return Ui32(arr[offs], arr[offs + 1], arr[offs + 2], arr[offs + 3]);
    }

    private interface AesCodeFunc {

        void AesCbc_Encode(int[] p, int offs, byte[] data, int numBlocks);
    }

    private class AesCbcDecode implements AesCodeFunc {

        @Override
        public void AesCbc_Encode(int[] p, int offs, byte[] data, int numBlocks) {
            int[] in = new int[4];
            int[] out = new int[4];

            for (int i = 0; numBlocks != 0; numBlocks--, i += AES_BLOCK_SIZE) {
                in[0] = GetUi32(data, i);
                in[1] = GetUi32(data, i + 4);
                in[2] = GetUi32(data, i + 8);
                in[3] = GetUi32(data, i + 12);

                Aes_Decode(p, offs + 4, out, in);

                SetUi32(data, i, p[offs] ^ out[0]);
                SetUi32(data, i + 4, p[offs + 1] ^ out[1]);
                SetUi32(data, i + 8, p[offs + 2] ^ out[2]);
                SetUi32(data, i + 12, p[offs + 3] ^ out[3]);

                p[offs] = in[0];
                p[offs + 1] = in[1];
                p[offs + 2] = in[2];
                p[offs + 3] = in[3];
            }

        }

        private void Aes_Decode(int[] w, int offs, int[] dest, int[] src) {
            int[] s = new int[4];
            int[] m = new int[4];
            int numRounds2 = gb3(w[offs]);

            offs += 4 + numRounds2 * 8;

            s[0] = src[0] ^ w[offs];
            s[1] = src[1] ^ w[offs + 1];
            s[2] = src[2] ^ w[offs + 2];
            s[3] = src[3] ^ w[offs + 3];

            for (; ; ) {
                offs -= 8;
                HD16(m, s, 4, w, offs);
                if (--numRounds2 == 0)
                    break;
                HD16(s, m, 0, w, offs);
            }

            FD4(dest, 0, w, offs, m);
            FD4(dest, 1, w, offs, m);
            FD4(dest, 2, w, offs, m);
            FD4(dest, 3, w, offs, m);
        }
    }

    private void HD16(int[] m, int[] s, int p, int[] w, int offs) {
        HD4(m, 0, s, p, w, offs);
        HD4(m, 1, s, p, w, offs);
        HD4(m, 2, s, p, w, offs);
        HD4(m, 3, s, p, w, offs);
    }

    private void HD4(int[] m, int i, int[] s, int p, int[] w, int offs) {
        int a = HD(i, 0, s);
        int b = HD(i, 1, s);
        int c = HD(i, 2, s);
        int d = HD(i, 3, s);
        m[i] = a ^ b ^ c ^ d ^ w[offs + p + i];
    }

    private int FD(int i, int x, int[] m) {
        return InvS[gb(m[(i - x) & 3], x)];
    }

    private int HD(int i, int x, int[] s) {
        return D[DD(x, gb(s[(i - x) & 3], x))];
    }

    private void FD4(int[] dest, int i, int[] w, int offs, int[] m) {
        dest[i] = Ui32(FD(i, 0, m), FD(i, 1, m), FD(i, 2, m), FD(i, 3, m)) ^ w[offs + i];
    }

    private class AesCbcEncode implements AesCodeFunc {

        @Override
        public void AesCbc_Encode(int[] p, int offs, byte[] data, int numBlocks) {
//        for (; numBlocks != 0; numBlocks--, data += AES_BLOCK_SIZE)
//        {
//            p[0] ^= GetUi32(data);
//            p[1] ^= GetUi32(data + 4);
//            p[2] ^= GetUi32(data + 8);
//            p[3] ^= GetUi32(data + 12);
//
//            Aes_Encode(p + 4, p, p);
//
//            SetUi32(data,      p[0]);
//            SetUi32(data + 4,  p[1]);
//            SetUi32(data + 8,  p[2]);
//            SetUi32(data + 12, p[3]);
//        }
        }
    }

    private class AesCtrCode implements AesCodeFunc {

        @Override
        public void AesCbc_Encode(int[] p, int offs, byte[] data, int numBlocks) {
//        for (; numBlocks != 0; numBlocks--)
//        {
//            UInt32 temp[4];
//            unsigned i;
//
//            if (++p[0] == 0)
//                p[1]++;
//
//            Aes_Encode(p + 4, temp, p);
//
//            for (i = 0; i < 4; i++, data += 4)
//            {
//                UInt32 t = temp[i];
//
//      #ifdef MY_CPU_LE_UNALIGN
//        *((UInt32 *)data) ^= t;
//      #else
//                data[0] ^= (t & 0xFF);
//                data[1] ^= ((t >> 8) & 0xFF);
//                data[2] ^= ((t >> 16) & 0xFF);
//                data[3] ^= ((t >> 24));
//      #endif
//            }
        }
    }

}
