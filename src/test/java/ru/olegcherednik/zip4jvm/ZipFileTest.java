package ru.olegcherednik.zip4jvm;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static ru.olegcherednik.zip4jvm.assertj.Zip4jAssertions.assertThatDirectory;
import static ru.olegcherednik.zip4jvm.assertj.Zip4jAssertions.assertThatZipFile;

/**
 * @author Oleg Cherednik
 * @since 01.09.2019
 */
@Test
@SuppressWarnings("FieldNamingConvention")
public class ZipFileTest {

    private static final Path rootDir = Zip4jSuite.generateSubDirNameWithTime(ZipFileTest.class);
    private static final Path file = rootDir.resolve("createZipArchiveAndAddFiles/src.zip");

    @BeforeClass
    public static void createDir() throws IOException {
        Files.createDirectories(rootDir);
    }

    @AfterClass(enabled = Zip4jSuite.clear)
    public static void removeDir() throws IOException {
        Zip4jSuite.removeDir(rootDir);
    }

    public void shouldCreateZipFileWhenUseZipFileAndAddFiles() throws IOException {
        try (ZipFile zipFile = new ZipFile(file)) {
            zipFile.add(Zip4jSuite.carsDir.resolve("bentley-continental.jpg"));
            zipFile.add(Zip4jSuite.carsDir.resolve("ferrari-458-italia.jpg"));
            zipFile.add(Zip4jSuite.carsDir.resolve("wiesmann-gt-mf5.jpg"));
        }

        assertThatDirectory(file.getParent()).exists().hasSubDirectories(0).hasFiles(1);
        assertThatZipFile(file).exists().rootEntry().hasSubDirectories(0).hasFiles(3);
        assertThatZipFile(file).file("bentley-continental.jpg").exists().isImage().hasSize(1_395_362);
        assertThatZipFile(file).file("ferrari-458-italia.jpg").exists().isImage().hasSize(320_894);
        assertThatZipFile(file).file("wiesmann-gt-mf5.jpg").exists().isImage().hasSize(729_633);
    }

    @Test(dependsOnMethods = "shouldCreateZipFileWhenUseZipFileAndAddFiles")
    public void shouldAddFilesToExistedZipWhenUseZipFile() throws IOException {
        try (ZipFile zipFile = new ZipFile(file)) {
            zipFile.add(Zip4jSuite.starWarsDir.resolve("one.jpg"));
            zipFile.add(Zip4jSuite.starWarsDir.resolve("two.jpg"));
            zipFile.add(Zip4jSuite.starWarsDir.resolve("three.jpg"));
            zipFile.add(Zip4jSuite.starWarsDir.resolve("four.jpg"));
        }

        assertThatDirectory(file.getParent()).exists().hasSubDirectories(0).hasFiles(1);
        assertThatZipFile(file).exists().rootEntry().hasSubDirectories(0).hasFiles(7);
        assertThatZipFile(file).file("bentley-continental.jpg").exists().isImage().hasSize(1_395_362);
        assertThatZipFile(file).file("ferrari-458-italia.jpg").exists().isImage().hasSize(320_894);
        assertThatZipFile(file).file("wiesmann-gt-mf5.jpg").exists().isImage().hasSize(729_633);
        assertThatZipFile(file).file("one.jpg").exists().isImage().hasSize(2_204_448);
        assertThatZipFile(file).file("two.jpg").exists().isImage().hasSize(277_857);
        assertThatZipFile(file).file("three.jpg").exists().isImage().hasSize(1_601_879);
        assertThatZipFile(file).file("four.jpg").exists().isImage().hasSize(1_916_776);
    }
}
