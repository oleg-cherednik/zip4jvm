package ru.olegcherednik.zip4jvm.encryption;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.olegcherednik.zip4jvm.TestDataAssert;
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
 * @since 29.07.2019
 */
@SuppressWarnings("FieldNamingConvention")
public class EncryptionAesTest {

    private static final Path rootDir = Zip4jvmSuite.generateSubDirNameWithTime(EncryptionAesTest.class);

    @BeforeClass
    public static void createDir() throws IOException {
        Files.createDirectories(rootDir);
    }

    @AfterClass(enabled = Zip4jvmSuite.clear)
    public static void removeDir() throws IOException {
        Zip4jvmSuite.removeDir(rootDir);
    }

    @Test
    public void shouldCreateNewZipWithFolderAndAesEncryption() throws IOException {
        ZipFileSettings settings = ZipFileSettings.builder()
                                                  .entrySettingsProvider(fileName ->
                                                          ZipEntrySettings.builder()
                                                                          .compression(Compression.STORE, CompressionLevel.NORMAL)
                                                                          .encryption(Encryption.AES_256, Zip4jvmSuite.password).build())
                                                  .comment("password: " + new String(Zip4jvmSuite.password)).build();
        Path zip = Zip4jvmSuite.subDirNameAsMethodName(rootDir).resolve("src.zip");
        ZipIt.add(zip, Zip4jvmSuite.contentSrcDir, settings);

        assertThatDirectory(zip.getParent()).exists().hasSubDirectories(0).hasFiles(1);
        assertThatZipFile(zip, Zip4jvmSuite.password).exists().rootEntry().matches(TestDataAssert.zipRootDirAssert);
    }

//    @Test
//    public void shouldCreateNewZipWithSelectedFilesAndAesEncryption() throws IOException {
//        ZipParameters parameters = ZipParameters.builder()
//                                                .compression(Compression.STORE)
//                                                .compressionLevel(CompressionLevel.NORMAL)
//                                                .encryption(Encryption.AES)
//                                                .strength(AesStrength.KEY_STRENGTH_256)
//                                                .comment("password: " + new String(Zip4jSuite.password))
//                                                .password(Zip4jSuite.password).build();
//
//        Path dstDir = Zip4jSuite.subDirNameAsMethodNameWithTme(rootDir);
//        Path zipFile = dstDir.resolve("src.zip");
//
//        ZipIt zip = ZipIt.builder().zipFile(zipFile).build();
//        zip.add(Zip4jSuite.filesCarsDir, parameters);
//
//        dstDir = dstDir.resolve("unzip");
//        UnzipIt unzip = UnzipIt.builder()
//                               .zipFile(zipFile)
//                               .password(Zip4jSuite.password).build();
//        unzip.extract(dstDir);
//
//        assertThatDirectory(zipFile.getParent()).exists().hasSubDirectories(0).hasFiles(1);
//        assertThatZipFile(zipFile, fileName -> Zip4jSuite.password).exists().directory("/").matches(TestUtils.zipCarsDirAssert);
//    }
//
//    public void shouldThrowExceptionWhenStandardEncryptionAndNullPassword() throws IOException {
//        ZipParameters parameters = ZipParameters.builder()
//                                                .compressionMethod(CompressionMethod.DEFLATE)
//                                                .compressionLevel(CompressionLevel.NORMAL)
//                                                .encryption(Encryption.PKWARE)
//                                                .password(null).build();
//
//        Path dstDir = Zip4jSuite.subDirNameAsMethodName(rootDir);
//        Path zipFile = dstDir.resolve("src.zip");
//        ZipIt zip = ZipIt.builder().zipFile(zipFile).build();
//
//        assertThatThrownBy(() -> zip.add(Zip4jSuite.srcDir, parameters)).isExactlyInstanceOf(Zip4jvmException.class);
//    }
//
//    public void shouldThrowExceptionWhenStandardEncryptionAndEmptyPassword() throws IOException {
//        ZipParameters parameters = ZipParameters.builder()
//                                                .compressionMethod(CompressionMethod.DEFLATE)
//                                                .compressionLevel(CompressionLevel.NORMAL)
//                                                .encryption(Encryption.PKWARE)
//                                                .password("".toCharArray()).build();
//
//        Path dstDir = Zip4jSuite.subDirNameAsMethodName(rootDir);
//        Path zipFile = dstDir.resolve("src.zip");
//        ZipIt zip = ZipIt.builder().zipFile(zipFile).build();
//
//        assertThatThrownBy(() -> zip.add(Zip4jSuite.srcDir, parameters)).isExactlyInstanceOf(Zip4jvmException.class);
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
//        Path dstDir = Zip4jSuite.subDirNameAsMethodName(rootDir);
//        Path zipFile = dstDir.resolve("src.zip");
//        ZipIt zip = ZipIt.builder().zipFile(zipFile).build();
//        zip.add(Zip4jSuite.srcDir, parameters);
//
//        dstDir = dstDir.resolve("unzip");
//        UnzipIt unzip = UnzipIt.builder()
//                               .zipFile(zipFile)
//                               .password(Zip4jSuite.password).build();
//        unzip.extract(dstDir);
//
//        assertThatDirectory(dstDir).matches(TestUtils.dirAssert);
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
//        Path dstDir = Zip4jSuite.subDirNameAsMethodName(rootDir);
//        Path zipFile = dstDir.resolve("src.zip");
//        ZipIt zip = ZipIt.builder().zipFile(zipFile).build();
//        zip.add(Zip4jSuite.srcDir, parameters);
//
//        Path dstDir1 = dstDir.resolve("unzip");
//        UnzipIt unzip = UnzipIt.builder()
//                               .zipFile(zipFile)
//                               .password(UUID.randomUUID().toString().toCharArray()).build();
//
//        assertThatThrownBy(() -> unzip.extract(dstDir1)).isExactlyInstanceOf(Zip4jvmException.class);
//    }

//    public void shouldUnzipWhenAesEncryption() throws IOException {
//        Path dstDir = Zip4jSuite.subDirNameAsMethodName(rootDir);
////        Path zipFile = dstDir.resolve("d:/zip4j/aes.zip");
//        Path zipFile = dstDir.resolve("d:/zip4j/tmp/aes.zip");
//        UnzipIt unzip = UnzipIt.builder()
//                               .zipFile(zipFile)
//                               .password(Zip4jSuite.password).build();
//        unzip.extract(dstDir);
//    }

}
