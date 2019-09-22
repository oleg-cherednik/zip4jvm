package ru.olegcherednik.zip4jvm.encryption;

import org.apache.commons.lang.ArrayUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
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
import static ru.olegcherednik.zip4jvm.TestData.filesDirCars;
import static ru.olegcherednik.zip4jvm.TestData.storeSolidAesZip;
import static ru.olegcherednik.zip4jvm.TestData.storeSplitAesZip;
import static ru.olegcherednik.zip4jvm.TestDataAssert.dirSrcAssert;
import static ru.olegcherednik.zip4jvm.TestDataAssert.zipDirCarsAssert;
import static ru.olegcherednik.zip4jvm.TestDataAssert.zipDirRootAssert;
import static ru.olegcherednik.zip4jvm.Zip4jvmSuite.password;
import static ru.olegcherednik.zip4jvm.Zip4jvmSuite.passwordStr;
import static ru.olegcherednik.zip4jvm.assertj.Zip4jvmAssertions.assertThatDirectory;
import static ru.olegcherednik.zip4jvm.assertj.Zip4jvmAssertions.assertThatZipFile;

/**
 * @author Oleg Cherednik
 * @since 29.07.2019
 */
@Test
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

    public void shouldCreateNewZipWithFolderAndAes256Encryption() throws IOException {
        ZipEntrySettings entrySettings = ZipEntrySettings.builder()
                                                         .compression(Compression.STORE, CompressionLevel.NORMAL)
                                                         .encryption(Encryption.AES_256, password).build();
        ZipFileSettings settings = ZipFileSettings.builder()
                                                  .entrySettingsProvider(fileName -> entrySettings)
                                                  .comment("password: " + passwordStr).build();

        Path zip = Zip4jvmSuite.subDirNameAsMethodName(rootDir).resolve("src.zip");

        ZipIt.add(zip, contentDirSrc, settings);
        assertThatDirectory(zip.getParent()).exists().hasSubDirectories(0).hasFiles(1);
        assertThatZipFile(zip, password).exists().root().matches(zipDirRootAssert);
    }

    public void shouldCreateNewZipWithFolderAndAes192Encryption() throws IOException {
        ZipEntrySettings entrySettings = ZipEntrySettings.builder()
                                                         .compression(Compression.STORE, CompressionLevel.NORMAL)
                                                         .encryption(Encryption.AES_192, password).build();
        ZipFileSettings settings = ZipFileSettings.builder()
                                                  .entrySettingsProvider(fileName -> entrySettings)
                                                  .comment("password: " + passwordStr).build();

        Path zip = Zip4jvmSuite.subDirNameAsMethodName(rootDir).resolve("src.zip");

        ZipIt.add(zip, contentDirSrc, settings);
        assertThatDirectory(zip.getParent()).exists().hasSubDirectories(0).hasFiles(1);
        assertThatZipFile(zip, password).exists().root().matches(zipDirRootAssert);
    }

    public void shouldCreateNewZipWithFolderAndAes128Encryption() throws IOException {
        ZipEntrySettings entrySettings = ZipEntrySettings.builder()
                                                         .compression(Compression.STORE, CompressionLevel.NORMAL)
                                                         .encryption(Encryption.AES_128, password).build();
        ZipFileSettings settings = ZipFileSettings.builder()
                                                  .entrySettingsProvider(fileName -> entrySettings)
                                                  .comment("password: " + passwordStr).build();

        Path zip = Zip4jvmSuite.subDirNameAsMethodName(rootDir).resolve("src.zip");

        ZipIt.add(zip, contentDirSrc, settings);
        assertThatDirectory(zip.getParent()).exists().hasSubDirectories(0).hasFiles(1);
        assertThatZipFile(zip, password).exists().root().matches(zipDirRootAssert);
    }

    public void shouldCreateNewZipWithSelectedFilesAndAesEncryption() throws IOException {
        ZipEntrySettings entrySettings = ZipEntrySettings.builder()
                                                         .compression(Compression.STORE, CompressionLevel.NORMAL)
                                                         .encryption(Encryption.AES_256, password).build();
        ZipFileSettings settings = ZipFileSettings.builder()
                                                  .entrySettingsProvider(fileName -> entrySettings)
                                                  .comment("password: " + passwordStr).build();

        Path zip = Zip4jvmSuite.subDirNameAsMethodName(rootDir).resolve("src.zip");

        ZipIt.add(zip, filesDirCars, settings);
        assertThatDirectory(zip.getParent()).exists().hasSubDirectories(0).hasFiles(1);
        assertThatZipFile(zip, password).exists().root().matches(zipDirCarsAssert);
    }

    public void shouldThrowExceptionWhenAesEncryptionAndNullOrEmptyPassword() throws IOException {
        assertThatThrownBy(() -> ZipEntrySettings.builder()
                                                 .compression(Compression.STORE, CompressionLevel.NORMAL)
                                                 .encryption(Encryption.AES_256, null).build())
                .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> ZipEntrySettings.builder()
                                                 .compression(Compression.STORE, CompressionLevel.NORMAL)
                                                 .encryption(Encryption.AES_256, ArrayUtils.EMPTY_CHAR_ARRAY).build())
                .isExactlyInstanceOf(EmptyPasswordException.class);
    }

    public void shouldUnzipWhenStoreSolidAes() throws IOException {
        Path destDir = Zip4jvmSuite.subDirNameAsMethodName(rootDir);

        UnzipIt.extract(storeSolidAesZip, destDir, String::toCharArray);
        assertThatDirectory(destDir).matches(dirSrcAssert);
    }

    public void shouldUnzipWhenStoreSplitAes() throws IOException {
        Path destDir = Zip4jvmSuite.subDirNameAsMethodName(rootDir);

        UnzipIt.extract(storeSplitAesZip, destDir, String::toCharArray);
        assertThatDirectory(destDir).matches(dirSrcAssert);
    }

    public void shouldThrowExceptionWhenUnzipAesEncryptedZipWithIncorrectPassword() throws IOException {
        Path destDir = Zip4jvmSuite.subDirNameAsMethodName(rootDir);

        assertThatThrownBy(() -> UnzipIt.extract(storeSplitAesZip, destDir, fileName -> UUID.randomUUID().toString().toCharArray()))
                .isExactlyInstanceOf(IncorrectPasswordException.class);
    }
}
