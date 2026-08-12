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

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.olegcherednik.zip4jvm.TestData.contentDirSrc;
import static ru.olegcherednik.zip4jvm.Zip4jvmSuite.SIZE_1MB;
import static ru.olegcherednik.zip4jvm.assertj.Zip4jvmAssertions.assertThatZipFile;

/**
 * @author Oleg Cherednik
 * @since 14.03.2019
 */
public class ZipFolderSplitTest {

    private static final Path DIR_ROOT = Zip4jvmSuite.generateSubDirNameWithTime();
    private static final Path SRC_ZIP = DIR_ROOT.resolve("src.zip");

    @BeforeClass
    public void createDir() {
        Zip4jvmSuite.createDir(DIR_ROOT);
    }

    @AfterClass(enabled = Zip4jvmSuite.clear)
    public void removeDir() {
        Zip4jvmSuite.removeDir(DIR_ROOT);
    }

    @Test
    public void shouldCreateNewZipWithFolder() {
        ZipSettings settings = ZipSettings.builder()
                                          .entrySettings(CompressionEnum.DEFLATE)
                                          .splitSize(SIZE_1MB)
                                          .build();

        ZipIt.zip(SRC_ZIP).settings(settings).add(contentDirSrc);
        assertThatZipFile(SRC_ZIP).parent().hasRegularFiles(6);
        assertThat(Files.exists(SRC_ZIP)).isTrue();
        assertThat(Files.isRegularFile(SRC_ZIP)).isTrue();
        // TODO ZipFile does not read split archive
        //        assertThatZipFile(zipFile).directory("/").matches(TestUtils.zipRootDirAssert);
    }
    //    TODO commented tests
    //    @Test(dependsOnMethods = "shouldCreateNewZipWithFolder")
    //    public void shouldThrowExceptionWhenModifySplitZip() {
    //        ZipFileWriterSettings settings = ZipFileWriterSettings.builder()
    //                                                  .entrySettings(
    //                                                          ZipEntrySettings.builder()
    //                                                                          .compression(Compression.DEFLATE,
    //                                                                          CompressionLevel.NORMAL).build())
    //                                                  .splitSize(2014 * 1024).build();
    //
    //        assertThatThrownBy(() -> ZipIt.add(zip, Zip4jSuite.starWarsDir, settings))
    //        .isExactlyInstanceOf(Zip4jvmException.class);
    //    }
}
