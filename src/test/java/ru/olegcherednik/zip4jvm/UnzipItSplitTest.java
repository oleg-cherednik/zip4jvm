package ru.olegcherednik.zip4jvm;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static ru.olegcherednik.zip4jvm.TestData.dirNameCars;
import static ru.olegcherednik.zip4jvm.TestData.fileNameBentley;
import static ru.olegcherednik.zip4jvm.TestData.fileNameSaintPetersburg;
import static ru.olegcherednik.zip4jvm.TestData.zipDeflateSplit;
import static ru.olegcherednik.zip4jvm.TestDataAssert.fileBentleyAssert;
import static ru.olegcherednik.zip4jvm.TestDataAssert.fileSaintPetersburgAssert;
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
        Path destDir = Zip4jvmSuite.subDirNameAsMethodNameWithTme(rootDir);
        List<String> fileNames = Arrays.asList(fileNameSaintPetersburg, dirNameCars + '/' + fileNameBentley);
        UnzipIt.zip(zipDeflateSplit).destDir(destDir).extract(fileNames);

        assertThatDirectory(destDir).exists().hasDirectories(0).hasFiles(2);
        assertThatFile(destDir.resolve(fileNameSaintPetersburg)).matches(fileSaintPetersburgAssert);
        assertThatFile(destDir.resolve(fileNameBentley)).matches(fileBentleyAssert);
    }

}
