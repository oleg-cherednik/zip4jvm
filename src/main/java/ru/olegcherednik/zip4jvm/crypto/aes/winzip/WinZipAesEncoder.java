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
package ru.olegcherednik.zip4jvm.crypto.aes.winzip;

import ru.olegcherednik.zip4jvm.crypto.Encoder;
import ru.olegcherednik.zip4jvm.crypto.aes.AesStrength;
import ru.olegcherednik.zip4jvm.io.out.DataOutput;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;
import ru.olegcherednik.zip4jvm.utils.quitely.Quietly;

import lombok.RequiredArgsConstructor;

import java.io.IOException;
import javax.crypto.Mac;

import static ru.olegcherednik.zip4jvm.crypto.aes.winzip.WinZipAesFactory.MAC_SIZE;

/**
 * @author Oleg Cherednik
 * @since 13.08.2019
 */
@RequiredArgsConstructor
public final class WinZipAesEncoder implements Encoder {

    private final byte[] salt;
    private final byte[] passwordChecksum;
    private final WinZipAesCipher cipher;
    private final Mac mac;

    @SuppressWarnings("NewMethodNamingConvention")
    public static WinZipAesEncoder aes128(ZipEntry zipEntry) {
        return create(zipEntry, AesStrength.S128);
    }

    @SuppressWarnings("NewMethodNamingConvention")
    public static WinZipAesEncoder aes192(ZipEntry zipEntry) {
        return create(zipEntry, AesStrength.S192);
    }

    @SuppressWarnings("NewMethodNamingConvention")
    public static WinZipAesEncoder aes256(ZipEntry zipEntry) {
        return create(zipEntry, AesStrength.S256);
    }

    private static WinZipAesEncoder create(ZipEntry zipEntry, AesStrength strength) {
        char[] password = zipEntry.getPassword();
        return new WinZipAesFactory(password, strength).createEncoder();
    }

    // ---------- Encoder ----------

    @Override
    public void writeEncryptionHeader(DataOutput out) throws IOException {
        out.writeBytes(salt);
        out.writeBytes(passwordChecksum);
    }

    @Override
    public void close(DataOutput out) throws IOException {
        out.write(mac.doFinal(), 0, MAC_SIZE);
    }

    // ---------- Encrypt ----------

    @Override
    public byte encrypt(byte b) {
        return Quietly.doRuntime(() -> {
            byte bb = cipher.update(b);
            mac.update(bb);
            return bb;
        });
    }

}
