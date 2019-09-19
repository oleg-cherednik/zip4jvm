package ru.olegcherednik.zip4jvm;

import org.assertj.core.api.Assertions;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.olegcherednik.zip4jvm.exception.Zip4jPathNotExistsException;
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
import static ru.olegcherednik.zip4jvm.assertj.Zip4jAssertions.assertThatDirectory;
import static ru.olegcherednik.zip4jvm.assertj.Zip4jAssertions.assertThatZipFile;

/**
 * @author Oleg Cherednik
 * @since 15.03.2019
 */
@Test
@SuppressWarnings("FieldNamingConvention")
public class ZipMiscTest {

    private static final Path rootDir = Zip4jSuite.generateSubDirNameWithTime(ZipMiscTest.class);

    @BeforeClass
    public static void createDir() throws IOException {
        Files.createDirectories(rootDir);
    }

    @AfterClass(enabled = Zip4jSuite.clear)
    public static void removeDir() throws IOException {
        Zip4jSuite.removeDir(rootDir);
    }

    public void shouldRetrieveAllEntryNamesForExistedZip() throws IOException {
        Assertions.assertThat(ZipMisc.getEntryNames(Zip4jSuite.deflateSolidZip)).hasSize(13);
    }

    public void shouldRetrieveAllEntryNamesForExistedEncryptedZip() throws IOException {
        Path zip = Zip4jSuite.copy(rootDir, Zip4jSuite.deflateSolidPkwareZip);
        assertThat(ZipMisc.getEntryNames(zip)).hasSize(13);
    }

    public void shouldThrowExceptionWhenAddedFileNotExists() throws IOException {
        ZipFileSettings settings = ZipFileSettings.builder()
                                                  .entrySettingsProvider(fileName ->
                                                          ZipEntrySettings.builder()
                                                                          .compression(Compression.STORE, CompressionLevel.NORMAL)
                                                                          .build())
                                                  .build();

        Path bentley = Zip4jSuite.carsDir.resolve("bentley-continental.jpg");
        Path ferrari = Zip4jSuite.carsDir.resolve("ferrari-458-italia.jpg");
        Path wiesmann = Zip4jSuite.carsDir.resolve("wiesmann-gt-mf5.jpg");
        Path notExisted = Zip4jSuite.carsDir.resolve(UUID.randomUUID().toString());
        List<Path> files = Arrays.asList(bentley, ferrari, wiesmann, notExisted);

        Path zip = Zip4jSuite.subDirNameAsMethodName(rootDir).resolve("src.zip");

        assertThatThrownBy(() -> ZipIt.add(zip, files, settings)).isExactlyInstanceOf(Zip4jPathNotExistsException.class);
    }

    public void shouldMergeSplitZip() throws IOException {
        assertThat(ZipMisc.isSplit(Zip4jSuite.deflateSplitZip)).isTrue();

        Path zip = Zip4jSuite.subDirNameAsMethodName(rootDir).resolve("src.zip");
        ZipMisc.merge(Zip4jSuite.deflateSplitZip, zip);

        assertThatDirectory(zip.getParent()).exists().hasSubDirectories(0).hasFiles(1);
        assertThatZipFile(zip).exists().rootEntry().matches(TestUtils.zipRootDirAssert);
    }

    public void shouldRetrieveTrueWhenSplitZipWithMultipleDisks() throws IOException {
        assertThat(ZipMisc.isSplit(Zip4jSuite.storeSplitZip)).isTrue();
    }

    public void shouldRetrieveTrueWhenSplitZipWithOneDisk() throws IOException {
        ZipFileSettings settings = ZipFileSettings.builder()
                                                  .splitSize(1024 * 1024)
                                                  .entrySettingsProvider(fileName ->
                                                          ZipEntrySettings.builder()
                                                                          .compression(Compression.STORE, CompressionLevel.NORMAL)
                                                                          .build())
                                                  .build();
        Path zip = Zip4jSuite.subDirNameAsMethodName(rootDir).resolve("src.zip");
        ZipIt.add(zip, Collections.singleton(Zip4jSuite.srcDir.resolve("Oleg Cherednik.txt")), settings);

        assertThat(ZipMisc.isSplit(Zip4jSuite.storeSplitZip)).isTrue();
    }

    public void shouldRemoveGivenFilesFromExistedZip() throws IOException {
        Path zip = Zip4jSuite.subDirNameAsMethodName(rootDir).resolve("src.zip");
        Files.createDirectories(zip.getParent());
        Files.copy(Zip4jSuite.storeSolidZip, zip);
        assertThatZipFile(zip).exists().rootEntry().matches(TestUtils.zipRootDirAssert);

        List<String> entryNames = Zip4jSuite.filesCarsDir.stream()
                                                         .map(file -> Zip4jSuite.srcDir.relativize(file).toString())
                                                         .collect(Collectors.toList());

        ZipMisc.removeEntry(zip, entryNames);
        assertThat(ZipMisc.getEntryNames(zip)).hasSize(10);
    }

    public void shouldRemoveFolderFromExistedZip() throws IOException {
        Path zip = Zip4jSuite.subDirNameAsMethodName(rootDir).resolve("src.zip");
        Files.createDirectories(zip.getParent());
        Files.copy(Zip4jSuite.storeSolidZip, zip);
        assertThatZipFile(zip).exists().rootEntry().matches(TestUtils.zipRootDirAssert);

        ZipMisc.removeEntry(zip, Zip4jSuite.srcDir.relativize(Zip4jSuite.carsDir).toString());
        assertThat(ZipMisc.getEntryNames(zip)).hasSize(10);
    }
}
