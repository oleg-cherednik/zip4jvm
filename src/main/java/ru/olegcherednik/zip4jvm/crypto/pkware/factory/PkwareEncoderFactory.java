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
package ru.olegcherednik.zip4jvm.crypto.pkware.factory;

import ru.olegcherednik.zip4jvm.crypto.Encoder;
import ru.olegcherednik.zip4jvm.crypto.EncoderFactory;
import ru.olegcherednik.zip4jvm.crypto.pkware.PkwareEncoder;
import ru.olegcherednik.zip4jvm.crypto.pkware.PkwareEngine;
import ru.olegcherednik.zip4jvm.crypto.pkware.PkwareHeader;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import static ru.olegcherednik.zip4jvm.utils.ValidationUtils.requireNotEmpty;

/**
 * @author Oleg Cherednik
 * @since 01.08.2026
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PkwareEncoderFactory implements EncoderFactory {

    public static final PkwareEncoderFactory INSTANCE = new PkwareEncoderFactory();

    // ---------- EncoderFactory ----------

    @Override
    public Encoder createEncoder(ZipEntry zipEntry) {
        requireNotEmpty(zipEntry.getPassword(), zipEntry.getFileName() + ".password");

        PkwareEngine engine = new PkwareEngine(zipEntry.getPassword());
        int key = zipEntry.isDataDescriptorAvailable() ? zipEntry.getLastModifiedTime()
                                                       : (int) zipEntry.getCrc32() >> 16;

        return new PkwareEncoder(engine, PkwareHeader.create(engine, key));
    }
}
