package ru.olegcherednik.zip4jvm.encryption;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.olegcherednik.zip4jvm.TestUtils;
import ru.olegcherednik.zip4jvm.Zip4jSuite;
import ru.olegcherednik.zip4jvm.ZipFile;
import ru.olegcherednik.zip4jvm.ZipIt;
import ru.olegcherednik.zip4jvm.exception.Zip4jEmptyPasswordException;
import ru.olegcherednik.zip4jvm.exception.Zip4jIncorrectPasswordException;
import ru.olegcherednik.zip4jvm.model.Compression;
import ru.olegcherednik.zip4jvm.model.CompressionLevel;
import ru.olegcherednik.zip4jvm.model.Encryption;
import ru.olegcherednik.zip4jvm.model.settings.ZipEntrySettings;
import ru.olegcherednik.zip4jvm.model.settings.ZipFileSettings;

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
        ZipFileSettings settings = ZipFileSettings.builder()
                                                  .defEntrySettings(
                                                                      ZipEntrySettings.builder()
                                                                                      .compression(Compression.DEFLATE, CompressionLevel.NORMAL)
                                                                                      .encryption(Encryption.PKWARE, fileName -> Zip4jSuite.password)
                                                                                      .build())
                                                  .comment("password: " + new String(Zip4jSuite.password)).build();

        Path zip = Zip4jSuite.subDirNameAsMethodName(rootDir).resolve("src.zip");
        ZipIt.add(zip, Zip4jSuite.contentSrcDir, settings);

        assertThatDirectory(zip.getParent()).exists().hasSubDirectories(0).hasFiles(1);
        assertThatZipFile(zip, Zip4jSuite.password).exists().rootEntry().matches(TestUtils.zipRootDirAssert);
    }

    public void shouldCreateNewZipWithSelectedFilesAndStandardEncryption() throws IOException {
        ZipFileSettings settings = ZipFileSettings.builder()
                                                  .defEntrySettings(
                                                                      ZipEntrySettings.builder()
                                                                                      .compression(Compression.DEFLATE, CompressionLevel.NORMAL)
                                                                                      .encryption(Encryption.PKWARE, fileName -> Zip4jSuite.password)
                                                                                      .build())
                                                  .comment("password: " + new String(Zip4jSuite.password)).build();
        Path zip = Zip4jSuite.subDirNameAsMethodName(rootDir).resolve("src.zip");
        ZipIt.add(zip, Zip4jSuite.filesCarsDir, settings);

        assertThatDirectory(zip.getParent()).exists().hasSubDirectories(0).hasFiles(1);
        assertThatZipFile(zip, Zip4jSuite.password).exists().rootEntry().hasSubDirectories(0).hasFiles(3);
        assertThatZipFile(zip, Zip4jSuite.password).directory("/").matches(TestUtils.zipCarsDirAssert);
    }

    public void shouldThrowExceptionWhenStandardEncryptionAndEmptyPassword() throws IOException {
        ZipFileSettings settings = ZipFileSettings.builder()
                                                  .defEntrySettings(
                                                                      ZipEntrySettings.builder()
                                                                                      .compression(Compression.DEFLATE, CompressionLevel.NORMAL)
                                                                                      .encryption(Encryption.PKWARE, fileName -> "".toCharArray())
                                                                                      .build())
                                                  .build();
        Path zip = Zip4jSuite.subDirNameAsMethodName(rootDir).resolve("src.zip");

        assertThatThrownBy(() -> ZipIt.add(zip, Zip4jSuite.srcDir, settings)).isInstanceOf(Zip4jEmptyPasswordException.class);
    }

    public void shouldUnzipWhenStandardEncryption() throws IOException {
        ZipFileSettings settings = ZipFileSettings.builder()
                                                  .defEntrySettings(
                                                                      ZipEntrySettings.builder()
                                                                                      .compression(Compression.DEFLATE, CompressionLevel.NORMAL)
                                                                                      .encryption(Encryption.PKWARE, fileName -> Zip4jSuite.password)
                                                                                      .build())
                                                  .comment("password: " + new String(Zip4jSuite.password)).build();

        Path zip = Zip4jSuite.subDirNameAsMethodName(rootDir).resolve("src.zip");
        ZipIt.add(zip, Zip4jSuite.contentSrcDir, settings);

        Path destDir = zip.getParent().resolve("unzip");
        ZipFile.Reader zipFile = ZipFile.read(zip, fileName -> Zip4jSuite.password);
        zipFile.extract(destDir);

        assertThatDirectory(destDir).matches(TestUtils.dirAssert);
    }

    public void shouldThrowExceptionWhenUnzipStandardEncryptedZipWithIncorrectPassword() throws IOException {
        ZipFileSettings settings = ZipFileSettings.builder()
                                                  .defEntrySettings(
                                                                      ZipEntrySettings.builder()
                                                                                      .compression(Compression.DEFLATE, CompressionLevel.NORMAL)
                                                                                      .encryption(Encryption.PKWARE, fileName -> Zip4jSuite.password)
                                                                                      .build())
                                                  .comment("password: " + new String(Zip4jSuite.password)).build();
        Path zip = Zip4jSuite.subDirNameAsMethodName(rootDir).resolve("src.zip");
        ZipIt.add(zip, Zip4jSuite.srcDir, settings);

        Path destDir = zip.getParent().resolve("unzip");
        ZipFile.Reader zipFile = ZipFile.read(zip, fileName -> UUID.randomUUID().toString().toCharArray());

        assertThatThrownBy(() -> zipFile.extract(destDir)).isExactlyInstanceOf(Zip4jIncorrectPasswordException.class);
    }

}
