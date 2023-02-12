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

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static ru.olegcherednik.zip4jvm.utils.ValidationUtils.requireDirectory;
import static ru.olegcherednik.zip4jvm.utils.ValidationUtils.requireExists;
import static ru.olegcherednik.zip4jvm.utils.ValidationUtils.requireNotNull;

/**
 * @author Oleg Cherednik
 * @since 05.09.2019
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PathUtils {

    public static final String DS_STORE = ".DS_Store";

    public static long size(Path path) {
        try {
            return Files.size(path);
        } catch(IOException ignore) {
            return 0;
        }
    }

    public static List<Path> getDirectoryContent(Path path) throws IOException {
        requireExists(path);
        requireDirectory(path, "PathUtils.path");
        requireNotNull(path, "PathUtils.path");

        try (Stream<Path> stream = Files.list(path)) {
            return stream.collect(Collectors.toList());
        }
    }

    public static String getNormalizeRelativePath(Path path, Path other) {
        return ZipUtils.normalizeFileName(path.getParent().relativize(other).toString());
    }

    public static String getFileName(Path path) {
        return path.getFileName().toString();
    }

    // @NotNull
    public static Path getSymbolicLinkTarget(Path path) throws IOException {
        assert Files.exists(path) : "Symlink target should be real";

        while (Files.isSymbolicLink(path)) {
            Path target = Files.readSymbolicLink(path);
            path = target.isAbsolute() ? Files.readSymbolicLink(path) : path.getParent().resolve(target);
        }

        return path;
    }

    public static boolean isEmptyDirectory(Path path) {
        try {
            return Files.isDirectory(path) && Files.list(path).count() == 0;
        } catch(IOException e) {
            return false;
        }
    }

}
