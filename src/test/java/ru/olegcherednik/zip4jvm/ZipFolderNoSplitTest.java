package ru.olegcherednik.zip4jvm;

import org.assertj.core.api.Assertions;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;
import ru.olegcherednik.zip4jvm.assertj.Zip4jvmAssertions;
import ru.olegcherednik.zip4jvm.model.Compression;
import ru.olegcherednik.zip4jvm.model.CompressionLevel;
import ru.olegcherednik.zip4jvm.model.settings.ZipEntrySettings;
import ru.olegcherednik.zip4jvm.model.settings.ZipFileSettings;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static ru.olegcherednik.zip4jvm.TestData.dirCars;
import static ru.olegcherednik.zip4jvm.TestData.dirEmpty;

/**
 * @author Oleg Cherednik
 * @since 14.03.2019
 */
@SuppressWarnings("FieldNamingConvention")
public class ZipFolderNoSplitTest {

    private static final Path rootDir = Zip4jvmSuite.generateSubDirNameWithTime(ZipFolderNoSplitTest.class);
    private static final Path zip = rootDir.resolve("src.zip");

    @BeforeClass
    public static void createDir() throws IOException {
        Files.createDirectories(rootDir);
    }

    @AfterClass(enabled = Zip4jvmSuite.clear)
    public static void removeDir() throws IOException {
        Zip4jvmSuite.removeDir(rootDir);
    }

    @Test
    public void shouldCreateNewZipWithFolder() throws IOException {
        ZipFileSettings settings = ZipFileSettings.builder()
                                                  .entrySettingsProvider(fileName ->
                                                          ZipEntrySettings.builder()
                                                                          .compression(Compression.DEFLATE, CompressionLevel.NORMAL).build())
                                                  .build();
        ZipIt.zip(zip).settings(settings).add(dirCars);

        Zip4jvmAssertions.assertThatDirectory(zip.getParent()).exists().hasDirectories(0).hasFiles(1);
        Zip4jvmAssertions.assertThatZipFile(zip).exists().root().hasDirectories(1).hasFiles(0);
        Zip4jvmAssertions.assertThatZipFile(zip).directory("cars/").matches(TestDataAssert.zipDirCarsAssert);
    }

    @Test(dependsOnMethods = "shouldCreateNewZipWithFolder")
    @Ignore
    public void shouldAddFolderToExistedZip() throws IOException {
        Assertions.assertThat(Files.exists(zip)).isTrue();
        Assertions.assertThat(Files.isRegularFile(zip)).isTrue();

        ZipFileSettings settings = ZipFileSettings.builder()
                                                  .entrySettingsProvider(fileName ->
                                                          ZipEntrySettings.builder()
                                                                          .compression(Compression.DEFLATE, CompressionLevel.NORMAL).build())
                                                  .build();
//        ZipIt.add(zip, Zip4jvmSuite.starWarsDir, settings);
//
//        Zip4jvmAssertions.assertThatDirectory(ZipFolderNoSplitTest.zip.getParent()).exists().hasSubDirectories(0).hasFiles(1);
//        Zip4jvmAssertions.assertThatZipFile(ZipFolderNoSplitTest.zip).exists().rootEntry().hasSubDirectories(2).hasFiles(0);
//        Zip4jvmAssertions.assertThatZipFile(ZipFolderNoSplitTest.zip).directory("cars/").matches(TestDataAssert.zipCarsDirAssert);
//        Zip4jvmAssertions.assertThatZipFile(ZipFolderNoSplitTest.zip).directory("Star Wars/").matches(TestDataAssert.zipStarWarsDirAssert);
    }

    @Test(dependsOnMethods = "shouldAddFolderToExistedZip")
    @Ignore
    public void shouldAddEmptyDirectoryToExistedZip() throws IOException {
        Assertions.assertThat(Files.exists(zip)).isTrue();
        Assertions.assertThat(Files.isRegularFile(zip)).isTrue();

        ZipFileSettings settings = ZipFileSettings.builder()
                                                  .entrySettingsProvider(fileName ->
                                                          ZipEntrySettings.builder()
                                                                          .compression(Compression.DEFLATE, CompressionLevel.NORMAL).build())
                                                  .build();
        ZipIt.zip(zip).settings(settings).add(dirEmpty);

        Zip4jvmAssertions.assertThatDirectory(zip.getParent()).exists().hasDirectories(0).hasFiles(1);
        Zip4jvmAssertions.assertThatZipFile(zip).exists().root().hasDirectories(3).hasFiles(0);
        Zip4jvmAssertions.assertThatZipFile(zip).directory("cars/").matches(TestDataAssert.zipDirCarsAssert);
//        Zip4jvmAssertions.assertThatZipFile(zip).directory("Star Wars/").matches(TestDataAssert.zipStarWarsDirAssert);
        Zip4jvmAssertions.assertThatZipFile(zip).directory("empty_dir/").matches(TestDataAssert.zipDirEmptyAssert);
    }

}
