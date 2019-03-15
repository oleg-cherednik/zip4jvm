package net.lingala.zip4j;

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

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Oleg Cherednik
 * @since 15.03.2019
 */
public class ZipCommentTest {

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
    public void shouldAddCommentToExistedNoSplitZip() throws ZipException, IOException {
        Path zipFile = destDir.resolve("src.zip");
        Path carsDir = srcDir.resolve("cars");

        ZipParameters parameters = ZipParameters.builder()
                                                .compressionMethod(CompressionMethod.DEFLATE)
                                                .compressionLevel(CompressionLevel.NORMAL).build();
        ZipIt zip = ZipIt.builder().zipFile(zipFile).build();
        zip.add(carsDir, parameters);

        assertThat(Files.exists(zipFile)).isTrue();
        assertThat(Files.isRegularFile(zipFile)).isTrue();
        TestUtils.checkDestinationDir(1, destDir);

        // ---

        ZipFileNew zipUtil = ZipFileNew.builder().zipFile(zipFile).build();
        assertThat(zipUtil.getComment()).isNull();

        zipUtil.setComment("Oleg Cherednik - Олег Чередник");
        assertThat(zipUtil.getComment()).isEqualTo("Oleg Cherednik - Олег Чередник");
    }

}
