/*
 * Copyright 2019 Oleg Cherednik (oleg.cherednik@gmail.com)
 *
 * Licensed under The Apache Software License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package ru.olegcherednik.zip4jvm.crypto.aes;

import ru.olegcherednik.zip4jvm.utils.quitely.Quietly;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

/**
 * Sun implementation (com.sun.crypto.provider.CounterMode) of 'AES/ECB/NoPadding'
 * is not compatible with WinZip specification. Have to implement custom one.
 *
 * @author Oleg Cherednik
 * @since 23.03.2025
 */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class WinZipAesCipher {

    private static final int BLOCK_SIZE = 16;

    private final Cipher cipher;
    private final byte[] iv = new byte[BLOCK_SIZE];
    private final byte[] counter = new byte[BLOCK_SIZE];

    private int nonce = BLOCK_SIZE;

    public static WinZipAesCipher getInstance(SecretKeySpec secretKeySpec) {
        return Quietly.doRuntime(() -> {
            Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
            // use custom AES implementation, so no worry for DECRYPT_MODE
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
            return new WinZipAesCipher(cipher);
        });
    }

    /*
     * Sun implementation (com.sun.crypto.provider.CounterMode) of 'AES/ECB/NoPadding'
     * is not compatible with WinZip specification. Have to implement custom one.
     */
    public void update(byte[] buf, int offs, int len) {
        for (int i = 0; i < len; i++)
            buf[offs + i] = update(buf[offs + i]);
    }

    public byte update(byte b) {
        if (nonce == iv.length) {
            ivUpdate();
            Quietly.doRuntime(() -> cipher.update(iv, 0, iv.length, counter));
            nonce = 0;
        }

        byte res = (byte) (b ^ counter[nonce]);
        nonce++;
        return res;
    }

    public int getBlockSize() {
        return cipher.getBlockSize();
    }

    private void ivUpdate() {
        for (int i = 0; i < iv.length; i++) {
            iv[i]++;

            if (iv[i] != 0)
                break;
        }
    }

}
