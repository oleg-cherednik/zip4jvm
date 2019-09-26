package ru.olegcherednik.zip4jvm;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static ru.olegcherednik.zip4jvm.TestData.dirCars;
import static ru.olegcherednik.zip4jvm.TestData.fileBentley;
import static ru.olegcherednik.zip4jvm.TestData.fileNameBentley;
import static ru.olegcherednik.zip4jvm.TestData.fileNameSaintPetersburg;
import static ru.olegcherednik.zip4jvm.TestData.fileSaintPetersburg;
import static ru.olegcherednik.zip4jvm.TestData.zipDirNameCars;
import static ru.olegcherednik.zip4jvm.TestDataAssert.zipDirCarsAssert;
import static ru.olegcherednik.zip4jvm.assertj.Zip4jvmAssertions.assertThatDirectory;
import static ru.olegcherednik.zip4jvm.assertj.Zip4jvmAssertions.assertThatZipFile;

/**
 * @author Oleg Cherednik
 * @since 26.09.2019
 */
@Test
@SuppressWarnings("FieldNamingConvention")
public class ZipItTest {

    private static final Path rootDir = Zip4jvmSuite.generateSubDirNameWithTime(ZipItTest.class);
    private static final Path defFile = rootDir.resolve("def/src.zip");

    @BeforeClass
    public static void createDir() throws IOException {
        Files.createDirectories(rootDir);
    }

    @AfterClass(enabled = Zip4jvmSuite.clear)
    public static void removeDir() throws IOException {
        Zip4jvmSuite.removeDir(rootDir);
    }

    public void shouldCreateZipWhenAddRegularFileAndDefaultSettings() throws IOException {
        ZipIt.add(defFile, fileBentley);
        assertThatDirectory(defFile.getParent()).exists().hasDirectories(0).hasFiles(1);
        assertThatZipFile(defFile).root().hasDirectories(0).hasFiles(1);
        assertThatZipFile(defFile).root().file(fileNameBentley).exists().hasSize(1_395_362);
    }

    public void shouldCreateZipWhenAddDirectoryAndDefaultSettings() throws IOException {
        Path zip = Zip4jvmSuite.subDirNameAsMethodName(rootDir).resolve("src.zip");

        ZipIt.add(zip, dirCars);
        assertThatDirectory(zip.getParent()).exists().hasDirectories(0).hasFiles(1);
        assertThatZipFile(zip).root().hasDirectories(1).hasFiles(0);
        assertThatZipFile(zip).directory(zipDirNameCars).matches(zipDirCarsAssert);
    }

    @Test(dependsOnMethods = "shouldCreateZipWhenAddRegularFileAndDefaultSettings")
    public void shouldAddRegularFileWhenZipExists() throws IOException {
        ZipIt.add(defFile, fileSaintPetersburg);
        assertThatDirectory(defFile.getParent()).exists().hasDirectories(0).hasFiles(1);
        assertThatZipFile(defFile).root().hasDirectories(0).hasFiles(2);
        assertThatZipFile(defFile).root().file(fileNameBentley).exists().hasSize(1_395_362);
        assertThatZipFile(defFile).root().file(fileNameSaintPetersburg).exists().hasSize(1_074_836);
    }

    @Test(dependsOnMethods = "shouldAddRegularFileWhenZipExists")
    public void shouldAddDirectoryWhenZipExists() throws IOException {
        ZipIt.add(defFile, dirCars);
        assertThatDirectory(defFile.getParent()).exists().hasDirectories(0).hasFiles(1);
        assertThatZipFile(defFile).root().hasDirectories(1).hasFiles(2);
        assertThatZipFile(defFile).root().file(fileNameBentley).exists().hasSize(1_395_362);
        assertThatZipFile(defFile).root().file(fileNameSaintPetersburg).exists().hasSize(1_074_836);
        assertThatZipFile(defFile).directory(zipDirNameCars).matches(zipDirCarsAssert);
    }

}
