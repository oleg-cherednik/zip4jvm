package com.cop.zip4j.compression;

import com.cop.zip4j.Zip4jSuite;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

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

//    public void shouldCreateSingleZipWithFilesWhenStoreCompression() throws IOException, Zip4jException {
//        ZipParameters parameters = ZipParameters.builder()
//                                                .compressionMethod(Compression.DEFLATE)
//                                                .defaultFolderPath(Zip4jSuite.srcDir).build();
//
//        Path bentley = Zip4jSuite.starWarsDir.resolve("bentley-continental.jpg");
//        Path ferrari = Zip4jSuite.starWarsDir.resolve("ferrari-458-italia.jpg");
//        Path wiesmann = Zip4jSuite.starWarsDir.resolve("wiesmann-gt-mf5.jpg");
//        List<Path> files = Arrays.asList(bentley, ferrari, wiesmann);
//
//        Path zipFile = Zip4jSuite.subDirNameAsMethodName(rootDir).resolve("src.zip");
//        ZipIt zip = ZipIt.builder().zipFile(zipFile).build();
//        zip.add(files, parameters);
//
//        assertThatDirectory(zipFile.getParent()).exists().hasSubDirectories(0).hasFiles(1);
//        assertThatZipFile(zipFile).exists().rootEntry().hasSubDirectories(1).hasFiles(0);
//        assertThatZipFile(zipFile).directory("cars/").matches(TestUtils.zipCarsDirAssert);
//    }

//    public void shouldCreateSplitZipWithFilesWhenStoreCompression() throws IOException, Zip4jException {
//        ZipParameters parameters = ZipParameters.builder()
//                                                .compressionMethod(Compression.STORE)
//                                                .defaultFolderPath(Zip4jSuite.srcDir)
//                                                .splitLength(1024 * 1024)
//                                                .build();
//
//        Path bentley = Zip4jSuite.carsDir.resolve("bentley-continental.jpg");
//        Path ferrari = Zip4jSuite.carsDir.resolve("ferrari-458-italia.jpg");
//        Path wiesmann = Zip4jSuite.carsDir.resolve("wiesmann-gt-mf5.jpg");
//        List<Path> files = Arrays.asList(bentley, ferrari, wiesmann);
//
//        Path zipFile = Zip4jSuite.subDirNameAsMethodName(rootDir).resolve("src.zip");
//        ZipIt zip = ZipIt.builder().zipFile(zipFile).build();
//        zip.add(files, parameters);
//
//        assertThatDirectory(zipFile.getParent()).exists().hasSubDirectories(0).hasFiles(3);
//        // TODO check split zip file
//    }
//
//    public void shouldCreateSingleZipWithEntireFolderWhenStoreCompression() throws IOException, Zip4jException {
//        ZipParameters parameters = ZipParameters.builder()
//                                                .compressionMethod(Compression.STORE)
//                                                .defaultFolderPath(Zip4jSuite.srcDir).build();
//
//        Path zipFile = Zip4jSuite.subDirNameAsMethodName(rootDir).resolve("src.zip");
//        ZipIt zipIt = ZipIt.builder().zipFile(zipFile).build();
//        zipIt.add(Zip4jSuite.carsDir, parameters);
//
//        assertThatDirectory(zipFile.getParent()).exists().hasSubDirectories(0).hasFiles(1);
//        assertThatZipFile(zipFile).exists().rootEntry().hasSubDirectories(1).hasFiles(0);
//        assertThatZipFile(zipFile).directory("cars/").matches(TestUtils.zipCarsDirAssert);
//    }
//
//    public void shouldCreateSplitZipWithEntireFolderWhenStoreCompression() throws IOException, Zip4jException {
//        ZipParameters parameters = ZipParameters.builder()
//                                                .compressionMethod(Compression.STORE)
//                                                .defaultFolderPath(Zip4jSuite.srcDir)
//                                                .splitLength(1024 * 1024)
//                                                .build();
//
//        Path zipFile = Zip4jSuite.subDirNameAsMethodName(rootDir).resolve("src.zip");
//        ZipIt zipIt = ZipIt.builder().zipFile(zipFile).build();
//        zipIt.add(Zip4jSuite.carsDir, parameters);
//
//        assertThatDirectory(zipFile.getParent()).exists().hasSubDirectories(0).hasFiles(3);
//        // TODO check split zip file
//    }
//
//    public void shouldUnzipWhenStoreCompression() throws IOException {
//        Path destDir = Zip4jSuite.subDirNameAsMethodName(rootDir);
//        UnzipIt unzip = UnzipIt.builder()
//                               .zipFile(Zip4jSuite.storeSolidZip)
//                               .build();
//        unzip.extract(destDir);
//        assertThatDirectory(destDir).matches(TestUtils.dirAssert);
//    }
//
//    public void shouldUnzipWhenSplitAndStoreCompression() throws IOException {
//        Path destDir = Zip4jSuite.subDirNameAsMethodName(rootDir);
//        UnzipIt unzip = UnzipIt.builder()
//                               .zipFile(Zip4jSuite.storeSplitZip)
//                               .build();
//        unzip.extract(destDir);
//        assertThatDirectory(destDir).matches(TestUtils.dirAssert);
//    }

}
