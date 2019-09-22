package ru.olegcherednik.zip4jvm;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.olegcherednik.zip4jvm.exception.PathNotExistsException;
import ru.olegcherednik.zip4jvm.model.Compression;
import ru.olegcherednik.zip4jvm.model.CompressionLevel;
import ru.olegcherednik.zip4jvm.model.settings.ZipEntrySettings;
import ru.olegcherednik.zip4jvm.model.settings.ZipFileSettings;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static ru.olegcherednik.zip4jvm.TestData.deflateSolidPkwareZip;
import static ru.olegcherednik.zip4jvm.TestData.deflateSolidZip;
import static ru.olegcherednik.zip4jvm.TestData.deflateSplitZip;
import static ru.olegcherednik.zip4jvm.TestData.dirCars;
import static ru.olegcherednik.zip4jvm.TestData.dirSrc;
import static ru.olegcherednik.zip4jvm.TestData.fileBentley;
import static ru.olegcherednik.zip4jvm.TestData.fileFerrari;
import static ru.olegcherednik.zip4jvm.TestData.fileWiesmann;
import static ru.olegcherednik.zip4jvm.TestData.filesDirCars;
import static ru.olegcherednik.zip4jvm.TestData.storeSolidZip;
import static ru.olegcherednik.zip4jvm.TestData.storeSplitZip;
import static ru.olegcherednik.zip4jvm.assertj.Zip4jvmAssertions.assertThatDirectory;
import static ru.olegcherednik.zip4jvm.assertj.Zip4jvmAssertions.assertThatZipFile;

/**
 * @author Oleg Cherednik
 * @since 15.03.2019
 */
@Test
@SuppressWarnings("FieldNamingConvention")
public class ZipMiscTest {

    private static final Path rootDir = Zip4jvmSuite.generateSubDirNameWithTime(ZipMiscTest.class);

    @BeforeClass
    public static void createDir() throws IOException {
        Files.createDirectories(rootDir);
    }

    @AfterClass(enabled = Zip4jvmSuite.clear)
    public static void removeDir() throws IOException {
        Zip4jvmSuite.removeDir(rootDir);
    }

    public void shouldRetrieveAllEntryNamesForExistedZip() throws IOException {
        assertThat(ZipMisc.getEntryNames(deflateSolidZip)).hasSize(13);
    }

    public void shouldRetrieveAllEntryNamesForExistedEncryptedZip() throws IOException {
        Path zip = Zip4jvmSuite.copy(rootDir, deflateSolidPkwareZip);
        assertThat(ZipMisc.getEntryNames(zip)).hasSize(13);
    }

    public void shouldThrowExceptionWhenAddedFileNotExists() throws IOException {
        ZipFileSettings settings = ZipFileSettings.builder()
                                                  .entrySettingsProvider(fileName ->
                                                          ZipEntrySettings.builder()
                                                                          .compression(Compression.STORE, CompressionLevel.NORMAL)
                                                                          .build())
                                                  .build();

        Path notExisted = dirCars.resolve(UUID.randomUUID().toString());
        List<Path> files = Arrays.asList(fileBentley, fileFerrari, fileWiesmann, notExisted);

        Path zip = Zip4jvmSuite.subDirNameAsMethodName(rootDir).resolve("src.zip");

        assertThatThrownBy(() -> ZipIt.add(zip, files, settings)).isExactlyInstanceOf(PathNotExistsException.class);
    }

    public void shouldMergeSplitZip() throws IOException {
        assertThat(ZipMisc.isSplit(deflateSplitZip)).isTrue();

        Path zip = Zip4jvmSuite.subDirNameAsMethodName(rootDir).resolve("src.zip");
        ZipMisc.merge(deflateSplitZip, zip);

        assertThatDirectory(zip.getParent()).exists().hasSubDirectories(0).hasFiles(1);
        assertThatZipFile(zip).exists().root().matches(TestDataAssert.zipDirRootAssert);
    }

    public void shouldRetrieveTrueWhenSplitZipWithMultipleDisks() throws IOException {
        assertThat(ZipMisc.isSplit(storeSplitZip)).isTrue();
    }

    public void shouldRetrieveTrueWhenSplitZipWithOneDisk() throws IOException {
        ZipFileSettings settings = ZipFileSettings.builder()
                                                  .splitSize(1024 * 1024)
                                                  .entrySettingsProvider(fileName ->
                                                          ZipEntrySettings.builder()
                                                                          .compression(Compression.STORE, CompressionLevel.NORMAL)
                                                                          .build())
                                                  .build();
        Path zip = Zip4jvmSuite.subDirNameAsMethodName(rootDir).resolve("src.zip");
        ZipIt.add(zip, Collections.singleton(dirSrc.resolve("Oleg Cherednik.txt")), settings);

        assertThat(ZipMisc.isSplit(storeSplitZip)).isTrue();
    }

    public void shouldRemoveGivenFilesFromExistedZip() throws IOException {
        Path zip = Zip4jvmSuite.subDirNameAsMethodName(rootDir).resolve("src.zip");
        Files.createDirectories(zip.getParent());
        Files.copy(storeSolidZip, zip);
        assertThatZipFile(zip).exists().root().matches(TestDataAssert.zipDirRootAssert);

        List<String> entryNames = filesDirCars.stream()
                                              .map(file -> dirSrc.relativize(file).toString())
                                              .collect(Collectors.toList());

        ZipMisc.removeEntry(zip, entryNames);
        assertThat(ZipMisc.getEntryNames(zip)).hasSize(10);
    }

    public void shouldRemoveFolderFromExistedZip() throws IOException {
        Path zip = Zip4jvmSuite.subDirNameAsMethodName(rootDir).resolve("src.zip");
        Files.createDirectories(zip.getParent());
        Files.copy(storeSolidZip, zip);
        assertThatZipFile(zip).exists().root().matches(TestDataAssert.zipDirRootAssert);

        ZipMisc.removeEntry(zip, dirSrc.relativize(dirCars).toString());
        assertThat(ZipMisc.getEntryNames(zip)).hasSize(10);
    }
}
