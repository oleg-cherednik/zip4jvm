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
import ru.olegcherednik.zip4jvm.exception.IncorrectZipEntryPasswordException;
import ru.olegcherednik.zip4jvm.exception.Zip4jvmException;
import ru.olegcherednik.zip4jvm.io.in.data.DataInput;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;
import ru.olegcherednik.zip4jvm.utils.quitely.Quietly;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ArrayUtils;

import java.io.IOException;
import java.util.Objects;
import javax.crypto.Cipher;
import javax.crypto.Mac;

import static ru.olegcherednik.zip4jvm.crypto.aes.AesEngine.MAC_SIZE;
import static ru.olegcherednik.zip4jvm.crypto.aes.AesEngine.PASSWORD_CHECKSUM_SIZE;

/**
 * @author Oleg Cherednik
 * @since 13.08.2019
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class AesDecoder implements Decoder {

    private final AesEngine engine;
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

    private static AesDecoder create(ZipEntry zipEntry, AesStrength strength, DataInput in) throws IOException {
        byte[] salt = in.readBytes(strength.getSaltSize());
        byte[] key = AesEngine.createKey(zipEntry.getPassword(), salt, strength);

        Cipher cipher = AesEngine.createCipher(strength.createSecretKeyForCipher(key));
        byte[] passwordChecksum = strength.createPasswordChecksum(key);
        checkPasswordChecksum(passwordChecksum, zipEntry, in);

        Mac mac = AesEngine.createMac(strength.createSecretKeyForMac(key));
        AesEngine engine = new AesEngine(cipher, mac);
        long compressedSize = AesEngine.getDataCompressedSize(zipEntry.getCompressedSize(), strength);
        return new AesDecoder(engine, compressedSize);
    }

    // ---------- Decrypt ----------

    @Override
    public int decrypt(byte[] buf, int offs, int len) {
        return engine.decrypt(buf, offs, len);
    }

    // ---------- Decoder ----------

    @Override
    public int getBlockSize() {
        return engine.getBlockSize();
    }

    @Override
    public void close(DataInput in) throws IOException {
        checkMessageAuthenticationCode(in);
    }

    // ----------

    private void checkMessageAuthenticationCode(DataInput in) throws IOException {
        byte[] expected = in.readBytes(MAC_SIZE);
        byte[] actual = ArrayUtils.subarray(engine.getMac(), 0, MAC_SIZE);

        if (!Objects.deepEquals(expected, actual))
            throw new Zip4jvmException("Message Authentication Code (MAC) is not correct");
    }

    private static void checkPasswordChecksum(byte[] actual, ZipEntry zipEntry, DataInput in) throws IOException {
        byte[] expected = in.readBytes(PASSWORD_CHECKSUM_SIZE);

        if (!Objects.deepEquals(expected, actual))
            throw new IncorrectZipEntryPasswordException(zipEntry.getFileName());
    }

}
