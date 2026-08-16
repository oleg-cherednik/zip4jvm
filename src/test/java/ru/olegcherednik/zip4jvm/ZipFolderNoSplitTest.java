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

import org.testng.annotations.Test;

import java.nio.file.Path;

import static ru.olegcherednik.zip4jvm.TestData.dirBikes;
import static ru.olegcherednik.zip4jvm.TestData.dirCars;
import static ru.olegcherednik.zip4jvm.TestData.dirEmpty;
import static ru.olegcherednik.zip4jvm.TestData.dirNameBikes;
import static ru.olegcherednik.zip4jvm.TestData.dirNameCars;
import static ru.olegcherednik.zip4jvm.TestData.dirNameEmpty;
import static ru.olegcherednik.zip4jvm.TestData.fileNameZipSrc;
import static ru.olegcherednik.zip4jvm.TestDataAssert.dirBikesAssert;
import static ru.olegcherednik.zip4jvm.TestDataAssert.dirCarsAssert;
import static ru.olegcherednik.zip4jvm.TestDataAssert.dirEmptyAssert;
import static ru.olegcherednik.zip4jvm.assertj.Zip4jvmAssertions.assertThatZipFile;

/**
 * @author Oleg Cherednik
 * @since 14.03.2019
 */
@Test
public class ZipFolderNoSplitTest extends BaseTest {

    private final Path zip = resolve(fileNameZipSrc);

    public void shouldCreateNewZipWithFolder() {
        ZipIt.zip(zip).add(dirCars);

        assertThatZipFile(zip)
                .isSolid()
                .root().hasOnlyDirectories(1)
                .withDirectory(dirNameCars, dirCarsAssert);
    }

    @Test(dependsOnMethods = "shouldCreateNewZipWithFolder")
    public void shouldAddFolderToExistedZip() {
        ZipIt.zip(zip).add(dirBikes);

        assertThatZipFile(zip)
                .isSolid()
                .root().hasOnlyDirectories(2)
                .withDirectory(dirNameCars, dirCarsAssert)
                .withDirectory(dirNameBikes, dirBikesAssert);
    }

    @Test(dependsOnMethods = "shouldAddFolderToExistedZip")
    public void shouldAddEmptyDirectoryToExistedZip() {
        ZipIt.zip(zip).add(dirEmpty);

        assertThatZipFile(zip)
                .isSolid()
                .root().hasOnlyDirectories(3)
                .withDirectory(dirNameCars, dirCarsAssert)
                .withDirectory(dirNameBikes, dirBikesAssert)
                .withDirectory(dirNameEmpty, dirEmptyAssert);
    }

}
