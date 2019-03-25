package net.lingala.zip4j;

import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.CompressionLevel;
import net.lingala.zip4j.model.CompressionMethod;
import net.lingala.zip4j.model.ZipParameters;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipFile;

import static net.lingala.zip4j.assertj.Zip4jAssertions.assertThat;

/**
 * @author Oleg Cherednik
 * @since 14.03.2019
 */
@SuppressWarnings("FieldNamingConvention")
public class ZipFolderNoSplitTest {

    private static final Path rootDir = Zip4jSuite.rootDir.resolve(ZipFolderNoSplitTest.class.getSimpleName());
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
    public void shouldCreateNewZipWithFolder() throws ZipException, IOException {
        ZipParameters parameters = ZipParameters.builder()
                                                .compressionMethod(CompressionMethod.DEFLATE)
                                                .compressionLevel(CompressionLevel.NORMAL)
                                                .defaultFolderPath(Zip4jSuite.srcDir).build();

        ZipIt zipIt = ZipIt.builder().zipFile(zipFile).build();
        zipIt.add(Zip4jSuite.carsDir, parameters);

        assertThat(Files.exists(zipFile)).isTrue();
        assertThat(Files.isRegularFile(zipFile)).isTrue();

        ZipFile zip = new ZipFile(zipFile.toFile());
        TestUtils.checkDestinationDir(1, zipFile.getParent());

        assertThat(zip).rootEntry().hasSubDirectories(1).hasFiles(0);
        TestUtils.checkCarsDirectory(zip, "cars/");
    }

    //    @Test(dependsOnMethods = "shouldCreateNewZipWithFolder")
//    public void shouldAddFolderToExistedZip() throws ZipException, IOException {
//        Path zipFile = destDir.resolve("src.zip");
//        Path starWarsDir = srcDir.resolve("Star Wars");
//
//        assertThat(Files.exists(zipFile)).isTrue();
//        assertThat(Files.isRegularFile(zipFile)).isTrue();
//
//        ZipParameters parameters = ZipParameters.builder()
//                                                .compressionMethod(CompressionMethod.DEFLATE)
//                                                .compressionLevel(CompressionLevel.NORMAL)
//                                                .defaultFolderPath(srcDir).build();
//        ZipIt zip = ZipIt.builder().zipFile(zipFile).build();
//        zip.add(starWarsDir, parameters);
//
//        assertThat(Files.exists(zipFile)).isTrue();
//        assertThat(Files.isRegularFile(zipFile)).isTrue();
//
//        // ---
//
//        UnzipIt unzip = UnzipIt.builder().zipFile(zipFile).build();
//        unzip.extract(resDir);
//
//        TestUtils.checkCarsDirectory(resDir.resolve("cars"));
//        TestUtils.checkStarWarsDirectory(resDir.resolve("Star Wars"));
//    }

    //    @Test(dependsOnMethods = "shouldAddFolderToExistedZip")
//    public void shouldAddEmptyDirectoryToExistedZip() throws ZipException, IOException {
//        Path zipFile = destDir.resolve("src.zip");
//        Path emptyDir = srcDir.resolve("empty_dir");
//
//        assertThat(Files.exists(zipFile)).isTrue();
//        assertThat(Files.isRegularFile(zipFile)).isTrue();
//
//        ZipParameters parameters = ZipParameters.builder()
//                                                .compressionMethod(CompressionMethod.DEFLATE)
//                                                .compressionLevel(CompressionLevel.NORMAL)
//                                                .defaultFolderPath(srcDir).build();
//        ZipIt zip = ZipIt.builder().zipFile(zipFile).build();
//        zip.add(emptyDir, parameters);
//
//        assertThat(Files.exists(zipFile)).isTrue();
//        assertThat(Files.isRegularFile(zipFile)).isTrue();
//
//        // ---
//
//        UnzipIt unzip = UnzipIt.builder().zipFile(zipFile).build();
//        unzip.extract(resDir);
//
//        TestUtils.checkCarsDirectory(resDir.resolve("cars"));
//        TestUtils.checkStarWarsDirectory(resDir.resolve("Star Wars"));
//        TestUtils.checkEmptyDirectory(resDir.resolve("empty_dir"));
//    }

}
