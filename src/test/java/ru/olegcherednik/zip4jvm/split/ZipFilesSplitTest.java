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
package ru.olegcherednik.zip4jvm.split;

import ru.olegcherednik.zip4jvm.Zip4jvmSuite;
import ru.olegcherednik.zip4jvm.ZipInfo;
import ru.olegcherednik.zip4jvm.ZipIt;
import ru.olegcherednik.zip4jvm.io.readers.ZipModelReader;
import ru.olegcherednik.zip4jvm.model.EndCentralDirectory;
import ru.olegcherednik.zip4jvm.model.Zip64;
import ru.olegcherednik.zip4jvm.model.ZipModel;
import ru.olegcherednik.zip4jvm.model.settings.CompressionEnum;
import ru.olegcherednik.zip4jvm.model.settings.ZipSettings;
import ru.olegcherednik.zip4jvm.model.src.SrcZip;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.olegcherednik.zip4jvm.TestData.fileBentley;
import static ru.olegcherednik.zip4jvm.TestData.fileDucati;
import static ru.olegcherednik.zip4jvm.TestData.fileFerrari;
import static ru.olegcherednik.zip4jvm.TestData.fileHonda;
import static ru.olegcherednik.zip4jvm.TestData.fileKawasaki;
import static ru.olegcherednik.zip4jvm.TestData.fileNameBentley;
import static ru.olegcherednik.zip4jvm.TestData.fileNameDucati;
import static ru.olegcherednik.zip4jvm.TestData.fileNameFerrari;
import static ru.olegcherednik.zip4jvm.TestData.fileNameHonda;
import static ru.olegcherednik.zip4jvm.TestData.fileNameKawasaki;
import static ru.olegcherednik.zip4jvm.TestData.fileNameSuzuki;
import static ru.olegcherednik.zip4jvm.TestData.fileNameWiesmann;
import static ru.olegcherednik.zip4jvm.TestData.fileSuzuki;
import static ru.olegcherednik.zip4jvm.TestData.fileWiesmann;
import static ru.olegcherednik.zip4jvm.TestDataAssert.dirCarsAssert;
import static ru.olegcherednik.zip4jvm.TestDataAssert.fileBentleyAssert;
import static ru.olegcherednik.zip4jvm.TestDataAssert.fileDucatiAssert;
import static ru.olegcherednik.zip4jvm.TestDataAssert.fileFerrariAssert;
import static ru.olegcherednik.zip4jvm.TestDataAssert.fileHondaAssert;
import static ru.olegcherednik.zip4jvm.TestDataAssert.fileKawasakiAssert;
import static ru.olegcherednik.zip4jvm.TestDataAssert.fileSuzukiAssert;
import static ru.olegcherednik.zip4jvm.TestDataAssert.fileWiesmannAssert;
import static ru.olegcherednik.zip4jvm.Zip4jvmSuite.SIZE_1MB;
import static ru.olegcherednik.zip4jvm.assertj.Zip4jvmAssertions.assertThatZipFile;

/**
 * @author Oleg Cherednik
 * @since 27.04.2019
 */
@Test
public class ZipFilesSplitTest {

    private static final Path ROOT_DIR = Zip4jvmSuite.generateSubDirNameWithTime(ZipFilesSplitTest.class);

    @BeforeClass
    public static void createDir() throws IOException {
        Files.createDirectories(ROOT_DIR);
    }

    @AfterClass(enabled = Zip4jvmSuite.clear)
    public static void removeDir() throws IOException {
        Zip4jvmSuite.removeDir(ROOT_DIR);
    }

    public void shouldCreateNewSplitZipWithFiles() {
        Path zip = Zip4jvmSuite.subDirNameAsMethodName(ROOT_DIR).resolve("src.zip");
        ZipIt.zip(zip).settings(splitSize(SIZE_1MB))
             .add(fileBentley, fileFerrari, fileWiesmann);
        assertThatZipFile(zip).parent().hasDirectories(0).hasRegularFiles(3);
        assertThatZipFile(zip).root().matches(dirCarsAssert);
        assertThatZipFile(zip).regularFile(fileNameBentley).hasDiskNo(0);
        assertThatZipFile(zip).regularFile(fileNameFerrari).hasDiskNo(1);
        assertThatZipFile(zip).regularFile(fileNameWiesmann).hasDiskNo(1);
    }

