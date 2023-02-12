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
import ru.olegcherednik.zip4jvm.exception.Zip4jvmException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
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

    public static long size(Path path) {
        try {
            return Files.size(path);
        } catch(IOException ignore) {
            return 0;
        }
    }

    public static List<Path> getDirectoryContent(Path dir) {
        assert Files.exists(dir);
        assert Files.isDirectory(dir);

        try (Stream<Path> stream = Files.list(dir)) {
            return stream.collect(Collectors.toList());
        } catch (IOException e) {
            throw new Zip4jvmException(e);
        }
    }

    public static String getFileName(Path path) {
        return path.getFileName().toString();
    }

}
