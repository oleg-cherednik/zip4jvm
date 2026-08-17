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

import ru.olegcherednik.zip4jvm.model.settings.ZipSettings;

import org.testng.annotations.Test;

import java.nio.file.Path;

import static ru.olegcherednik.zip4jvm.TestData.dirBikes;
import static ru.olegcherednik.zip4jvm.TestData.dirCars;
import static ru.olegcherednik.zip4jvm.TestData.dirNameBikes;
import static ru.olegcherednik.zip4jvm.TestData.dirNameCars;
import static ru.olegcherednik.zip4jvm.TestData.fileNameZipSrc;
import static ru.olegcherednik.zip4jvm.TestDataAssert.dirBikesAssert;
import static ru.olegcherednik.zip4jvm.TestDataAssert.dirCarsAssert;
import static ru.olegcherednik.zip4jvm.Zip4jvmSuite.SIZE_1MB;
import static ru.olegcherednik.zip4jvm.Zip4jvmSuite.SIZE_2MB;
import static ru.olegcherednik.zip4jvm.assertj.Zip4jvmAssertions.assertThatZipFile;

/**
 * @author Oleg Cherednik
 * @since 14.03.2019
 */
@Test
public class ZipFolderSplitTest extends BaseTest {

    private final Path zip = resolve(fileNameZipSrc);

    public void shouldCreateNewZipWithFolder() {
        ZipSettings settings = ZipSettings.builder().splitSize(SIZE_1MB).build();

        ZipIt.zip(zip).settings(settings).add(dirCars);

        assertThatZipFile(zip)
                .withParent(dir -> dir.hasRegularFiles(3))
                .root().hasOnlyDirectories(1)
                .directory(dirNameCars).matches(dirCarsAssert);
    }

    @Test(dependsOnMethods = "shouldCreateNewZipWithFolder")
    public void shouldChangeSplitSizeWhenWhenModifySplitZip() {
        ZipSettings settings = ZipSettings.builder().splitSize(SIZE_2MB).build();

        ZipIt.zip(zip).settings(settings).add(dirBikes);

        assertThatZipFile(zip)
                .withParent(dir -> dir.hasRegularFiles(2))
                .root().hasOnlyDirectories(2)
                .withDirectory(dirNameCars, dirCarsAssert)
                .withDirectory(dirNameBikes, dirBikesAssert);
    }

}
