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
package ru.olegcherednik.zip4jvm.crypto.strong;

import ru.olegcherednik.zip4jvm.io.ByteOrder;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.codec.digest.PureJavaCrc32;

import java.util.Collections;
import java.util.List;
import java.util.zip.Checksum;

/**
 * see 7.2.4
 *
 * @author Oleg Cherednik
 * @since 11.10.2019
 */
@Getter
@Setter
public class DecryptionHeader {

    // TODO if 0 - then CRC32 + 64bit FileSize should be used to decrypt data
    // size:2 - size of initialization vector (n)
    // size:n - initialization vector for this file
    private byte[] iv;
    // size:4 - size of remaining decryption header data
    // size:2 - version of this record (should be 3)
    private int version;
    // size:2 - encryption algorithm identifier
    private int encryptionAlgorithmCode;
    private EncryptionAlgorithm encryptionAlgorithm;
    // size:2 - bit length of encryption key
    private int bitLength;
    // size:2 - Processing flags
    private Flags flags;
    // size:2 - size of Encrypted Random Data (m)
    // size:m - encrypted random data
    private byte[] encryptedRandomData;
    // size:4 - number of recipients (n)
    // size:2 - hash algorithm to be used to calculate Public Key hash (absent for password based encryption)
    private int hashAlgorithmCode;
    private HashAlgorithm hashAlgorithm;
    // size:2 - size of Public Key hash (absent for password based encryption) (p)
    // size:n - Recipient List Element (absent for password based encryption)
    private List<Recipient> recipients = Collections.emptyList();
    // size:2 - size of password validation data (k)
    // size:k - password validation data + last 4 bytes crc32
    private byte[] passwordValidationData;

    public void setEncryptionAlgorithm(int code) {
        encryptionAlgorithmCode = code;
        encryptionAlgorithm = EncryptionAlgorithm.parseCode(code);
    }

    public void setHashAlgorithm(int code) {
        hashAlgorithmCode = code;
        hashAlgorithm = HashAlgorithm.parseCode(code);
    }

    @SuppressWarnings("NewMethodNamingConvention")
    public static long getActualCrc32(byte[] passwordValidationData) {
        Checksum crc = new PureJavaCrc32();
        crc.update(passwordValidationData, 0, passwordValidationData.length - 4);
        return crc.getValue();
    }

    @SuppressWarnings("NewMethodNamingConvention")
    public static long getExpectedCrc32(byte[] passwordValidationData, ByteOrder byteOrder) {
        return byteOrder.getLong(passwordValidationData, passwordValidationData.length - 4, 4);
    }

}
