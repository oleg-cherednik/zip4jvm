package ru.olegcherednik.zip4jvm.crypto.aes;

/**
 * @author Oleg Cherednik
 * @since 22.11.2022
 */
public class AesCtrCode implements AesCodeFunc {

    @Override
    public void AesCbc_Encode(long[] p, int[] data, int numBlocks) {
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
