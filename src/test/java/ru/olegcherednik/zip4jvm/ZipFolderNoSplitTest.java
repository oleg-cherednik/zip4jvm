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

import ru.olegcherednik.zip4jvm.model.settings.CompressionEnum;
import ru.olegcherednik.zip4jvm.model.settings.ZipSettings;

import org.testng.annotations.Test;

import java.nio.file.Path;

import static ru.olegcherednik.zip4jvm.TestData.dirCars;
import static ru.olegcherednik.zip4jvm.TestData.dirNameCars;
import static ru.olegcherednik.zip4jvm.TestDataAssert.dirCarsAssert;
import static ru.olegcherednik.zip4jvm.assertj.Zip4jvmAssertions.assertThatZipFile;

/**
 * @author Oleg Cherednik
 * @since 14.03.2019
 */
@Test
public class ZipFolderNoSplitTest extends BaseTest {

    public void shouldCreateNewZipWithFolder() {
        Path zip = getZip();
        ZipIt.zip(zip).settings(ZipSettings.of(CompressionEnum.DEFLATE)).add(dirCars);

        assertThatZipFile(zip)
                .withParent(dir -> dir.hasOnlyRegularFiles(1))
                .root().hasOnlyDirectories(1)
                .withDirectory(dirNameCars, dirCarsAssert);
    }

    // @Test(dependsOnMethods = "shouldCreateNewZipWithFolder")
    // @Ignore
    // public void shouldAddFolderToExistedZip() throws IOException {
    //    Assertions.assertThat(Files.exists(zip)).isTrue();
    //    Assertions.assertThat(Files.isRegularFile(zip)).isTrue();
    //
    //    ZipSettings settings = ZipSettings.builder()
    //                                      .entrySettingsProvider(fileName ->
    //                                                                   ZipEntrySettings.builder()
    //                                                                       .compression(Compression.DEFLATE,
    //                                                                                          CompressionLevel.NORMAL)
    //                                                                                     .build())
    //                                      .build();
    // TODO commented test
    //        ZipIt.add(zip, Zip4jvmSuite.starWarsDir, settings);
    //
    //   Zip4jvmAssertions.assertThatDirectory(ZipFolderNoSplitTest.zip.getParent()).exists()
    //   .hasSubDirectories(0).hasFiles(1);
    //   Zip4jvmAssertions.assertThatZipFile(ZipFolderNoSplitTest.zip).exists().rootEntry()
    //   .hasSubDirectories(2).hasFiles(0);
    //   Zip4jvmAssertions.assertThatZipFile(ZipFolderNoSplitTest.zip).directory("cars/")
    //   .matches(TestDataAssert.zipCarsDirAssert);
    //   Zip4jvmAssertions.assertThatZipFile(ZipFolderNoSplitTest.zip).directory("Star Wars/")
    //   .matches(TestDataAssert.zipStarWarsDirAssert);
    //}

    //    @Test(dependsOnMethods = "shouldAddFolderToExistedZip")
    //    @Ignore
    //    public void shouldAddEmptyDirectoryToExistedZip() {
    //        assertThat(Files.exists(SRC_ZIP)).isTrue();
    //        assertThat(Files.isRegularFile(SRC_ZIP)).isTrue();
    //
    //        ZipIt.zip(SRC_ZIP).settings(ZipSettings.of(CompressionEnum.DEFLATE)).add(dirEmpty);
    //
    //        assertThatZipFile(SRC_ZIP)
    //                .withParent(dir -> dir.hasOnlyRegularFiles(1))
    //                .root().hasOnlyDirectories(3)
    //                .withDirectory(dirNameCars, dirCarsAssert)
    //                .withDirectory(dirNameEmpty, dirEmptyAssert);
    //        // TODO commented test
    //        // Zip4jvmAssertions.assertThatZipFile(zip).directory("Star Wars/")
    //        // =.matches(TestDataAssert.zipStarWarsDirAssert);
    //    }

}
