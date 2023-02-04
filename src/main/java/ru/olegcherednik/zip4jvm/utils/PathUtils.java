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
import org.apache.commons.collections4.IterableUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Stream;

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
        requireNotNull(path, "PathUtils.path");
        requireExists(path);

        return Files.isDirectory(path) ? IterableUtils.toList(Files.newDirectoryStream(path))
                                       : Collections.singletonList(path);
    }

    public static Map<Path, String> getRelativeContent(Path path) throws IOException {
        if (path == null || !Files.exists(path))
            return Collections.emptyMap();

        if (Files.isSymbolicLink(path))
            return getSymlinkRelativeContent(path);
        if (Files.isRegularFile(path))
            return getRegularFileRelativeContent(path, getFileName(path));
        if (Files.isDirectory(path))
            return getDirectoryRelativeContent(path);

        return Collections.emptyMap();
    }

    private static Map<Path, String> getRegularFileRelativeContent(Path path, String fileName) {
        if (DS_STORE.equalsIgnoreCase(fileName) || DS_STORE.equalsIgnoreCase(getFileName(path)))
            return Collections.emptyMap();

        return Collections.singletonMap(path, ZipUtils.normalizeFileName(fileName));
    }

    private static Map<Path, String> getDirectoryRelativeContent(Path path) throws IOException {
        if (isEmptyDirectory(path))
            return Collections.singletonMap(path, path.getFileName().toString());

        Map<Path, String> map = new TreeMap<>();

        try (Stream<Path> paths = Files.walk(path)) {
            paths.filter(p -> Files.isRegularFile(p) || isEmptyDirectory(p))
                 .forEach(p -> map.computeIfAbsent(p, other -> getNormalizeRelativePath(path, other)));
        }

        return Collections.unmodifiableMap(map);
    }

    private static String getNormalizeRelativePath(Path path, Path other) {
        return ZipUtils.normalizeFileName(path.getParent().relativize(other).toString());
    }

    private static String getFileName(Path path) {
        return path.getFileName().toString();
    }

    private static Map<Path, String> getSymlinkRelativeContent(Path path) throws IOException {
        Path symlinkTarget = getSymbolicLinkTarget(path);

        if (Files.isRegularFile(symlinkTarget))
            return getRegularFileRelativeContent(symlinkTarget, getFileName(path));
        if (Files.isDirectory(symlinkTarget)) {
            Map<Path, String> pathFileName = new HashMap<>();

            for (Path child : getDirectoryContent(symlinkTarget)) {
                Map<Path, String> map = new TreeMap<>(getRelativeContent(child));

                for (Path key : map.keySet())
                    map.put(key, path.getFileName() + "/" + map.get(key));

                pathFileName.putAll(map);
            }

            return Collections.unmodifiableMap(pathFileName);
        }

        log.warn("not supported symlink type: " + path);
        return Collections.emptyMap();
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

    private static boolean isEmptyDirectory(Path path) {
        try {
            return Files.isDirectory(path) && Files.list(path).count() == 0;
        } catch(IOException e) {
            return false;
        }
    }

}
