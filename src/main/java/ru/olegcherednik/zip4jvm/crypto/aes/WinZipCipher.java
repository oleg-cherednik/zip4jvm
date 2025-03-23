package ru.olegcherednik.zip4jvm.crypto.aes;

import lombok.RequiredArgsConstructor;

import javax.crypto.Cipher;
import javax.crypto.ShortBufferException;

/**
 * @author Oleg Cherednik
 * @since 23.03.2025
 */
@RequiredArgsConstructor
public class WinZipCipher {

    private static final int BLOCK_SIZE = 16;

    private final Cipher cipher;
    private final byte[] iv = new byte[BLOCK_SIZE];
    private final byte[] counter = new byte[BLOCK_SIZE];

    private int nonce = BLOCK_SIZE;

    public void update(byte[] input, int inputOffset, int inputLen, byte[] output) throws ShortBufferException {
        cipher.update(input, inputOffset, inputLen, output);
    }

    /*
     * Sun implementation (com.sun.crypto.provider.CounterMode) of 'AES/ECB/NoPadding' is not compatible with WinZip
     * specification. Have to implement custom one.
     */
    public void update(byte[] buf, int offs, int len) throws ShortBufferException {
        for (int i = 0; i < len; i++)
            buf[offs + i] = update(buf[offs + i]);
    }

    public byte update(byte b) throws ShortBufferException {
        if (nonce == iv.length) {
            ivUpdate();
            cipher.update(iv, 0, iv.length, counter);
            nonce = 0;
        }

        return (byte) (b ^ counter[nonce++]);
    }

    private void ivUpdate() {
        for (int i = 0; i < iv.length; i++) {
            iv[i]++;

            if (iv[i] != 0)
                break;
        }
    }


    public int getBlockSize() {
        return cipher.getBlockSize();
    }

}
