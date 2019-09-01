package com.cop.zip4j.compression;

import com.cop.zip4j.TestUtils;
import com.cop.zip4j.UnzipIt;
import com.cop.zip4j.Zip4jSuite;
import com.cop.zip4j.ZipIt;
import com.cop.zip4j.model.Compression;
import com.cop.zip4j.model.Encryption;
import com.cop.zip4j.model.ZipParameters;
import com.cop.zip4j.model.aes.AesStrength;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.cop.zip4j.assertj.Zip4jAssertions.assertThatDirectory;
import static com.cop.zip4j.assertj.Zip4jAssertions.assertThatZipFile;

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
                                                .compression(Compression.DEFLATE)
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
                                                .compression(Compression.DEFLATE)
                                                .defaultFolderPath(Zip4jSuite.srcDir)
                                                .splitLength(1024 * 1024)
                                                .build();

        Path zipFile = Zip4jSuite.subDirNameAsMethodName(rootDir).resolve("src.zip");
        ZipIt zip = ZipIt.builder().zipFile(zipFile).build();
        zip.add(Zip4jSuite.filesStarWarsDir, parameters);

        assertThatDirectory(zipFile.getParent()).exists().hasSubDirectories(0).hasFiles(6);
        // TODO check split zip file
    }

    public void shouldCreateSingleZipWithEntireFolderWhenDeflateCompression() throws IOException {
        ZipParameters parameters = ZipParameters.builder()
                                                .compression(Compression.DEFLATE)
                                                .defaultFolderPath(Zip4jSuite.srcDir).build();

        Path zipFile = Zip4jSuite.subDirNameAsMethodName(rootDir).resolve("src.zip");
        ZipIt zipIt = ZipIt.builder().zipFile(zipFile).build();
        zipIt.add(Zip4jSuite.starWarsDir, parameters);

        assertThatDirectory(zipFile.getParent()).exists().hasSubDirectories(0).hasFiles(1);
        assertThatZipFile(zipFile).exists().rootEntry().hasSubDirectories(1).hasFiles(0);
        assertThatZipFile(zipFile).directory("Star Wars/").matches(TestUtils.zipStarWarsDirAssert);
    }

    public void shouldCreateSplitZipWithEntireFolderWhenStoreCompression() throws IOException {
        ZipParameters parameters = ZipParameters.builder()
                                                .compression(Compression.STORE)
                                                .defaultFolderPath(Zip4jSuite.srcDir)
                                                .splitLength(1024 * 1024)
                                                .build();

        Path zipFile = Zip4jSuite.subDirNameAsMethodName(rootDir).resolve("src.zip");
        ZipIt zipIt = ZipIt.builder().zipFile(zipFile).build();
        zipIt.add(Zip4jSuite.starWarsDir, parameters);

        assertThatDirectory(zipFile.getParent()).exists().hasSubDirectories(0).hasFiles(6);
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
                                                .compression(Compression.DEFLATE)
                                                .encryption(Encryption.PKWARE)
                                                .defaultFolderPath(Zip4jSuite.srcDir)
                                                .comment("password: " + new String(Zip4jSuite.password))
                                                .password(Zip4jSuite.password).build();

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
                                                .compression(Compression.DEFLATE)
                                                .encryption(Encryption.AES)
                                                .strength(AesStrength.KEY_STRENGTH_256)
                                                .comment("password: " + new String(Zip4jSuite.password))
                                                .password(Zip4jSuite.password).build();

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
