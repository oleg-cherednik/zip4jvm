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
package ru.olegcherednik.zip4jvm.crypto.aes.strong.cd;

import ru.olegcherednik.zip4jvm.crypto.Decoder;
import ru.olegcherednik.zip4jvm.crypto.aes.strong.StrongAesCipher;
import ru.olegcherednik.zip4jvm.crypto.aes.AesStrength;
import ru.olegcherednik.zip4jvm.crypto.strong.DecryptionHeader;
import ru.olegcherednik.zip4jvm.io.ByteOrder;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author Oleg Cherednik
 * @since 21.11.2024
 */
@RequiredArgsConstructor
public final class CentralDirectoryStrongAesDecoder implements Decoder {

    private final StrongAesCipher cipher;
    @Getter
    private final long compressedSize;

    @SuppressWarnings("NewMethodNamingConvention")
    public static CentralDirectoryStrongAesDecoder aes128(DecryptionHeader decryptionHeader,
                                                          char[] password,
                                                          long compressedSize,
                                                          ByteOrder byteOrder) {
        return create(decryptionHeader, password, AesStrength.S128, compressedSize, byteOrder);
    }

    @SuppressWarnings("NewMethodNamingConvention")
    public static CentralDirectoryStrongAesDecoder aes192(DecryptionHeader decryptionHeader,
                                                          char[] password,
                                                          long compressedSize,
                                                          ByteOrder byteOrder) {
        return create(decryptionHeader, password, AesStrength.S192, compressedSize, byteOrder);
    }

    @SuppressWarnings("NewMethodNamingConvention")
    public static CentralDirectoryStrongAesDecoder aes256(DecryptionHeader decryptionHeader,
                                                          char[] password,
                                                          long compressedSize,
                                                          ByteOrder byteOrder) {
        return create(decryptionHeader, password, AesStrength.S256, compressedSize, byteOrder);
    }

    public static CentralDirectoryStrongAesDecoder create(DecryptionHeader decryptionHeader,
                                                          char[] password,
                                                          AesStrength strength,
                                                          long compressedSize,
                                                          ByteOrder byteOrder) {
//        char[] password = zipEntry.getPassword();
//        long compressedSize = zipEntry.getCompressedSize();
//        String fileName = zipEntry.getFileName();
        return new CentralDirectoryStrongAesFactory(password).createDecoder(compressedSize,
                                                                            decryptionHeader,
                                                                            strength,
                                                                            byteOrder);
    }

    // ---------- Decoder ----------

    @Override
    public int getBlockSize() {
        return cipher.getBlockSize();
    }

    // ---------- Decrypt ----------

    @Override
    public int decrypt(byte[] buf, int offs, int len) {
        return cipher.update(buf, offs, len);
    }

}