    public void shouldSetTotalDiskWhenSplitZip64() {
        Path zip = Zip4jvmSuite.subDirNameAsMethodName(ROOT_DIR).resolve("src.zip");
        ZipSettings settings = ZipSettings.builder()
                                          .zip64(true)
                                          .entrySettings(CompressionEnum.DEFLATE)
                                          .splitSize(SIZE_1MB).build();

        ZipIt.zip(zip).settings(settings)
             .add(fileBentley, fileFerrari, fileWiesmann);

        SrcZip srcZip = SrcZip.of(zip);
        ZipModelReader reader = new ZipModelReader(srcZip);
        reader.readCentralData();

        EndCentralDirectory endCentralDirectory = reader.getEndCentralDirectory();
        Zip64.EndCentralDirectoryLocator locator = reader.getZip64().getEndCentralDirectoryLocator();

        assertThat(endCentralDirectory.getMainDiskNo()).isEqualTo(ZipModel.MAX_TOTAL_DISKS);
        assertThat(endCentralDirectory.getTotalDisks()).isEqualTo(ZipModel.MAX_TOTAL_DISKS);
        assertThat(locator.getMainDiskNo()).isEqualTo(2);
        assertThat(locator.getTotalDisks()).isEqualTo(3);

        assertThat(ZipModelReader.getTotalDisks(srcZip)).isEqualTo(3);
    }

    public void shouldSetTotalDiskWhenSplit() {
        Path zip = Zip4jvmSuite.subDirNameAsMethodName(ROOT_DIR).resolve("src.zip");
        ZipIt.zip(zip).settings(splitSize(SIZE_1MB))
             .add(fileBentley, fileFerrari, fileWiesmann);

        SrcZip srcZip = SrcZip.of(zip);
        ZipModelReader reader = new ZipModelReader(srcZip);
        reader.readCentralData();

        EndCentralDirectory endCentralDirectory = reader.getEndCentralDirectory();

        assertThat(endCentralDirectory.getMainDiskNo()).isEqualTo(2);
        assertThat(endCentralDirectory.getTotalDisks()).isEqualTo(2);
        assertThat(reader.getZip64()).isSameAs(Zip64.NULL);
        assertThat(ZipModelReader.getTotalDisks(srcZip)).isEqualTo(3);
    }

    ssspublic void shouldNotChangeExistedDiskWhenAddSplitZip() throws IOException {
        Path zip = Zip4jvmSuite.subDirNameAsMethodName(ROOT_DIR).resolve("src.zip");
        ZipIt.zip(zip).settings(splitSize(SIZE_1MB))
             .add(fileBentley, fileFerrari, fileWiesmann);

        System.out.println("---");
        ZipInfo.zip(zip).printShortInfo();

        ZipIt.zip(zip).settings(splitSize(SIZE_1MB))
             .add(fileDucati, fileHonda, fileKawasaki, fileSuzuki);

        System.out.println("+++");
        ZipInfo.zip(zip).printShortInfo();

        assertThatZipFile(zip).parent().hasDirectories(0).hasRegularFiles(4);
        assertThatZipFile(zip).root().exists().hasEntries(7).hasRegularFiles(7);
        assertThatZipFile(zip).root().regularFile(fileNameBentley).matches(fileBentleyAssert);
        assertThatZipFile(zip).root().regularFile(fileNameFerrari).matches(fileFerrariAssert);
        assertThatZipFile(zip).root().regularFile(fileNameWiesmann).matches(fileWiesmannAssert);
        assertThatZipFile(zip).root().regularFile(fileNameDucati).matches(fileDucatiAssert);
        assertThatZipFile(zip).root().regularFile(fileNameHonda).matches(fileHondaAssert);
        assertThatZipFile(zip).root().regularFile(fileNameKawasaki).matches(fileKawasakiAssert);
        assertThatZipFile(zip).root().regularFile(fileNameSuzuki).matches(fileSuzukiAssert);

        assertThatZipFile(zip).regularFile(fileNameBentley).hasDiskNo(0);
        assertThatZipFile(zip).regularFile(fileNameFerrari).hasDiskNo(1);
        assertThatZipFile(zip).regularFile(fileNameWiesmann).hasDiskNo(1);
        assertThatZipFile(zip).regularFile(fileNameDucati).hasDiskNo(2);
        assertThatZipFile(zip).regularFile(fileNameHonda).hasDiskNo(2);
        assertThatZipFile(zip).regularFile(fileNameKawasaki).hasDiskNo(2);
        assertThatZipFile(zip).regularFile(fileNameSuzuki).hasDiskNo(2);
    }

    private static ZipSettings splitSize(long splitSize) {
        return ZipSettings.builder()
                          .entrySettings(CompressionEnum.DEFLATE)
                          .splitSize(splitSize).build();
    }

}
