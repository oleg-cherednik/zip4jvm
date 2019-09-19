package ru.olegcherednik.zip4jvm;

import org.apache.commons.io.FileUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;
import ru.olegcherednik.zip4jvm.assertj.Zip4jAssertions;
import ru.olegcherednik.zip4jvm.model.Compression;
import ru.olegcherednik.zip4jvm.model.CompressionLevel;
import ru.olegcherednik.zip4jvm.model.Encryption;
import ru.olegcherednik.zip4jvm.model.settings.ZipEntrySettings;
import ru.olegcherednik.zip4jvm.model.settings.ZipFileSettings;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author Oleg Cherednik
 * @since 06.04.2019
 */
@SuppressWarnings({ "FieldNamingConvention", "NewClassNamingConvention" })
public class Zip64Test {

    private static final Path rootDir = Zip4jSuite.generateSubDirNameWithTime(Zip64Test.class);

    @BeforeClass
    public static void createDir() throws IOException {
        Files.createDirectories(rootDir);
    }

    @AfterClass(enabled = Zip4jSuite.clear)
    public static void removeDir() throws IOException {
        Zip4jSuite.removeDir(rootDir);
    }

    private Path zipFile1;

    @Test
    public void shouldZipWhenZip64() throws IOException {
        ZipFileSettings settings = ZipFileSettings.builder()
                                                  .entrySettingsProvider(fileName ->
                                                          ZipEntrySettings.builder()
                                                                          .compression(Compression.STORE, CompressionLevel.NORMAL)
                                                                          .build())
                                                  .zip64(true).build();
        zipFile1 = Zip4jSuite.subDirNameAsMethodName(rootDir).resolve("src.zip");
        ZipIt.add(zipFile1, Zip4jSuite.contentSrcDir, settings);

        // TODO it seems it could be checked with commons-compress
//        assertThatDirectory(zipFile.getParent()).exists().hasSubDirectories(0).hasFiles(1);
//        assertThatZipFile(zipFile).directory("/").matches(TestUtils.zipRootDirAssert);
    }

    @Test(dependsOnMethods = "shouldZipWhenZip64")
    public void shouldUnzipWhenZip64() throws IOException {
        Path destDir = Zip4jSuite.subDirNameAsMethodName(rootDir);
        UnzipIt.extract(zipFile1, destDir);
        Zip4jAssertions.assertThatDirectory(destDir).matches(TestUtils.dirAssert);
    }

    private Path zipFile2;

    @Test
    public void shouldZipWhenZip64AndAesEncryption() throws IOException {
        ZipFileSettings settings = ZipFileSettings.builder()
                                                  .entrySettingsProvider(fileName ->
                                                          ZipEntrySettings.builder()
                                                                          .compression(Compression.STORE, CompressionLevel.NORMAL)
                                                                          .encryption(Encryption.AES_256, Zip4jSuite.password).build())
                                                  .comment("password: " + new String(Zip4jSuite.password))
                                                  .zip64(true).build();

        zipFile2 = Zip4jSuite.subDirNameAsMethodName(rootDir).resolve("src.zip");
        ZipIt.add(zipFile2, Zip4jSuite.contentSrcDir, settings);

        // TODO it seems it could be checked with commons-compress
//        assertThatDirectory(zipFile.getParent()).exists().hasSubDirectories(0).hasFiles(1);
//        assertThatZipFile(zipFile).directory("/").matches(TestUtils.zipRootDirAssert);
    }

    @Test(dependsOnMethods = "shouldZipWhenZip64AndAesEncryption")
    public void shouldUnzipWhenZip64AndAesEncryption() throws IOException {
        Path destDir = Zip4jSuite.subDirNameAsMethodName(rootDir);
        UnzipIt.extract(zipFile2, destDir, fileName -> Zip4jSuite.password);
        Zip4jAssertions.assertThatDirectory(destDir).matches(TestUtils.dirAssert);
    }

    private Path zipFile3;

