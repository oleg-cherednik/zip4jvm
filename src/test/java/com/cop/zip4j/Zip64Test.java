package com.cop.zip4j;

import com.cop.zip4j.model.Compression;
import com.cop.zip4j.model.Encryption;
import com.cop.zip4j.model.ZipParameters;
import com.cop.zip4j.model.aes.AesStrength;
import org.apache.commons.io.FileUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.cop.zip4j.assertj.Zip4jAssertions.assertThatDirectory;

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
        ZipParameters parameters = ZipParameters.builder()
                                                .compression(Compression.STORE)
                                                .zip64(true)
                                                .build();

        zipFile1 = Zip4jSuite.subDirNameAsMethodName(rootDir).resolve("src.zip");
        ZipIt zip = ZipIt.builder().zipFile(zipFile1).build();
        zip.add(Zip4jSuite.srcDir, parameters);

        // TODO it seems it could be checked with commons-compress
//        assertThatDirectory(zipFile.getParent()).exists().hasSubDirectories(0).hasFiles(1);
//        assertThatZipFile(zipFile).directory("/").matches(TestUtils.zipRootDirAssert);
    }

    @Test(dependsOnMethods = "shouldZipWhenZip64")
    public void shouldUnzipWhenZip64() throws IOException {
        Path dstDir = Zip4jSuite.subDirNameAsMethodName(rootDir);
        UnzipIt unzip = UnzipIt.builder()
                               .zipFile(zipFile1)
                               .build();
        unzip.extract(dstDir);
        assertThatDirectory(dstDir).matches(TestUtils.dirAssert);
    }

    private Path zipFile2;

    @Test
    public void shouldZipWhenZip64AndAesEncryption() throws IOException {
        ZipParameters parameters = ZipParameters.builder()
                                                .compression(Compression.STORE)
                                                .encryption(Encryption.AES)
                                                .strength(AesStrength.KEY_STRENGTH_256)
                                                .zip64(true)
                                                .comment("password: " + new String(Zip4jSuite.password))
                                                .password(Zip4jSuite.password).build();

        zipFile2 = Zip4jSuite.subDirNameAsMethodName(rootDir).resolve("src.zip");
        ZipIt zip = ZipIt.builder().zipFile(zipFile2).build();
        zip.add(Zip4jSuite.srcDir, parameters);

        // TODO it seems it could be checked with commons-compress
//        assertThatDirectory(zipFile.getParent()).exists().hasSubDirectories(0).hasFiles(1);
//        assertThatZipFile(zipFile).directory("/").matches(TestUtils.zipRootDirAssert);
    }

    @Test(dependsOnMethods = "shouldZipWhenZip64AndAesEncryption")
    public void shouldUnzipWhenZip64AndAesEncryption() throws IOException {
        Path dstDir = Zip4jSuite.subDirNameAsMethodName(rootDir);
        UnzipIt unzip = UnzipIt.builder()
                               .zipFile(zipFile2)
                               .password(Zip4jSuite.password)
                               .build();
        unzip.extract(dstDir);
        assertThatDirectory(dstDir).matches(TestUtils.dirAssert);
    }

    private Path zipFile3;

    @Test
    public void shouldZipWhenZip64AndSplit() throws IOException {
        ZipParameters parameters = ZipParameters.builder()
                                                .compression(Compression.STORE)
                                                .zip64(true)
                                                .splitLength(1024 * 1024)
                                                .build();

        zipFile3 = Zip4jSuite.subDirNameAsMethodName(rootDir).resolve("src.zip");
        ZipIt zip = ZipIt.builder().zipFile(zipFile3).build();
        zip.add(Zip4jSuite.srcDir, parameters);

        // TODO it seems it could be checked with commons-compress
//        assertThatDirectory(zipFile.getParent()).exists().hasSubDirectories(0).hasFiles(1);
//        assertThatZipFile(zipFile).directory("/").matches(TestUtils.zipRootDirAssert);
    }

    @Test(dependsOnMethods = "shouldZipWhenZip64AndSplit")
    public void shouldUnzipWhenZip64AndSplit() throws IOException {
        Path dstDir = Zip4jSuite.subDirNameAsMethodName(rootDir);
        UnzipIt unzip = UnzipIt.builder()
                               .zipFile(zipFile3)
                               .password(Zip4jSuite.password)
                               .build();
        unzip.extract(dstDir);
        assertThatDirectory(dstDir).matches(TestUtils.dirAssert);
    }

    @Test
    public void shouldUseZip64WhenTotalEntriesOver65535() throws IOException {

//        Path dir = Paths.get("d:/zip4j/tmp/data/dir_00");//Zip4jSuite.subDirNameAsMethodName(rootDir).resolve("data");
//        createData(dir);

        ZipParameters parameters = ZipParameters.builder()
                                                .compression(Compression.STORE)
                                                .zip64(true)
                                                .build();

        Path zipFile = Zip4jSuite.subDirNameAsMethodName(rootDir).resolve("src.zip");
        ZipIt zip = ZipIt.builder().zipFile(zipFile).build();
        zip.add(Zip4jSuite.srcDir.resolve("Oleg Cherednik.txt"), parameters);
//
//
//        int a = 0;
//        a++;


//        Path zipFile = Paths.get("d:/zip4j/tmp/data.zip");
//        Path zipFile = Paths.get("d:/zip4j/tmp/winzip.zip");
//        Path zipFile = Paths.get("d:/zip4j/foo/Zip64Test/1567417831668/shouldUseZip64WhenTotalEntriesOver65535/src.zip");
        UnzipIt unzipIt = UnzipIt.builder()
                                 .zipFile(zipFile)
                                 .build();

        unzipIt.extract(Paths.get("d:/zip4j/tmp/zip64"));


        // TODO it seems it could be checked with commons-compress
//        assertThatDirectory(zipFile.getParent()).exists().hasSubDirectories(0).hasFiles(1);
//        assertThatZipFile(zipFile).directory("/").matches(TestUtils.zipRootDirAssert);
    }

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
