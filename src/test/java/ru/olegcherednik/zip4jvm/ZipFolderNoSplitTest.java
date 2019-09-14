package ru.olegcherednik.zip4jvm;

import org.assertj.core.api.Assertions;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;
import ru.olegcherednik.zip4jvm.assertj.Zip4jAssertions;
import ru.olegcherednik.zip4jvm.model.Compression;
import ru.olegcherednik.zip4jvm.model.CompressionLevel;
import ru.olegcherednik.zip4jvm.model.settings.ZipEntrySettings;
import ru.olegcherednik.zip4jvm.model.settings.ZipFileSettings;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author Oleg Cherednik
 * @since 14.03.2019
 */
@SuppressWarnings("FieldNamingConvention")
public class ZipFolderNoSplitTest {

    private static final Path rootDir = Zip4jSuite.generateSubDirNameWithTime(ZipFolderNoSplitTest.class);
    private static final Path zip = rootDir.resolve("src.zip");

    @BeforeClass
    public static void createDir() throws IOException {
        Files.createDirectories(rootDir);
    }

    @AfterClass(enabled = Zip4jSuite.clear)
    public static void removeDir() throws IOException {
        Zip4jSuite.removeDir(rootDir);
    }

    @Test
    public void shouldCreateNewZipWithFolder() throws IOException {
        ZipFileSettings settings = ZipFileSettings.builder()
                                                  .entrySettings(
                                                          ZipEntrySettings.builder()
                                                                          .compression(Compression.DEFLATE, CompressionLevel.NORMAL).build())
                                                  .build();
        ZipIt.add(zip, Zip4jSuite.carsDir, settings);

        Zip4jAssertions.assertThatDirectory(zip.getParent()).exists().hasSubDirectories(0).hasFiles(1);
        Zip4jAssertions.assertThatZipFile(zip).exists().rootEntry().hasSubDirectories(1).hasFiles(0);
        Zip4jAssertions.assertThatZipFile(zip).directory("cars/").matches(TestUtils.zipCarsDirAssert);
    }

    @Test(dependsOnMethods = "shouldCreateNewZipWithFolder")
    @Ignore
    public void shouldAddFolderToExistedZip() throws IOException {
        Assertions.assertThat(Files.exists(zip)).isTrue();
        Assertions.assertThat(Files.isRegularFile(zip)).isTrue();

        ZipFileSettings settings = ZipFileSettings.builder()
                                                  .entrySettings(
                                                          ZipEntrySettings.builder()
                                                                          .compression(Compression.DEFLATE, CompressionLevel.NORMAL).build())
                                                  .build();
        ZipIt.add(zip, Zip4jSuite.starWarsDir, settings);

        Zip4jAssertions.assertThatDirectory(ZipFolderNoSplitTest.zip.getParent()).exists().hasSubDirectories(0).hasFiles(1);
        Zip4jAssertions.assertThatZipFile(ZipFolderNoSplitTest.zip).exists().rootEntry().hasSubDirectories(2).hasFiles(0);
        Zip4jAssertions.assertThatZipFile(ZipFolderNoSplitTest.zip).directory("cars/").matches(TestUtils.zipCarsDirAssert);
        Zip4jAssertions.assertThatZipFile(ZipFolderNoSplitTest.zip).directory("Star Wars/").matches(TestUtils.zipStarWarsDirAssert);
    }

    @Test(dependsOnMethods = "shouldAddFolderToExistedZip")
    @Ignore
    public void shouldAddEmptyDirectoryToExistedZip() throws IOException {
        Assertions.assertThat(Files.exists(zip)).isTrue();
        Assertions.assertThat(Files.isRegularFile(zip)).isTrue();

        ZipFileSettings settings = ZipFileSettings.builder()
                                                  .entrySettings(
                                                          ZipEntrySettings.builder()
                                                                          .compression(Compression.DEFLATE, CompressionLevel.NORMAL).build())
                                                  .build();
        ZipIt.add(zip, Zip4jSuite.emptyDir, settings);

        Zip4jAssertions.assertThatDirectory(zip.getParent()).exists().hasSubDirectories(0).hasFiles(1);
        Zip4jAssertions.assertThatZipFile(zip).exists().rootEntry().hasSubDirectories(3).hasFiles(0);
        Zip4jAssertions.assertThatZipFile(zip).directory("cars/").matches(TestUtils.zipCarsDirAssert);
        Zip4jAssertions.assertThatZipFile(zip).directory("Star Wars/").matches(TestUtils.zipStarWarsDirAssert);
        Zip4jAssertions.assertThatZipFile(zip).directory("empty_dir/").matches(TestUtils.zipEmptyDirAssert);
    }

}
