package ru.olegcherednik.zip4jvm.compression;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.olegcherednik.zip4jvm.TestUtils;
import ru.olegcherednik.zip4jvm.Zip4jSuite;
import ru.olegcherednik.zip4jvm.ZipFile;
import ru.olegcherednik.zip4jvm.ZipIt;
import ru.olegcherednik.zip4jvm.model.Compression;
import ru.olegcherednik.zip4jvm.model.CompressionLevel;
import ru.olegcherednik.zip4jvm.model.Encryption;
import ru.olegcherednik.zip4jvm.model.settings.ZipEntrySettings;
import ru.olegcherednik.zip4jvm.model.settings.ZipFileSettings;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static ru.olegcherednik.zip4jvm.assertj.Zip4jAssertions.assertThatDirectory;
import static ru.olegcherednik.zip4jvm.assertj.Zip4jAssertions.assertThatZipFile;

/**
 * @author Oleg Cherednik
 * @since 06.08.2019
 */
@Test
@SuppressWarnings("FieldNamingConvention")
public class CompressionDeflateTest {

    private static final Path rootDir = Zip4jSuite.generateSubDirNameWithTime(CompressionDeflateTest.class);

    @BeforeClass
    public static void createDir() throws IOException {
        Files.createDirectories(rootDir);
    }

    @AfterClass(enabled = Zip4jSuite.clear)
    public static void removeDir() throws IOException {
        Zip4jSuite.removeDir(rootDir);
    }

    public void shouldCreateSingleZipWithFilesWhenDeflateCompression() throws IOException {
        ZipFileSettings settings = ZipFileSettings.builder()
                                                  .entrySettings(
                                                                      ZipEntrySettings.builder()
                                                                                      .compression(Compression.DEFLATE, CompressionLevel.NORMAL)
                                                                                      .build())
                                                  .build();
        Path zip = Zip4jSuite.subDirNameAsMethodName(rootDir).resolve("src.zip");
        ZipIt.add(zip, Zip4jSuite.filesStarWarsDir, settings);

        assertThatDirectory(zip.getParent()).exists().hasSubDirectories(0).hasFiles(1);
        assertThatZipFile(zip).directory("/").matches(TestUtils.zipStarWarsDirAssert);
    }

    public void shouldCreateSplitZipWithFilesWhenDeflateCompression() throws IOException {
        ZipFileSettings settings = ZipFileSettings.builder()
                                                  .entrySettings(
                                                                      ZipEntrySettings.builder()
                                                                                      .compression(Compression.DEFLATE, CompressionLevel.NORMAL)
                                                                                      .build())
                                                  .splitSize(1024 * 1024).build();
        Path zip = Zip4jSuite.subDirNameAsMethodName(rootDir).resolve("src.zip");
        ZipIt.add(zip, Zip4jSuite.filesStarWarsDir, settings);

        assertThatDirectory(zip.getParent()).exists().hasSubDirectories(0).hasFiles(6);
        // TODO check split zip file
    }

    public void shouldCreateSingleZipWithEntireFolderWhenDeflateCompression() throws IOException {
        ZipFileSettings settings = ZipFileSettings.builder()
                                                  .entrySettings(
                                                                      ZipEntrySettings.builder()
                                                                                      .compression(Compression.DEFLATE, CompressionLevel.NORMAL)
                                                                                      .build())
                                                  .build();

        Path zip = Zip4jSuite.subDirNameAsMethodName(rootDir).resolve("src.zip");
        ZipIt.add(zip, Zip4jSuite.starWarsDir, settings);

        assertThatDirectory(zip.getParent()).exists().hasSubDirectories(0).hasFiles(1);
        assertThatZipFile(zip).exists().rootEntry().hasSubDirectories(1).hasFiles(0);
        assertThatZipFile(zip).directory("Star Wars/").matches(TestUtils.zipStarWarsDirAssert);
    }

    public void shouldCreateSplitZipWithEntireFolderWhenStoreCompression() throws IOException {
        ZipFileSettings settings = ZipFileSettings.builder()
                                                  .splitSize(1024 * 1024)
                                                  .entrySettings(
                                                                      ZipEntrySettings.builder()
                                                                                      .compression(Compression.STORE, CompressionLevel.NORMAL)
                                                                                      .build())
                                                  .build();

        Path zip = Zip4jSuite.subDirNameAsMethodName(rootDir).resolve("src.zip");
        ZipIt.add(zip, Zip4jSuite.starWarsDir, settings);

        assertThatDirectory(zip.getParent()).exists().hasSubDirectories(0).hasFiles(6);
        // TODO check split zip file
    }

    public void shouldUnzipWhenDeflateCompression() throws IOException {
        Path destDir = Zip4jSuite.subDirNameAsMethodName(rootDir);
        ZipFile.Reader zipFile = ZipFile.read(Zip4jSuite.deflateSolidZip);
        zipFile.extract(destDir);

        assertThatDirectory(destDir).matches(TestUtils.dirAssert);
    }

    public void shouldUnzipWhenWhenStoreCompressionAndPkwareEncryption() throws IOException {
        ZipFileSettings settings = ZipFileSettings.builder()
                                                  .entrySettings(
                                                                      ZipEntrySettings.builder()
                                                                                      .compression(Compression.DEFLATE, CompressionLevel.NORMAL)
                                                                                      .encryption(Encryption.PKWARE, fileName -> Zip4jSuite.password)
                                                                                      .build())
                                                  .comment("password: " + new String(Zip4jSuite.password)).build();
        Path zip = Zip4jSuite.subDirNameAsMethodName(rootDir).resolve("src.zip");
        ZipIt.add(zip, Zip4jSuite.filesCarsDir, settings);

        assertThatDirectory(zip.getParent()).exists().hasSubDirectories(0).hasFiles(1);
        assertThatZipFile(zip, Zip4jSuite.password).directory("/").matches(TestUtils.zipCarsDirAssert);

        Path dirUnzip = Zip4jSuite.subDirNameAsMethodName(rootDir).resolve("unzip");
        ZipFile.Reader zipFile = ZipFile.read(zip, fileName -> Zip4jSuite.password);
        zipFile.extract(dirUnzip);
        assertThatDirectory(dirUnzip).matches(TestUtils.carsDirAssert);
    }

    public void shouldUnzipWhenWhenDeflateCompressionAndAesEncryption() throws IOException {
        ZipFileSettings settings = ZipFileSettings.builder()
                                                  .entrySettings(
                                                                      ZipEntrySettings.builder()
                                                                                      .compression(Compression.DEFLATE, CompressionLevel.NORMAL)
                                                                                      .encryption(Encryption.AES_256, fileName -> Zip4jSuite.password)
                                                                                      .build())
                                                  .comment("password: " + new String(Zip4jSuite.password)).build();
        Path zip = Zip4jSuite.subDirNameAsMethodName(rootDir).resolve("src.zip");
        ZipIt.add(zip, Zip4jSuite.filesCarsDir, settings);

        assertThatDirectory(zip.getParent()).exists().hasSubDirectories(0).hasFiles(1);
        assertThatZipFile(zip, Zip4jSuite.password).directory("/").matches(TestUtils.zipCarsDirAssert);

        Path dirUnzip = Zip4jSuite.subDirNameAsMethodName(rootDir).resolve("unzip");
        ZipFile.Reader zipFile = ZipFile.read(zip, fileName -> Zip4jSuite.password);
        zipFile.extract(dirUnzip);

        assertThatDirectory(dirUnzip).matches(TestUtils.carsDirAssert);
    }

}
