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

import ru.olegcherednik.zip4jvm.crypto.Encoder;
import ru.olegcherednik.zip4jvm.io.out.DataOutput;

import lombok.RequiredArgsConstructor;

import javax.crypto.Mac;

import static ru.olegcherednik.zip4jvm.crypto.aes.factory.BaseWinZipAesFactory.MAC_SIZE;

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

    // ---------- Encoder ----------

    @Override
    public byte encrypt(byte b) {
        throw new RuntimeException();
    }

    @Override
    public void encrypt(byte b, DataOutput out) {
        byte bb = cipher.update(b);
        mac.update(bb);
        out.write(bb);
    }

    @Override
    public void writeEncryptionHeader(DataOutput out) {
        out.writeBytes(salt);
        out.writeBytes(passwordChecksum);
    }

    @Override
    public void close(DataOutput out) {
        out.write(mac.doFinal(), 0, MAC_SIZE);
    }

}
