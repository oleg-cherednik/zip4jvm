package ru.olegcherednik.zip4jvm;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.olegcherednik.zip4jvm.model.Compression;
import ru.olegcherednik.zip4jvm.model.CompressionLevel;
import ru.olegcherednik.zip4jvm.model.Encryption;
import ru.olegcherednik.zip4jvm.model.settings.ZipEntrySettings;
import ru.olegcherednik.zip4jvm.model.settings.ZipFileSettings;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Function;

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
        ZipEntrySettings entrySettings = ZipEntrySettings.builder().compression(Compression.STORE, CompressionLevel.NORMAL).build();

        try (ZipFile.Writer zipFile = ZipFile.write(file, fileName -> entrySettings)) {
            zipFile.addPath(Zip4jSuite.carsDir.resolve("bentley-continental.jpg"));
            zipFile.addPath(Zip4jSuite.carsDir.resolve("ferrari-458-italia.jpg"));
            zipFile.addPath(Zip4jSuite.carsDir.resolve("wiesmann-gt-mf5.jpg"));
        }

        assertThatDirectory(file.getParent()).exists().hasSubDirectories(0).hasFiles(1);
        assertThatZipFile(file).exists().rootEntry().hasSubDirectories(0).hasFiles(3);
        assertThatZipFile(file).file("bentley-continental.jpg").exists().isImage().hasSize(1_395_362);
        assertThatZipFile(file).file("ferrari-458-italia.jpg").exists().isImage().hasSize(320_894);
        assertThatZipFile(file).file("wiesmann-gt-mf5.jpg").exists().isImage().hasSize(729_633);
    }

    @Test(dependsOnMethods = "shouldCreateZipFileWhenUseZipFileAndAddFiles")
    public void shouldAddFilesToExistedZipWhenUseZipFile() throws IOException {
        ZipEntrySettings entrySettings = ZipEntrySettings.builder().compression(Compression.STORE, CompressionLevel.NORMAL).build();

        try (ZipFile.Writer zipFile = ZipFile.write(file, fileName -> entrySettings)) {
            zipFile.addPath(Zip4jSuite.starWarsDir.resolve("one.jpg"));
            zipFile.addPath(Zip4jSuite.starWarsDir.resolve("two.jpg"));
            zipFile.addPath(Zip4jSuite.starWarsDir.resolve("three.jpg"));
            zipFile.addPath(Zip4jSuite.starWarsDir.resolve("four.jpg"));
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

    public void shouldCreateZipFileWithEntryCommentWhenUseZipFile() throws IOException {
        Path file = Zip4jSuite.subDirNameAsMethodName(rootDir).resolve("src.zip");

        Function<String, ZipEntrySettings> entrySettingsProvider = fileName -> {
            if ("bentley-continental.jpg".equals(fileName))
                return ZipEntrySettings.builder().compression(Compression.STORE, CompressionLevel.NORMAL).comment("bentley-continental").build();
            if ("ferrari-458-italia.jpg".equals(fileName))
                return ZipEntrySettings.builder().compression(Compression.DEFLATE, CompressionLevel.NORMAL).comment("ferrari-458-italia").build();
            if ("wiesmann-gt-mf5.jpg".equals(fileName))
                return ZipEntrySettings.builder().compression(Compression.STORE, CompressionLevel.NORMAL).comment("wiesmann-gt-mf5").build();
            return ZipEntrySettings.DEFAULT;
        };

        try (ZipFile.Writer zipFile = ZipFile.write(file, entrySettingsProvider)) {
            zipFile.addPath(Zip4jSuite.carsDir.resolve("bentley-continental.jpg"));
            zipFile.addPath(Zip4jSuite.carsDir.resolve("ferrari-458-italia.jpg"));
            zipFile.addPath(Zip4jSuite.carsDir.resolve("wiesmann-gt-mf5.jpg"));
        }

        assertThatDirectory(file.getParent()).exists().hasSubDirectories(0).hasFiles(1);
        assertThatZipFile(file).exists().rootEntry().hasSubDirectories(0).hasFiles(3);
        assertThatZipFile(file).file("bentley-continental.jpg").exists().isImage().hasSize(1_395_362).hasComment("bentley-continental");
        assertThatZipFile(file).file("ferrari-458-italia.jpg").exists().isImage().hasSize(320_894).hasComment("ferrari-458-italia");
        assertThatZipFile(file).file("wiesmann-gt-mf5.jpg").exists().isImage().hasSize(729_633).hasComment("wiesmann-gt-mf5");
    }

    // TODO add unzip tests for such ZipFile

    public void shouldCreateZipFileWithEntryDifferentEncryptionAndPasswordWhenUseZipFile() throws IOException {
        Function<String, ZipEntrySettings> entrySettingsProvider = fileName -> {
            if ("bentley-continental.jpg".equals(fileName))
                return ZipEntrySettings.builder().compression(Compression.STORE, CompressionLevel.NORMAL).build();
            if ("ferrari-458-italia.jpg".equals(fileName))
                return ZipEntrySettings.builder()
                                       .compression(Compression.STORE, CompressionLevel.NORMAL)
                                       .encryption(Encryption.PKWARE, "1".toCharArray()).build();
            if ("wiesmann-gt-mf5.jpg".equals(fileName))
                return ZipEntrySettings.builder()
                                       .compression(Compression.STORE, CompressionLevel.NORMAL)
                                       .encryption(Encryption.AES_256, "2".toCharArray()).build();
            return ZipEntrySettings.DEFAULT.toBuilder().password(Zip4jSuite.password).build();
        };

        Path file = Zip4jSuite.subDirNameAsMethodName(rootDir).resolve("src.zip");

        try (ZipFile.Writer zipFile = ZipFile.write(file, entrySettingsProvider)) {
            zipFile.addPath(Zip4jSuite.carsDir.resolve("bentley-continental.jpg"));
            zipFile.addPath(Zip4jSuite.carsDir.resolve("ferrari-458-italia.jpg"));
            zipFile.addPath(Zip4jSuite.carsDir.resolve("wiesmann-gt-mf5.jpg"));
        }

        assertThatDirectory(file.getParent()).exists().hasSubDirectories(0).hasFiles(1);
//        assertThatZipFile(file).exists().rootEntry().hasSubDirectories(0).hasFiles(3);
//        assertThatZipFile(file).file("bentley-continental.jpg").exists().isImage().hasSize(1_395_362).hasComment("bentley-continental");
//        assertThatZipFile(file).file("ferrari-458-italia.jpg").exists().isImage().hasSize(320_894).hasComment("ferrari-458-italia");
//        assertThatZipFile(file).file("wiesmann-gt-mf5.jpg").exists().isImage().hasSize(729_633).hasComment("wiesmann-gt-mf5");
    }

    public void shouldCreateZipFileWithContentWhenUseZipFile() throws IOException {
        Function<String, ZipEntrySettings> entrySettingsProvider = fileName -> {
            if (fileName.startsWith("Star Wars/"))
                return ZipEntrySettings.builder().compression(Compression.DEFLATE, CompressionLevel.NORMAL).build();
            if (!fileName.contains("/"))
                return ZipEntrySettings.builder()
                                       .compression(Compression.DEFLATE, CompressionLevel.MAXIMUM)
                                       .encryption(Encryption.PKWARE, Zip4jSuite.password).build();
            return ZipEntrySettings.builder().compression(Compression.STORE, CompressionLevel.NORMAL).build();
        };

        ZipFileSettings zipFileSettings = ZipFileSettings.builder()
                                                         .comment("Global Comment")
                                                         .entrySettingsProvider(entrySettingsProvider).build();

        Path file = Zip4jSuite.subDirNameAsMethodName(rootDir).resolve("src.zip");

        try (ZipFile.Writer zipFile = ZipFile.write(file, zipFileSettings)) {
            zipFile.addPath(Zip4jSuite.carsDir);
            zipFile.addPath(Zip4jSuite.filesStarWarsDir);
            zipFile.addPath(Zip4jSuite.filesSrcDir);
        }

//        assertThatDirectory(file.getParent()).exists().hasSubDirectories(0).hasFiles(1);
//        assertThatZipFile(file).exists().rootEntry().hasSubDirectories(0).hasFiles(3);
//        assertThatZipFile(file).file("bentley-continental.jpg").exists().isImage().hasSize(1_395_362);
//        assertThatZipFile(file).file("ferrari-458-italia.jpg").exists().isImage().hasSize(320_894);
//        assertThatZipFile(file).file("wiesmann-gt-mf5.jpg").exists().isImage().hasSize(729_633);
    }

    public void shouldCreateZipFileWithEmptyDirectoryWhenAddEmptyDirectory() throws IOException {
        ZipFileSettings zipFileSettings = ZipFileSettings.builder()
                                                         .entrySettingsProvider(fileName -> ZipEntrySettings.builder().build())
                                                         .build();

        Path zip = Zip4jSuite.subDirNameAsMethodName(rootDir).resolve("src.zip");

        try (ZipFile.Writer zipFile = ZipFile.write(zip, zipFileSettings)) {
            zipFile.addPath(Zip4jSuite.emptyDir);
        }

        assertThatDirectory(zip.getParent()).exists().hasSubDirectories(0).hasFiles(1);
        assertThatZipFile(zip).exists().rootEntry().hasSubDirectories(1).hasFiles(0);
//        assertThatZipFile(file).file("bentley-continental.jpg").exists().isImage().hasSize(1_395_362);
//        assertThatZipFile(file).file("ferrari-458-italia.jpg").exists().isImage().hasSize(320_894);
//        assertThatZipFile(file).file("wiesmann-gt-mf5.jpg").exists().isImage().hasSize(729_633);
    }
}
