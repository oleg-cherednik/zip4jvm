package net.lingala.zip4j;

import net.lingala.zip4j.core.ZipFileUnzip;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.CompressionLevel;
import net.lingala.zip4j.model.CompressionMethod;
import net.lingala.zip4j.model.UnzipParameters;
import net.lingala.zip4j.model.ZipParameters;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Oleg Cherednik
 * @since 14.03.2019
 */
public class ZipFolderNoSplitTest {

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

    //    @BeforeMethod(dependsOnMethods = "createDirectory")
//    public void copyTestData() throws IOException {
//        Path dataDir = Paths.get("src/test/resources/data").toAbsolutePath();
//
//        Files.walk(dataDir).forEach(path -> {
//            try {
//                if (Files.isDirectory(path))
//                    Files.createDirectories(srcDir.resolve(dataDir.relativize(path)));
//                else if (Files.isRegularFile(path))
//                    Files.copy(path, srcDir.resolve(dataDir.relativize(path)));
//            } catch(IOException e) {
//                e.printStackTrace();
//            }
//        });
//    }

//    @AfterMethod
//    public void removeDirectory() throws IOException {
//        FileUtils.deleteQuietly(root.toFile());
//        FileUtils.deleteQuietly(destDir.toFile());
//    }

    @Test
    public void shouldCreateNewZipWithFolder() throws ZipException, IOException {
        Path zipFile = destDir.resolve("src.zip");
        Path carsDir = srcDir.resolve("cars");

        ZipParameters parameters = ZipParameters.builder()
                                                .compressionMethod(CompressionMethod.DEFLATE)
                                                .compressionLevel(CompressionLevel.NORMAL)
                                                .defaultFolderPath(srcDir.toString())
                                                .build();
        ZipIt zip = ZipIt.builder().zipFile(zipFile).build();
        zip.add(carsDir, parameters);

        assertThat(Files.exists(zipFile)).isTrue();
        assertThat(Files.isRegularFile(zipFile)).isTrue();

        // ---

        new ZipFileUnzip(zipFile).extract(resDir, new UnzipParameters());
        TestUtils.checkCarsDirectory(resDir.resolve("cars"));
    }

    @Test(dependsOnMethods = "shouldCreateNewZipWithDirectory")
    public void shouldAddFolderToExistedZip() throws ZipException, IOException {
        Path zipFile = destDir.resolve("src.zip");
        Path starWarsDir = srcDir.resolve("Star Wars");

        assertThat(Files.exists(zipFile)).isTrue();
        assertThat(Files.isRegularFile(zipFile)).isTrue();

        ZipParameters parameters = ZipParameters.builder()
                                                .compressionMethod(CompressionMethod.DEFLATE)
                                                .compressionLevel(CompressionLevel.NORMAL)
                                                .defaultFolderPath(srcDir.toString())
                                                .build();
        ZipIt zip = ZipIt.builder().zipFile(zipFile).build();
        zip.add(starWarsDir, parameters);

        assertThat(Files.exists(zipFile)).isTrue();
        assertThat(Files.isRegularFile(zipFile)).isTrue();

        // ---

        new ZipFileUnzip(zipFile).extract(resDir, new UnzipParameters());
        TestUtils.checkCarsDirectory(resDir.resolve("cars"));
        TestUtils.checkStarWarsDirectory(resDir.resolve("Star Wars"));
    }

    @Test(dependsOnMethods = "shouldAddFolderToExistedZip")
    public void shouldAddEmptyDirectoryToExistedZip() throws ZipException, IOException {
        Path zipFile = destDir.resolve("src.zip");
        Path emptyDir = srcDir.resolve("empty_dir");

        assertThat(Files.exists(zipFile)).isTrue();
        assertThat(Files.isRegularFile(zipFile)).isTrue();

        ZipParameters parameters = ZipParameters.builder()
                                                .compressionMethod(CompressionMethod.DEFLATE)
                                                .compressionLevel(CompressionLevel.NORMAL)
                                                .defaultFolderPath(srcDir.toString())
                                                .build();
        ZipIt zip = ZipIt.builder().zipFile(zipFile).build();
        zip.add(emptyDir, parameters);

        assertThat(Files.exists(zipFile)).isTrue();
        assertThat(Files.isRegularFile(zipFile)).isTrue();

        // ---

        new ZipFileUnzip(zipFile).extract(resDir, new UnzipParameters());
        TestUtils.checkCarsDirectory(resDir.resolve("cars"));
        TestUtils.checkStarWarsDirectory(resDir.resolve("Star Wars"));
        TestUtils.checkEmptyDirectory(resDir.resolve("empty_dir"));
    }

}
