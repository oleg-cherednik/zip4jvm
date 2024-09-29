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
import ru.olegcherednik.zip4jvm.exception.Zip4jvmException;
import ru.olegcherednik.zip4jvm.io.out.data.DataOutput;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;
import ru.olegcherednik.zip4jvm.utils.quitely.Quietly;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import java.io.IOException;

import static ru.olegcherednik.zip4jvm.crypto.aes.AesEngine.MAC_SIZE;

/**
 * @author Oleg Cherednik
 * @since 13.08.2019
 */
public final class AesEncoder implements Encoder {

    private final byte[] salt;
    private final byte[] passwordChecksum;
    private final AesEngine engine;

    public static AesEncoder create(ZipEntry zipEntry) {
        return Quietly.doQuietly(() -> {
            AesStrength strength = AesEngine.getStrength(zipEntry.getEncryptionMethod());
            byte[] salt = strength.generateSalt();
            byte[] key = AesEngine.createKey(zipEntry.getPassword(), salt, strength);

            Cipher cipher = AesEngine.createCipher(strength.createSecretKeyForCipher(key));
            Mac mac = AesEngine.createMac(strength.createSecretKeyForMac(key));
            byte[] passwordChecksum = strength.createPasswordChecksum(key);

            return new AesEncoder(cipher, mac, salt, passwordChecksum);
        });
    }

    // ---------- Encoder ----------

    @Override
    public void writeEncryptionHeader(DataOutput out) throws IOException {
        out.writeBytes(salt);
        out.writeBytes(passwordChecksum);
    }

    @Override
    public void close(DataOutput out) throws IOException {
        out.write(engine.getMac(), 0, MAC_SIZE);
    }

    // ---------- Encrypt ----------

    @Override
    public void encrypt(byte[] buf, int offs, int len) {
        engine.encrypt(buf, offs, len);
    }

    // ----------

    @SuppressWarnings({ "AssignmentOrReturnOfFieldWithMutableType", "MethodCanBeVariableArityMethod" })
    private AesEncoder(Cipher cipher, Mac mac, byte[] salt, byte[] passwordChecksum) {
        this.salt = salt;
        this.passwordChecksum = passwordChecksum;
        engine = new AesEngine(cipher, mac);
    }

}
