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
package ru.olegcherednik.zip4jvm.crypto.strong.desede;

import ru.olegcherednik.zip4jvm.crypto.Encoder;
import ru.olegcherednik.zip4jvm.crypto.strong.DecryptionHeader;
import ru.olegcherednik.zip4jvm.io.out.DataOutput;
import ru.olegcherednik.zip4jvm.io.writers.DecryptionHeaderWriter;
import ru.olegcherednik.zip4jvm.utils.quitely.Quietly;

import lombok.RequiredArgsConstructor;

import javax.crypto.Cipher;

/**
 * Writes PKWARE "strong encryption" 3DES entries (see APPNOTE 7.2), i.e. the
 * encryption counterpart of {@link StrongTripleDesCipherFactory}. 3DES is a
 * block cipher operating in CBC mode, so this encoder buffers incoming bytes
 * into {@link #BLOCK_SIZE}-byte blocks and appends a final PKCS#5 padding block
 * on {@link #close(DataOutput)}.
 *
 * @author Oleg Cherednik
 * @since 24.07.2026
 */
@RequiredArgsConstructor
public final class StrongTripleDesEncoder implements Encoder {

    public static final int BLOCK_SIZE = 8;

    private final DecryptionHeader decryptionHeader;
    private final Cipher cipher;
    private final byte[] block = new byte[BLOCK_SIZE];

    private int blockLen;

    // ---------- Encoder ----------

    @Override
    public void encrypt(byte b, DataOutput out) {
        block[blockLen++] = b;

        if (blockLen == BLOCK_SIZE) {
            out.writeBytes(Quietly.doRuntime(() -> cipher.update(block)));
            blockLen = 0;
        }
    }

    @Override
    public void writeEncryptionHeaderWhenRequired(DataOutput out) {
        new DecryptionHeaderWriter(decryptionHeader).write(out);
    }

    @Override
    public void close(DataOutput out) {
        int pad = BLOCK_SIZE - blockLen;

        for (int i = blockLen; i < BLOCK_SIZE; i++)
            block[i] = (byte) pad;

        out.writeBytes(Quietly.doRuntime(() -> cipher.doFinal(block)));
        blockLen = 0;
    }

}
