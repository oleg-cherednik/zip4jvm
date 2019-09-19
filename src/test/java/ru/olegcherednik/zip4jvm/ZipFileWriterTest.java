package ru.olegcherednik.zip4jvm;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.olegcherednik.zip4jvm.model.Compression;
import ru.olegcherednik.zip4jvm.model.CompressionLevel;
import ru.olegcherednik.zip4jvm.model.Encryption;
import ru.olegcherednik.zip4jvm.model.entry.v2.RegularFileZipEntrySource;
import ru.olegcherednik.zip4jvm.model.settings.ZipEntrySettings;
import ru.olegcherednik.zip4jvm.model.settings.ZipFileSettings;

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
    private static final Path splitFile = rootDir.resolve("split/src.zip");
    private static final Path supplierSolidFile = rootDir.resolve("supplier/split/src.zip");

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
        ZipEntrySettings bentleySettings = ZipEntrySettings.builder()
                                                           .compression(Compression.STORE, CompressionLevel.NORMAL).build();
        ZipEntrySettings ferrariSettings = ZipEntrySettings.builder()
                                                           .compression(Compression.DEFLATE, CompressionLevel.NORMAL).build();
        ZipEntrySettings wiesmannSettings = ZipEntrySettings.builder()
                                                            .encryption(Encryption.PKWARE, fileName -> Zip4jSuite.password)
                                                            .compression(Compression.DEFLATE, CompressionLevel.NORMAL).build();
        ZipEntrySettings oneSettings = ZipEntrySettings.builder()
                                                       .encryption(Encryption.AES_256, fileName -> Zip4jSuite.password)
                                                       .compression(Compression.DEFLATE, CompressionLevel.NORMAL).build();

        try (ZipFile.Writer zipFile = ZipFile.write(solidFile, zipFileSettings)) {
            zipFile.add(Zip4jSuite.fileBentleyContinental, bentleySettings);
            zipFile.add(Zip4jSuite.fileFerrari, ferrariSettings);
            zipFile.add(Zip4jSuite.fileWiesmann, wiesmannSettings);
            zipFile.add(Zip4jSuite.starWarsDir.resolve("one.jpg"), oneSettings);
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
        ZipFileSettings zipFileSettings = ZipFileSettings.builder()
                                                         .defEntrySettings(ZipEntrySettings.builder()
                                                                                           .password(fileName -> Zip4jSuite.password)
                                                                                           .build())
                                                         .build();
        ZipEntrySettings twoSettings = ZipEntrySettings.builder()
                                                       .compression(Compression.STORE, CompressionLevel.NORMAL)
                                                       .encryption(Encryption.PKWARE, fileName -> Zip4jSuite.password).build();
        ZipEntrySettings threeSettings = ZipEntrySettings.builder()
                                                         .compression(Compression.DEFLATE, CompressionLevel.NORMAL)
                                                         .encryption(Encryption.AES_256, fileName -> Zip4jSuite.password).build();

        try (ZipFile.Writer zipFile = ZipFile.write(solidFile, zipFileSettings)) {
            zipFile.add(Zip4jSuite.starWarsDir.resolve("two.jpg"), twoSettings);
            zipFile.add(Zip4jSuite.starWarsDir.resolve("three.jpg"), threeSettings);
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
        ZipFileSettings zipFileSettings = ZipFileSettings.builder()
                                                         .splitSize(1024 * 1024)
                                                         .build();
        ZipEntrySettings settings = ZipEntrySettings.builder()
                                                    .compression(Compression.STORE, CompressionLevel.NORMAL)
                                                    .build();

        try (ZipFile.Writer zipFile = ZipFile.write(splitFile, zipFileSettings)) {
            zipFile.add(Zip4jSuite.carsDir.resolve("bentley-continental.jpg"), settings);
            zipFile.add(Zip4jSuite.carsDir.resolve("ferrari-458-italia.jpg"), settings);
            zipFile.add(Zip4jSuite.carsDir.resolve("wiesmann-gt-mf5.jpg"), settings);
        }

        assertThatDirectory(splitFile.getParent()).exists().hasSubDirectories(0).hasFiles(3);
//        assertThatZipFile(splitFile).exists().rootEntry().hasSubDirectories(0).hasFiles(3);
//        assertThatZipFile(splitFile).file("bentley-continental.jpg").exists().isImage().hasSize(1_395_362);
//        assertThatZipFile(splitFile).file("ferrari-458-italia.jpg").exists().isImage().hasSize(320_894);
//        assertThatZipFile(splitFile).file("wiesmann-gt-mf5.jpg").exists().isImage().hasSize(729_633);
    }

    @Test(dependsOnMethods = "shouldCreateZipFileWhenUseZipFileAndAddFilesSplit")
    public void shouldAddFilesToExistedZipWhenUseZipFileSplit() throws IOException {
        ZipFileSettings zipFileSettings = ZipFileSettings.builder()
                                                         .splitSize(1024 * 1024)
                                                         .build();
        ZipEntrySettings entrySettings = ZipEntrySettings.builder().compression(Compression.STORE, CompressionLevel.NORMAL).build();

        try (ZipFile.Writer zipFile = ZipFile.write(splitFile, zipFileSettings)) {
            zipFile.add(Zip4jSuite.starWarsDir.resolve("one.jpg"), entrySettings);
            zipFile.add(Zip4jSuite.starWarsDir.resolve("two.jpg"), entrySettings);
            zipFile.add(Zip4jSuite.starWarsDir.resolve("three.jpg"), entrySettings);
            zipFile.add(Zip4jSuite.starWarsDir.resolve("four.jpg"), entrySettings);
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
        ZipFileSettings zipFileSettings = ZipFileSettings.builder().build();
        ZipEntrySettings bentleySettings = ZipEntrySettings.builder()
                                                           .compression(Compression.STORE, CompressionLevel.NORMAL).build();
        ZipEntrySettings ferrariSettings = ZipEntrySettings.builder()
                                                           .compression(Compression.DEFLATE, CompressionLevel.NORMAL).build();
        ZipEntrySettings wiesmannSettings = ZipEntrySettings.builder()
                                                            .encryption(Encryption.PKWARE, fileName -> Zip4jSuite.password)
                                                            .compression(Compression.DEFLATE, CompressionLevel.NORMAL).build();
        ZipEntrySettings oneSettings = ZipEntrySettings.builder()
                                                       .encryption(Encryption.AES_256, fileName -> Zip4jSuite.password)
                                                       .compression(Compression.DEFLATE, CompressionLevel.NORMAL).build();

        try (ZipFile.Writer zipFile = ZipFile.write(supplierSolidFile, zipFileSettings)) {
            zipFile.add(RegularFileZipEntrySource.of(Zip4jSuite.fileBentleyContinental, "bentley-continental.jpg"), bentleySettings);
            zipFile.add(RegularFileZipEntrySource.of(Zip4jSuite.fileFerrari, "ferrari-458-italia.jpg"), ferrariSettings);
            zipFile.add(RegularFileZipEntrySource.of(Zip4jSuite.fileWiesmann, "wiesmann-gt-mf5.jpg"), wiesmannSettings);
            zipFile.add(RegularFileZipEntrySource.of(Zip4jSuite.starWarsDir.resolve("one.jpg"), "one.jpg"), oneSettings);
        }

        assertThatDirectory(supplierSolidFile.getParent()).exists().hasSubDirectories(0).hasFiles(1);
        assertThatZipFile(supplierSolidFile, Zip4jSuite.password).exists().rootEntry().hasSubDirectories(0).hasFiles(4);
        assertThatZipFile(supplierSolidFile, Zip4jSuite.password).file("bentley-continental.jpg").exists().isImage().hasSize(1_395_362);
        assertThatZipFile(supplierSolidFile, Zip4jSuite.password).file("ferrari-458-italia.jpg").exists().isImage().hasSize(320_894);
        assertThatZipFile(supplierSolidFile, Zip4jSuite.password).file("wiesmann-gt-mf5.jpg").exists().isImage().hasSize(729_633);
        assertThatZipFile(supplierSolidFile, Zip4jSuite.password).file("one.jpg").exists().isImage().hasSize(2_204_448);
    }
}
