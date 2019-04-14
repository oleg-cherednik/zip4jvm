package net.lingala.zip4j;

import net.lingala.zip4j.exception.ZipException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static net.lingala.zip4j.assertj.Zip4jAssertions.assertThatDirectory;
import static net.lingala.zip4j.assertj.Zip4jAssertions.assertThatFile;

/**
 * @author Oleg Cherednik
 * @since 14.03.2019
 */
@SuppressWarnings("FieldNamingConvention")
public class UnzipItTest {

    private static final Path rootDir = Zip4jSuite.rootDir.resolve(UnzipItTest.class.getSimpleName());

    @BeforeClass
    public static void createDir() throws IOException {
        Files.createDirectories(rootDir);
    }

    @AfterClass(enabled = Zip4jSuite.clear)
    public static void removeDir() throws IOException {
        Zip4jSuite.removeDir(rootDir);
    }

    @Test
    public void shouldUnzipRequiredFiles() throws ZipException, IOException {
        List<String> entries = Arrays.asList("saint-petersburg.jpg", "cars/bentley-continental.jpg");
        UnzipIt unzip = UnzipIt.builder().zipFile(Zip4jSuite.noSplitZip).build();
        unzip.extract(rootDir, entries);

        assertThatDirectory(rootDir).exists().hasSubDirectories(1).hasFiles(1);
        assertThatDirectory(rootDir.resolve("cars/")).exists().hasSubDirectories(0).hasFiles(1);
        assertThatFile(rootDir.resolve("saint-petersburg.jpg")).exists().isImage().hasSize(1_074_836);
        assertThatFile(rootDir.resolve("cars/bentley-continental.jpg")).exists().isImage().hasSize(1_395_362);
    }

    @Test(dependsOnMethods = "shouldUnzipRequiredFiles")
    public void shouldUnzipOneFile() throws ZipException, IOException {
        UnzipIt unzip = UnzipIt.builder().zipFile(Zip4jSuite.noSplitZip).build();
        unzip.extract(rootDir, "cars/ferrari-458-italia.jpg");

        assertThatDirectory(rootDir).exists().hasSubDirectories(1).hasFiles(1);
        assertThatDirectory(rootDir.resolve("cars/")).exists().hasSubDirectories(0).hasFiles(2);
        assertThatFile(rootDir.resolve("saint-petersburg.jpg")).exists().isImage().hasSize(1_074_836);
        assertThatFile(rootDir.resolve("cars/bentley-continental.jpg")).exists().isImage().hasSize(1_395_362);
        assertThatFile(rootDir.resolve("cars/ferrari-458-italia.jpg")).exists().isImage().hasSize(320_894);
    }

    @Test(dependsOnMethods = "shouldUnzipOneFile")
    public void shouldUnzipFolder() throws ZipException, IOException {
        UnzipIt unzip = UnzipIt.builder().zipFile(Zip4jSuite.noSplitZip).build();
        unzip.extract(rootDir, "Star Wars");

        assertThatDirectory(rootDir).exists().hasSubDirectories(2).hasFiles(1);
        assertThatDirectory(rootDir.resolve("cars/")).exists().hasSubDirectories(0).hasFiles(2);
        assertThatFile(rootDir.resolve("saint-petersburg.jpg")).exists().isImage().hasSize(1_074_836);
        assertThatFile(rootDir.resolve("cars/bentley-continental.jpg")).exists().isImage().hasSize(1_395_362);
        assertThatFile(rootDir.resolve("cars/ferrari-458-italia.jpg")).exists().isImage().hasSize(320_894);

        Path starWarsDir = rootDir.resolve("Star Wars/");
        assertThatDirectory(starWarsDir).exists().hasSubDirectories(0).hasFiles(4);
        assertThatFile(starWarsDir.resolve("0qQnv2v.jpg")).isImage().hasSize(2_204_448);
        assertThatFile(starWarsDir.resolve("080fc325efa248454e59b84be24ea829.jpg")).isImage().hasSize(277_857);
        assertThatFile(starWarsDir.resolve("pE9Hkw6.jpg")).isImage().hasSize(1_601_879);
        assertThatFile(starWarsDir.resolve("star-wars-wallpapers-29931-7188436.jpg")).isImage().hasSize(1_916_776);
    }

    @Test
    public void shouldUnzipOneFileFromEncryptedNoSplitZip() throws IOException {
        Path rootDir = Zip4jSuite.generateSubDirName(UnzipItTest.rootDir, "shouldUnzipOneFileFromEncryptedNoSplitZip");

        UnzipIt unzip = UnzipIt.builder()
//                               .zipFile(Zip4jSuite.noSplitZip)
                               .zipFile(Paths.get("d:/zip4j/srca.zip"))
                               .password("2".toCharArray()).build();

        unzip.extract(rootDir, "cars/ferrari-458-italia.jpg");
        assertThatDirectory(rootDir).exists().hasSubDirectories(1).hasFiles(0);

        assertThatDirectory(rootDir.resolve("cars")).exists().hasSubDirectories(0).hasFiles(1);
        assertThatFile(rootDir.resolve("cars/ferrari-458-italia.jpg")).exists().isImage().hasSize(320_894);
    }
}
