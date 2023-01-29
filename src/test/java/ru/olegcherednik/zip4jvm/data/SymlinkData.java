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
package ru.olegcherednik.zip4jvm.data;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static ru.olegcherednik.zip4jvm.TestData.dirSrcData;
import static ru.olegcherednik.zip4jvm.TestData.dirSrcSymlink;
import static ru.olegcherednik.zip4jvm.TestData.fileDucati;
import static ru.olegcherednik.zip4jvm.TestData.fileHonda;
import static ru.olegcherednik.zip4jvm.TestData.fileNameDucati;
import static ru.olegcherednik.zip4jvm.TestData.symlinkAbsDirData;
import static ru.olegcherednik.zip4jvm.TestData.symlinkAbsFileDucati;
import static ru.olegcherednik.zip4jvm.TestData.symlinkAbsFileHonda;
import static ru.olegcherednik.zip4jvm.TestData.symlinkRelDirData;
import static ru.olegcherednik.zip4jvm.TestData.symlinkRelFileDucati;
import static ru.olegcherednik.zip4jvm.TestData.symlinkRelFileHonda;

/**
 * @author Oleg Cherednik
 * @since 22.01.2023
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SymlinkData {

    public static void createSymlinkData() throws IOException {
        Files.createDirectories(dirSrcSymlink);

        Path destFileDucati = dirSrcSymlink.resolve(fileNameDucati);
        Files.copy(fileDucati, destFileDucati);

        createRelativeSymlink(symlinkRelFileDucati, destFileDucati);
        createRelativeSymlink(symlinkRelFileHonda, fileHonda);
        createRelativeSymlink(symlinkRelDirData, dirSrcData);

        createAbsoluteSymlink(symlinkAbsFileDucati, destFileDucati);
        createAbsoluteSymlink(symlinkAbsFileHonda, fileHonda);
        createAbsoluteSymlink(symlinkAbsDirData, dirSrcData);

        int a = 0;
        a++;
//        Files.copy(fileDucati, dirSrcSymlink.resolve(fileDucati.getFileName()));
//        Path dataDir = Paths.get("src/test/resources/symlink").toAbsolutePath();
//
//        Files.walk(dataDir).forEach(path -> {
//            try {
//                if (Files.isDirectory(path))
//                    Files.createDirectories(dirSrcSymlink.resolve(dataDir.relativize(path)));
//                else if (Files.isRegularFile(path))
//                    Files.copy(path, dirSrcSymlink.resolve(dataDir.relativize(path)));
//            } catch(IOException e) {
//                e.printStackTrace();
//            }
//        });

//        assertThatDirectory(dirSrcSymlink).matches(rootAssert);
    }

    private static void createRelativeSymlink(Path symlink, Path target) throws IOException {
        Files.createSymbolicLink(symlink, symlink.getParent().relativize(target));
    }

    private static void createAbsoluteSymlink(Path symlink, Path target) throws IOException {
        Files.createSymbolicLink(symlink, target);
    }

}
