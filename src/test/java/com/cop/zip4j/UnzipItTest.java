package com.cop.zip4j;

import com.cop.zip4j.exception.ZipException;
import com.cop.zip4j.model.CompressionLevel;
import com.cop.zip4j.model.CompressionMethod;
import com.cop.zip4j.model.Encryption;
import com.cop.zip4j.model.ZipParameters;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
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
    public void shouldUnzipRequiredFiles() throws ZipException, IOException {
        Path destDir = Zip4jSuite.subDirNameAsMethodName(rootDir);
        List<String> entries = Arrays.asList("saint-petersburg.jpg", "cars/bentley-continental.jpg");
        UnzipIt unzip = UnzipIt.builder().zipFile(Zip4jSuite.noSplitZip).build();
        unzip.extract(destDir, entries);

        assertThatDirectory(destDir).exists().hasSubDirectories(1).hasFiles(1);
        assertThatDirectory(destDir.resolve("cars/")).exists().hasSubDirectories(0).hasFiles(1);
        assertThatFile(destDir.resolve("saint-petersburg.jpg")).exists().isImage().hasSize(1_074_836);
        assertThatFile(destDir.resolve("cars/bentley-continental.jpg")).exists().isImage().hasSize(1_395_362);
    }

    @Test
    public void shouldUnzipRequiredFilesWhenSplit() throws ZipException, IOException {
        Path destDir = Zip4jSuite.subDirNameAsMethodName(rootDir);
        List<String> entries = Arrays.asList("saint-petersburg.jpg", "cars/bentley-continental.jpg");
        UnzipIt unzip = UnzipIt.builder().zipFile(Zip4jSuite.splitZip).build();
        unzip.extract(destDir, entries);

        assertThatDirectory(destDir).exists().hasSubDirectories(1).hasFiles(1);
        assertThatDirectory(destDir.resolve("cars/")).exists().hasSubDirectories(0).hasFiles(1);
        assertThatFile(destDir.resolve("saint-petersburg.jpg")).exists().isImage().hasSize(1_074_836);
        assertThatFile(destDir.resolve("cars/bentley-continental.jpg")).exists().isImage().hasSize(1_395_362);
    }

    @Test
    public void shouldUnzipOneFile() throws ZipException, IOException {
        Path destDir = Zip4jSuite.subDirNameAsMethodName(rootDir);
        UnzipIt unzip = UnzipIt.builder().zipFile(Zip4jSuite.noSplitZip).build();
        unzip.extract(destDir, "cars/ferrari-458-italia.jpg");

        assertThatDirectory(destDir).exists().hasSubDirectories(1).hasFiles(0);
        assertThatDirectory(destDir.resolve("cars/")).exists().hasSubDirectories(0).hasFiles(1);
        assertThatFile(destDir.resolve("cars/ferrari-458-italia.jpg")).exists().isImage().hasSize(320_894);
    }

    @Test
    public void shouldUnzipFolder() throws ZipException, IOException {
        Path destDir = Zip4jSuite.subDirNameAsMethodName(rootDir);
        UnzipIt unzip = UnzipIt.builder().zipFile(Zip4jSuite.noSplitZip).build();
        unzip.extract(destDir, "Star Wars");

        assertThatDirectory(destDir).exists().hasSubDirectories(1).hasFiles(0);

        Path starWarsDir = destDir.resolve("Star Wars/");
        assertThatDirectory(starWarsDir).exists().hasSubDirectories(0).hasFiles(4);
        assertThatFile(starWarsDir.resolve("0qQnv2v.jpg")).isImage().hasSize(2_204_448);
        assertThatFile(starWarsDir.resolve("080fc325efa248454e59b84be24ea829.jpg")).isImage().hasSize(277_857);
        assertThatFile(starWarsDir.resolve("pE9Hkw6.jpg")).isImage().hasSize(1_601_879);
        assertThatFile(starWarsDir.resolve("star-wars-wallpapers-29931-7188436.jpg")).isImage().hasSize(1_916_776);
    }

    @Test
    public void shouldUnzipEncryptedZip() throws IOException {
        ZipParameters parameters = ZipParameters.builder()
                                                .compressionMethod(CompressionMethod.DEFLATE)
                                                .compressionLevel(CompressionLevel.NORMAL)
                                                .encryption(Encryption.PKWARE)
                                                .comment("password: " + new String(Zip4jSuite.password))
                                                .password(Zip4jSuite.password).build();

        Path destDir = Zip4jSuite.subDirNameAsMethodName(rootDir);
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
