package ru.olegcherednik.zip4jvm;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static ru.olegcherednik.zip4jvm.TestData.dirNameBikes;
import static ru.olegcherednik.zip4jvm.TestData.zipDeflateSolid;
import static ru.olegcherednik.zip4jvm.TestData.zipDeflateSplit;
import static ru.olegcherednik.zip4jvm.assertj.Zip4jvmAssertions.assertThatDirectory;
import static ru.olegcherednik.zip4jvm.assertj.Zip4jvmAssertions.assertThatFile;

/**
 * @author Oleg Cherednik
 * @since 14.03.2019
 */
@Test
@SuppressWarnings("FieldNamingConvention")
public class UnzipItTest {

    private static final Path rootDir = Zip4jvmSuite.generateSubDirName(UnzipItTest.class);

    @BeforeClass
    public static void createDir() throws IOException {
        Files.createDirectories(rootDir);
    }

    @AfterClass(enabled = Zip4jvmSuite.clear)
    public static void removeDir() throws IOException {
        Zip4jvmSuite.removeDir(rootDir);
    }

    public void shouldUnzipRequiredFiles() throws IOException {
        Path destDir = Zip4jvmSuite.subDirNameAsMethodNameWithTme(rootDir);
        List<String> fileNames = Arrays.asList("saint-petersburg.jpg", "cars/bentley-continental.jpg");
        UnzipIt.zip(zipDeflateSolid).destDir(destDir).extract(fileNames);

        assertThatDirectory(destDir).exists().hasDirectories(0).hasFiles(2);
        assertThatFile(destDir.resolve("saint-petersburg.jpg")).exists().isImage().hasSize(1_074_836);
        assertThatFile(destDir.resolve("bentley-continental.jpg")).exists().isImage().hasSize(1_395_362);
    }

    @Test
    @Ignore
    public void shouldUnzipRequiredFilesWhenSplit() throws IOException {
        Path destDir = Zip4jvmSuite.subDirNameAsMethodNameWithTme(rootDir);
        List<String> fileNames = Arrays.asList("saint-petersburg.jpg", "cars/bentley-continental.jpg");
        UnzipIt.zip(zipDeflateSplit).destDir(destDir).extract(fileNames);

        assertThatDirectory(destDir).exists().hasDirectories(0).hasFiles(2);
        assertThatFile(destDir.resolve("saint-petersburg.jpg")).exists().isImage().hasSize(1_074_836);
        assertThatFile(destDir.resolve("bentley-continental.jpg")).exists().isImage().hasSize(1_395_362);
    }

    public void shouldUnzipOneFile() throws IOException {
        Path destDir = Zip4jvmSuite.subDirNameAsMethodNameWithTme(rootDir);
        UnzipIt.zip(zipDeflateSolid).destDir(destDir).extract("cars/ferrari-458-italia.jpg");

        assertThatDirectory(destDir).exists().hasDirectories(0).hasFiles(1);
        assertThatFile(destDir.resolve("ferrari-458-italia.jpg")).exists().isImage().hasSize(320_894);
    }

    public void shouldUnzipFolder() throws IOException {
        Path destDir = Zip4jvmSuite.subDirNameAsMethodNameWithTme(rootDir);
        UnzipIt.zip(zipDeflateSolid).destDir(destDir).extract(dirNameBikes);

        assertThatDirectory(destDir).exists().hasDirectories(1).hasFiles(0);
        assertThatDirectory(destDir.resolve(dirNameBikes)).matches(TestDataAssert.dirBikesAssert);
    }

    public void shouldFoo() throws IOException, URISyntaxException {
        Path destDir = Zip4jvmSuite.subDirNameAsMethodNameWithTme(rootDir);
        Path zip = Paths.get(UnzipItTest.class.getResource("/zip/cjk_filename.zip").toURI()).toAbsolutePath();

        UnzipIt.zip(zip).destDir(destDir).extract();
//        assertThatDirectory(destDir).exists().hasDirectories(0).hasFiles(1);
//        assertThatDirectory(destDir).file("hello.txt").exists().hasSize(11).hasContent("hello,itsme");
    }

}
