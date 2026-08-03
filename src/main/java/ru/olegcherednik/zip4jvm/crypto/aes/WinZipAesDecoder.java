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

import ru.olegcherednik.zip4jvm.crypto.Decoder;
import ru.olegcherednik.zip4jvm.exception.Zip4jvmException;
import ru.olegcherednik.zip4jvm.io.in.DataInput;
import ru.olegcherednik.zip4jvm.utils.quitely.Quietly;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Objects;
import javax.crypto.Mac;

import static ru.olegcherednik.zip4jvm.crypto.aes.factory.BaseWinZipAesFactory.MAC_SIZE;

/**
 * @author Oleg Cherednik
 * @since 13.08.2019
 */
@RequiredArgsConstructor
public final class WinZipAesDecoder implements Decoder {

    private final WinZipAesCipher cipher;
    private final Mac mac;
    @Getter
    private final long compressedSize;

    // ---------- Decoder ----------

    @Override
    public int decrypt(byte[] buf, int offs, int len) {
        mac.update(buf, offs, len);
        cipher.update(buf, offs, len);
        return len;
    }

    @Override
    public int getBlockSize() {
        return cipher.getBlockSize();
    }

    @Override
    public void close(DataInput in) {
        checkMessageAuthenticationCode(in);
    }

    // ----------

    private void checkMessageAuthenticationCode(DataInput in) {
        byte[] expected = Quietly.doRuntime(() -> in.readBytes(MAC_SIZE));
        byte[] actual = ArrayUtils.subarray(mac.doFinal(), 0, MAC_SIZE);

        if (!Objects.deepEquals(expected, actual))
            throw new Zip4jvmException("Message Authentication Code (MAC) is not correct");
    }

}
