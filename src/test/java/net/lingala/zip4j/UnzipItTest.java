package net.lingala.zip4j;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.CompressionLevel;
import net.lingala.zip4j.model.CompressionMethod;
import net.lingala.zip4j.model.ZipParameters;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

/**
 * @author Oleg Cherednik
 * @since 14.03.2019
 */
@Test
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

    public void shouldUnzipRequiredFiles() throws ZipException, IOException {
        Path zipFile = destDir.resolve("src.zip");

        List<Path> files = Arrays.asList(
                srcDir.resolve("saint-petersburg.jpg"),
                srcDir.resolve("sig-sauer-pistol.jpg"),
                srcDir.resolve("cars/bentley-continental.jpg"),
                srcDir.resolve("cars/ferrari-458-italia.jpg"));

        ZipParameters parameters = ZipParameters.builder()
                                                .compressionMethod(CompressionMethod.DEFLATE)
                                                .compressionLevel(CompressionLevel.NORMAL)
                                                .defaultFolderPath(srcDir).build();

        // TODO should be changed to ZipIt
        new ZipFile(zipFile).addFiles(files, parameters);

        // ---

        List<String> entries = Arrays.asList("saint-petersburg.jpg", "cars/bentley-continental.jpg");
        UnzipIt unzip = UnzipIt.builder().zipFile(zipFile).build();
        unzip.extract(resDir, entries);

        Path carsDir = resDir.resolve("cars");

        TestUtils.checkDirectory(resDir, 1, 1);
        TestUtils.checkDirectory(carsDir, 0, 1);
        TestUtils.checkImage(resDir.resolve("saint-petersburg.jpg"), 1_074_836);
        TestUtils.checkImage(resDir.resolve("cars/bentley-continental.jpg"), 1_395_362);
    }
}
