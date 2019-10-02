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
import java.nio.file.Paths;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static ru.olegcherednik.zip4jvm.TestData.contentDirSrc;
import static ru.olegcherednik.zip4jvm.TestData.filesDirCars;
import static ru.olegcherednik.zip4jvm.TestData.zipStoreSolidPkware;
import static ru.olegcherednik.zip4jvm.TestData.zipStoreSplitPkware;
import static ru.olegcherednik.zip4jvm.TestDataAssert.dirSrcAssert;
import static ru.olegcherednik.zip4jvm.TestDataAssert.zipDirCarsAssert;
import static ru.olegcherednik.zip4jvm.TestDataAssert.zipDirRootAssert;
import static ru.olegcherednik.zip4jvm.Zip4jvmSuite.password;
import static ru.olegcherednik.zip4jvm.Zip4jvmSuite.passwordStr;
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

    public void shouldCreateNewZipWithFolderAndPkwareEncryption() throws IOException {
        ZipEntrySettings entrySettings = ZipEntrySettings.builder()
                                                         .compression(Compression.DEFLATE, CompressionLevel.NORMAL)
                                                         .encryption(Encryption.PKWARE, password).build();
        ZipFileSettings settings = ZipFileSettings.builder()
                                                  .entrySettingsProvider(fileName -> entrySettings)
                                                  .comment("password: " + passwordStr).build();

        Path zip = Zip4jvmSuite.subDirNameAsMethodName(rootDir).resolve("src.zip");

        ZipIt.zip(zip).settings(settings).add(contentDirSrc);
        assertThatDirectory(zip.getParent()).exists().hasDirectories(0).hasFiles(1);
        assertThatZipFile(zip, password).exists().root().matches(zipDirRootAssert);
    }

    public void shouldCreateNewZipWithSelectedFilesAndPkwareEncryption() throws IOException {
        ZipEntrySettings entrySettings = ZipEntrySettings.builder()
                                                         .compression(Compression.DEFLATE, CompressionLevel.NORMAL)
                                                         .encryption(Encryption.PKWARE, password).build();
        ZipFileSettings settings = ZipFileSettings.builder()
                                                  .entrySettingsProvider(fileName -> entrySettings)
                                                  .comment("password: " + passwordStr).build();

        Path zip = Zip4jvmSuite.subDirNameAsMethodName(rootDir).resolve("src.zip");

        ZipIt.zip(zip).settings(settings).add(filesDirCars);
        assertThatDirectory(zip.getParent()).exists().hasDirectories(0).hasFiles(1);
        assertThatZipFile(zip, password).exists().root().hasDirectories(0).hasFiles(3);
        assertThatZipFile(zip, password).root().matches(zipDirCarsAssert);
    }

    public void shouldThrowExceptionWhenPkwareEncryptionAndEmptyPassword() throws IOException {
        assertThatThrownBy(() -> ZipEntrySettings.builder()
                                                 .compression(Compression.STORE, CompressionLevel.NORMAL)
                                                 .encryption(Encryption.PKWARE, null).build())
                .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> ZipEntrySettings.builder()
                                                 .compression(Compression.STORE, CompressionLevel.NORMAL)
                                                 .encryption(Encryption.PKWARE, ArrayUtils.EMPTY_CHAR_ARRAY).build())
                .isExactlyInstanceOf(EmptyPasswordException.class);
    }

    public void shouldUnzipWhenStoreSolidPkware() throws IOException {
        Path destDir = Zip4jvmSuite.subDirNameAsMethodName(rootDir);

        UnzipIt.extract(zipStoreSolidPkware, destDir, fileName -> password);
        assertThatDirectory(destDir).matches(dirSrcAssert);
    }

    public void shouldUnzipWhenStoreSplitPkware() throws IOException {
        Path destDir = Zip4jvmSuite.subDirNameAsMethodName(rootDir);

        UnzipIt.extract(zipStoreSplitPkware, destDir, fileName -> password);
        assertThatDirectory(destDir).matches(dirSrcAssert);
    }

    public void shouldThrowExceptionWhenUnzipPkwareEncryptedZipWithIncorrectPassword() throws IOException {
        Path destDir = Zip4jvmSuite.subDirNameAsMethodName(rootDir);

        assertThatThrownBy(() -> UnzipIt.extract(zipStoreSplitPkware, destDir, fileName -> UUID.randomUUID().toString().toCharArray()))
                .isExactlyInstanceOf(IncorrectPasswordException.class);
    }

    public void shouldUnzipWhenZip64ContainsOnlyOneCrcByteMatch() throws IOException {
        Path destDir = Zip4jvmSuite.subDirNameAsMethodNameWithTme(rootDir);
        Path zip = Paths.get("src/test/resources/zip/zip64_crc1byte_check.zip").toAbsolutePath();

        UnzipIt.extract(zip, destDir, fileName -> "Shu1an@2019GTS".toCharArray());
        assertThatDirectory(destDir).exists().hasDirectories(0).hasFiles(1);
        assertThatDirectory(destDir).file("hello.txt").exists().hasSize(11).hasContent("hello,itsme");
    }

}
