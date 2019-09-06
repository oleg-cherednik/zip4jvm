package ru.olegcherednik.zip4jvm.compression;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.olegcherednik.zip4jvm.TestUtils;
import ru.olegcherednik.zip4jvm.UnzipIt;
import ru.olegcherednik.zip4jvm.Zip4jSuite;
import ru.olegcherednik.zip4jvm.ZipIt;
import ru.olegcherednik.zip4jvm.model.Compression;
import ru.olegcherednik.zip4jvm.model.CompressionLevel;
import ru.olegcherednik.zip4jvm.model.Encryption;
import ru.olegcherednik.zip4jvm.model.ZipEntrySettings;
import ru.olegcherednik.zip4jvm.model.ZipFileSettings;
import ru.olegcherednik.zip4jvm.model.ZipParameters;

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
        ZipParameters parameters = ZipParameters.builder()
                                                .compression(Compression.DEFLATE, CompressionLevel.NORMAL)
                                                .defaultFolderPath(Zip4jSuite.srcDir).build();

        Path zipFile = Zip4jSuite.subDirNameAsMethodName(rootDir).resolve("src.zip");
        ZipIt zip = ZipIt.builder().zipFile(zipFile).build();
        zip.add(Zip4jSuite.filesStarWarsDir, parameters);

        assertThatDirectory(zipFile.getParent()).exists().hasSubDirectories(0).hasFiles(1);
        assertThatZipFile(zipFile).exists().rootEntry().hasSubDirectories(1).hasFiles(0);
        assertThatZipFile(zipFile).directory("Star Wars/").matches(TestUtils.zipStarWarsDirAssert);
    }

    public void shouldCreateSplitZipWithFilesWhenDeflateCompression() throws IOException {
        ZipParameters parameters = ZipParameters.builder()
                                                .compression(Compression.DEFLATE, CompressionLevel.NORMAL)
                                                .defaultFolderPath(Zip4jSuite.srcDir)
                                                .splitLength(1024 * 1024).build();

        Path zipFile = Zip4jSuite.subDirNameAsMethodName(rootDir).resolve("src.zip");
        ZipIt zip = ZipIt.builder().zipFile(zipFile).build();
        zip.add(Zip4jSuite.filesStarWarsDir, parameters);

        assertThatDirectory(zipFile.getParent()).exists().hasSubDirectories(0).hasFiles(6);
        // TODO check split zip file
    }

    public void shouldCreateSingleZipWithEntireFolderWhenDeflateCompression() throws IOException {
        ZipFileSettings settings = ZipFileSettings.builder()
                                                  .entrySettings(
                                                          ZipEntrySettings.builder()
                                                                          .compression(Compression.DEFLATE, CompressionLevel.NORMAL).build())
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
                                                                          .compression(Compression.STORE, CompressionLevel.NORMAL).build())
                                                  .build();

        Path zip = Zip4jSuite.subDirNameAsMethodName(rootDir).resolve("src.zip");
        ZipIt.add(zip, Zip4jSuite.starWarsDir, settings);

        assertThatDirectory(zip.getParent()).exists().hasSubDirectories(0).hasFiles(6);
        // TODO check split zip file
    }

    public void shouldUnzipWhenDeflateCompression() throws IOException {
        Path dstDir = Zip4jSuite.subDirNameAsMethodName(rootDir);
        UnzipIt unzip = UnzipIt.builder()
                               .zipFile(Zip4jSuite.deflateSolidZip)
                               .build();
        unzip.extract(dstDir);
        assertThatDirectory(dstDir).matches(TestUtils.dirAssert);
    }

    public void shouldUnzipWhenWhenStoreCompressionAndPkwareEncryption() throws IOException {
        ZipParameters parameters = ZipParameters.builder()
                                                .compression(Compression.DEFLATE, CompressionLevel.NORMAL)
                                                .encryption(Encryption.PKWARE, Zip4jSuite.password)
                                                .defaultFolderPath(Zip4jSuite.srcDir)
                                                .comment("password: " + new String(Zip4jSuite.password)).build();

        Path zipFile = Zip4jSuite.subDirNameAsMethodName(rootDir).resolve("src.zip");
        ZipIt zip = ZipIt.builder().zipFile(zipFile).build();
        zip.add(Zip4jSuite.filesCarsDir, parameters);

        assertThatDirectory(zipFile.getParent()).exists().hasSubDirectories(0).hasFiles(1);
        assertThatZipFile(zipFile, Zip4jSuite.password).exists().rootEntry().hasSubDirectories(1).hasFiles(0);
        assertThatZipFile(zipFile, Zip4jSuite.password).directory("cars/").matches(TestUtils.zipCarsDirAssert);

        Path dirUnzip = Zip4jSuite.subDirNameAsMethodName(rootDir).resolve("unzip");
        UnzipIt unzip = UnzipIt.builder()
                               .zipFile(zipFile)
                               .password(Zip4jSuite.password)
                               .build();
        unzip.extract(dirUnzip);

        assertThatDirectory(dirUnzip).exists().hasSubDirectories(1).hasFiles(0);
        assertThatDirectory(dirUnzip).directory("cars/").matches(TestUtils.carsDirAssert);
    }

    public void shouldUnzipWhenWhenDeflateCompressionAndAesEncryption() throws IOException {
        ZipParameters parameters = ZipParameters.builder()
                                                .compression(Compression.DEFLATE, CompressionLevel.NORMAL)
                                                .encryption(Encryption.AES_256, Zip4jSuite.password)
                                                .comment("password: " + new String(Zip4jSuite.password)).build();

        Path zipFile = Zip4jSuite.subDirNameAsMethodName(rootDir).resolve("src.zip");
        ZipIt zip = ZipIt.builder().zipFile(zipFile).build();
        zip.add(Zip4jSuite.filesCarsDir, parameters);

        assertThatDirectory(zipFile.getParent()).exists().hasSubDirectories(0).hasFiles(1);
        assertThatZipFile(zipFile, Zip4jSuite.password).directory("/").matches(TestUtils.zipCarsDirAssert);

        Path dirUnzip = Zip4jSuite.subDirNameAsMethodName(rootDir).resolve("unzip");
        UnzipIt unzip = UnzipIt.builder()
                               .zipFile(zipFile)
                               .password(Zip4jSuite.password)
                               .build();
        unzip.extract(dirUnzip);

        assertThatDirectory(dirUnzip).matches(TestUtils.carsDirAssert);
    }

}
