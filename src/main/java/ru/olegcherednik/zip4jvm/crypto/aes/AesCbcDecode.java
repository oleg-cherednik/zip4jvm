package ru.olegcherednik.zip4jvm.crypto.aes;

import static ru.olegcherednik.zip4jvm.crypto.aes.MyAes.AES_BLOCK_SIZE;

/**
 * @author Oleg Cherednik
 * @since 22.11.2022
 */
public class AesCbcDecode implements AesCodeFunc {

    @Override
    public void AesCbc_Encode(long[] p, int[] data, int numBlocks) {

        long[] in = new long[4];
        long[] out = new long[4];

        for (int j = 0; numBlocks != 0; numBlocks--, j += AES_BLOCK_SIZE) {
            in[0] = data[j];
            in[1] = data[j + 1];
            in[2] = data[j + 2];
            in[3] = data[j + 3];

//            Aes_Decode(p + 4, out, in);
//
//            SetUi32(data,      p[0] ^ out[0]);
//            SetUi32(data + 4,  p[1] ^ out[1]);
//            SetUi32(data + 8,  p[2] ^ out[2]);
//            SetUi32(data + 12, p[3] ^ out[3]);

            p[0] = in[0];
            p[1] = in[1];
            p[2] = in[2];
            p[3] = in[3];
        }

    }
}