    @Test
    public void shouldZipWhenZip64AndSplit() throws IOException {
        ZipFileSettings settings = ZipFileSettings.builder()
                                                  .entrySettingsProvider(fileName ->
                                                          ZipEntrySettings.builder()
                                                                          .compression(Compression.STORE, CompressionLevel.NORMAL)
                                                                          .build())
                                                  .splitSize(1024 * 1024)
                                                  .zip64(true).build();

        zipFile3 = Zip4jSuite.subDirNameAsMethodName(rootDir).resolve("src.zip");
        ZipIt.add(zipFile3, Zip4jSuite.contentSrcDir, settings);

        // TODO it seems it could be checked with commons-compress
//        assertThatDirectory(zipFile.getParent()).exists().hasSubDirectories(0).hasFiles(1);
//        assertThatZipFile(zipFile).directory("/").matches(TestUtils.zipRootDirAssert);
    }

    @Test(dependsOnMethods = "shouldZipWhenZip64AndSplit")
    @Ignore
    public void shouldUnzipWhenZip64AndSplit() throws IOException {
        Path destDir = Zip4jSuite.subDirNameAsMethodName(rootDir);
        UnzipIt.extract(zipFile3, destDir, fileName -> Zip4jSuite.password);
        Zip4jAssertions.assertThatDirectory(destDir).matches(TestUtils.dirAssert);
    }

//    //    @Test
//    public void shouldUseZip64WhenTotalEntriesOver65535() throws IOException {
//        Path dir = Zip4jSuite.subDirNameAsMethodName(rootDir).resolve("data");
////        createData(dir);
//
//        ZipParameters parameters = ZipParameters.builder()
//                                                .compression(Compression.STORE, CompressionLevel.NORMAL)
//                                                .build();
//
//        Path zipFile = Zip4jSuite.subDirNameAsMethodName(rootDir).resolve("src.zip");
//        ZipIt zip = ZipIt.builder().zipFile(zipFile).build();
//        zip.add(Paths.get("d:/zip4j/tmp/data"), parameters);
//
//
////        UnzipIt unzipIt = UnzipIt.builder()
////                                 .zipFile(zipFile)
////                                 .build();
////
////        unzipIt.extract(Paths.get("d:/zip4j/tmp/zip64"));
//
////        assertThatDirectory(zipFile.getParent()).exists().hasSubDirectories(0).hasFiles(1);
////        assertThatZipFile(zipFile).directory("/").matches(TestUtils.zipRootDirAssert);
//    }

//    //    @Test
//    public void shouldUseZip64WhenZipFileOver3Gb() throws IOException {
////        Path dir = Zip4jSuite.subDirNameAsMethodName(rootDir).resolve("data");
////        createData(dir);
//
//        ZipParameters parameters = ZipParameters.builder()
//                                                .compression(Compression.STORE, CompressionLevel.NORMAL)
//                                                .build();
//
//        Path zipFile = Zip4jSuite.subDirNameAsMethodName(rootDir).resolve("src.zip");
//        ZipIt zip = ZipIt.builder().zipFile(zipFile).build();
//        zip.add(Paths.get("d:/zip4j/ferdinand.mkv"), parameters);
//
//
////        UnzipIt unzipIt = UnzipIt.builder()
////                                 .zipFile(zipFile)
////                                 .build();
////
////        unzipIt.extract(Paths.get("d:/zip4j/tmp/zip64"));
//
////        assertThatDirectory(zipFile.getParent()).exists().hasSubDirectories(0).hasFiles(1);
////        assertThatZipFile(zipFile).directory("/").matches(TestUtils.zipRootDirAssert);
//    }

    /**
     * Create 65_535 + 1 entries under {@code root} directory
     */
    private static void createData(Path root) throws IOException {
        for (int i = 0, j = 1; i <= 65; i++) {
            Path dir = root.resolve(String.format("dir_%02d", i));
            Files.createDirectories(dir);

            for (; j <= 1000 * i + (i == 65 ? 536 : 1000); j++)
                FileUtils.writeStringToFile(dir.resolve(String.format("%04d.txt", j)).toFile(), "oleg", StandardCharsets.UTF_8);
        }

    }


}
