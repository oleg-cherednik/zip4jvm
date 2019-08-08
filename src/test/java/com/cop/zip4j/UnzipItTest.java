package com.cop.zip4j;

import com.cop.zip4j.exception.Zip4jException;
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
    public void shouldUnzipRequiredFiles() throws Zip4jException, IOException {
        Path destDir = Zip4jSuite.subDirNameAsMethodNameWithTme(rootDir);
        List<String> entries = Arrays.asList("saint-petersburg.jpg", "cars/bentley-continental.jpg");
        UnzipIt unzip = UnzipIt.builder().zipFile(Zip4jSuite.deflateSolidZip).build();
        unzip.extract(destDir, entries);

        assertThatDirectory(destDir).exists().hasSubDirectories(1).hasFiles(1);
        assertThatDirectory(destDir.resolve("cars/")).exists().hasSubDirectories(0).hasFiles(1);
        assertThatFile(destDir.resolve("saint-petersburg.jpg")).exists().isImage().hasSize(1_074_836);
        assertThatFile(destDir.resolve("cars/bentley-continental.jpg")).exists().isImage().hasSize(1_395_362);
    }

    @Test
    @Ignore
    public void shouldUnzipRequiredFilesWhenSplit() throws Zip4jException, IOException {
        Path destDir = Zip4jSuite.subDirNameAsMethodNameWithTme(rootDir);
        List<String> entries = Arrays.asList("saint-petersburg.jpg", "cars/bentley-continental.jpg");
        UnzipIt unzip = UnzipIt.builder().zipFile(Zip4jSuite.deflateSplitZip).build();
        unzip.extract(destDir, entries);

        assertThatDirectory(destDir).exists().hasSubDirectories(1).hasFiles(1);
        assertThatDirectory(destDir.resolve("cars/")).exists().hasSubDirectories(0).hasFiles(1);
        assertThatFile(destDir.resolve("saint-petersburg.jpg")).exists().isImage().hasSize(1_074_836);
        assertThatFile(destDir.resolve("cars/bentley-continental.jpg")).exists().isImage().hasSize(1_395_362);
    }

    @Test
    public void shouldUnzipOneFile() throws Zip4jException, IOException {
        Path destDir = Zip4jSuite.subDirNameAsMethodNameWithTme(rootDir);
        UnzipIt unzip = UnzipIt.builder().zipFile(Zip4jSuite.deflateSolidZip).build();
        unzip.extract(destDir, "cars/ferrari-458-italia.jpg");

        assertThatDirectory(destDir).exists().hasSubDirectories(1).hasFiles(0);
        assertThatDirectory(destDir.resolve("cars/")).exists().hasSubDirectories(0).hasFiles(1);
        assertThatFile(destDir.resolve("cars/ferrari-458-italia.jpg")).exists().isImage().hasSize(320_894);
    }

    @Test
    public void shouldUnzipFolder() throws Zip4jException, IOException {
        Path destDir = Zip4jSuite.subDirNameAsMethodNameWithTme(rootDir);
        UnzipIt unzip = UnzipIt.builder().zipFile(Zip4jSuite.deflateSolidZip).build();
        unzip.extract(destDir, "Star Wars");

        assertThatDirectory(destDir).exists().hasSubDirectories(1).hasFiles(0);

        Path starWarsDir = destDir.resolve("Star Wars/");
        assertThatDirectory(starWarsDir).exists().hasSubDirectories(0).hasFiles(4);
        assertThatFile(starWarsDir.resolve("one.jpg")).isImage().hasSize(2_204_448);
        assertThatFile(starWarsDir.resolve("two.jpg")).isImage().hasSize(277_857);
        assertThatFile(starWarsDir.resolve("three.jpg")).isImage().hasSize(1_601_879);
        assertThatFile(starWarsDir.resolve("four.jpg")).isImage().hasSize(1_916_776);
    }

    @Test
    public void shouldUnzipEncryptedZip() throws IOException {
        ZipParameters parameters = ZipParameters.builder()
                                                .compressionMethod(Compression.DEFLATE)
                                                .compressionLevel(CompressionLevel.NORMAL)
                                                .encryption(Encryption.PKWARE)
                                                .comment("password: " + new String(Zip4jSuite.password))
                                                .password(Zip4jSuite.password).build();

        Path destDir = Zip4jSuite.subDirNameAsMethodNameWithTme(rootDir);
        Path zipFile = destDir.resolve("src.zip");
        ZipIt zip = ZipIt.builder().zipFile(zipFile).build();
        zip.add(Zip4jSuite.srcDir, parameters);

        destDir = destDir.resolve("unzip");

        UnzipIt unzip = UnzipIt.builder()
                               .zipFile(zipFile)
                               .password(Zip4jSuite.password)
                               .build();

        unzip.extract(destDir);
        assertThatDirectory(destDir).matches(TestUtils.dirAssert);
    }
}
