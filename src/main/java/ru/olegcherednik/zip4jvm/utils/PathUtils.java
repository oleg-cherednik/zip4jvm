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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.TreeMap;

import static ru.olegcherednik.zip4jvm.utils.ValidationUtils.requireExists;
import static ru.olegcherednik.zip4jvm.utils.ValidationUtils.requireNotNull;

/**
 * @author Oleg Cherednik
 * @since 05.09.2019
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PathUtils {

    public static long size(Path path) {
        try {
            return Files.size(path);
        } catch(IOException ignore) {
            return 0;
        }
    }

    public static Map<Path, String> getRelativeContent(Path path) throws IOException {
        requireNotNull(path, "PathUtils.path");
        requireExists(path);

        Map<Path, String> pathFileName = new TreeMap<>();

        if (Files.isRegularFile(path))
            pathFileName.put(path, ZipUtils.normalizeFileName(path.getFileName().toString()));
        else if (Files.isDirectory(path)) {
            if (isEmptyDirectory(path))
                pathFileName.put(path, path.getFileName().toString());
            else {
                Files.walk(path)
                     .filter(p -> Files.isRegularFile(p) || isEmptyDirectory(p))
                     .forEach(p -> pathFileName.putIfAbsent(p, ZipUtils.normalizeFileName(path.getParent().relativize(p).toString())));
            }
        }

        return pathFileName;
    }

    private static boolean isEmptyDirectory(Path path) {
        try {
            return Files.isDirectory(path) && Files.list(path).count() == 0;
        } catch(IOException e) {
            return false;
        }
    }

}
