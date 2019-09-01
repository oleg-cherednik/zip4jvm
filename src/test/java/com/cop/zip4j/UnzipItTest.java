package com.cop.zip4j;

import com.cop.zip4j.model.Compression;
import com.cop.zip4j.model.CompressionLevel;
import com.cop.zip4j.model.Encryption;
import com.cop.zip4j.model.ZipParameters;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static com.cop.zip4j.assertj.Zip4jAssertions.assertThatDirectory;
import static com.cop.zip4j.assertj.Zip4jAssertions.assertThatFile;

/**
 * @author Oleg Cherednik
 * @since 14.03.2019
 */
@SuppressWarnings("FieldNamingConvention")
public class UnzipItTest {

    private static final Path rootDir = Zip4jSuite.generateSubDirName(UnzipItTest.class);

    @BeforeClass
    public static void createDir() throws IOException {
        Files.createDirectories(rootDir);
    }

    @AfterClass(enabled = Zip4jSuite.clear)
    public static void removeDir() throws IOException {
        Zip4jSuite.removeDir(rootDir);
    }

    @Test
    public void shouldUnzipRequiredFiles() throws IOException {
        Path dstDir = Zip4jSuite.subDirNameAsMethodNameWithTme(rootDir);
        List<String> entries = Arrays.asList("saint-petersburg.jpg", "cars/bentley-continental.jpg");
        UnzipIt unzip = UnzipIt.builder().zipFile(Zip4jSuite.deflateSolidZip).build();
        unzip.extract(dstDir, entries);

        assertThatDirectory(dstDir).exists().hasSubDirectories(1).hasFiles(1);
        assertThatDirectory(dstDir.resolve("cars/")).exists().hasSubDirectories(0).hasFiles(1);
        assertThatFile(dstDir.resolve("saint-petersburg.jpg")).exists().isImage().hasSize(1_074_836);
        assertThatFile(dstDir.resolve("cars/bentley-continental.jpg")).exists().isImage().hasSize(1_395_362);
    }

    @Test
    @Ignore
    public void shouldUnzipRequiredFilesWhenSplit() throws IOException {
        Path dstDir = Zip4jSuite.subDirNameAsMethodNameWithTme(rootDir);
        List<String> entries = Arrays.asList("saint-petersburg.jpg", "cars/bentley-continental.jpg");
        UnzipIt unzip = UnzipIt.builder().zipFile(Zip4jSuite.deflateSplitZip).build();
        unzip.extract(dstDir, entries);

        assertThatDirectory(dstDir).exists().hasSubDirectories(1).hasFiles(1);
        assertThatDirectory(dstDir.resolve("cars/")).exists().hasSubDirectories(0).hasFiles(1);
        assertThatFile(dstDir.resolve("saint-petersburg.jpg")).exists().isImage().hasSize(1_074_836);
        assertThatFile(dstDir.resolve("cars/bentley-continental.jpg")).exists().isImage().hasSize(1_395_362);
    }

    @Test
    public void shouldUnzipOneFile() throws IOException {
        Path dstDir = Zip4jSuite.subDirNameAsMethodNameWithTme(rootDir);
        UnzipIt unzip = UnzipIt.builder().zipFile(Zip4jSuite.deflateSolidZip).build();
        unzip.extract(dstDir, "cars/ferrari-458-italia.jpg");

        assertThatDirectory(dstDir).exists().hasSubDirectories(1).hasFiles(0);
        assertThatDirectory(dstDir.resolve("cars/")).exists().hasSubDirectories(0).hasFiles(1);
        assertThatFile(dstDir.resolve("cars/ferrari-458-italia.jpg")).exists().isImage().hasSize(320_894);
    }

    @Test
    public void shouldUnzipFolder() throws IOException {
        Path dstDir = Zip4jSuite.subDirNameAsMethodNameWithTme(rootDir);
        UnzipIt unzip = UnzipIt.builder().zipFile(Zip4jSuite.deflateSolidZip).build();
        unzip.extract(dstDir, "Star Wars");

        assertThatDirectory(dstDir).exists().hasSubDirectories(1).hasFiles(0);

        Path starWarsDir = dstDir.resolve("Star Wars/");
        assertThatDirectory(starWarsDir).exists().hasSubDirectories(0).hasFiles(4);
        assertThatFile(starWarsDir.resolve("one.jpg")).isImage().hasSize(2_204_448);
        assertThatFile(starWarsDir.resolve("two.jpg")).isImage().hasSize(277_857);
        assertThatFile(starWarsDir.resolve("three.jpg")).isImage().hasSize(1_601_879);
        assertThatFile(starWarsDir.resolve("four.jpg")).isImage().hasSize(1_916_776);
    }

    @Test
    public void shouldUnzipEncryptedZip() throws IOException {
        ZipParameters parameters = ZipParameters.builder()
                                                .compression(Compression.DEFLATE)
                                                .compressionLevel(CompressionLevel.NORMAL)
                                                .encryption(Encryption.PKWARE)
                                                .comment("password: " + new String(Zip4jSuite.password))
                                                .password(Zip4jSuite.password).build();

        Path dstDir = Zip4jSuite.subDirNameAsMethodNameWithTme(rootDir);
        Path zipFile = dstDir.resolve("src.zip");
        ZipIt zip = ZipIt.builder().zipFile(zipFile).build();
        zip.add(Zip4jSuite.srcDir, parameters);

        dstDir = dstDir.resolve("unzip");

        UnzipIt unzip = UnzipIt.builder()
                               .zipFile(zipFile)
                               .password(Zip4jSuite.password)
                               .build();

        unzip.extract(dstDir);
        assertThatDirectory(dstDir).matches(TestUtils.dirAssert);
    }
}
