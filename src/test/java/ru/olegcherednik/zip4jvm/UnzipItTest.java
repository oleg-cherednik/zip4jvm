package ru.olegcherednik.zip4jvm;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;
import ru.olegcherednik.zip4jvm.assertj.Zip4jAssertions;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

/**
 * @author Oleg Cherednik
 * @since 14.03.2019
 */
@Test
@SuppressWarnings("FieldNamingConvention")
public class UnzipItTest {

    private static final Path rootDir = Zip4jSuite.generateSubDirName(UnzipItTest.class);

    @BeforeClass
    public static void createDir() throws IOException {
        Files.createDirectories(rootDir);
    }

    @AfterClass(enabled = Zip4jSuite.clear)
    public static void removeDir() throws IOException {
        Zip4jSuite.removeDir(rootDir);
    }

    public void shouldUnzipRequiredFiles() throws IOException {
        Path destDir = Zip4jSuite.subDirNameAsMethodNameWithTme(rootDir);
        List<String> fileNames = Arrays.asList("saint-petersburg.jpg", "cars/bentley-continental.jpg");
        ZipFileReader zipFile = new ZipFileReader(Zip4jSuite.deflateSolidZip);
        zipFile.extract(destDir, fileNames);

        Zip4jAssertions.assertThatDirectory(destDir).exists().hasSubDirectories(0).hasFiles(2);
        Zip4jAssertions.assertThatFile(destDir.resolve("saint-petersburg.jpg")).exists().isImage().hasSize(1_074_836);
        Zip4jAssertions.assertThatFile(destDir.resolve("bentley-continental.jpg")).exists().isImage().hasSize(1_395_362);
    }

    @Test
    @Ignore
    public void shouldUnzipRequiredFilesWhenSplit() throws IOException {
        Path destDir = Zip4jSuite.subDirNameAsMethodNameWithTme(rootDir);
        List<String> entries = Arrays.asList("saint-petersburg.jpg", "cars/bentley-continental.jpg");
        ZipFileReader zipFile = new ZipFileReader(Zip4jSuite.deflateSplitZip);
        zipFile.extract(destDir, entries);

        Zip4jAssertions.assertThatDirectory(destDir).exists().hasSubDirectories(0).hasFiles(2);
        Zip4jAssertions.assertThatFile(destDir.resolve("saint-petersburg.jpg")).exists().isImage().hasSize(1_074_836);
        Zip4jAssertions.assertThatFile(destDir.resolve("bentley-continental.jpg")).exists().isImage().hasSize(1_395_362);
    }

    public void shouldUnzipOneFile() throws IOException {
        Path destDir = Zip4jSuite.subDirNameAsMethodNameWithTme(rootDir);
        ZipFileReader zipFile = new ZipFileReader(Zip4jSuite.deflateSolidZip);
        zipFile.extract(destDir, "cars/ferrari-458-italia.jpg");

        Zip4jAssertions.assertThatDirectory(destDir).exists().hasSubDirectories(0).hasFiles(1);
        Zip4jAssertions.assertThatFile(destDir.resolve("ferrari-458-italia.jpg")).exists().isImage().hasSize(320_894);
    }

    public void shouldUnzipFolder() throws IOException {
        Path destDir = Zip4jSuite.subDirNameAsMethodNameWithTme(rootDir);
        ZipFileReader zipFile = new ZipFileReader(Zip4jSuite.deflateSolidZip);
        zipFile.extract(destDir, "Star Wars");

        Zip4jAssertions.assertThatDirectory(destDir).exists().hasSubDirectories(1).hasFiles(0);

        Path starWarsDir = destDir.resolve("Star Wars/");
        Zip4jAssertions.assertThatDirectory(starWarsDir).exists().hasSubDirectories(0).hasFiles(4);
        Zip4jAssertions.assertThatFile(starWarsDir.resolve("one.jpg")).isImage().hasSize(2_204_448);
        Zip4jAssertions.assertThatFile(starWarsDir.resolve("two.jpg")).isImage().hasSize(277_857);
        Zip4jAssertions.assertThatFile(starWarsDir.resolve("three.jpg")).isImage().hasSize(1_601_879);
        Zip4jAssertions.assertThatFile(starWarsDir.resolve("four.jpg")).isImage().hasSize(1_916_776);
    }

}
