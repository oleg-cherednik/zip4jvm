/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
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
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;
import ru.olegcherednik.zip4jvm.utils.quitely.Quietly;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ArrayUtils;

import java.io.IOException;
import java.util.Objects;
import javax.crypto.Mac;

import static ru.olegcherednik.zip4jvm.crypto.aes.WinZipAesFactory.MAC_SIZE;

/**
 * @author Oleg Cherednik
 * @since 13.08.2019
 */
@RequiredArgsConstructor
public final class AesDecoder implements Decoder {

    private final WinZipCipher cipher;
    private final Mac mac;
    @Getter
    private final long compressedSize;

    @SuppressWarnings("NewMethodNamingConvention")
    public static AesDecoder create128(ZipEntry zipEntry, DataInput in) throws IOException {
        return create(zipEntry, AesStrength.S128, in);
    }

    @SuppressWarnings("NewMethodNamingConvention")
    public static AesDecoder create192(ZipEntry zipEntry, DataInput in) throws IOException {
        return create(zipEntry, AesStrength.S192, in);
    }

    @SuppressWarnings("NewMethodNamingConvention")
    public static AesDecoder create256(ZipEntry zipEntry, DataInput in) throws IOException {
        return create(zipEntry, AesStrength.S256, in);
    }

    private static AesDecoder create(ZipEntry zipEntry, AesStrength strength, DataInput in) {
        char[] password = zipEntry.getPassword();
        long compressedSize = zipEntry.getCompressedSize();
        String fileName = zipEntry.getFileName();
        return new WinZipAesFactory(password, strength).createDecoder(compressedSize, fileName, in);
    }

    // ---------- Decrypt ----------

    @Override
    public int decrypt(byte[] buf, int offs, int len) {
        Quietly.doRuntime(() -> {
            mac.update(buf, offs, len);
            cipher.update(buf, offs, len);
        });

        return len;
    }

    // ---------- Decoder ----------

    @Override
    public int getBlockSize() {
        return cipher.getBlockSize();
    }

    @Override
    public void close(DataInput in) throws IOException {
        checkMessageAuthenticationCode(in);
    }

    // ----------

    private void checkMessageAuthenticationCode(DataInput in) throws IOException {
        byte[] expected = in.readBytes(MAC_SIZE);
        byte[] actual = ArrayUtils.subarray(mac.doFinal(), 0, MAC_SIZE);

        if (!Objects.deepEquals(expected, actual))
            throw new Zip4jvmException("Message Authentication Code (MAC) is not correct");
    }

}
