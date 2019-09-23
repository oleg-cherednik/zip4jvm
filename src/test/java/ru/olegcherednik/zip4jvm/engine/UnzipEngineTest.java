package ru.olegcherednik.zip4jvm.engine;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.olegcherednik.zip4jvm.Zip4jvmSuite;
import ru.olegcherednik.zip4jvm.ZipFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static ru.olegcherednik.zip4jvm.TestData.dirNameBikes;
import static ru.olegcherednik.zip4jvm.TestData.dirNameCars;
import static ru.olegcherednik.zip4jvm.TestData.dirNameEmpty;
import static ru.olegcherednik.zip4jvm.TestData.fileNameBentley;
import static ru.olegcherednik.zip4jvm.TestData.fileNameDucati;
import static ru.olegcherednik.zip4jvm.TestData.fileNameEmpty;
import static ru.olegcherednik.zip4jvm.TestData.fileNameFerrari;
import static ru.olegcherednik.zip4jvm.TestData.fileNameHonda;
import static ru.olegcherednik.zip4jvm.TestData.fileNameKawasaki;
import static ru.olegcherednik.zip4jvm.TestData.fileNameMcdonnelDouglas;
import static ru.olegcherednik.zip4jvm.TestData.fileNameOlegCherednik;
import static ru.olegcherednik.zip4jvm.TestData.fileNameSaintPetersburg;
import static ru.olegcherednik.zip4jvm.TestData.fileNameSigSauer;
import static ru.olegcherednik.zip4jvm.TestData.fileNameSuzuki;
import static ru.olegcherednik.zip4jvm.TestData.fileNameWiesmann;
import static ru.olegcherednik.zip4jvm.TestData.zipDeflateSolid;
import static ru.olegcherednik.zip4jvm.TestData.zipDeflateSolidAes;
import static ru.olegcherednik.zip4jvm.TestData.zipDeflateSolidPkware;
import static ru.olegcherednik.zip4jvm.TestData.zipStoreSolidPkware;
import static ru.olegcherednik.zip4jvm.TestData.zipStoreSplitAes;
import static ru.olegcherednik.zip4jvm.TestDataAssert.dirCarsAssert;
import static ru.olegcherednik.zip4jvm.assertj.Zip4jvmAssertions.assertThatDirectory;

/**
 * @author Oleg Cherednik
 * @since 07.09.2019
 */
@Test
@SuppressWarnings("FieldNamingConvention")
public class UnzipEngineTest {

    private static final Path rootDir = Zip4jvmSuite.generateSubDirNameWithTime(UnzipEngineTest.class);

    @BeforeClass
    public static void createDir() throws IOException {
        Files.createDirectories(rootDir);
    }

    @AfterClass(enabled = Zip4jvmSuite.clear)
    public static void removeDir() throws IOException {
        Zip4jvmSuite.removeDir(rootDir);
    }

    public void shouldUnzipZipFileIntoDestinationFolderWhenDeflateSolid() throws IOException {
        Path destDir = Zip4jvmSuite.subDirNameAsMethodName(rootDir);

        ZipFile.Reader zipFile = ZipFile.read(zipDeflateSolid);
        zipFile.extract(destDir, dirNameCars);

        assertThatDirectory(destDir).exists().hasDirectories(1).hasFiles(0);
        assertThatDirectory(destDir.resolve(dirNameCars)).matches(dirCarsAssert);
    }

    public void shouldUnzipZipFileIntoDestinationFolderWhenDeflateSolidPkware() throws IOException {
        Path destDir = Zip4jvmSuite.subDirNameAsMethodName(rootDir);

        ZipFile.Reader zipFile = ZipFile.read(zipDeflateSolidPkware, fileName -> Zip4jvmSuite.password);
        zipFile.extract(destDir, dirNameCars);

        assertThatDirectory(destDir).exists().hasDirectories(1).hasFiles(0);
        assertThatDirectory(destDir.resolve(dirNameCars)).matches(dirCarsAssert);
    }

    public void shouldUnzipZipFileIntoDestinationFolderWhenDeflateSolidAes() throws IOException {
        Path destDir = Zip4jvmSuite.subDirNameAsMethodName(rootDir);

        ZipFile.Reader zipFile = ZipFile.read(zipDeflateSolidAes, String::toCharArray);
        zipFile.extract(destDir, dirNameCars);

        assertThatDirectory(destDir).exists().hasDirectories(1).hasFiles(0);
        assertThatDirectory(destDir.resolve(dirNameCars)).matches(dirCarsAssert);
    }

    public void shouldIterateOverAllEntriesWhenStoreSolidPkware() throws IOException {
        List<String> entryNames = new ArrayList<>();

        for (ZipFile.Entry entry : ZipFile.read(zipStoreSolidPkware))
            entryNames.add(entry.getFileName());

        assertThat(entryNames).containsExactlyInAnyOrder(
                dirNameBikes + '/' + fileNameDucati,
                dirNameBikes + '/' + fileNameHonda,
                dirNameBikes + '/' + fileNameKawasaki,
                dirNameBikes + '/' + fileNameSuzuki,
                dirNameCars + '/' + fileNameBentley,
                dirNameCars + '/' + fileNameFerrari,
                dirNameCars + '/' + fileNameWiesmann,
                dirNameEmpty,
                fileNameEmpty,
                fileNameMcdonnelDouglas,
                fileNameOlegCherednik,
                fileNameSaintPetersburg,
                fileNameSigSauer);
    }

    public void shouldRetrieveStreamWithAllEntriesWhenStoreSplitAes() throws IOException {
        List<String> entryNames = ZipFile.read(zipStoreSplitAes).stream()
                                         .map(ZipFile.Entry::getFileName)
                                         .collect(Collectors.toList());

        assertThat(entryNames).containsExactlyInAnyOrder(
                dirNameBikes + '/' + fileNameDucati,
                dirNameBikes + '/' + fileNameHonda,
                dirNameBikes + '/' + fileNameKawasaki,
                dirNameBikes + '/' + fileNameSuzuki,
                dirNameCars + '/' + fileNameBentley,
                dirNameCars + '/' + fileNameFerrari,
                dirNameCars + '/' + fileNameWiesmann,
                dirNameEmpty,
                fileNameEmpty,
                fileNameMcdonnelDouglas,
                fileNameOlegCherednik,
                fileNameSaintPetersburg,
                fileNameSigSauer);
    }

    public void shouldThrowNullPointerExceptionWhenArgumentIsNull() {
        assertThatThrownBy(() -> ZipFile.read(zipStoreSplitAes).extract((String)null)).isExactlyInstanceOf(NullPointerException.class);
    }

    public void shouldThrowExceptionWhenExtractNotExistedEntry() {
        assertThatThrownBy(() -> ZipFile.read(zipStoreSplitAes).extract("<unknown>")).isExactlyInstanceOf(FileNotFoundException.class);
    }
}
