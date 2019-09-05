package ru.olegcherednik.zip4jvm.encryption;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;
import ru.olegcherednik.zip4jvm.TestUtils;
import ru.olegcherednik.zip4jvm.UnzipIt;
import ru.olegcherednik.zip4jvm.Zip4jSuite;
import ru.olegcherednik.zip4jvm.ZipIt;
import ru.olegcherednik.zip4jvm.exception.Zip4jEmptyPasswordException;
import ru.olegcherednik.zip4jvm.exception.Zip4jIncorrectPasswordException;
import ru.olegcherednik.zip4jvm.model.Compression;
import ru.olegcherednik.zip4jvm.model.CompressionLevel;
import ru.olegcherednik.zip4jvm.model.Encryption;
import ru.olegcherednik.zip4jvm.model.ZipParameters;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static ru.olegcherednik.zip4jvm.assertj.Zip4jAssertions.assertThatDirectory;
import static ru.olegcherednik.zip4jvm.assertj.Zip4jAssertions.assertThatZipFile;

/**
 * @author Oleg Cherednik
 * @since 28.07.2019
 */
@Test
@SuppressWarnings("FieldNamingConvention")
public class EncryptionPkwareTest {

    private static final Path rootDir = Zip4jSuite.generateSubDirNameWithTime(EncryptionPkwareTest.class);

    @BeforeClass
    public static void createDir() throws IOException {
        Files.createDirectories(rootDir);
    }

    @AfterClass(enabled = Zip4jSuite.clear)
    public static void removeDir() throws IOException {
        Zip4jSuite.removeDir(rootDir);
    }

    public void shouldCreateNewZipWithFolderAndStandardEncryption() throws IOException {
        ZipParameters parameters = ZipParameters.builder()
                                                .compression(Compression.DEFLATE, CompressionLevel.NORMAL)
                                                .encryption(Encryption.PKWARE, Zip4jSuite.password)
                                                .comment("password: " + new String(Zip4jSuite.password)).build();

        Path dstDir = Zip4jSuite.subDirNameAsMethodName(rootDir);
        Path zipFile = dstDir.resolve("src.zip");
        ZipIt zip = ZipIt.builder().zipFile(zipFile).build();
        zip.add(Zip4jSuite.srcDir, parameters);

        assertThatDirectory(dstDir).exists().hasSubDirectories(0).hasFiles(1);
        assertThatZipFile(zipFile, Zip4jSuite.password).exists().rootEntry().matches(TestUtils.zipRootDirAssert);
    }

    public void shouldCreateNewZipWithSelectedFilesAndStandardEncryption() throws IOException {
        ZipParameters parameters = ZipParameters.builder()
                                                .compression(Compression.DEFLATE, CompressionLevel.NORMAL)
                                                .encryption(Encryption.PKWARE, Zip4jSuite.password)
                                                .comment("password: " + new String(Zip4jSuite.password)).build();

        Path dstDir = Zip4jSuite.subDirNameAsMethodName(rootDir);
        Path zipFile = dstDir.resolve("src.zip");

        ZipIt zip = ZipIt.builder().zipFile(zipFile).build();
        zip.add(Zip4jSuite.filesCarsDir, parameters);

        assertThatDirectory(zipFile.getParent()).exists().hasSubDirectories(0).hasFiles(1);
        assertThatZipFile(zipFile, Zip4jSuite.password).exists().rootEntry().hasSubDirectories(0).hasFiles(3);
        assertThatZipFile(zipFile, Zip4jSuite.password).directory("/").matches(TestUtils.zipCarsDirAssert);
    }

    public void shouldThrowExceptionWhenStandardEncryptionAndEmptyPassword() throws IOException {
        ZipParameters parameters = ZipParameters.builder()
                                                .compression(Compression.DEFLATE, CompressionLevel.NORMAL)
                                                .encryption(Encryption.PKWARE, "".toCharArray()).build();

        Path dstDir = Zip4jSuite.subDirNameAsMethodName(rootDir);
        Path zipFile = dstDir.resolve("src.zip");
        ZipIt zip = ZipIt.builder().zipFile(zipFile).build();

        assertThatThrownBy(() -> zip.add(Zip4jSuite.srcDir, parameters)).isInstanceOf(Zip4jEmptyPasswordException.class);
    }

    @Ignore
    public void shouldUnzipWhenStandardEncryption() throws IOException {
        ZipParameters parameters = ZipParameters.builder()
                                                .compression(Compression.DEFLATE, CompressionLevel.NORMAL)
                                                .encryption(Encryption.PKWARE, Zip4jSuite.password)
                                                .comment("password: " + new String(Zip4jSuite.password)).build();

        Path dstDir = Zip4jSuite.subDirNameAsMethodName(rootDir);
        Path zipFile = dstDir.resolve("src.zip");
        ZipIt zip = ZipIt.builder().zipFile(zipFile).build();
        zip.add(Zip4jSuite.srcDir, parameters);

        dstDir = dstDir.resolve("unzip");
        UnzipIt unzip = UnzipIt.builder()
                               .zipFile(zipFile)
                               .password(Zip4jSuite.password).build();
        unzip.extract(dstDir);

        assertThatDirectory(dstDir).matches(TestUtils.dirAssert);
    }

    public void shouldThrowExceptionWhenUnzipStandardEncryptedZipWithIncorrectPassword() throws IOException {
        ZipParameters parameters = ZipParameters.builder()
                                                .compression(Compression.DEFLATE, CompressionLevel.NORMAL)
                                                .encryption(Encryption.PKWARE, Zip4jSuite.password)
                                                .comment("password: " + new String(Zip4jSuite.password)).build();

        Path dstDir = Zip4jSuite.subDirNameAsMethodName(rootDir);
        Path zipFile = dstDir.resolve("src.zip");
        ZipIt zip = ZipIt.builder().zipFile(zipFile).build();
        zip.add(Zip4jSuite.srcDir, parameters);

        Path dstDir1 = dstDir.resolve("unzip");
        UnzipIt unzip = UnzipIt.builder()
                               .zipFile(zipFile)
                               .password(UUID.randomUUID().toString().toCharArray()).build();

        assertThatThrownBy(() -> unzip.extract(dstDir1)).isExactlyInstanceOf(Zip4jIncorrectPasswordException.class);
    }

}
