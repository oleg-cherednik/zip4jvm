package net.lingala.zip4j;

import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.CompressionLevel;
import net.lingala.zip4j.model.CompressionMethod;
import net.lingala.zip4j.model.InputStreamMeta;
import net.lingala.zip4j.model.ZipParameters;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static net.lingala.zip4j.assertj.Zip4jAssertions.assertThatDirectory;
import static net.lingala.zip4j.assertj.Zip4jAssertions.assertThatZipFile;

/**
 * @author Oleg Cherednik
 * @since 15.03.2019
 */
@SuppressWarnings("FieldNamingConvention")
public class ZipStreamNoSplitTest {

    private static final Path rootDir = Zip4jSuite.rootDir.resolve(ZipStreamNoSplitTest.class.getSimpleName());
    private static final Path zipFile = rootDir.resolve("src.zip");

    @BeforeClass
    public static void createDir() throws IOException {
        Files.createDirectories(rootDir);
    }

    @AfterClass(enabled = Zip4jSuite.clear)
    public static void removeDir() throws IOException {
        Zip4jSuite.removeDir(rootDir);
    }

    @Test
    public void shouldCreateNewZipFromGivenStream() throws ZipException, IOException {
        ZipParameters parameters = ZipParameters.builder()
                                                .compressionMethod(CompressionMethod.DEFLATE)
                                                .compressionLevel(CompressionLevel.NORMAL)
                                                .defaultFolderPath(Zip4jSuite.srcDir).build();

        Path bentley = Zip4jSuite.carsDir.resolve("bentley-continental.jpg");
        Path ferrari = Zip4jSuite.carsDir.resolve("ferrari-458-italia.jpg");
        Path wiesmann = Zip4jSuite.carsDir.resolve("wiesmann-gt-mf5.jpg");

        List<InputStreamMeta> files = Arrays.asList(
                new InputStreamMeta(new FileInputStream(bentley.toFile()), Zip4jSuite.srcDir.relativize(bentley).toString()),
                new InputStreamMeta(new FileInputStream(ferrari.toFile()), Zip4jSuite.srcDir.relativize(ferrari).toString()),
                new InputStreamMeta(new FileInputStream(wiesmann.toFile()), Zip4jSuite.srcDir.relativize(wiesmann).toString()));

        ZipIt zip = ZipIt.builder().zipFile(zipFile).build();
        zip.addStream(files, parameters);

        assertThatDirectory(zipFile.getParent()).exists().hasSubDirectories(0).hasFiles(1);
        assertThatZipFile(zipFile).exists().rootEntry().hasSubDirectories(1).hasFiles(0);
        assertThatZipFile(zipFile).directory("cars/").matches(TestUtils.carsDirAssert);
    }

    @Test(dependsOnMethods = "shouldCreateNewZipFromGivenStream")
    public void shouldAddNewFilesFromStreamToExistedZip() throws IOException, ZipException {
        assertThatZipFile(zipFile).exists();

        ZipParameters parameters = ZipParameters.builder()
                                                .compressionMethod(CompressionMethod.DEFLATE)
                                                .compressionLevel(CompressionLevel.NORMAL)
                                                .defaultFolderPath(Zip4jSuite.srcDir).build();

        Path one = Zip4jSuite.starWarsDir.resolve("0qQnv2v.jpg");
        Path two = Zip4jSuite.starWarsDir.resolve("080fc325efa248454e59b84be24ea829.jpg");
        Path three = Zip4jSuite.starWarsDir.resolve("pE9Hkw6.jpg");
        Path four = Zip4jSuite.starWarsDir.resolve("star-wars-wallpapers-29931-7188436.jpg");

        List<InputStreamMeta> files = Arrays.asList(
                new InputStreamMeta(new FileInputStream(one.toFile()), Zip4jSuite.srcDir.relativize(one).toString()),
                new InputStreamMeta(new FileInputStream(two.toFile()), Zip4jSuite.srcDir.relativize(two).toString()),
                new InputStreamMeta(new FileInputStream(three.toFile()), Zip4jSuite.srcDir.relativize(three).toString()),
                new InputStreamMeta(new FileInputStream(four.toFile()), Zip4jSuite.srcDir.relativize(four).toString()));

        ZipIt zip = ZipIt.builder().zipFile(zipFile).build();
        zip.addStream(files, parameters);

        assertThatDirectory(zipFile.getParent()).exists().hasSubDirectories(0).hasFiles(1);
        assertThatZipFile(zipFile).exists().rootEntry().hasSubDirectories(2).hasFiles(0);
        assertThatZipFile(zipFile).directory("cars/").matches(TestUtils.carsDirAssert);
        assertThatZipFile(zipFile).directory("Star Wars/").matches(TestUtils.starWarsDirAssert);
    }
}
