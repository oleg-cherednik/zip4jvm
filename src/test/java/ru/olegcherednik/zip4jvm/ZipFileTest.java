package ru.olegcherednik.zip4jvm;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.olegcherednik.zip4jvm.model.Compression;
import ru.olegcherednik.zip4jvm.model.CompressionLevel;
import ru.olegcherednik.zip4jvm.model.Encryption;
import ru.olegcherednik.zip4jvm.model.ZipEntrySettings;
import ru.olegcherednik.zip4jvm.model.ZipFileSettings;

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
        ZipFileSettings zipFileSettings = ZipFileSettings.builder().build();
        ZipEntrySettings settings = ZipEntrySettings.builder().compression(Compression.STORE, CompressionLevel.NORMAL).build();

        try (ZipFile zipFile = new ZipFile(file, zipFileSettings)) {
            zipFile.add(Zip4jSuite.carsDir.resolve("bentley-continental.jpg"), settings);
            zipFile.add(Zip4jSuite.carsDir.resolve("ferrari-458-italia.jpg"), settings);
            zipFile.add(Zip4jSuite.carsDir.resolve("wiesmann-gt-mf5.jpg"), settings);
        }

        assertThatDirectory(file.getParent()).exists().hasSubDirectories(0).hasFiles(1);
        assertThatZipFile(file).exists().rootEntry().hasSubDirectories(0).hasFiles(3);
        assertThatZipFile(file).file("bentley-continental.jpg").exists().isImage().hasSize(1_395_362);
        assertThatZipFile(file).file("ferrari-458-italia.jpg").exists().isImage().hasSize(320_894);
        assertThatZipFile(file).file("wiesmann-gt-mf5.jpg").exists().isImage().hasSize(729_633);
    }

    @Test(dependsOnMethods = "shouldCreateZipFileWhenUseZipFileAndAddFiles")
    public void shouldAddFilesToExistedZipWhenUseZipFile() throws IOException {
        ZipEntrySettings settings = ZipEntrySettings.builder().compression(Compression.STORE, CompressionLevel.NORMAL).build();

        try (ZipFile zipFile = new ZipFile(file)) {
            zipFile.add(Zip4jSuite.starWarsDir.resolve("one.jpg"), settings);
            zipFile.add(Zip4jSuite.starWarsDir.resolve("two.jpg"), settings);
            zipFile.add(Zip4jSuite.starWarsDir.resolve("three.jpg"), settings);
            zipFile.add(Zip4jSuite.starWarsDir.resolve("four.jpg"), settings);
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

        try (ZipFile zipFile = new ZipFile(file)) {
            zipFile.add(Zip4jSuite.carsDir.resolve("bentley-continental.jpg"),
                    ZipEntrySettings.builder().compression(Compression.STORE, CompressionLevel.NORMAL).comment("bentley-continental").build());
            zipFile.add(Zip4jSuite.carsDir.resolve("ferrari-458-italia.jpg"),
                    ZipEntrySettings.builder().compression(Compression.DEFLATE, CompressionLevel.NORMAL).comment("ferrari-458-italia").build());
            zipFile.add(Zip4jSuite.carsDir.resolve("wiesmann-gt-mf5.jpg"),
                    ZipEntrySettings.builder().compression(Compression.STORE, CompressionLevel.NORMAL).comment("wiesmann-gt-mf5").build());
        }

        assertThatDirectory(file.getParent()).exists().hasSubDirectories(0).hasFiles(1);
        assertThatZipFile(file).exists().rootEntry().hasSubDirectories(0).hasFiles(3);
        assertThatZipFile(file).file("bentley-continental.jpg").exists().isImage().hasSize(1_395_362).hasComment("bentley-continental");
        assertThatZipFile(file).file("ferrari-458-italia.jpg").exists().isImage().hasSize(320_894).hasComment("ferrari-458-italia");
        assertThatZipFile(file).file("wiesmann-gt-mf5.jpg").exists().isImage().hasSize(729_633).hasComment("wiesmann-gt-mf5");
    }

    // TODO add unzip tests for such ZipFile

    public void shouldCreateZipFileWithEntryDifferentEncryptionAndPasswordWhenUseZipFile() throws IOException {
        char[] ferrariPassword = "1".toCharArray();
        char[] wiesmannPassword = "2".toCharArray();
        Path file = Zip4jSuite.subDirNameAsMethodName(rootDir).resolve("src.zip");

        try (ZipFile zipFile = new ZipFile(file)) {
            zipFile.add(Zip4jSuite.carsDir.resolve("bentley-continental.jpg"),
                    ZipEntrySettings.builder().compression(Compression.STORE, CompressionLevel.NORMAL).build());
            zipFile.add(Zip4jSuite.carsDir.resolve("ferrari-458-italia.jpg"),
                    ZipEntrySettings.builder().compression(Compression.STORE, CompressionLevel.NORMAL)
                                    .encryption(Encryption.PKWARE, ferrariPassword).build());
            zipFile.add(Zip4jSuite.carsDir.resolve("wiesmann-gt-mf5.jpg"),
                    ZipEntrySettings.builder().compression(Compression.STORE, CompressionLevel.NORMAL)
                                    .encryption(Encryption.AES_256, wiesmannPassword).build());
        }

        assertThatDirectory(file.getParent()).exists().hasSubDirectories(0).hasFiles(1);
//        assertThatZipFile(file).exists().rootEntry().hasSubDirectories(0).hasFiles(3);
//        assertThatZipFile(file).file("bentley-continental.jpg").exists().isImage().hasSize(1_395_362).hasComment("bentley-continental");
//        assertThatZipFile(file).file("ferrari-458-italia.jpg").exists().isImage().hasSize(320_894).hasComment("ferrari-458-italia");
//        assertThatZipFile(file).file("wiesmann-gt-mf5.jpg").exists().isImage().hasSize(729_633).hasComment("wiesmann-gt-mf5");
    }

    public void shouldCreateZipFileWithContentWhenUseZipFile() throws IOException {
        ZipFileSettings zipFileSettings = ZipFileSettings.builder()
                                                         .comment("Global Comment")
                                                         .defZipEntrySettings(ZipEntrySettings.builder()
                                                                                              .compression(Compression.STORE, CompressionLevel.NORMAL)
                                                                                              .build())
                                                         .build();

        ZipEntrySettings starWarsSettings = ZipEntrySettings.builder()
                                                            .compression(Compression.DEFLATE, CompressionLevel.NORMAL)
                                                            .basePath(Zip4jSuite.starWarsDir.getFileName().toString()).build();

        ZipEntrySettings srcSettings = ZipEntrySettings.builder()
                                                       .compression(Compression.DEFLATE, CompressionLevel.MAXIMUM)
                                                       .encryption(Encryption.PKWARE, Zip4jSuite.password).build();

        Path file = Zip4jSuite.subDirNameAsMethodName(rootDir).resolve("src.zip");

        try (ZipFile zipFile = new ZipFile(file, zipFileSettings)) {
            zipFile.add(Zip4jSuite.carsDir);
            zipFile.add(Zip4jSuite.filesStarWarsDir, starWarsSettings);
            zipFile.add(Zip4jSuite.filesSrcDir, srcSettings);
        }

//        assertThatDirectory(file.getParent()).exists().hasSubDirectories(0).hasFiles(1);
//        assertThatZipFile(file).exists().rootEntry().hasSubDirectories(0).hasFiles(3);
//        assertThatZipFile(file).file("bentley-continental.jpg").exists().isImage().hasSize(1_395_362);
//        assertThatZipFile(file).file("ferrari-458-italia.jpg").exists().isImage().hasSize(320_894);
//        assertThatZipFile(file).file("wiesmann-gt-mf5.jpg").exists().isImage().hasSize(729_633);
    }
}
