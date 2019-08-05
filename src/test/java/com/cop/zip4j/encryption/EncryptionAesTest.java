package com.cop.zip4j.encryption;

import com.cop.zip4j.UnzipIt;
import com.cop.zip4j.Zip4jSuite;
import com.cop.zip4j.ZipIt;
import com.cop.zip4j.model.Compression;
import com.cop.zip4j.model.CompressionLevel;
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
import java.util.Arrays;
import java.util.List;

/**
 * @author Oleg Cherednik
 * @since 29.07.2019
 */
@Test
//@Ignore
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

//    public void shouldCreateNewZipWithFolderAndAesEncryption() throws IOException {
//        ZipParameters parameters = ZipParameters.builder()
//                                                .compressionMethod(CompressionMethod.STORE)
//                                                .compressionLevel(CompressionLevel.NORMAL)
//                                                .encryption(Encryption.AES)
//                                                .strength(AesStrength.KEY_STRENGTH_256)
//                                                .comment("password: " + new String(Zip4jSuite.password))
//                                                .password(Zip4jSuite.password).build();
//
//        Path destDir = Zip4jSuite.subDirNameAsMethodName(rootDir);
//        Path zipFile = destDir.resolve("src.zip");
//        ZipIt zip = ZipIt.builder().zipFile(zipFile).build();
//        zip.add(Zip4jSuite.srcDir, parameters);
//
////        assertThatDirectory(destDir).exists().hasSubDirectories(0).hasFiles(1);
////        assertThatEncryptedZipFile(zipFile, Zip4jSuite.password).exists().rootEntry().matches(TestUtils.zipRootDirAssert);
//    }

    public void shouldCreateNewZipWithSelectedFilesAndAesEncryption() throws IOException {
        ZipParameters parameters = ZipParameters.builder()
                                                .compressionMethod(Compression.STORE)
                                                .compressionLevel(CompressionLevel.NORMAL)
                                                .encryption(Encryption.AES_NEW)
                                                .strength(AesStrength.KEY_STRENGTH_256)
                                                .comment("password: " + new String(Zip4jSuite.password))
                                                .password(Zip4jSuite.password).build();

        Path destDir = Zip4jSuite.subDirNameAsMethodNameWithTme(rootDir);
        Path zipFile = destDir.resolve("src.zip");

//        Path bentley = Zip4jSuite.carsDir.resolve("bentley-continental.jpg");
//        Path ferrari = Zip4jSuite.carsDir.resolve("ferrari-458-italia.jpg");
//        Path wiesmann = Zip4jSuite.carsDir.resolve("wiesmann-gt-mf5.jpg");
//        Path txt = Zip4jSuite.srcDir.resolve("Oleg Cherednik.txt");
        Path txt = Paths.get("d:/zip4j/tmp/foo.txt");
        List<Path> files = Arrays.asList(txt);

        ZipIt zip = ZipIt.builder().zipFile(zipFile).build();
        zip.add(files, parameters);

        destDir = destDir.resolve("unzip");
        UnzipIt unzip = UnzipIt.builder()
                               .zipFile(zipFile)
                               .password(Zip4jSuite.password).build();
        unzip.extract(destDir);


//        assertThatDirectory(zipFile.getParent()).exists().hasSubDirectories(0).hasFiles(1);
//        assertThatZipFile(zipFile, Zip4jSuite.password).exists().rootEntry().hasSubDirectories(0).hasFiles(3);
//        assertThatEncryptedZipFile(zipFile, Zip4jSuite.password).directory("/").matches(TestUtils.zipCarsDirAssert);
    }
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

//    public void shouldUnzipWhenAesEncryption() throws IOException {
//        Path destDir = Zip4jSuite.subDirNameAsMethodName(rootDir);
////        Path zipFile = destDir.resolve("d:/zip4j/aes.zip");
//        Path zipFile = destDir.resolve("d:/zip4j/tmp/aes.zip");
//        UnzipIt unzip = UnzipIt.builder()
//                               .zipFile(zipFile)
//                               .password(Zip4jSuite.password).build();
//        unzip.extract(destDir);
//    }

}
