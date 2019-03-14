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

import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author Oleg Cherednik
 * @since 14.03.2019
 */
public class ZipFolderSplitTest {

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
    public void shouldCreateNewZipWithFolder() throws ZipException, IOException {
        Path zipFile = destDir.resolve("src.zip");

        ZipParameters parameters = ZipParameters.builder()
                                                .compressionMethod(CompressionMethod.DEFLATE)
                                                .compressionLevel(CompressionLevel.NORMAL)
                                                .splitLength(1024 * 1024).build();
        ZipIt zip = ZipIt.builder().zipFile(zipFile).build();
        zip.add(srcDir, parameters);

        TestUtils.checkDestinationDir(10, destDir);

        // ---

        new ZipFileUnzip(zipFile).extract(resDir, new UnzipParameters());
        TestUtils.checkResultDir(resDir);
    }

    @Test(dependsOnMethods = "shouldCreateNewZipWithFolder")
    public void shouldThrowExceptionWhenModifySplitZip() {
        Path zipFile = destDir.resolve("src.zip");
        Path carsDir = srcDir.resolve("cars");

        ZipParameters parameters = ZipParameters.builder()
                                                .compressionMethod(CompressionMethod.DEFLATE)
                                                .compressionLevel(CompressionLevel.NORMAL)
                                                .defaultFolderPath(srcDir)
                                                .splitLength(1024 * 1024).build();
        ZipIt zip = ZipIt.builder().zipFile(zipFile).build();
        assertThatThrownBy(() -> zip.add(carsDir, parameters)).isExactlyInstanceOf(ZipException.class);
    }
}
