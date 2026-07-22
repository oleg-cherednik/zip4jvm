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
package ru.olegcherednik.zip4jvm.crypto.strong.aes;

import ru.olegcherednik.zip4jvm.crypto.strong.StrongDecoder;
import ru.olegcherednik.zip4jvm.exception.IncorrectPasswordException;
import ru.olegcherednik.zip4jvm.exception.IncorrectZipEntryPasswordException;
import ru.olegcherednik.zip4jvm.io.in.DataInput;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;

/**
 * @author Oleg Cherednik
 * @since 04.12.2022
 */
public final class StrongAesDecoder extends StrongDecoder {

    @SuppressWarnings("NewMethodNamingConvention")
    public static StrongAesDecoder aes128(ZipEntry zipEntry, DataInput in) {
        return create(zipEntry, in);
    }

    @SuppressWarnings("NewMethodNamingConvention")
    public static StrongAesDecoder aes192(ZipEntry zipEntry, DataInput in) {
        return create(zipEntry, in);
    }

    @SuppressWarnings("NewMethodNamingConvention")
    public static StrongAesDecoder aes256(ZipEntry zipEntry, DataInput in) {
        return create(zipEntry, in);
    }

    private static StrongAesDecoder create(ZipEntry zipEntry, DataInput in) {
        try {
            char[] password = zipEntry.getPassword();
            long compressedSize = zipEntry.getCompressedSize();
            return StrongAesFactory.INSTANCE.createDecoder(password, compressedSize, in);
        } catch (IncorrectPasswordException e) {
            throw new IncorrectZipEntryPasswordException(zipEntry.getFileName());
        }
    }

    public StrongAesDecoder(StrongAesCipher cipher, long compressedSize) {
        super(cipher, compressedSize);
    }

}
