package ru.olegcherednik.zip4jvm;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.olegcherednik.zip4jvm.exception.SplitPartNotFoundException;
import ru.olegcherednik.zip4jvm.model.Compression;
import ru.olegcherednik.zip4jvm.model.CompressionLevel;
import ru.olegcherednik.zip4jvm.model.settings.ZipEntrySettings;
import ru.olegcherednik.zip4jvm.model.settings.ZipSettings;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static ru.olegcherednik.zip4jvm.TestData.dirBikes;
import static ru.olegcherednik.zip4jvm.TestData.dirCars;
import static ru.olegcherednik.zip4jvm.TestData.dirNameCars;
import static ru.olegcherednik.zip4jvm.TestData.fileNameBentley;
import static ru.olegcherednik.zip4jvm.TestData.fileNameSaintPetersburg;
import static ru.olegcherednik.zip4jvm.TestData.zipDeflateSplit;
import static ru.olegcherednik.zip4jvm.TestDataAssert.fileBentleyAssert;
import static ru.olegcherednik.zip4jvm.TestDataAssert.fileSaintPetersburgAssert;
import static ru.olegcherednik.zip4jvm.Zip4jvmSuite.SIZE_1MB;
import static ru.olegcherednik.zip4jvm.assertj.Zip4jvmAssertions.assertThatDirectory;
import static ru.olegcherednik.zip4jvm.assertj.Zip4jvmAssertions.assertThatFile;

/**
 * @author Oleg Cherednik
 * @since 14.03.2019
 */
@Test
@SuppressWarnings("FieldNamingConvention")
public class UnzipItSplitTest {

    private static final Path rootDir = Zip4jvmSuite.generateSubDirName(UnzipItSplitTest.class);

    @BeforeClass
    public static void createDir() throws IOException {
        Files.createDirectories(rootDir);
    }

    @AfterClass(enabled = Zip4jvmSuite.clear)
    public static void removeDir() throws IOException {
        Zip4jvmSuite.removeDir(rootDir);
    }

    public void shouldUnzipRequiredFilesWhenSplit() throws IOException {
        Path destDir = Zip4jvmSuite.subDirNameAsMethodNameWithTime(rootDir);
        List<String> fileNames = Arrays.asList(fileNameSaintPetersburg, dirNameCars + '/' + fileNameBentley);
        UnzipIt.zip(zipDeflateSplit).destDir(destDir).extract(fileNames);

        assertThatDirectory(destDir).exists().hasDirectories(0).hasFiles(2);
        assertThatFile(destDir.resolve(fileNameSaintPetersburg)).matches(fileSaintPetersburgAssert);
        assertThatFile(destDir.resolve(fileNameBentley)).matches(fileBentleyAssert);
    }

    public void shouldThrowFileNotFoundExceptionAndNotExtractPartialFilesWhenZipPartMissing() throws IOException {
        ZipEntrySettings entrySettings = ZipEntrySettings.builder().compression(Compression.STORE, CompressionLevel.NORMAL).build();
        ZipSettings settings = ZipSettings.builder()
                                          .entrySettingsProvider(fileName -> entrySettings)
                                          .splitSize(SIZE_1MB)
                                          .build();

        Path destDir = Zip4jvmSuite.subDirNameAsMethodNameWithTime(rootDir);
        Path zip = destDir.resolve("src.zip");
        ZipIt.zip(zip).settings(settings).add(Arrays.asList(dirBikes, dirCars));
        assertThatDirectory(destDir).exists().hasDirectories(0).hasFiles(4);

        Files.delete(destDir.resolve("src.z02"));
        assertThatDirectory(destDir).exists().hasDirectories(0).hasFiles(3);

        Path unzipDir = destDir.resolve("unzip");
        Files.createDirectory(unzipDir);

        assertThatThrownBy(() -> UnzipIt.zip(zip).destDir(unzipDir).extract()).isExactlyInstanceOf(SplitPartNotFoundException.class);
        assertThatDirectory(unzipDir).isEmpty();
    }

}
