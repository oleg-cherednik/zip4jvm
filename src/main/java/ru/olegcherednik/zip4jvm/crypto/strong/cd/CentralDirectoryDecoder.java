package ru.olegcherednik.zip4jvm.crypto.strong.cd;

import lombok.RequiredArgsConstructor;

import javax.crypto.Cipher;

/**
 * @author Oleg Cherednik
 * @since 27.09.2024
 */
@RequiredArgsConstructor
public class CentralDirectoryDecoder {

    private final Cipher cipher;

    public byte[] decrypt(byte[] buf, int offs, int len) {
        return cipher.update(buf, offs, len);
    }

}
