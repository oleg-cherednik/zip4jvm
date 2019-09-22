package ru.olegcherednik.zip4jvm.encryption;

import org.apache.commons.lang.ArrayUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.olegcherednik.zip4jvm.TestDataAssert;
import ru.olegcherednik.zip4jvm.UnzipIt;
import ru.olegcherednik.zip4jvm.Zip4jvmSuite;
import ru.olegcherednik.zip4jvm.ZipIt;
import ru.olegcherednik.zip4jvm.exception.EmptyPasswordException;
import ru.olegcherednik.zip4jvm.exception.IncorrectPasswordException;
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
import static ru.olegcherednik.zip4jvm.TestData.contentDirSrc;
import static ru.olegcherednik.zip4jvm.TestData.dirSrc;
import static ru.olegcherednik.zip4jvm.TestData.filesDirCars;
import static ru.olegcherednik.zip4jvm.assertj.Zip4jvmAssertions.assertThatDirectory;
import static ru.olegcherednik.zip4jvm.assertj.Zip4jvmAssertions.assertThatZipFile;

/**
 * @author Oleg Cherednik
 * @since 28.07.2019
 */
@Test
@SuppressWarnings("FieldNamingConvention")
public class EncryptionPkwareTest {

    private static final Path rootDir = Zip4jvmSuite.generateSubDirNameWithTime(EncryptionPkwareTest.class);

    @BeforeClass
    public static void createDir() throws IOException {
        Files.createDirectories(rootDir);
    }

    @AfterClass(enabled = Zip4jvmSuite.clear)
    public static void removeDir() throws IOException {
        Zip4jvmSuite.removeDir(rootDir);
    }

    public void shouldCreateNewZipWithFolderAndStandardEncryption() throws IOException {
        ZipFileSettings settings = ZipFileSettings.builder()
                                                  .entrySettingsProvider(fileName ->
                                                          ZipEntrySettings.builder()
                                                                          .compression(Compression.DEFLATE, CompressionLevel.NORMAL)
                                                                          .encryption(Encryption.PKWARE, Zip4jvmSuite.password).build())
                                                  .comment("password: " + new String(Zip4jvmSuite.password)).build();

        Path zip = Zip4jvmSuite.subDirNameAsMethodName(rootDir).resolve("src.zip");
        ZipIt.add(zip, contentDirSrc, settings);

        assertThatDirectory(zip.getParent()).exists().hasSubDirectories(0).hasFiles(1);
        assertThatZipFile(zip, Zip4jvmSuite.password).exists().root().matches(TestDataAssert.zipRootDirAssert);
    }

    public void shouldCreateNewZipWithSelectedFilesAndStandardEncryption() throws IOException {
        ZipFileSettings settings = ZipFileSettings.builder()
                                                  .entrySettingsProvider(fileName ->
                                                          ZipEntrySettings.builder()
                                                                          .compression(Compression.DEFLATE, CompressionLevel.NORMAL)
                                                                          .encryption(Encryption.PKWARE, Zip4jvmSuite.password).build())
                                                  .comment("password: " + new String(Zip4jvmSuite.password)).build();
        Path zip = Zip4jvmSuite.subDirNameAsMethodName(rootDir).resolve("src.zip");
        ZipIt.add(zip, filesDirCars, settings);

        assertThatDirectory(zip.getParent()).exists().hasSubDirectories(0).hasFiles(1);
        assertThatZipFile(zip, Zip4jvmSuite.password).exists().root().hasSubDirectories(0).hasFiles(3);
        assertThatZipFile(zip, Zip4jvmSuite.password).root().matches(TestDataAssert.zipCarsDirAssert);
    }

    public void shouldThrowExceptionWhenStandardEncryptionAndEmptyPassword() throws IOException {
        ZipFileSettings settings = ZipFileSettings.builder()
                                                  .entrySettingsProvider(fileName ->
                                                          ZipEntrySettings.builder()
                                                                          .compression(Compression.DEFLATE, CompressionLevel.NORMAL)
                                                                          .encryption(Encryption.PKWARE, ArrayUtils.EMPTY_CHAR_ARRAY).build())
                                                  .build();
        Path zip = Zip4jvmSuite.subDirNameAsMethodName(rootDir).resolve("src.zip");

        assertThatThrownBy(() -> ZipIt.add(zip, dirSrc, settings)).isInstanceOf(EmptyPasswordException.class);
    }

    public void shouldUnzipWhenStandardEncryption() throws IOException {
        ZipFileSettings settings = ZipFileSettings.builder()
                                                  .entrySettingsProvider(fileName ->
                                                          ZipEntrySettings.builder()
                                                                          .compression(Compression.DEFLATE, CompressionLevel.NORMAL)
                                                                          .encryption(Encryption.PKWARE, Zip4jvmSuite.password).build())
                                                  .comment("password: " + new String(Zip4jvmSuite.password)).build();

        Path zip = Zip4jvmSuite.subDirNameAsMethodName(rootDir).resolve("src.zip");
        ZipIt.add(zip, contentDirSrc, settings);

        Path destDir = zip.getParent().resolve("unzip");
        UnzipIt.extract(zip, destDir, fileName -> Zip4jvmSuite.password);
        assertThatDirectory(destDir).matches(TestDataAssert.dirAssert);
    }

    public void shouldThrowExceptionWhenUnzipStandardEncryptedZipWithIncorrectPassword() throws IOException {
        ZipFileSettings settings = ZipFileSettings.builder()
                                                  .entrySettingsProvider(fileName ->
                                                          ZipEntrySettings.builder()
                                                                          .compression(Compression.DEFLATE, CompressionLevel.NORMAL)
                                                                          .encryption(Encryption.PKWARE, Zip4jvmSuite.password).build())
                                                  .comment("password: " + new String(Zip4jvmSuite.password)).build();
        Path zip = Zip4jvmSuite.subDirNameAsMethodName(rootDir).resolve("src.zip");
        ZipIt.add(zip, dirSrc, settings);

        Path destDir = zip.getParent().resolve("unzip");
        assertThatThrownBy(() -> UnzipIt.extract(zip, destDir, fileName -> UUID.randomUUID().toString().toCharArray()))
                .isExactlyInstanceOf(IncorrectPasswordException.class);
    }

}
