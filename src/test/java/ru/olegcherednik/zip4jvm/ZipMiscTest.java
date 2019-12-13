package ru.olegcherednik.zip4jvm;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.olegcherednik.zip4jvm.exception.EntryDuplicationException;
import ru.olegcherednik.zip4jvm.exception.EntryNotFoundException;
import ru.olegcherednik.zip4jvm.exception.PathNotExistsException;
import ru.olegcherednik.zip4jvm.model.Compression;
import ru.olegcherednik.zip4jvm.model.CompressionLevel;
import ru.olegcherednik.zip4jvm.model.settings.ZipEntrySettings;
import ru.olegcherednik.zip4jvm.model.settings.ZipSettings;

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
import static ru.olegcherednik.zip4jvm.TestData.dirCars;
import static ru.olegcherednik.zip4jvm.TestData.dirNameCars;
import static ru.olegcherednik.zip4jvm.TestData.dirSrc;
import static ru.olegcherednik.zip4jvm.TestData.fileBentley;
import static ru.olegcherednik.zip4jvm.TestData.fileFerrari;
import static ru.olegcherednik.zip4jvm.TestData.fileNameFerrari;
import static ru.olegcherednik.zip4jvm.TestData.fileWiesmann;
import static ru.olegcherednik.zip4jvm.TestData.filesDirCars;
import static ru.olegcherednik.zip4jvm.TestData.zipDeflateSolid;
import static ru.olegcherednik.zip4jvm.TestData.zipDeflateSolidPkware;
import static ru.olegcherednik.zip4jvm.TestData.zipDeflateSplit;
import static ru.olegcherednik.zip4jvm.TestData.zipStoreSolid;
import static ru.olegcherednik.zip4jvm.TestData.zipStoreSplit;
import static ru.olegcherednik.zip4jvm.TestDataAssert.rootAssert;
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
    private static final Path zipMerge = rootDir.resolve("merge/src.zip");

    @BeforeClass
    public static void createDir() throws IOException {
        Files.createDirectories(rootDir);
    }

    @AfterClass(enabled = Zip4jvmSuite.clear)
    public static void removeDir() throws IOException {
        Zip4jvmSuite.removeDir(rootDir);
    }

    public void shouldRetrieveAllEntryNamesForExistedZip() throws IOException {
        assertThat(ZipMisc.zip(zipDeflateSolid).getEntries()).hasSize(13);
    }

    public void shouldRetrieveAllEntryNamesForExistedEncryptedZip() throws IOException {
        Path zip = Zip4jvmSuite.copy(rootDir, zipDeflateSolidPkware);
        assertThat(ZipMisc.zip(zip).getEntries()).hasSize(13);
    }

    public void shouldThrowExceptionWhenAddedFileNotExists() throws IOException {
        ZipSettings settings = ZipSettings.builder()
                                          .entrySettingsProvider(fileName ->
                                                  ZipEntrySettings.builder()
                                                                  .compression(Compression.STORE, CompressionLevel.NORMAL)
                                                                  .build())
                                          .build();

        Path notExisted = dirCars.resolve(UUID.randomUUID().toString());
        List<Path> files = Arrays.asList(fileBentley, fileFerrari, fileWiesmann, notExisted);

        Path zip = Zip4jvmSuite.subDirNameAsMethodName(rootDir).resolve("src.zip");

        assertThatThrownBy(() -> ZipIt.zip(zip).settings(settings).add(files)).isExactlyInstanceOf(PathNotExistsException.class);
    }

    public void shouldMergeSplitZip() throws IOException {
        ZipMisc.zip(zipDeflateSplit).merge(zipMerge);
        assertThatDirectory(zipMerge.getParent()).exists().hasDirectories(0).hasFiles(1);
        assertThatZipFile(zipMerge).exists().root().matches(rootAssert);
    }

    @Test(dependsOnMethods = "shouldMergeSplitZip")
    public void shouldThrowExceptionWhenMergeWithDuplicatedEntries() throws IOException {
        assertThatThrownBy(() -> ZipMisc.zip(zipDeflateSplit).merge(zipMerge)).isExactlyInstanceOf(EntryDuplicationException.class);
    }

    public void shouldRetrieveTrueWhenSplitZipWithMultipleDisks() throws IOException {
        assertThat(ZipMisc.zip(zipStoreSplit).isSplit()).isTrue();
    }

    public void shouldRetrieveTrueWhenSplitZipWithOneDisk() throws IOException {
        ZipSettings settings = ZipSettings.builder()
                                          .splitSize(1024 * 1024)
                                          .entrySettingsProvider(fileName ->
                                                  ZipEntrySettings.builder()
                                                                  .compression(Compression.STORE, CompressionLevel.NORMAL)
                                                                  .build())
                                          .build();
        Path zip = Zip4jvmSuite.subDirNameAsMethodName(rootDir).resolve("src.zip");
        ZipIt.zip(zip).settings(settings).add(Collections.singleton(dirSrc.resolve("Oleg Cherednik.txt")));

        assertThat(ZipMisc.zip(zipStoreSplit).isSplit()).isTrue();
    }

    public void shouldRemoveGivenFilesFromExistedZip() throws IOException {
        Path zip = Zip4jvmSuite.subDirNameAsMethodName(rootDir).resolve("src.zip");
        Files.createDirectories(zip.getParent());
        Files.copy(zipStoreSolid, zip);
        assertThatZipFile(zip).exists().root().matches(rootAssert);

        List<String> entryNames = filesDirCars.stream()
                                              .map(file -> dirSrc.relativize(file).toString())
                                              .collect(Collectors.toList());

        ZipMisc zipFile = ZipMisc.zip(zip);

        zipFile.removeEntryByNamePrefix(entryNames);
        assertThat(zipFile.getEntries()).hasSize(10);
    }

    public void shouldRemoveFolderFromExistedZip() throws IOException {
        Path zip = Zip4jvmSuite.subDirNameAsMethodName(rootDir).resolve("src.zip");
        Files.createDirectories(zip.getParent());
        Files.copy(zipStoreSolid, zip);
        assertThatZipFile(zip).exists().root().matches(rootAssert);

        ZipMisc zipFile = ZipMisc.zip(zip);

        zipFile.removeEntryByNamePrefix(dirSrc.relativize(dirCars).toString());
        assertThat(zipFile.getEntries()).hasSize(10);
    }

    public void shouldThrowExceptionWhenRemovedEntryWithExactNameDoesNotExists() throws IOException {
        Path zip = Zip4jvmSuite.subDirNameAsMethodName(rootDir).resolve("src.zip");
        Files.createDirectories(zip.getParent());
        Files.copy(zipStoreSolid, zip);

        ZipMisc zipFile = ZipMisc.zip(zip);
        assertThat(zipFile.getEntries()).hasSize(13);

        assertThatThrownBy(() -> ZipMisc.zip(zip).removeEntryByName(dirNameCars)).isExactlyInstanceOf(EntryNotFoundException.class);
        assertThat(zipFile.getEntries()).hasSize(13);
    }

    public void shouldRemoveOnlyOneEntryWhenEntryContainsSubEntries() throws IOException {
        Path zip = Zip4jvmSuite.subDirNameAsMethodName(rootDir).resolve("src.zip");

        try (ZipFile.Writer zipFile = ZipIt.zip(zip).open()) {
            zipFile.add(ZipFile.Entry.of(dirCars, dirNameCars));
            zipFile.add(ZipFile.Entry.of(fileFerrari, dirNameCars + '/' + fileNameFerrari));
        }

        ZipMisc zipFile = ZipMisc.zip(zip);
        assertThat(zipFile.getEntries()).hasSize(2);

        zipFile.removeEntryByName(dirNameCars);
        assertThat(zipFile.getEntries()).hasSize(1);
    }
}
