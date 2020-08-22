package tangible.c;

public class Sha1 {

    public static final int SHA1_NUM_BLOCK_WORDS = 16;
    public static final int SHA1_NUM_DIGEST_WORDS = 5;

    public static final int SHA1_BLOCK_SIZE = SHA1_NUM_BLOCK_WORDS * 4;
    public static final int SHA1_DIGEST_SIZE = SHA1_NUM_DIGEST_WORDS * 4;

    public static void Sha1_Init(CSha1 p) {
        p.state[0] = 0x67452301;
        p.state[1] = 0xEFCDAB89;
        p.state[2] = 0x98BADCFE;
        p.state[3] = 0x10325476;
        p.state[4] = 0xC3D2E1F0;
        p.count = 0;
    }

    public static void Sha1_Update(CSha1 p, byte[] data, int size) {
//        unsigned pos, pos2;
//        if (size == 0)
//            return;
//        pos = (unsigned)p -> count & 0x3F;
//        p -> count += size;
//        pos2 = pos & 3;
//        pos >>= 2;
//
//        if (pos2 != 0) {
//            UInt32 w;
//            pos2 = (3 - pos2) * 8;
//            w = ((UInt32) * data++) << pos2;
//            if (--size && pos2) {
//                pos2 -= 8;
//                w |= ((UInt32) * data++) << pos2;
//                if (--size && pos2) {
//                    pos2 -= 8;
//                    w |= ((UInt32) * data++) << pos2;
//                    size--;
//                }
//            }
//            p -> buffer[pos] |= w;
//            if (pos2 == 0)
//                pos++;
//        }
//
//        for (; ; ) {
//            if (pos == SHA1_NUM_BLOCK_WORDS) {
//                for (; ; ) {
//                    size_t i;
//                    Sha1_UpdateBlock(p);
//                    if (size < SHA1_BLOCK_SIZE)
//                        break;
//                    size -= SHA1_BLOCK_SIZE;
//                    for (i = 0; i < SHA1_NUM_BLOCK_WORDS; i += 2) {
//                        p -> buffer[i] = GetBe32(data);
//                        p -> buffer[i + 1] = GetBe32(data + 4);
//                        data += 8;
//                    }
//                }
//                pos = 0;
//            }
//            if (size < 4)
//                break;
//
//            p -> buffer[pos] = GetBe32(data);
//            data += 4;
//            size -= 4;
//            pos++;
//        }
//
//        if (size != 0) {
//            UInt32 w = ((UInt32)data[0]) << 24;
//            if (size > 1) {
//                w |= ((UInt32)data[1]) << 16;
//                if (size > 2)
//                    w |= ((UInt32)data[2]) << 8;
//            }
//            p -> buffer[pos] = w;
//        }
    }

    public static void Sha1_Final(CSha1 p, byte[] digest) {
//        unsigned pos = (unsigned)p -> count & 0x3F;
//        unsigned pos2 = (pos & 3);
//        UInt64 numBits;
//        UInt32 w;
//        unsigned i;
//
//        pos >>= 2;
//
//        w = 0;
//        if (pos2 != 0)
//            w = p -> buffer[pos];
//        p -> buffer[pos++] = w | (((UInt32)0x80000000) >> (8 * pos2));
//
//        while (pos != (SHA1_NUM_BLOCK_WORDS - 2)) {
//            pos &= 0xF;
//            if (pos == 0)
//                Sha1_UpdateBlock(p);
//            p -> buffer[pos++] = 0;
//        }
//
//        numBits = (p -> count << 3);
//        p -> buffer[SHA1_NUM_BLOCK_WORDS - 2] = (UInt32)(numBits >> 32);
//        p -> buffer[SHA1_NUM_BLOCK_WORDS - 1] = (UInt32)(numBits);
//        Sha1_UpdateBlock(p);
//
//        for (i = 0; i < SHA1_NUM_DIGEST_WORDS; i++) {
//            UInt32 v = p -> state[i];
//            SetBe32(digest, v);
//            digest += 4;
//        }
//
//        Sha1_Init(p);
    }


}
