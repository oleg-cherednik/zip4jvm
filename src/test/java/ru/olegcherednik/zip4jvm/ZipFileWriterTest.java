package ru.olegcherednik.zip4jvm;

import org.apache.commons.io.IOUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.olegcherednik.zip4jvm.model.Compression;
import ru.olegcherednik.zip4jvm.model.CompressionLevel;
import ru.olegcherednik.zip4jvm.model.Encryption;
import ru.olegcherednik.zip4jvm.model.entry.v2.ZipEntryMeta;
import ru.olegcherednik.zip4jvm.model.settings.ZipEntrySettings;
import ru.olegcherednik.zip4jvm.model.settings.ZipFileSettings;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Function;

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
    private static final Path splitFile = rootDir.resolve("split/src.zip");
    private static final Path supplierSolidFile = rootDir.resolve("supplier/split/src.zip");
    private static final Path memorySolidFile = rootDir.resolve("memory/split/src.zip");

    @BeforeClass
    public static void createDir() throws IOException {
        Files.createDirectories(rootDir);
    }

    @AfterClass(enabled = Zip4jSuite.clear)
    public static void removeDir() throws IOException {
        Zip4jSuite.removeDir(rootDir);
    }

    public void shouldCreateZipFileWhenUseZipFileAndAddFiles() throws IOException {
        Function<String, ZipEntrySettings> entrySettingsProvider = fileName -> {
            if ("bentley-continental.jpg".equals(fileName))
                return ZipEntrySettings.builder().compression(Compression.STORE, CompressionLevel.NORMAL).build();
            if ("ferrari-458-italia.jpg".equals(fileName))
                return ZipEntrySettings.builder().compression(Compression.DEFLATE, CompressionLevel.NORMAL).build();
            if ("wiesmann-gt-mf5.jpg".equals(fileName))
                return ZipEntrySettings.builder()
                                       .encryption(Encryption.PKWARE, fn -> Zip4jSuite.password)
                                       .compression(Compression.DEFLATE, CompressionLevel.NORMAL).build();
            if ("one.jpg".equals(fileName))
                return ZipEntrySettings.builder()
                                       .encryption(Encryption.AES_256, fn -> Zip4jSuite.password)
                                       .compression(Compression.DEFLATE, CompressionLevel.NORMAL).build();
            return ZipEntrySettings.DEFAULT;
        };

        try (ZipFile.Writer zipFile = ZipFile.write(solidFile, entrySettingsProvider)) {
            zipFile.add(Zip4jSuite.fileBentleyContinental);
            zipFile.add(Zip4jSuite.fileFerrari);
            zipFile.add(Zip4jSuite.fileWiesmann);
            zipFile.add(Zip4jSuite.starWarsDir.resolve("one.jpg"));
        }

        assertThatDirectory(solidFile.getParent()).exists().hasSubDirectories(0).hasFiles(1);
        assertThatZipFile(solidFile, Zip4jSuite.password).exists().rootEntry().hasSubDirectories(0).hasFiles(4);
        assertThatZipFile(solidFile, Zip4jSuite.password).file("bentley-continental.jpg").exists().isImage().hasSize(1_395_362);
        assertThatZipFile(solidFile, Zip4jSuite.password).file("ferrari-458-italia.jpg").exists().isImage().hasSize(320_894);
        assertThatZipFile(solidFile, Zip4jSuite.password).file("wiesmann-gt-mf5.jpg").exists().isImage().hasSize(729_633);
        assertThatZipFile(solidFile, Zip4jSuite.password).file("one.jpg").exists().isImage().hasSize(2_204_448);
    }

    @Test(dependsOnMethods = "shouldCreateZipFileWhenUseZipFileAndAddFiles")
    public void shouldAddFilesToExistedZipWhenUseZipFile() throws IOException {
        Function<String, ZipEntrySettings> entrySettingsProvider = fileName -> {
            if ("two.jpg".equals(fileName))
                return ZipEntrySettings.builder()
                                       .compression(Compression.STORE, CompressionLevel.NORMAL)
                                       .encryption(Encryption.PKWARE, fn -> Zip4jSuite.password).build();
            if ("three.jpg".equals(fileName))
                return ZipEntrySettings.builder()
                                       .compression(Compression.DEFLATE, CompressionLevel.NORMAL)
                                       .encryption(Encryption.AES_256, fn -> Zip4jSuite.password).build();
            return ZipEntrySettings.DEFAULT;
        };


        try (ZipFile.Writer zipFile = ZipFile.write(solidFile, entrySettingsProvider)) {
            zipFile.add(Zip4jSuite.starWarsDir.resolve("two.jpg"));
            zipFile.add(Zip4jSuite.starWarsDir.resolve("three.jpg"));
        }

        assertThatDirectory(solidFile.getParent()).exists().hasSubDirectories(0).hasFiles(1);
        assertThatZipFile(solidFile, Zip4jSuite.password).exists().rootEntry().hasSubDirectories(0).hasFiles(6);
        assertThatZipFile(solidFile, Zip4jSuite.password).file("bentley-continental.jpg").exists().isImage().hasSize(1_395_362);
        assertThatZipFile(solidFile, Zip4jSuite.password).file("ferrari-458-italia.jpg").exists().isImage().hasSize(320_894);
        assertThatZipFile(solidFile, Zip4jSuite.password).file("one.jpg").exists().isImage().hasSize(2_204_448);
        assertThatZipFile(solidFile, Zip4jSuite.password).file("two.jpg").exists().isImage().hasSize(277_857);
        assertThatZipFile(solidFile, Zip4jSuite.password).file("three.jpg").exists().isImage().hasSize(1_601_879);
    }

    public void shouldCreateZipFileWhenUseZipFileAndAddFilesSplit() throws IOException {
        Function<String, ZipEntrySettings> entrySettingsProvider =
                fileName -> ZipEntrySettings.builder().compression(Compression.STORE, CompressionLevel.NORMAL).build();

        ZipFileSettings zipFileSettings = ZipFileSettings.builder()
                                                         .entrySettingsProvider(entrySettingsProvider)
                                                         .splitSize(1024 * 1024).build();

        try (ZipFile.Writer zipFile = ZipFile.write(splitFile, zipFileSettings)) {
            zipFile.add(Zip4jSuite.carsDir.resolve("bentley-continental.jpg"));
            zipFile.add(Zip4jSuite.carsDir.resolve("ferrari-458-italia.jpg"));
            zipFile.add(Zip4jSuite.carsDir.resolve("wiesmann-gt-mf5.jpg"));
        }

        assertThatDirectory(splitFile.getParent()).exists().hasSubDirectories(0).hasFiles(3);
//        assertThatZipFile(splitFile).exists().rootEntry().hasSubDirectories(0).hasFiles(3);
//        assertThatZipFile(splitFile).file("bentley-continental.jpg").exists().isImage().hasSize(1_395_362);
//        assertThatZipFile(splitFile).file("ferrari-458-italia.jpg").exists().isImage().hasSize(320_894);
//        assertThatZipFile(splitFile).file("wiesmann-gt-mf5.jpg").exists().isImage().hasSize(729_633);
    }

    @Test(dependsOnMethods = "shouldCreateZipFileWhenUseZipFileAndAddFilesSplit")
    public void shouldAddFilesToExistedZipWhenUseZipFileSplit() throws IOException {
        Function<String, ZipEntrySettings> entrySettingsProvider =
                fileName -> ZipEntrySettings.builder().compression(Compression.STORE, CompressionLevel.NORMAL).build();

        ZipFileSettings zipFileSettings = ZipFileSettings.builder()
                                                         .entrySettingsProvider(entrySettingsProvider)
                                                         .splitSize(1024 * 1024).build();

        try (ZipFile.Writer zipFile = ZipFile.write(splitFile, zipFileSettings)) {
            zipFile.add(Zip4jSuite.starWarsDir.resolve("one.jpg"));
            zipFile.add(Zip4jSuite.starWarsDir.resolve("two.jpg"));
            zipFile.add(Zip4jSuite.starWarsDir.resolve("three.jpg"));
            zipFile.add(Zip4jSuite.starWarsDir.resolve("four.jpg"));
        }

        assertThatDirectory(splitFile.getParent()).exists().hasSubDirectories(0).hasFiles(9);
//        assertThatZipFile(splitFile).exists().rootEntry().hasSubDirectories(0).hasFiles(7);
//        assertThatZipFile(splitFile).file("bentley-continental.jpg").exists().isImage().hasSize(1_395_362);
//        assertThatZipFile(splitFile).file("ferrari-458-italia.jpg").exists().isImage().hasSize(320_894);
//        assertThatZipFile(splitFile).file("wiesmann-gt-mf5.jpg").exists().isImage().hasSize(729_633);
//        assertThatZipFile(splitFile).file("one.jpg").exists().isImage().hasSize(2_204_448);
//        assertThatZipFile(splitFile).file("two.jpg").exists().isImage().hasSize(277_857);
//        assertThatZipFile(splitFile).file("three.jpg").exists().isImage().hasSize(1_601_879);
//        assertThatZipFile(splitFile).file("four.jpg").exists().isImage().hasSize(1_916_776);
    }

    public void shouldCreateZipFileWhenUseZipFileAndAddFilesUsingSupplier() throws IOException {
        Function<String, ZipEntrySettings> entrySettingsProvider = fileName -> {
            if ("bentley-continental.jpg".equals(fileName))
                return ZipEntrySettings.builder().compression(Compression.STORE, CompressionLevel.NORMAL).build();
            if ("ferrari-458-italia.jpg".equals(fileName))
                return ZipEntrySettings.builder().compression(Compression.DEFLATE, CompressionLevel.NORMAL).build();
            if ("wiesmann-gt-mf5.jpg".equals(fileName))
                return ZipEntrySettings.builder()
                                       .encryption(Encryption.PKWARE, fn -> Zip4jSuite.password)
                                       .compression(Compression.DEFLATE, CompressionLevel.NORMAL).build();
            if ("one.jpg".equals(fileName))
                return ZipEntrySettings.builder()
                                       .encryption(Encryption.AES_256, fn -> Zip4jSuite.password)
                                       .compression(Compression.DEFLATE, CompressionLevel.NORMAL).build();
            return ZipEntrySettings.DEFAULT;
        };

        try (ZipFile.Writer zipFile = ZipFile.write(supplierSolidFile, entrySettingsProvider)) {
            zipFile.addMeta(ZipEntryMeta.of(Zip4jSuite.fileBentleyContinental, "bentley-continental.jpg"));
            zipFile.addMeta(ZipEntryMeta.of(Zip4jSuite.fileFerrari, "ferrari-458-italia.jpg"));
            zipFile.addMeta(ZipEntryMeta.of(Zip4jSuite.fileWiesmann, "wiesmann-gt-mf5.jpg"));
            zipFile.addMeta(ZipEntryMeta.of(Zip4jSuite.starWarsDir.resolve("one.jpg"), "one.jpg"));
        }

        assertThatDirectory(supplierSolidFile.getParent()).exists().hasSubDirectories(0).hasFiles(1);
        assertThatZipFile(supplierSolidFile, Zip4jSuite.password).exists().rootEntry().hasSubDirectories(0).hasFiles(4);
        assertThatZipFile(supplierSolidFile, Zip4jSuite.password).file("bentley-continental.jpg").exists().isImage().hasSize(1_395_362);
        assertThatZipFile(supplierSolidFile, Zip4jSuite.password).file("ferrari-458-italia.jpg").exists().isImage().hasSize(320_894);
        assertThatZipFile(supplierSolidFile, Zip4jSuite.password).file("wiesmann-gt-mf5.jpg").exists().isImage().hasSize(729_633);
        assertThatZipFile(supplierSolidFile, Zip4jSuite.password).file("one.jpg").exists().isImage().hasSize(2_204_448);
    }

    public void shouldCreateZipFileWhenUseZipFileAndAddFilesWithText() throws IOException {
        Function<String, ZipEntrySettings> entrySettingsProvider = fileName -> {
            if ("one.txt".equals(fileName))
                return ZipEntrySettings.builder().compression(Compression.STORE, CompressionLevel.NORMAL).build();
            if ("two.txt".equals(fileName))
                return ZipEntrySettings.builder().compression(Compression.DEFLATE, CompressionLevel.NORMAL).build();
            if ("three.txt".equals(fileName))
                return ZipEntrySettings.builder()
                                       .encryption(Encryption.PKWARE, fn -> Zip4jSuite.password)
                                       .compression(Compression.DEFLATE, CompressionLevel.NORMAL).build();
            if ("four.txt".equals(fileName))
                return ZipEntrySettings.builder()
                                       .encryption(Encryption.AES_256, fn -> Zip4jSuite.password)
                                       .compression(Compression.DEFLATE, CompressionLevel.NORMAL).build();
            return ZipEntrySettings.DEFAULT;
        };

        ZipEntryMeta oneSource = ZipEntryMeta.builder()
                                             .inputStream(() -> IOUtils.toInputStream("one.txt", StandardCharsets.UTF_8))
                                             .fileName("one.txt").build();
        ZipEntryMeta twoSource = ZipEntryMeta.builder()
                                             .inputStream(() -> IOUtils.toInputStream("two.txt", StandardCharsets.UTF_8))
                                             .fileName("two.txt").build();
        ZipEntryMeta threeSource = ZipEntryMeta.builder()
                                               .inputStream(() -> IOUtils.toInputStream("three.txt", StandardCharsets.UTF_8))
                                               .fileName("three.txt").build();
        ZipEntryMeta fourSource = ZipEntryMeta.builder()
                                              .inputStream(() -> IOUtils.toInputStream("four.txt", StandardCharsets.UTF_8))
                                              .fileName("four.txt").build();

        try (ZipFile.Writer zipFile = ZipFile.write(memorySolidFile, entrySettingsProvider)) {
            zipFile.addMeta(oneSource);
            zipFile.addMeta(twoSource);
            zipFile.addMeta(threeSource);
            zipFile.addMeta(fourSource);
        }

        assertThatDirectory(memorySolidFile.getParent()).exists().hasSubDirectories(0).hasFiles(1);
        assertThatZipFile(memorySolidFile, Zip4jSuite.password).exists().rootEntry().hasSubDirectories(0).hasFiles(4);
//        assertThatZipFile(memorySolidFile, Zip4jSuite.password).file("bentley-continental.jpg").exists().isImage().hasSize(1_395_362);
//        assertThatZipFile(memorySolidFile, Zip4jSuite.password).file("ferrari-458-italia.jpg").exists().isImage().hasSize(320_894);
//        assertThatZipFile(memorySolidFile, Zip4jSuite.password).file("wiesmann-gt-mf5.jpg").exists().isImage().hasSize(729_633);
//        assertThatZipFile(memorySolidFile, Zip4jSuite.password).file("one.jpg").exists().isImage().hasSize(2_204_448);
    }
}
