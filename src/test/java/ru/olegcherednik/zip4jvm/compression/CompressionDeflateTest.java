package ru.olegcherednik.zip4jvm.compression;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.olegcherednik.zip4jvm.TestUtils;
import ru.olegcherednik.zip4jvm.UnzipIt;
import ru.olegcherednik.zip4jvm.Zip4jvmSuite;
import ru.olegcherednik.zip4jvm.ZipIt;
import ru.olegcherednik.zip4jvm.model.Compression;
import ru.olegcherednik.zip4jvm.model.CompressionLevel;
import ru.olegcherednik.zip4jvm.model.Encryption;
import ru.olegcherednik.zip4jvm.model.settings.ZipEntrySettings;
import ru.olegcherednik.zip4jvm.model.settings.ZipFileSettings;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static ru.olegcherednik.zip4jvm.assertj.Zip4jvmAssertions.assertThatDirectory;
import static ru.olegcherednik.zip4jvm.assertj.Zip4jvmAssertions.assertThatZipFile;

/**
 * @author Oleg Cherednik
 * @since 06.08.2019
 */
@Test
@SuppressWarnings("FieldNamingConvention")
public class CompressionDeflateTest {

    private static final Path rootDir = Zip4jvmSuite.generateSubDirNameWithTime(CompressionDeflateTest.class);

    @BeforeClass
    public static void createDir() throws IOException {
        Files.createDirectories(rootDir);
    }

    @AfterClass(enabled = Zip4jvmSuite.clear)
    public static void removeDir() throws IOException {
        Zip4jvmSuite.removeDir(rootDir);
    }

    public void shouldCreateSingleZipWithFilesWhenDeflateCompression() throws IOException {
        ZipFileSettings settings = ZipFileSettings.builder()
                                                  .entrySettingsProvider(fileName ->
                                                          ZipEntrySettings.builder()
                                                                          .compression(Compression.DEFLATE, CompressionLevel.NORMAL)
                                                                          .build())
                                                  .build();
        Path zip = Zip4jvmSuite.subDirNameAsMethodName(rootDir).resolve("src.zip");
        ZipIt.add(zip, Zip4jvmSuite.filesStarWarsDir, settings);

        assertThatDirectory(zip.getParent()).exists().hasSubDirectories(0).hasFiles(1);
        assertThatZipFile(zip).directory("/").matches(TestUtils.zipStarWarsDirAssert);
    }

    public void shouldCreateSplitZipWithFilesWhenDeflateCompression() throws IOException {
        ZipFileSettings settings = ZipFileSettings.builder()
                                                  .entrySettingsProvider(fileName ->
                                                          ZipEntrySettings.builder()
                                                                          .compression(Compression.DEFLATE, CompressionLevel.NORMAL)
                                                                          .build())
                                                  .splitSize(1024 * 1024).build();
        Path zip = Zip4jvmSuite.subDirNameAsMethodName(rootDir).resolve("src.zip");
        ZipIt.add(zip, Zip4jvmSuite.filesStarWarsDir, settings);

        assertThatDirectory(zip.getParent()).exists().hasSubDirectories(0).hasFiles(6);
        // TODO check split zip file
    }

    public void shouldCreateSingleZipWithEntireFolderWhenDeflateCompression() throws IOException {
        ZipFileSettings settings = ZipFileSettings.builder()
                                                  .entrySettingsProvider(fileName ->
                                                          ZipEntrySettings.builder()
                                                                          .compression(Compression.DEFLATE, CompressionLevel.NORMAL)
                                                                          .build())
                                                  .build();

        Path zip = Zip4jvmSuite.subDirNameAsMethodName(rootDir).resolve("src.zip");
        ZipIt.add(zip, Zip4jvmSuite.starWarsDir, settings);

        assertThatDirectory(zip.getParent()).exists().hasSubDirectories(0).hasFiles(1);
        assertThatZipFile(zip).exists().rootEntry().hasSubDirectories(1).hasFiles(0);
        assertThatZipFile(zip).directory("Star Wars/").matches(TestUtils.zipStarWarsDirAssert);
    }

    public void shouldCreateSplitZipWithEntireFolderWhenStoreCompression() throws IOException {
        ZipFileSettings settings = ZipFileSettings.builder()
                                                  .splitSize(1024 * 1024)
                                                  .entrySettingsProvider(fileName ->
                                                          ZipEntrySettings.builder()
                                                                          .compression(Compression.STORE, CompressionLevel.NORMAL)
                                                                          .build())
                                                  .build();

        Path zip = Zip4jvmSuite.subDirNameAsMethodName(rootDir).resolve("src.zip");
        ZipIt.add(zip, Zip4jvmSuite.starWarsDir, settings);

        assertThatDirectory(zip.getParent()).exists().hasSubDirectories(0).hasFiles(6);
        // TODO check split zip file
    }

    public void shouldUnzipWhenDeflateCompression() throws IOException {
        Path destDir = Zip4jvmSuite.subDirNameAsMethodName(rootDir);
        UnzipIt.extract(Zip4jvmSuite.deflateSolidZip, destDir);
        assertThatDirectory(destDir).matches(TestUtils.dirAssert);
    }

    public void shouldUnzipWhenWhenStoreCompressionAndPkwareEncryption() throws IOException {
        ZipFileSettings settings = ZipFileSettings.builder()
                                                  .entrySettingsProvider(fileName ->
                                                          ZipEntrySettings.builder()
                                                                          .compression(Compression.DEFLATE, CompressionLevel.NORMAL)
                                                                          .encryption(Encryption.PKWARE, Zip4jvmSuite.password).build())
                                                  .comment("password: " + new String(Zip4jvmSuite.password)).build();
        Path zip = Zip4jvmSuite.subDirNameAsMethodName(rootDir).resolve("src.zip");
        ZipIt.add(zip, Zip4jvmSuite.filesCarsDir, settings);

        assertThatDirectory(zip.getParent()).exists().hasSubDirectories(0).hasFiles(1);
        assertThatZipFile(zip, Zip4jvmSuite.password).directory("/").matches(TestUtils.zipCarsDirAssert);

        Path dirUnzip = Zip4jvmSuite.subDirNameAsMethodName(rootDir).resolve("unzip");
        UnzipIt.extract(zip, dirUnzip, fileName -> Zip4jvmSuite.password);
        assertThatDirectory(dirUnzip).matches(TestUtils.carsDirAssert);
    }

    public void shouldUnzipWhenWhenDeflateCompressionAndAesEncryption() throws IOException {
        ZipFileSettings settings = ZipFileSettings.builder()
                                                  .entrySettingsProvider(fileName ->
                                                          ZipEntrySettings.builder()
                                                                          .compression(Compression.DEFLATE, CompressionLevel.NORMAL)
                                                                          .encryption(Encryption.AES_256, Zip4jvmSuite.password).build())
                                                  .comment("password: " + new String(Zip4jvmSuite.password)).build();
        Path zip = Zip4jvmSuite.subDirNameAsMethodName(rootDir).resolve("src.zip");
        ZipIt.add(zip, Zip4jvmSuite.filesCarsDir, settings);

        assertThatDirectory(zip.getParent()).exists().hasSubDirectories(0).hasFiles(1);
        assertThatZipFile(zip, Zip4jvmSuite.password).directory("/").matches(TestUtils.zipCarsDirAssert);

        Path dirUnzip = Zip4jvmSuite.subDirNameAsMethodName(rootDir).resolve("unzip");
        UnzipIt.extract(zip, dirUnzip, fileName -> Zip4jvmSuite.password);
        assertThatDirectory(dirUnzip).matches(TestUtils.carsDirAssert);
    }

}
