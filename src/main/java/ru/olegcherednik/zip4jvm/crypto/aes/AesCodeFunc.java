package ru.olegcherednik.zip4jvm.crypto.aes;

/**
 * @author Oleg Cherednik
 * @since 22.11.2022
 */
interface AesCodeFunc {

    void AesCbc_Encode(long[] p, int[] data, int numBlocks);
}
