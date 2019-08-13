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
import java.nio.file.Paths;
import java.util.Collections;

import static com.cop.zip4j.assertj.Zip4jAssertions.assertThatDirectory;
import static com.cop.zip4j.assertj.Zip4jAssertions.assertThatZipFile;

/**
 * @author Oleg Cherednik
 * @since 15.03.2019
 */
@Test
@SuppressWarnings("FieldNamingConvention")
public class CompressionStoreTest {

    private static final Path rootDir = Zip4jSuite.generateSubDirNameWithTime(CompressionStoreTest.class);

    @BeforeClass
    public static void createDir() throws IOException {
        Files.createDirectories(rootDir);
    }

    @AfterClass(enabled = Zip4jSuite.clear)
    public static void removeDir() throws IOException {
        Zip4jSuite.removeDir(rootDir);
    }

    public void shouldCreateSingleZipWithFilesWhenStoreCompression() throws IOException {
        ZipParameters parameters = ZipParameters.builder()
                                                .compressionMethod(Compression.STORE)
                                                .defaultFolderPath(Zip4jSuite.srcDir).build();

        Path zipFile = Zip4jSuite.subDirNameAsMethodName(rootDir).resolve("src.zip");
        ZipIt zip = ZipIt.builder().zipFile(zipFile).build();
        zip.add(Zip4jSuite.filesCarsDir, parameters);

        assertThatDirectory(zipFile.getParent()).exists().hasSubDirectories(0).hasFiles(1);
        assertThatZipFile(zipFile).exists().rootEntry().hasSubDirectories(1).hasFiles(0);
        assertThatZipFile(zipFile).directory("cars/").matches(TestUtils.zipCarsDirAssert);
    }

    public void shouldCreateSplitZipWithFilesWhenStoreCompression() throws IOException {
        ZipParameters parameters = ZipParameters.builder()
                                                .compressionMethod(Compression.STORE)
                                                .defaultFolderPath(Zip4jSuite.srcDir)
                                                .splitLength(1024 * 1024)
                                                .build();

        Path zipFile = Zip4jSuite.subDirNameAsMethodName(rootDir).resolve("src.zip");
        ZipIt zip = ZipIt.builder().zipFile(zipFile).build();
        zip.add(Zip4jSuite.filesCarsDir, parameters);

        assertThatDirectory(zipFile.getParent()).exists().hasSubDirectories(0).hasFiles(3);
        // TODO check split zip file
    }

    public void shouldCreateSingleZipWithEntireFolderWhenStoreCompression() throws IOException {
        ZipParameters parameters = ZipParameters.builder()
                                                .compressionMethod(Compression.STORE)
                                                .defaultFolderPath(Zip4jSuite.srcDir).build();

        Path zipFile = Zip4jSuite.subDirNameAsMethodName(rootDir).resolve("src.zip");
        ZipIt zipIt = ZipIt.builder().zipFile(zipFile).build();
        zipIt.add(Zip4jSuite.carsDir, parameters);

        assertThatDirectory(zipFile.getParent()).exists().hasSubDirectories(0).hasFiles(1);
        assertThatZipFile(zipFile).exists().rootEntry().hasSubDirectories(1).hasFiles(0);
        assertThatZipFile(zipFile).directory("cars/").matches(TestUtils.zipCarsDirAssert);
    }

    public void shouldCreateSplitZipWithEntireFolderWhenStoreCompression() throws IOException {
        ZipParameters parameters = ZipParameters.builder()
                                                .compressionMethod(Compression.STORE)
                                                .defaultFolderPath(Zip4jSuite.srcDir)
                                                .splitLength(1024 * 1024)
                                                .build();

        Path zipFile = Zip4jSuite.subDirNameAsMethodName(rootDir).resolve("src.zip");
        ZipIt zipIt = ZipIt.builder().zipFile(zipFile).build();
        zipIt.add(Zip4jSuite.carsDir, parameters);

        assertThatDirectory(zipFile.getParent()).exists().hasSubDirectories(0).hasFiles(3);
        // TODO check split zip file
    }

    public void shouldUnzipWhenStoreCompression() throws IOException {
        Path dstDir = Zip4jSuite.subDirNameAsMethodName(rootDir);
        UnzipIt unzip = UnzipIt.builder()
                               .zipFile(Zip4jSuite.storeSolidZip)
                               .build();
        unzip.extract(dstDir);
        assertThatDirectory(dstDir).matches(TestUtils.dirAssert);
    }

    public void shouldUnzipWhenSplitAndStoreCompression() throws IOException {
        Path dstDir = Zip4jSuite.subDirNameAsMethodName(rootDir);
        UnzipIt unzip = UnzipIt.builder()
                               .zipFile(Zip4jSuite.storeSplitZip)
                               .build();
        unzip.extract(dstDir);
        assertThatDirectory(dstDir).matches(TestUtils.dirAssert);
    }

    public void shouldUnzipWhenWhenStoreCompressionAndPkwareEncryption() throws IOException {
        ZipParameters parameters = ZipParameters.builder()
                                                .compressionMethod(Compression.STORE)
                                                .encryption(Encryption.PKWARE)
                                                .defaultFolderPath(Zip4jSuite.srcDir)
                                                .comment("password: " + new String(Zip4jSuite.password))
                                                .password(Zip4jSuite.password).build();

        Path zipFile = Zip4jSuite.subDirNameAsMethodName(rootDir).resolve("src.zip");
        ZipIt zip = ZipIt.builder().zipFile(zipFile).build();
        zip.add(Zip4jSuite.filesCarsDir, parameters);
//        zip.add(Collections.singleton(Paths.get("c:/zip4j/foo/src/Oleg Cherednik.txt")), parameters);

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

    public void shouldUnzipWhenWhenStoreCompressionAndAesEncryption() throws IOException {
        ZipParameters parameters = ZipParameters.builder()
                                                .compressionMethod(Compression.STORE)
                                                .encryption(Encryption.AES)
                                                .strength(AesStrength.KEY_STRENGTH_256)
//                                                .defaultFolderPath(Zip4jSuite.srcDir)
                                                .comment("password: " + new String(Zip4jSuite.password))
                                                .password(Zip4jSuite.password).build();

        Path zipFile = Zip4jSuite.subDirNameAsMethodName(rootDir).resolve("src.zip");
        ZipIt zip = ZipIt.builder().zipFile(zipFile).build();
//        zip.add(Zip4jSuite.filesCarsDir, parameters);
        zip.add(Collections.singleton(Paths.get("d:/zip4j/tmp/tmp.txt")), parameters);

//        assertThatDirectory(zipFile.getParent()).exists().hasSubDirectories(0).hasFiles(1);
//        assertThatZipFile(zipFile, Zip4jSuite.password).exists().rootEntry().hasSubDirectories(1).hasFiles(0);
//        assertThatZipFile(zipFile, Zip4jSuite.password).directory("cars/").matches(TestUtils.zipCarsDirAssert);

//        Path dirUnzip = Zip4jSuite.subDirNameAsMethodName(rootDir).resolve("unzip");
//        UnzipIt unzip = UnzipIt.builder()
//                               .zipFile(zipFile)
//                               .password(Zip4jSuite.password)
//                               .build();
//        unzip.extract(dirUnzip);

//        assertThatDirectory(dirUnzip).exists().hasSubDirectories(1).hasFiles(0);
//        assertThatDirectory(dirUnzip).directory("cars/").matches(TestUtils.carsDirAssert);
    }

}
