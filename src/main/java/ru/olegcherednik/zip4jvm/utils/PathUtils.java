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
package ru.olegcherednik.zip4jvm.utils;

import ru.olegcherednik.zip4jvm.exception.Zip4jvmException;
import ru.olegcherednik.zip4jvm.model.src.SrcZip;
import ru.olegcherednik.zip4jvm.utils.function.InputStreamSupplier;
import ru.olegcherednik.zip4jvm.utils.quitely.Quietly;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileTime;
import java.nio.file.attribute.PosixFilePermission;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Oleg Cherednik
 * @since 05.09.2019
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PathUtils {

    public static final String DS_STORE = ".DS_Store";
    public static final char SLASH = '/';
    public static final char BACK_SLASH = '\\';

    public static long size(Path path) {
        return Quietly.doRuntime(() -> Files.size(path));
    }

    public static List<Path> list(Path dir) {
        assert Files.exists(dir);
        assert Files.isDirectory(dir);

        try (Stream<Path> stream = Files.list(dir)) {
            return stream.collect(Collectors.toList());
        } catch (IOException e) {
            throw new Zip4jvmException(e);
        }
    }

    public static String getName(Path path) {
        return path.getFileName().toString();
    }

    public static String getOffsStr(long offs) {
        return String.format("offs: %s (0x%s)", offs, Long.toHexString(offs));
    }

    public static String getOffsStr(long absOffs, long diskOffs, int diskNo) {
        return String.format("absOffs: %s (0x%s) | diskOffs: %s (0x%s) | disk: %s",
                             absOffs, Long.toHexString(absOffs),
                             diskOffs, Long.toHexString(diskOffs),
                             diskNo);
    }

    public static InputStreamSupplier newInputStreamSupplier(Path path) {
        return new InputStreamSupplier() {
            @Override
            public long getSize() {
                return size(path);
            }

            @Override
            public InputStream get() {
                return newInputStream(path);
            }
        };
    }

    public static InputStream newInputStream(Path path) {
        return Quietly.doRuntime(() -> Files.newInputStream(path));
    }

    public static OutputStream newOutputStream(Path path) {
        return Quietly.doRuntime(() -> Files.newOutputStream(path));
    }

    public static boolean deleteIfExists(Path path) {
        return Quietly.doRuntime(() -> Files.deleteIfExists(path));
    }

    public static void deleteIfExists(SrcZip srcZip) {
        srcZip.getDisks().forEach(disk -> deleteIfExists(disk.getPath()));
    }

    public static Path createDirectories(Path dir, FileAttribute<?>... attrs) {
        return Quietly.doRuntime(() -> Files.createDirectories(dir, attrs));
    }

    public static Path setPosixFilePermissions(Path dir, Set<PosixFilePermission> perms) {
        return Quietly.doRuntime(() -> Files.setPosixFilePermissions(dir, perms));
    }

    public static Path move(Path source, Path target, CopyOption... options) {
        return Quietly.doRuntime(() -> Files.move(source, target, options));
    }

    public static Path setLastModifiedTime(Path path, FileTime time) {
        return Quietly.doRuntime(() -> Files.setLastModifiedTime(path, time));
    }

    public static void copyByteArray(Path out, byte[] buf, OpenOption... options) {
        Quietly.doRuntime(() -> Files.write(out, buf, options));
    }

}
