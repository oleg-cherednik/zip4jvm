package net.lingala.zip4j;

import net.lingala.zip4j.exception.ZipException;
import org.apache.commons.io.IOUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static net.lingala.zip4j.assertj.Zip4jAssertions.assertThatFile;

/**
 * @author Oleg Cherednik
 * @since 22.03.2019
 */
@SuppressWarnings("FieldNamingConvention")
public class UnzipStreamTest {

    private static final Path rootDir = Zip4jSuite.rootDir.resolve(UnzipStreamTest.class.getSimpleName());

    @BeforeClass
    public static void createDir() throws IOException {
        Files.createDirectories(rootDir);
    }

    @AfterClass(enabled = Zip4jSuite.clear)
    public static void removeDir() throws IOException {
        Zip4jSuite.removeDir(rootDir);
    }

    @Test
    public void shouldUnzipEntryToStreamWhenNoSplit() throws ZipException, IOException {
        Path imgFile = rootDir.resolve("bentley-continental.jpg");
        UnzipIt unzip = UnzipIt.builder().zipFile(Zip4jSuite.noSplitZip).build();

        try (InputStream in = unzip.extract("cars/bentley-continental.jpg");
             OutputStream out = new FileOutputStream(imgFile.toFile())) {
            IOUtils.copyLarge(in, out);
        }

        assertThatFile(imgFile).exists().isImage().hasSize(1_395_362);
    }

    @Test
    public void shouldUnzipEntryToStreamWhenSplit() throws ZipException, IOException {
        Path imgFile = rootDir.resolve("ferrari-458-italia.jpg");
        UnzipIt unzip = UnzipIt.builder().zipFile(Zip4jSuite.splitZip).build();

        try (InputStream in = unzip.extract("cars/ferrari-458-italia.jpg");
             OutputStream out = new FileOutputStream(imgFile.toFile())) {
            IOUtils.copyLarge(in, out);
        }

        assertThatFile(imgFile).exists().isImage().hasSize(320_894);
    }
}
