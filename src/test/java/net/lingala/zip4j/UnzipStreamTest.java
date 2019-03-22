package net.lingala.zip4j;

import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.CompressionLevel;
import net.lingala.zip4j.model.CompressionMethod;
import net.lingala.zip4j.model.ZipParameters;
import org.apache.commons.io.IOUtils;
import org.testng.annotations.BeforeMethod;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Oleg Cherednik
 * @since 22.03.2019
 */
public class UnzipStreamTest {

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
    public void shouldUnzipEntryToStreamWhenNoSplit() throws ZipException, IOException {
        Path zipFile = destDir.resolve("src.zip");

        ZipParameters parameters = ZipParameters.builder()
                                                .compressionMethod(CompressionMethod.DEFLATE)
                                                .compressionLevel(CompressionLevel.NORMAL).build();
        ZipIt zip = ZipIt.builder().zipFile(zipFile).build();
        zip.add(srcDir, parameters);

        assertThat(Files.exists(zipFile)).isTrue();
        assertThat(Files.isRegularFile(zipFile)).isTrue();

        // ---

        Path imgFile = resDir.resolve("bentley-continental.jpg");

        if (!Files.exists(imgFile.getParent()))
            Files.createDirectories(imgFile.getParent());

        UnzipIt unzip = UnzipIt.builder().zipFile(zipFile).build();

        try (InputStream in = unzip.extract("cars/bentley-continental.jpg");
             OutputStream out = new FileOutputStream(imgFile.toFile())) {
            IOUtils.copyLarge(in, out);
        }

        TestUtils.checkImage(imgFile, 1_395_362);
    }

//    @Test
    public void shouldUnzipEntryToStreamWhenSplit() throws ZipException, IOException {
        Path zipFile = destDir.resolve("src.zip");

        ZipParameters parameters = ZipParameters.builder()
                                                .compressionMethod(CompressionMethod.DEFLATE)
                                                .compressionLevel(CompressionLevel.NORMAL)
                                                .splitLength(1024 * 512).build();

        ZipIt zip = ZipIt.builder().zipFile(zipFile).build();
        zip.add(Collections.singletonList(srcDir.resolve("cars/bentley-continental.jpg")), parameters);

        assertThat(Files.exists(zipFile)).isTrue();
        assertThat(Files.isRegularFile(zipFile)).isTrue();

        // ---

        Path imgFile = resDir.resolve("bentley-continental.jpg");

        if (!Files.exists(imgFile.getParent()))
            Files.createDirectories(imgFile.getParent());

        UnzipIt unzip = UnzipIt.builder().zipFile(zipFile).build();

        try (InputStream in = unzip.extract("bentley-continental.jpg");
             OutputStream out = new FileOutputStream(imgFile.toFile())) {
            IOUtils.copyLarge(in, out);
        }

        TestUtils.checkImage(imgFile, 1_395_362);
    }
}
