package net.lingala.zip4j;

import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.CompressionLevel;
import net.lingala.zip4j.model.CompressionMethod;
import net.lingala.zip4j.model.ZipParameters;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author Oleg Cherednik
 * @since 06.04.2019
 */
@SuppressWarnings({ "FieldNamingConvention", "NewClassNamingConvention" })
public class Zip64Test {

    private static final Path rootDir = Zip4jSuite.rootDir.resolve(Zip64Test.class.getSimpleName());
    private static final Path zipFile = rootDir.resolve("src.zip");

    @BeforeClass
    public static void createDir() throws IOException {
        Zip4jSuite.removeDir(rootDir);
        Files.createDirectories(rootDir);
    }

    @AfterClass(enabled = Zip4jSuite.clear)
    public static void removeDir() throws IOException {
        Zip4jSuite.removeDir(rootDir);
    }

//    @Test
    public void shouldCreateNewZipWithZip64() throws IOException, ZipException {
        ZipParameters parameters = ZipParameters.builder()
                                                .compressionMethod(CompressionMethod.DEFLATE)
                                                .compressionLevel(CompressionLevel.NORMAL)
//                                                .defaultFolderPath(Zip4jSuite.srcDir)
                                                .zip64(false).build();

        ZipIt zip = ZipIt.builder().zipFile(zipFile).build();
        zip.add(Paths.get("d:/zip4j/ferdinand.mkv"), parameters);

        int a = 0;
        a++;

//        assertThatDirectory(zipFile.getParent()).exists().hasSubDirectories(0).hasFiles(1);
//        assertThatZipFile(zipFile).exists().rootEntry().hasSubDirectories(1).hasFiles(0);
//        assertThatZipFile(zipFile).directory("cars/").matches(TestUtils.zipCarsDirAssert);
    }

//    @Test
    public void shouldReadZip64() throws IOException {
        Path zipFile = Paths.get("d:/zip4j/ferdinand.zip");
        UnzipIt unzip = UnzipIt.builder().zipFile(zipFile).build();
        unzip.extract(rootDir.resolve("unzip"));
    }

//    @Test
    public void shouldReadZip64Split() throws IOException {
        Path zipFile = Paths.get("d:/zip4j/zip64split/ferdinand.zip");
        UnzipIt unzip = UnzipIt.builder().zipFile(zipFile).build();
        unzip.extract(rootDir.resolve("unzip"));
    }

//    @Test
    public void shouldReadZip64SplitMulti() throws IOException {
        Path zipFile = Paths.get("d:/zip4j/zip64split_multi/ferdinand.zip");
        UnzipIt unzip = UnzipIt.builder().zipFile(zipFile).build();
        unzip.extract(rootDir.resolve("unzip"));
    }
}
