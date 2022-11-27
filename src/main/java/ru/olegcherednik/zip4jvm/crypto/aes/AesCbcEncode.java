package ru.olegcherednik.zip4jvm.crypto.aes;

/**
 * @author Oleg Cherednik
 * @since 22.11.2022
 */
public class AesCbcEncode implements AesCodeFunc {

    @Override
    public void AesCbc_Encode(long[] p, int[] data, int numBlocks) {
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
