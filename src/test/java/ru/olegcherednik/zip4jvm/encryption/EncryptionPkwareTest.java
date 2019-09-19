package ru.olegcherednik.zip4jvm.encryption;

import org.apache.commons.lang.ArrayUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.olegcherednik.zip4jvm.TestUtils;
import ru.olegcherednik.zip4jvm.UnzipIt;
import ru.olegcherednik.zip4jvm.Zip4jvmSuite;
import ru.olegcherednik.zip4jvm.ZipIt;
import ru.olegcherednik.zip4jvm.exception.Zip4jvmEmptyPasswordException;
import ru.olegcherednik.zip4jvm.exception.Zip4jvmIncorrectPasswordException;
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
        ZipIt.add(zip, Zip4jvmSuite.contentSrcDir, settings);

        assertThatDirectory(zip.getParent()).exists().hasSubDirectories(0).hasFiles(1);
        assertThatZipFile(zip, Zip4jvmSuite.password).exists().rootEntry().matches(TestUtils.zipRootDirAssert);
    }

    public void shouldCreateNewZipWithSelectedFilesAndStandardEncryption() throws IOException {
        ZipFileSettings settings = ZipFileSettings.builder()
                                                  .entrySettingsProvider(fileName ->
                                                          ZipEntrySettings.builder()
                                                                          .compression(Compression.DEFLATE, CompressionLevel.NORMAL)
                                                                          .encryption(Encryption.PKWARE, Zip4jvmSuite.password).build())
                                                  .comment("password: " + new String(Zip4jvmSuite.password)).build();
        Path zip = Zip4jvmSuite.subDirNameAsMethodName(rootDir).resolve("src.zip");
        ZipIt.add(zip, Zip4jvmSuite.filesCarsDir, settings);

        assertThatDirectory(zip.getParent()).exists().hasSubDirectories(0).hasFiles(1);
        assertThatZipFile(zip, Zip4jvmSuite.password).exists().rootEntry().hasSubDirectories(0).hasFiles(3);
        assertThatZipFile(zip, Zip4jvmSuite.password).directory("/").matches(TestUtils.zipCarsDirAssert);
    }

    public void shouldThrowExceptionWhenStandardEncryptionAndEmptyPassword() throws IOException {
        ZipFileSettings settings = ZipFileSettings.builder()
                                                  .entrySettingsProvider(fileName ->
                                                          ZipEntrySettings.builder()
                                                                          .compression(Compression.DEFLATE, CompressionLevel.NORMAL)
                                                                          .encryption(Encryption.PKWARE, ArrayUtils.EMPTY_CHAR_ARRAY).build())
                                                  .build();
        Path zip = Zip4jvmSuite.subDirNameAsMethodName(rootDir).resolve("src.zip");

        assertThatThrownBy(() -> ZipIt.add(zip, Zip4jvmSuite.srcDir, settings)).isInstanceOf(Zip4jvmEmptyPasswordException.class);
    }

    public void shouldUnzipWhenStandardEncryption() throws IOException {
        ZipFileSettings settings = ZipFileSettings.builder()
                                                  .entrySettingsProvider(fileName ->
                                                          ZipEntrySettings.builder()
                                                                          .compression(Compression.DEFLATE, CompressionLevel.NORMAL)
                                                                          .encryption(Encryption.PKWARE, Zip4jvmSuite.password).build())
                                                  .comment("password: " + new String(Zip4jvmSuite.password)).build();

        Path zip = Zip4jvmSuite.subDirNameAsMethodName(rootDir).resolve("src.zip");
        ZipIt.add(zip, Zip4jvmSuite.contentSrcDir, settings);

        Path destDir = zip.getParent().resolve("unzip");
        UnzipIt.extract(zip, destDir, fileName -> Zip4jvmSuite.password);
        assertThatDirectory(destDir).matches(TestUtils.dirAssert);
    }

    public void shouldThrowExceptionWhenUnzipStandardEncryptedZipWithIncorrectPassword() throws IOException {
        ZipFileSettings settings = ZipFileSettings.builder()
                                                  .entrySettingsProvider(fileName ->
                                                          ZipEntrySettings.builder()
                                                                          .compression(Compression.DEFLATE, CompressionLevel.NORMAL)
                                                                          .encryption(Encryption.PKWARE, Zip4jvmSuite.password).build())
                                                  .comment("password: " + new String(Zip4jvmSuite.password)).build();
        Path zip = Zip4jvmSuite.subDirNameAsMethodName(rootDir).resolve("src.zip");
        ZipIt.add(zip, Zip4jvmSuite.srcDir, settings);

        Path destDir = zip.getParent().resolve("unzip");
        assertThatThrownBy(() -> UnzipIt.extract(zip, destDir, fileName -> UUID.randomUUID().toString().toCharArray()))
                .isExactlyInstanceOf(Zip4jvmIncorrectPasswordException.class);
    }

}
