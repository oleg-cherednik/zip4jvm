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
package ru.olegcherednik.zip4jvm.model;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

/**
 * see 4.4.2
 *
 * @author Oleg Cherednik
 * @since 16.10.2019
 */
@Getter
@EqualsAndHashCode(doNotUseGetters = true)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class Version {

    public static final Version NULL = new Version(FileSystem.UNKNOWN, 0);

    private final FileSystem fileSystem;
    private final int zipSpecificationVersion;

    public static Version of(int data) {
        return new Version(Version.FileSystem.parseCode(data >> 8), data & 0xFF);
    }

    public static Version of(FileSystem fileSystem, int zipSpecificationVersion) {
        if (Optional.ofNullable(fileSystem).orElse(FileSystem.UNKNOWN) == FileSystem.UNKNOWN)
            return NULL;
        return new Version(fileSystem, zipSpecificationVersion);
    }

    public int getData() {
        return fileSystem.getCode() << 8 | zipSpecificationVersion & 0xFF;
    }

    @Override
    public String toString() {
        return this == NULL ? "<null>" : fileSystem.title + " / " + zipSpecificationVersion / 10.;
    }

    @Getter
    @RequiredArgsConstructor(access = AccessLevel.PACKAGE)
    public enum FileSystem {
        MS_DOS_OS2_NT_FAT(0, "MS-DOS, OS/2, NT FAT"),
        AMIGA(1, "Amiga"),
        OPEN_VMS(2, "VMS"),
        UNIX(3, "Unix"),
        VM_CMS(4, "VM/CMS"),
        ATARI_ST(5, "Atari ST"),
        OS2_NT_HPFS(6, "OS/2, NT HPFS"),
        MACINTOSH_HFS(7, "Macintosh HFS"),
        Z_SYSTEM(8, "Z-System"),
        CP_M(9, "CP/M"),
        TOPS_20(10, "TOPS-20"),
        NTFS(11, "NTFS"),
        SMS_QDOS(12, "SMS/QDOS"),
        ACRON_RISC(13, "Acorn RISC OS"),
        WIN32_VFAT(14, "Win32 VFAT"),
        MVS(15, "MVS"),
        BE_OS(16, "BeOS"),
        TANDEM(17, "Tandem NSK"),
        TANDEM_NSK(18, "Tandem NSK"),
        MAC_OSX(19, "Mac OS X"),
        UNKNOWN(255, "unknown");

        private final int code;
        private final String title;

        public static FileSystem parseCode(int code) {
            for (FileSystem fileSystem : values())
                if (fileSystem.code == code)
                    return fileSystem;
            return UNKNOWN;
        }
    }
}
