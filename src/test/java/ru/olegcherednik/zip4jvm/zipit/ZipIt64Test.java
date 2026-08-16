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
package ru.olegcherednik.zip4jvm.zipit;

import ru.olegcherednik.zip4jvm.BaseTest;
import ru.olegcherednik.zip4jvm.UnzipIt;
import ru.olegcherednik.zip4jvm.ZipIt;
import ru.olegcherednik.zip4jvm.model.settings.EncryptionEnum;
import ru.olegcherednik.zip4jvm.model.settings.ZipSettings;

import org.testng.annotations.Test;

import java.nio.file.Path;

import static ru.olegcherednik.zip4jvm.TestData.contentDirSrc;
import static ru.olegcherednik.zip4jvm.TestDataAssert.rootAssert;
import static ru.olegcherednik.zip4jvm.Zip4jvmSuite.SIZE_1MB;
import static ru.olegcherednik.zip4jvm.Zip4jvmSuite.password;
import static ru.olegcherednik.zip4jvm.Zip4jvmSuite.passwordStr;
import static ru.olegcherednik.zip4jvm.assertj.Zip4jvmAssertions.assertThatDirectory;
import static ru.olegcherednik.zip4jvm.assertj.Zip4jvmAssertions.assertThatZipFile;

/**
 * @author Oleg Cherednik
 * @since 06.04.2019
 */
@Test
@SuppressWarnings("NewClassNamingConvention")
public class ZipIt64Test extends BaseTest {

    private Path zipSimple;
    private Path zipAes;
    private Path zipSplit;

    public void shouldZipWhenZip64() {
        ZipSettings settings = ZipSettings.builder().zip64(true).build();

        zipSimple = getZip();
        ZipIt.zip(zipSimple).settings(settings).add(contentDirSrc);

        assertThatZipFile(zipSimple).parent().hasOnlyRegularFiles(1);
        assertThatZipFile(zipSimple).root().matches(rootAssert);
    }

    @Test(dependsOnMethods = "shouldZipWhenZip64")
    public void shouldUnzipWhenZip64() {
        Path dstDir = getTestRoot();
        UnzipIt.zip(zipSimple).dstDir(dstDir).extract();
        assertThatDirectory(dstDir).matches(rootAssert);
    }

    public void shouldZipWhenZip64AndAesEncryption() {
        ZipSettings settings = ZipSettings.builder()
                                          .entrySettings(EncryptionEnum.AES_256, password)
                                          .comment("password: " + passwordStr)
                                          .zip64(true).build();

        zipAes = getZip();
        ZipIt.zip(zipAes).settings(settings).add(contentDirSrc);

        assertThatZipFile(zipAes).parent().hasOnlyRegularFiles(1);
        assertThatZipFile(zipAes, password).root().matches(rootAssert);
    }

    @Test(dependsOnMethods = "shouldZipWhenZip64AndAesEncryption")
    public void shouldUnzipWhenZip64AndAesEncryption() {
        Path dstDir = getTestRoot();
        UnzipIt.zip(zipAes).dstDir(dstDir).password(password).extract();
        assertThatDirectory(dstDir).matches(rootAssert);
    }

    public void shouldZipWhenZip64AndSplit() {
        ZipSettings settings = ZipSettings.builder().splitSize(SIZE_1MB).zip64(true).build();

        zipSplit = getZip();
        ZipIt.zip(zipSplit).settings(settings).add(contentDirSrc);

        // TODO it seems it could be checked with commons-compress
        //        assertThatDirectory(zipFile.getParent()).exists().hasSubDirectories(0).hasFiles(1);
        //        assertThatZipFile(zipFile).directory("/").matches(TestUtils.zipRootDirAssert);
    }

    @Test(dependsOnMethods = "shouldZipWhenZip64AndSplit")
    public void shouldUnzipWhenZip64AndSplit() {
        Path dstDir = getTestRoot();
        UnzipIt.zip(zipSplit).dstDir(dstDir).extract();
        assertThatDirectory(dstDir).matches(rootAssert);
    }

    // TODO it works but it's too slow
    //    @SuppressWarnings("AbbreviationAsWordInName")
    //    public void shouldUseZip64WhenTotalEntriesOverFFFF()   {
    //        Path zipManyEntries = getZip();
    //
    //        ZipIt.zip(zipManyEntries)
    //             .execute(zipFile -> IntStream.rangeClosed(1, ZipModel.MAX_TOTAL_ENTRIES + 1)
    //                                          .mapToObj(i -> "file_" + i + ".txt")
    //                                          .map(fileName ->
    //                                                       ZipFile.Entry.regularFile(() -> IOUtils.toInputStream(
    //                                                                                        fileName, Charsets.UTF_8),
    //                                                                                 fileName,
    //                                                                                 System.currentTimeMillis(),
    //                                                                                 0,
    //                                                                                 new ExternalFileAttributes()))
    //                                          .forEach(zipFile::add));
    //
    //        ZipModel zipModel = ZipModelBuilder.read(SrcZip.of(zipManyEntries));
    //
    //        assertThatZipFile(zipManyEntries).parent().hasOnlyRegularFiles(1);
    //        assertThat(zipModel.getEntryNames()).hasSize(ZipModel.MAX_TOTAL_ENTRIES + 1);
    //        assertThat(zipModel.isZip64()).isTrue();
    //    }

    // TODO it works but it's too slow

    //    @Test(dependsOnMethods = "shouldUseZip64WhenTotalEntriesOverFFFF")
    //    public void shouldUnzipZip64WhenTotalEntriesOverFFFF()   {
    //        Path dstDir = Zip4jvmSuite.subDirNameAsMethodName(rootDir);
    //        UnzipIt.extract(zipManyEntries, dstDir);
    //        assertThatDirectory(dstDir).hasDirectories(0).hasFiles(ZipModel.MAX_TOTAL_ENTRIES + 1);
    //    }

    //    public void shouldUseZip64WhenEntrySizeOverFFFFFFFF()   {
    //        Path dir = Zip4jvmSuite.subDirNameAsMethodName(rootDir);
    //        Files.createDirectories(dir);
    //
    //        Path file = dir.resolve("file.txt");
    //
    //        try (RandomAccessFile f = new RandomAccessFile(file.toFile(), "rw")) {
    //            f.setLength(ZipModel.MAX_ENTRY_SIZE + 1);
    //        }
    //
    //        Path zipHugeEntry = dir.resolve("src.zip");
    //        ZipEntrySettings entrySettings = ZipEntrySettings.builder().compression(Compression.STORE,
    //        CompressionLevel.NORMAL).build();
    //        ZipSettings settings = ZipSettings.builder().entrySettingsProvider(fileNam -> entrySettings).build();
    //        ZipIt.zip(zipHugeEntry).settings(settings).add(Arrays.asList(file, fileBentley));
    //
    //        ZipModel zipModel = ZipModelBuilder.read(SrcZip.of(zipHugeEntry));
    //        assertThat(zipModel.getZipEntryByFileName("file.txt").getUncompressedSize())
    //        .isEqualTo(ZipModel.MAX_ENTRY_SIZE + 1);
    //        assertThat(zipModel.getZipEntryByFileName(fileNameBentley).getUncompressedSize()).isEqualTo(1_395_362);
    //
    //        // TODO asserts in zip should be using
    //    }

}
