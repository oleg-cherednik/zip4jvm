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
import ru.olegcherednik.zip4jvm.crypto.strong.DecryptionHeader;
import ru.olegcherednik.zip4jvm.io.ByteOrder;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import javax.crypto.Cipher;

/**
 * @author Oleg Cherednik
 * @since 21.11.2024
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class AesCentralDirectoryDecoder implements Decoder {

    private final AesStrongEngine engine;
    @Getter
    private final long compressedSize;

    @SuppressWarnings("NewMethodNamingConvention")
    public static AesCentralDirectoryDecoder create128(DecryptionHeader decryptionHeader,
                                                       char[] password,
                                                       long compressedSize,
                                                       ByteOrder byteOrder) throws IOException {
        return create(decryptionHeader, password, AesStrength.S128, compressedSize, byteOrder);
    }

    @SuppressWarnings("NewMethodNamingConvention")
    public static AesCentralDirectoryDecoder create192(DecryptionHeader decryptionHeader,
                                                       char[] password,
                                                       long compressedSize,
                                                       ByteOrder byteOrder) throws IOException {
        return create(decryptionHeader, password, AesStrength.S192, compressedSize, byteOrder);
    }

    @SuppressWarnings("NewMethodNamingConvention")
    public static AesCentralDirectoryDecoder create256(DecryptionHeader decryptionHeader,
                                                       char[] password,
                                                       long compressedSize,
                                                       ByteOrder byteOrder) throws IOException {
        return create(decryptionHeader, password, AesStrength.S256, compressedSize, byteOrder);
    }

    private static AesCentralDirectoryDecoder create(DecryptionHeader decryptionHeader,
                                                     char[] password,
                                                     AesStrength strength,
                                                     long compressedSize,
                                                     ByteOrder byteOrder) throws IOException {
        Cipher cipher = AesStrongEngine.createCipher(decryptionHeader, password, strength, byteOrder);
        AesStrongEngine engine = new AesStrongEngine(cipher);
        return new AesCentralDirectoryDecoder(engine, compressedSize);
    }

    // ---------- Decoder ----------

    @Override
    public int getBlockSize() {
        return engine.getBlockSize();
    }

    // ---------- Decrypt ----------

    @Override
    public int decrypt(byte[] buf, int offs, int len) {
        return engine.decrypt(buf, offs, len);
    }

}
