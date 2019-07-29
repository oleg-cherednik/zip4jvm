package com.cop.zip4j.encryption;

import com.cop.zip4j.TestUtils;
import com.cop.zip4j.Zip4jSuite;
import com.cop.zip4j.ZipIt;
import com.cop.zip4j.model.AesStrength;
import com.cop.zip4j.model.CompressionLevel;
import com.cop.zip4j.model.CompressionMethod;
import com.cop.zip4j.model.Encryption;
import com.cop.zip4j.model.ZipParameters;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.cop.zip4j.assertj.Zip4jAssertions.assertThatDirectory;
import static com.cop.zip4j.assertj.Zip4jAssertions.assertThatEncryptedZipFile;

/**
 * @author Oleg Cherednik
 * @since 29.07.2019
 */
@Test
@SuppressWarnings("FieldNamingConvention")
public class EncryptionAesTest {

    private static final Path rootDir = Zip4jSuite.generateSubDirName(EncryptionAesTest.class);

    @BeforeClass
    public static void createDir() throws IOException {
        Files.createDirectories(rootDir);
    }

    @AfterClass(enabled = Zip4jSuite.clear)
    public static void removeDir() throws IOException {
        Zip4jSuite.removeDir(rootDir);
    }

    public void shouldCreateNewZipWithFolderAndAesEncryption() throws IOException {
        ZipParameters parameters = ZipParameters.builder()
                                                .compressionMethod(CompressionMethod.DEFLATE)
                                                .compressionLevel(CompressionLevel.NORMAL)
                                                .encryption(Encryption.AES)
                                                .aesStrength(AesStrength.KEY_STRENGTH_256)
                                                .comment("password: " + new String(Zip4jSuite.password))
                                                .password(Zip4jSuite.password).build();

        Path destDir = Zip4jSuite.subDirNameAsMethodName(rootDir);
        Path zipFile = destDir.resolve("src.zip");
        ZipIt zip = ZipIt.builder().zipFile(zipFile).build();
        zip.add(Zip4jSuite.srcDir, parameters);

        assertThatDirectory(destDir).exists().hasSubDirectories(0).hasFiles(1);
        assertThatEncryptedZipFile(zipFile, Zip4jSuite.password).exists().rootEntry().matches(TestUtils.zipRootDirAssert);
    }

//    public void shouldCreateNewZipWithSelectedFilesAndStandardEncryption() throws IOException {
//        ZipParameters parameters = ZipParameters.builder()
//                                                .compressionMethod(CompressionMethod.DEFLATE)
//                                                .compressionLevel(CompressionLevel.NORMAL)
//                                                .encryption(Encryption.PKWARE)
//                                                .comment("password: " + new String(Zip4jSuite.password))
//                                                .password(Zip4jSuite.password).build();
//
//        Path destDir = Zip4jSuite.subDirNameAsMethodName(rootDir);
//        Path zipFile = destDir.resolve("src.zip");
//
//        Path bentley = Zip4jSuite.carsDir.resolve("bentley-continental.jpg");
//        Path ferrari = Zip4jSuite.carsDir.resolve("ferrari-458-italia.jpg");
//        Path wiesmann = Zip4jSuite.carsDir.resolve("wiesmann-gt-mf5.jpg");
//        List<Path> files = Arrays.asList(bentley, ferrari, wiesmann);
//
//        ZipIt zip = ZipIt.builder().zipFile(zipFile).build();
//        zip.add(files, parameters);
//
//        assertThatDirectory(zipFile.getParent()).exists().hasSubDirectories(0).hasFiles(1);
//        assertThatEncryptedZipFile(zipFile, Zip4jSuite.password).exists().rootEntry().hasSubDirectories(0).hasFiles(3);
//        assertThatEncryptedZipFile(zipFile, Zip4jSuite.password).directory("/").matches(TestUtils.zipCarsDirAssert);
//    }
//
//    public void shouldThrowExceptionWhenStandardEncryptionAndNullPassword() throws IOException {
//        ZipParameters parameters = ZipParameters.builder()
//                                                .compressionMethod(CompressionMethod.DEFLATE)
//                                                .compressionLevel(CompressionLevel.NORMAL)
//                                                .encryption(Encryption.PKWARE)
//                                                .password(null).build();
//
//        Path destDir = Zip4jSuite.subDirNameAsMethodName(rootDir);
//        Path zipFile = destDir.resolve("src.zip");
//        ZipIt zip = ZipIt.builder().zipFile(zipFile).build();
//
//        assertThatThrownBy(() -> zip.add(Zip4jSuite.srcDir, parameters)).isExactlyInstanceOf(Zip4jException.class);
//    }
//
//    public void shouldThrowExceptionWhenStandardEncryptionAndEmptyPassword() throws IOException {
//        ZipParameters parameters = ZipParameters.builder()
//                                                .compressionMethod(CompressionMethod.DEFLATE)
//                                                .compressionLevel(CompressionLevel.NORMAL)
//                                                .encryption(Encryption.PKWARE)
//                                                .password("".toCharArray()).build();
//
//        Path destDir = Zip4jSuite.subDirNameAsMethodName(rootDir);
//        Path zipFile = destDir.resolve("src.zip");
//        ZipIt zip = ZipIt.builder().zipFile(zipFile).build();
//
//        assertThatThrownBy(() -> zip.add(Zip4jSuite.srcDir, parameters)).isExactlyInstanceOf(Zip4jException.class);
//    }
//
//    public void shouldUnzipWhenStandardEncryption() throws IOException {
//        ZipParameters parameters = ZipParameters.builder()
//                                                .compressionMethod(CompressionMethod.DEFLATE)
//                                                .compressionLevel(CompressionLevel.NORMAL)
//                                                .encryption(Encryption.PKWARE)
//                                                .comment("password: " + new String(Zip4jSuite.password))
//                                                .password(Zip4jSuite.password).build();
//
//        Path destDir = Zip4jSuite.subDirNameAsMethodName(rootDir);
//        Path zipFile = destDir.resolve("src.zip");
//        ZipIt zip = ZipIt.builder().zipFile(zipFile).build();
//        zip.add(Zip4jSuite.srcDir, parameters);
//
//        destDir = destDir.resolve("unzip");
//        UnzipIt unzip = UnzipIt.builder()
//                               .zipFile(zipFile)
//                               .password(Zip4jSuite.password).build();
//        unzip.extract(destDir);
//
//        assertThatDirectory(destDir).matches(TestUtils.dirAssert);
//    }
//
//    public void shouldThrowExceptionWhenUnzipStandardEncryptedZipWithIncorrectPassword() throws IOException {
//        ZipParameters parameters = ZipParameters.builder()
//                                                .compressionMethod(CompressionMethod.DEFLATE)
//                                                .compressionLevel(CompressionLevel.NORMAL)
//                                                .encryption(Encryption.PKWARE)
//                                                .comment("password: " + new String(Zip4jSuite.password))
//                                                .password(Zip4jSuite.password).build();
//
//        Path destDir = Zip4jSuite.subDirNameAsMethodName(rootDir);
//        Path zipFile = destDir.resolve("src.zip");
//        ZipIt zip = ZipIt.builder().zipFile(zipFile).build();
//        zip.add(Zip4jSuite.srcDir, parameters);
//
//        Path destDir1 = destDir.resolve("unzip");
//        UnzipIt unzip = UnzipIt.builder()
//                               .zipFile(zipFile)
//                               .password(UUID.randomUUID().toString().toCharArray()).build();
//
//        assertThatThrownBy(() -> unzip.extract(destDir1)).isExactlyInstanceOf(Zip4jException.class);
//    }

}
