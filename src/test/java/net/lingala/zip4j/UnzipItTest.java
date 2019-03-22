package net.lingala.zip4j;

import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.CompressionLevel;
import net.lingala.zip4j.model.CompressionMethod;
import net.lingala.zip4j.model.ZipParameters;
import org.testng.annotations.BeforeMethod;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Oleg Cherednik
 * @since 14.03.2019
 */
public class UnzipItTest {
    private Path root;
    private Path srcDir;
    private Path destDir;
    private Path resDir;

    @BeforeMethod
    public void createDirectory() throws IOException {
        root = Paths.get("d:/zip4j");//Files.createTempDirectory("zip4j");
        srcDir = root.resolve("src");
        destDir = root.resolve("dest");
        resDir = destDir.resolve("res");

        Files.createDirectories(srcDir);
        Files.createDirectories(destDir);
//        Files.createDirectories(resDir);
    }

//    @Test
    public void shouldUnzipRequiredFiles() throws ZipException, IOException {
        Path zipFile = destDir.resolve("src.zip");

        ZipParameters parameters = ZipParameters.builder()
                                                .compressionMethod(CompressionMethod.DEFLATE)
                                                .compressionLevel(CompressionLevel.NORMAL).build();
        ZipIt zip = ZipIt.builder().zipFile(zipFile).build();
        zip.add(srcDir, parameters);

        assertThat(Files.exists(zipFile)).isTrue();
        assertThat(Files.isRegularFile(zipFile)).isTrue();

        // ---

        List<String> entries = Arrays.asList("saint-petersburg.jpg", "cars/bentley-continental.jpg");
        UnzipIt unzip = UnzipIt.builder().zipFile(zipFile).build();
        unzip.extract(resDir, entries);

        TestUtils.checkDirectory(resDir, 1, 1);
        TestUtils.checkDirectory(resDir.resolve("cars"), 0, 1);
        TestUtils.checkImage(resDir.resolve("saint-petersburg.jpg"), 1_074_836);
        TestUtils.checkImage(resDir.resolve("cars/bentley-continental.jpg"), 1_395_362);
    }

//    @Test(dependsOnMethods = "shouldUnzipRequiredFiles")
    public void shouldUnzipOneFile() throws ZipException, IOException {
        Path zipFile = destDir.resolve("src.zip");

        assertThat(Files.exists(zipFile)).isTrue();
        assertThat(Files.isRegularFile(zipFile)).isTrue();

        UnzipIt unzip = UnzipIt.builder().zipFile(zipFile).build();
        unzip.extract(resDir, "cars/ferrari-458-italia.jpg");

        TestUtils.checkDirectory(resDir, 1, 1);
        TestUtils.checkDirectory(resDir.resolve("cars"), 0, 2);
        TestUtils.checkImage(resDir.resolve("saint-petersburg.jpg"), 1_074_836);
        TestUtils.checkImage(resDir.resolve("cars/bentley-continental.jpg"), 1_395_362);
        TestUtils.checkImage(resDir.resolve("cars/ferrari-458-italia.jpg"), 320_894);
    }

//    @Test(dependsOnMethods = "shouldUnzipOneFile")
    public void shouldUnzipFolder() throws ZipException, IOException {
        Path zipFile = destDir.resolve("src.zip");

        assertThat(Files.exists(zipFile)).isTrue();
        assertThat(Files.isRegularFile(zipFile)).isTrue();

        UnzipIt unzip = UnzipIt.builder().zipFile(zipFile).build();
        unzip.extract(resDir, "Star Wars");

        TestUtils.checkDirectory(resDir, 2, 1);
        TestUtils.checkDirectory(resDir.resolve("cars"), 0, 2);
        TestUtils.checkStarWarsDirectory(resDir.resolve("Star Wars"));

        TestUtils.checkImage(resDir.resolve("saint-petersburg.jpg"), 1_074_836);
        TestUtils.checkImage(resDir.resolve("cars/bentley-continental.jpg"), 1_395_362);
        TestUtils.checkImage(resDir.resolve("cars/ferrari-458-italia.jpg"), 320_894);
    }
}
