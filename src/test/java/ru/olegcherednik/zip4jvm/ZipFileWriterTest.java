package ru.olegcherednik.zip4jvm;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.olegcherednik.zip4jvm.model.Compression;
import ru.olegcherednik.zip4jvm.model.CompressionLevel;
import ru.olegcherednik.zip4jvm.model.settings.ZipEntrySettings;
import ru.olegcherednik.zip4jvm.model.settings.ZipFileWriterSettings;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static ru.olegcherednik.zip4jvm.assertj.Zip4jAssertions.assertThatDirectory;
import static ru.olegcherednik.zip4jvm.assertj.Zip4jAssertions.assertThatZipFile;

/**
 * @author Oleg Cherednik
 * @since 12.09.2019
 */
@Test
@SuppressWarnings("FieldNamingConvention")
public class ZipFileWriterTest {

    private static final Path rootDir = Zip4jSuite.generateSubDirNameWithTime(ZipFileWriterTest.class);
    private static final Path solidFile = rootDir.resolve("solid/src.zip");

    @BeforeClass
    public static void createDir() throws IOException {
        Files.createDirectories(rootDir);
    }

    @AfterClass(enabled = Zip4jSuite.clear)
    public static void removeDir() throws IOException {
        Zip4jSuite.removeDir(rootDir);
    }

    public void shouldCreateZipFileWhenUseZipFileAndAddFiles() throws IOException {
        ZipFileWriterSettings zipFileSettings = ZipFileWriterSettings.builder().build();
        ZipEntrySettings settings = ZipEntrySettings.builder().compression(Compression.STORE, CompressionLevel.NORMAL).build();

        try (ZipFile.Writer zipFile = ZipFile.write(solidFile, zipFileSettings)) {
            zipFile.add(Zip4jSuite.carsDir.resolve("bentley-continental.jpg"), settings);
            zipFile.add(Zip4jSuite.carsDir.resolve("ferrari-458-italia.jpg"), settings);
            zipFile.add(Zip4jSuite.carsDir.resolve("wiesmann-gt-mf5.jpg"), settings);
        }

        assertThatDirectory(solidFile.getParent()).exists().hasSubDirectories(0).hasFiles(1);
        assertThatZipFile(solidFile).exists().rootEntry().hasSubDirectories(0).hasFiles(3);
        assertThatZipFile(solidFile).file("bentley-continental.jpg").exists().isImage().hasSize(1_395_362);
        assertThatZipFile(solidFile).file("ferrari-458-italia.jpg").exists().isImage().hasSize(320_894);
        assertThatZipFile(solidFile).file("wiesmann-gt-mf5.jpg").exists().isImage().hasSize(729_633);
    }

    @Test(dependsOnMethods = "shouldCreateZipFileWhenUseZipFileAndAddFiles")
    public void shouldAddFilesToExistedZipWhenUseZipFile() throws IOException {
        ZipEntrySettings entrySettings = ZipEntrySettings.builder().compression(Compression.STORE, CompressionLevel.NORMAL).build();

        try (ZipFile.Writer zipFile = ZipFile.write(solidFile)) {
            zipFile.add(Zip4jSuite.starWarsDir.resolve("one.jpg"), entrySettings);
            zipFile.add(Zip4jSuite.starWarsDir.resolve("two.jpg"), entrySettings);
            zipFile.add(Zip4jSuite.starWarsDir.resolve("three.jpg"), entrySettings);
            zipFile.add(Zip4jSuite.starWarsDir.resolve("four.jpg"), entrySettings);
        }

        assertThatDirectory(solidFile.getParent()).exists().hasSubDirectories(0).hasFiles(1);
        assertThatZipFile(solidFile).exists().rootEntry().hasSubDirectories(0).hasFiles(7);
        assertThatZipFile(solidFile).file("bentley-continental.jpg").exists().isImage().hasSize(1_395_362);
        assertThatZipFile(solidFile).file("ferrari-458-italia.jpg").exists().isImage().hasSize(320_894);
        assertThatZipFile(solidFile).file("wiesmann-gt-mf5.jpg").exists().isImage().hasSize(729_633);
        assertThatZipFile(solidFile).file("one.jpg").exists().isImage().hasSize(2_204_448);
        assertThatZipFile(solidFile).file("two.jpg").exists().isImage().hasSize(277_857);
        assertThatZipFile(solidFile).file("three.jpg").exists().isImage().hasSize(1_601_879);
        assertThatZipFile(solidFile).file("four.jpg").exists().isImage().hasSize(1_916_776);
    }
}
