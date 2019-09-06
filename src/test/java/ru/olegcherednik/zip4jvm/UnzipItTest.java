package ru.olegcherednik.zip4jvm;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;
import ru.olegcherednik.zip4jvm.assertj.Zip4jAssertions;
import ru.olegcherednik.zip4jvm.model.Compression;
import ru.olegcherednik.zip4jvm.model.CompressionLevel;
import ru.olegcherednik.zip4jvm.model.Encryption;
import ru.olegcherednik.zip4jvm.model.ZipEntrySettings;
import ru.olegcherednik.zip4jvm.model.ZipFileSettings;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

/**
 * @author Oleg Cherednik
 * @since 14.03.2019
 */
@Test
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

    public void shouldUnzipRequiredFiles() throws IOException {
        Path dstDir = Zip4jSuite.subDirNameAsMethodNameWithTme(rootDir);
        List<String> entries = Arrays.asList("saint-petersburg.jpg", "cars/bentley-continental.jpg");
        UnzipIt unzip = UnzipIt.builder().zipFile(Zip4jSuite.deflateSolidZip).build();
        unzip.extract(dstDir, entries);

        Zip4jAssertions.assertThatDirectory(dstDir).exists().hasSubDirectories(1).hasFiles(1);
        Zip4jAssertions.assertThatDirectory(dstDir.resolve("cars/")).exists().hasSubDirectories(0).hasFiles(1);
        Zip4jAssertions.assertThatFile(dstDir.resolve("saint-petersburg.jpg")).exists().isImage().hasSize(1_074_836);
        Zip4jAssertions.assertThatFile(dstDir.resolve("cars/bentley-continental.jpg")).exists().isImage().hasSize(1_395_362);
    }

    @Test
    @Ignore
    public void shouldUnzipRequiredFilesWhenSplit() throws IOException {
        Path dstDir = Zip4jSuite.subDirNameAsMethodNameWithTme(rootDir);
        List<String> entries = Arrays.asList("saint-petersburg.jpg", "cars/bentley-continental.jpg");
        UnzipIt unzip = UnzipIt.builder().zipFile(Zip4jSuite.deflateSplitZip).build();
        unzip.extract(dstDir, entries);

        Zip4jAssertions.assertThatDirectory(dstDir).exists().hasSubDirectories(1).hasFiles(1);
        Zip4jAssertions.assertThatDirectory(dstDir.resolve("cars/")).exists().hasSubDirectories(0).hasFiles(1);
        Zip4jAssertions.assertThatFile(dstDir.resolve("saint-petersburg.jpg")).exists().isImage().hasSize(1_074_836);
        Zip4jAssertions.assertThatFile(dstDir.resolve("cars/bentley-continental.jpg")).exists().isImage().hasSize(1_395_362);
    }

    public void shouldUnzipOneFile() throws IOException {
        Path dstDir = Zip4jSuite.subDirNameAsMethodNameWithTme(rootDir);
        UnzipIt unzip = UnzipIt.builder().zipFile(Zip4jSuite.deflateSolidZip).build();
        unzip.extract(dstDir, "cars/ferrari-458-italia.jpg");

        Zip4jAssertions.assertThatDirectory(dstDir).exists().hasSubDirectories(1).hasFiles(0);
        Zip4jAssertions.assertThatDirectory(dstDir.resolve("cars/")).exists().hasSubDirectories(0).hasFiles(1);
        Zip4jAssertions.assertThatFile(dstDir.resolve("cars/ferrari-458-italia.jpg")).exists().isImage().hasSize(320_894);
    }

    public void shouldUnzipFolder() throws IOException {
        Path dstDir = Zip4jSuite.subDirNameAsMethodNameWithTme(rootDir);
        UnzipIt unzip = UnzipIt.builder().zipFile(Zip4jSuite.deflateSolidZip).build();
        unzip.extract(dstDir, "Star Wars");

        Zip4jAssertions.assertThatDirectory(dstDir).exists().hasSubDirectories(1).hasFiles(0);

        Path starWarsDir = dstDir.resolve("Star Wars/");
        Zip4jAssertions.assertThatDirectory(starWarsDir).exists().hasSubDirectories(0).hasFiles(4);
        Zip4jAssertions.assertThatFile(starWarsDir.resolve("one.jpg")).isImage().hasSize(2_204_448);
        Zip4jAssertions.assertThatFile(starWarsDir.resolve("two.jpg")).isImage().hasSize(277_857);
        Zip4jAssertions.assertThatFile(starWarsDir.resolve("three.jpg")).isImage().hasSize(1_601_879);
        Zip4jAssertions.assertThatFile(starWarsDir.resolve("four.jpg")).isImage().hasSize(1_916_776);
    }

    public void shouldUnzipEncryptedZip() throws IOException {
        ZipFileSettings settings = ZipFileSettings.builder()
                                                  .entrySettings(
                                                          ZipEntrySettings.builder()
                                                                          .compression(Compression.DEFLATE, CompressionLevel.NORMAL)
                                                                          .encryption(Encryption.PKWARE, Zip4jSuite.password).build())
                                                  .comment("password: " + new String(Zip4jSuite.password)).build();

        Path zip = Zip4jSuite.subDirNameAsMethodName(rootDir).resolve("src.zip");
        ZipIt.add(zip, Zip4jSuite.contentSrcDir, settings);

        Path destDir = zip.getParent().resolve("unzip");
        UnzipIt unzip = UnzipIt.builder()
                               .zipFile(zip)
                               .password(Zip4jSuite.password)
                               .build();

        unzip.extract(destDir);
        Zip4jAssertions.assertThatDirectory(destDir).matches(TestUtils.dirAssert);
    }
}
