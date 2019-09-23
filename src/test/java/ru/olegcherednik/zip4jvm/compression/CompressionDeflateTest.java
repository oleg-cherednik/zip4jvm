package ru.olegcherednik.zip4jvm.compression;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.olegcherednik.zip4jvm.UnzipIt;
import ru.olegcherednik.zip4jvm.Zip4jvmSuite;
import ru.olegcherednik.zip4jvm.ZipIt;
import ru.olegcherednik.zip4jvm.model.Compression;
import ru.olegcherednik.zip4jvm.model.CompressionLevel;
import ru.olegcherednik.zip4jvm.model.settings.ZipEntrySettings;
import ru.olegcherednik.zip4jvm.model.settings.ZipFileSettings;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static ru.olegcherednik.zip4jvm.TestData.zipDeflateSolidAes;
import static ru.olegcherednik.zip4jvm.TestData.zipDeflateSolidPkware;
import static ru.olegcherednik.zip4jvm.TestData.zipDeflateSolid;
import static ru.olegcherednik.zip4jvm.TestData.dirBikes;
import static ru.olegcherednik.zip4jvm.TestData.dirCars;
import static ru.olegcherednik.zip4jvm.TestData.dirNameCars;
import static ru.olegcherednik.zip4jvm.TestData.filesDirBikes;
import static ru.olegcherednik.zip4jvm.TestData.filesDirCars;
import static ru.olegcherednik.zip4jvm.TestData.zipDirNameBikes;
import static ru.olegcherednik.zip4jvm.TestData.zipDirNameCars;
import static ru.olegcherednik.zip4jvm.TestDataAssert.dirSrcAssert;
import static ru.olegcherednik.zip4jvm.TestDataAssert.dirCarsAssert;
import static ru.olegcherednik.zip4jvm.TestDataAssert.zipDirBikesAssert;
import static ru.olegcherednik.zip4jvm.TestDataAssert.zipDirCarsAssert;
import static ru.olegcherednik.zip4jvm.Zip4jvmSuite.SIZE_1MB;
import static ru.olegcherednik.zip4jvm.Zip4jvmSuite.password;
import static ru.olegcherednik.zip4jvm.assertj.Zip4jvmAssertions.assertThatDirectory;
import static ru.olegcherednik.zip4jvm.assertj.Zip4jvmAssertions.assertThatZipFile;

/**
 * @author Oleg Cherednik
 * @since 06.08.2019
 */
@Test
@SuppressWarnings("FieldNamingConvention")
public class CompressionDeflateTest {

    private static final Path rootDir = Zip4jvmSuite.generateSubDirNameWithTime(CompressionDeflateTest.class);

    @BeforeClass
    public static void createDir() throws IOException {
        Files.createDirectories(rootDir);
    }

    @AfterClass(enabled = Zip4jvmSuite.clear)
    public static void removeDir() throws IOException {
        Zip4jvmSuite.removeDir(rootDir);
    }

    public void shouldCreateSingleZipWithFilesWhenDeflateCompression() throws IOException {
        ZipEntrySettings entrySettings = ZipEntrySettings.builder().compression(Compression.DEFLATE, CompressionLevel.NORMAL).build();
        ZipFileSettings settings = ZipFileSettings.builder().entrySettingsProvider(fileName -> entrySettings).build();

        Path zip = Zip4jvmSuite.subDirNameAsMethodName(rootDir).resolve("src.zip");

        ZipIt.add(zip, filesDirBikes, settings);
        assertThatDirectory(zip.getParent()).exists().hasSubDirectories(0).hasFiles(1);
        assertThatZipFile(zip).root().matches(zipDirBikesAssert);
    }

    public void shouldCreateSplitZipWithFilesWhenDeflateCompression() throws IOException {
        ZipEntrySettings entrySettings = ZipEntrySettings.builder().compression(Compression.DEFLATE, CompressionLevel.NORMAL).build();
        ZipFileSettings settings = ZipFileSettings.builder().entrySettingsProvider(fileName -> entrySettings).splitSize(SIZE_1MB).build();

        Path zip = Zip4jvmSuite.subDirNameAsMethodName(rootDir).resolve("src.zip");

        ZipIt.add(zip, filesDirCars, settings);
        assertThatDirectory(zip.getParent()).exists().hasSubDirectories(0).hasFiles(3);
        assertThatZipFile(zip).root().matches(zipDirCarsAssert);
    }

    public void shouldCreateSingleZipWithEntireFolderWhenDeflateCompression() throws IOException {
        ZipEntrySettings entrySettings = ZipEntrySettings.builder().compression(Compression.DEFLATE, CompressionLevel.NORMAL).build();
        ZipFileSettings settings = ZipFileSettings.builder().entrySettingsProvider(fileName -> entrySettings).build();

        Path zip = Zip4jvmSuite.subDirNameAsMethodName(rootDir).resolve("src.zip");
        ZipIt.add(zip, dirBikes, settings);

        assertThatDirectory(zip.getParent()).exists().hasSubDirectories(0).hasFiles(1);
        assertThatZipFile(zip).exists().root().hasDirectories(1).hasFiles(0);
        assertThatZipFile(zip).directory(zipDirNameBikes).matches(zipDirBikesAssert);
    }

    public void shouldCreateSplitZipWithEntireFolderWhenStoreCompression() throws IOException {
        ZipEntrySettings entrySettings = ZipEntrySettings.builder().compression(Compression.STORE, CompressionLevel.NORMAL).build();
        ZipFileSettings settings = ZipFileSettings.builder().entrySettingsProvider(fileName -> entrySettings).splitSize(SIZE_1MB).build();

        Path zip = Zip4jvmSuite.subDirNameAsMethodName(rootDir).resolve("src.zip");

        ZipIt.add(zip, dirCars, settings);
        assertThatDirectory(zip.getParent()).exists().hasSubDirectories(0).hasFiles(3);
        assertThatZipFile(zip).root().hasDirectories(1).hasFiles(0);
        assertThatZipFile(zip).directory(zipDirNameCars).matches(zipDirCarsAssert);
    }

    public void shouldUnzipWhenDeflateCompression() throws IOException {
        Path destDir = Zip4jvmSuite.subDirNameAsMethodName(rootDir);
        UnzipIt.extract(zipDeflateSolid, destDir);
        assertThatDirectory(destDir).matches(dirSrcAssert);
    }

    public void shouldUnzipWhenWhenDeflateCompressionAndPkwareEncryption() throws IOException {
        Path destDir = Zip4jvmSuite.subDirNameAsMethodName(rootDir);

        UnzipIt.extract(zipDeflateSolidPkware, destDir, dirNameCars, fileName -> password);
        assertThatDirectory(destDir).exists().hasSubDirectories(1).hasFiles(0);
        assertThatDirectory(destDir.resolve(dirNameCars)).matches(dirCarsAssert);
    }

    public void shouldUnzipWhenWhenDeflateCompressionAndAesEncryption() throws IOException {
        Path destDir = Zip4jvmSuite.subDirNameAsMethodName(rootDir);

        UnzipIt.extract(zipDeflateSolidAes, destDir, dirNameCars, String::toCharArray);
        assertThatDirectory(destDir).exists().hasSubDirectories(1).hasFiles(0);
        assertThatDirectory(destDir.resolve(dirNameCars)).matches(dirCarsAssert);
    }

}
