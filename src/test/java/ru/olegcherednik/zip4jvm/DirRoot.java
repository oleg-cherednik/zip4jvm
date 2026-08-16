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

package ru.olegcherednik.zip4jvm;

import lombok.Getter;

import java.nio.file.Path;

import static ru.olegcherednik.zip4jvm.TestData.dirTime;
import static ru.olegcherednik.zip4jvm.TestData.fileNameZipSrc;

/**
 * @author Oleg Cherednik
 * @since 16.08.2026
 */
@Getter
public class DirRoot {

    private final Path path = generateSubDirNameWithTime();

    public void createDir() {
        Zip4jvmSuite.createDir(path);
    }

    public void removeDir() {
        Zip4jvmSuite.removeDir(path);
    }

    public Path getTestRoot() {
        return subDirNameAsMethodName(path);
    }

    // ---------- static ----------

    public static Path generateSubDirNameWithTime() {
        Class<?> cls = CallerInfo.getCallerClass(DirRoot.class);
        String baseDir = Zip4jvmSuite.class.getPackage().getName();
        String[] parts = cls.getName().substring(baseDir.length() + 1).split("\\.");
        Path path = dirTime;

        for (String part : parts)
            path = path.resolve(part);

        return path;
    }

    public static Path subDirNameAsMethodName(Path rootDir) {
        String methodName = CallerInfo.getCallerMethodName(DirRoot.class);
        return Zip4jvmSuite.createDir(rootDir.resolve(methodName));
    }

    @SuppressWarnings("DynamicRegexReplaceableByCompiledPattern")
    public static Path subDirNameAsRelativePathToRoot(Path rootDir, Path zip) {
        Path path;

        zip = zip.toAbsolutePath();

        if (zip.toString().contains("resources")) {
            Path parent = zip.getParent();

            while (!"resources".equalsIgnoreCase(parent.getFileName().toString())
                    && !"resources".equalsIgnoreCase(parent.getParent().getFileName().toString())) {
                parent = parent.getParent();
            }

            path = parent.relativize(zip);
        } else
            path = dirTime.relativize(zip);

        String dirName = path.toString().replaceAll("\\\\", "_");
        return rootDir.resolve(dirName);
    }

}
