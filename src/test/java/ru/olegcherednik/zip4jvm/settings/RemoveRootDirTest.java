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
package ru.olegcherednik.zip4jvm.settings;

import ru.olegcherednik.zip4jvm.BaseTest;
import ru.olegcherednik.zip4jvm.ZipIt;
import ru.olegcherednik.zip4jvm.model.settings.ZipSettings;

import org.apache.commons.io.FileUtils;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;

import static ru.olegcherednik.zip4jvm.TestData.dirCars;
import static ru.olegcherednik.zip4jvm.TestData.dirNameCars;
import static ru.olegcherednik.zip4jvm.TestData.fileNameZipSrc;
import static ru.olegcherednik.zip4jvm.TestDataAssert.dirCarsAssert;
import static ru.olegcherednik.zip4jvm.assertj.Zip4jvmAssertions.assertThatZipFile;

/**
 * This test checks {@link ZipSettings#isRemoveRootDir()} option.
 *
 * @author Oleg Cherednik
 * @since 03.08.2026
 */
@Test
public class RemoveRootDirTest extends BaseTest {

    public void shouldKeepRootDirWhenDefault() {
        Path zip = getZip();
        ZipIt.zip(zip).add(dirCars);

        assertThatZipFile(zip)
                .isSolid().root().hasOnlyDirectories(1)
                .withDirectory(dirNameCars, dirCarsAssert);
    }

    public void shouldKeepRootDirWhenRemoveRootDirFalse() {
        ZipSettings settings = ZipSettings.builder().removeRootDir(false).build();

        Path zip = getZip();
        ZipIt.zip(zip).settings(settings).add(dirCars);

        assertThatZipFile(zip)
                .isSolid().root().hasOnlyDirectories(1)
                .withDirectory(dirNameCars, dirCarsAssert);
    }

    public void shouldRemoveRootDirWhenRemoveRootDirTrue() {
        ZipSettings settings = ZipSettings.builder().removeRootDir(true).build();

        Path zip = getZip();
        ZipIt.zip(zip).settings(settings).add(dirCars);

        assertThatZipFile(zip)
                .isSolid()
                .root().matches(dirCarsAssert);
    }

    /**
     * We expect, that only <tt>root</tt> dir name will be ignored, but keep <tt>cars</tt>.
     * <p>
     * E.g. we add a <tt>cars</tt> directory:
     * <pre>
     * >/-
     * > |-- root
     * > |    |-- cars
     * > |    |    |-- bentley-continental.jpg
     * > |    |    |-- ferrari-458-italia.jpg
     * > |    |    |-- wiesmann-gt-mf5.jpg
     * </pre>
     * As result, we have the following zip file content:
     * <pre>
     * >/-
     * > |-- cars
     * > |    |-- bentley-continental.jpg
     * > |    |-- ferrari-458-italia.jpg
     * > |    |-- wiesmann-gt-mf5.jpg
     * </pre>
     */
    public void shouldRemoveFirstRootDirWhenRemoveRootDirTrueAndMultipleRootDirs() throws IOException {
        ZipSettings settings = ZipSettings.builder().removeRootDir(true).build();

        Path dir = getTestRoot();
        Path dstDir = dir.resolve("dst");
        Path srcDir = dir.resolve("src");
        Path rootDir = srcDir.resolve("root");
        Path carsDir = rootDir.resolve(dirNameCars);

        FileUtils.copyDirectory(dirCars.toFile(), carsDir.toFile());

        Path zip = dstDir.resolve(fileNameZipSrc);
        ZipIt.zip(zip).settings(settings).add(rootDir);

        assertThatZipFile(zip)
                .isSolid().root().hasOnlyDirectories(1)
                .withDirectory(dirNameCars, dirCarsAssert);
    }

}
