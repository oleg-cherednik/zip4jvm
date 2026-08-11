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
package ru.olegcherednik.zip4jvm.utils;

import ru.olegcherednik.zip4jvm.utils.quitely.Quietly;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.codec.digest.PureJavaCrc32;
import org.apache.commons.io.IOUtils;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.zip.CheckedInputStream;
import java.util.zip.Checksum;

/**
 * @author Oleg Cherednik
 * @since 31.10.2024
 */
@SuppressWarnings("NewMethodNamingConvention")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ChecksumUtils {

    @SuppressWarnings("PMD.EmptyControlStatement")
    public static long crc32(InputStream is) {
        return Quietly.doRuntime(() -> {
            Checksum crc32 = new PureJavaCrc32();
            InputStream bis = is instanceof BufferedInputStream ? is : new BufferedInputStream(is);

            try (InputStream in = new CheckedInputStream(bis, crc32)) {
                byte[] buf = new byte[1024];

                while (in.read(buf) != IOUtils.EOF) {
                    // read file in completely
                }
            }

            return crc32.getValue();
        });
    }

    public static long crc32(String str) {
        return Quietly.doRuntime(() -> {
            byte[] buf = str.getBytes(StandardCharsets.UTF_8);
            Checksum crc32 = new PureJavaCrc32();
            crc32.update(buf, 0, buf.length);
            return crc32.getValue();
        });
    }

}
