package net.lingala.zip4j;

import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.CompressionLevel;
import net.lingala.zip4j.model.CompressionMethod;
import net.lingala.zip4j.model.InputStreamMeta;
import net.lingala.zip4j.model.ZipParameters;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Oleg Cherednik
 * @since 15.03.2019
 */
public class ZipStreamNoSplitTest {

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

    @Test
    public void shouldCreateNewZipFromGivenStream() throws ZipException, IOException {
        Path zipFile = destDir.resolve("src.zip");

        ZipParameters parameters = ZipParameters.builder()
                                                .compressionMethod(CompressionMethod.DEFLATE)
                                                .compressionLevel(CompressionLevel.NORMAL)
                                                .defaultFolderPath(srcDir).build();

        Path bentley = srcDir.resolve("cars/bentley-continental.jpg");
        Path ferrari = srcDir.resolve("cars/ferrari-458-italia.jpg");
        Path wiesmann = srcDir.resolve("cars/wiesmann-gt-mf5.jpg");

        List<InputStreamMeta> files = Arrays.asList(
                new InputStreamMeta(new FileInputStream(bentley.toFile()), srcDir.relativize(bentley).toString()),
                new InputStreamMeta(new FileInputStream(ferrari.toFile()), srcDir.relativize(ferrari).toString()),
                new InputStreamMeta(new FileInputStream(wiesmann.toFile()), srcDir.relativize(wiesmann).toString()));

        ZipIt zip = ZipIt.builder().zipFile(zipFile).build();
        zip.addStream(files, parameters);

        assertThat(Files.exists(zipFile)).isTrue();
        assertThat(Files.isRegularFile(zipFile)).isTrue();
        TestUtils.checkDestinationDir(1, destDir);

        // ---

        UnzipIt unzip = UnzipIt.builder().zipFile(zipFile).build();
        unzip.extract(resDir);

        TestUtils.checkDirectory(resDir, 1, 0);
        TestUtils.checkCarsDirectory(resDir.resolve("cars"));
    }

    @Test(dependsOnMethods = "shouldCreateNewZipFromGivenStream")
    public void shouldAddNewFilesFromStreamToExistedZip() throws IOException, ZipException {
        Path zipFile = destDir.resolve("src.zip");
        assertThat(Files.exists(zipFile)).isTrue();
        assertThat(Files.isRegularFile(zipFile)).isTrue();

        ZipParameters parameters = ZipParameters.builder()
                                                .compressionMethod(CompressionMethod.DEFLATE)
                                                .compressionLevel(CompressionLevel.NORMAL)
                                                .defaultFolderPath(srcDir).build();

        Path one = srcDir.resolve("Star Wars/0qQnv2v.jpg");
        Path two = srcDir.resolve("Star Wars/080fc325efa248454e59b84be24ea829.jpg");
        Path three = srcDir.resolve("Star Wars/pE9Hkw6.jpg");
        Path four = srcDir.resolve("Star Wars/star-wars-wallpapers-29931-7188436.jpg");

        List<InputStreamMeta> files = Arrays.asList(
                new InputStreamMeta(new FileInputStream(one.toFile()), srcDir.relativize(one).toString()),
                new InputStreamMeta(new FileInputStream(two.toFile()), srcDir.relativize(two).toString()),
                new InputStreamMeta(new FileInputStream(three.toFile()), srcDir.relativize(three).toString()),
                new InputStreamMeta(new FileInputStream(four.toFile()), srcDir.relativize(four).toString()));

        ZipIt zip = ZipIt.builder().zipFile(zipFile).build();
        zip.addStream(files, parameters);

        // ---

        UnzipIt unzip = UnzipIt.builder().zipFile(zipFile).build();
        unzip.extract(resDir);

        TestUtils.checkDirectory(resDir, 2, 0);
        TestUtils.checkCarsDirectory(resDir.resolve("cars"));
        TestUtils.checkStarWarsDirectory(resDir.resolve("Star Wars"));
    }
}
