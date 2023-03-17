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
package ru.olegcherednik.zip4jvm.assertj;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import ru.olegcherednik.zip4jvm.model.ExternalFileAttributes;

import static ru.olegcherednik.zip4jvm.model.ExternalFileAttributes.PROP_OS_NAME;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class ZipEntryUtils {

    public static boolean isDirectory(ZipArchiveEntry entry) {
        return entry.isDirectory();
    }

    public static boolean isRegularFile(ZipArchiveEntry entry) {
        return !isDirectory(entry) && !getExternalAttributes(entry).isSymlink();
    }

    public static boolean isSymlink(ZipArchiveEntry entry) {
        return !isDirectory(entry) && getExternalAttributes(entry).isSymlink();

    }

    private static ExternalFileAttributes getExternalAttributes(ZipArchiveEntry entry) {
        long attr = entry.getExternalAttributes();

        return ExternalFileAttributes.build(PROP_OS_NAME)
                                     .readFrom(new byte[] {
                                             (byte)(attr & 0xFF),
                                             (byte)((attr >> 8) & 0xFF),
                                             (byte)((attr >> 16) & 0xFF),
                                             (byte)((attr >> 24) & 0xFF)
                                     });
    }
}
