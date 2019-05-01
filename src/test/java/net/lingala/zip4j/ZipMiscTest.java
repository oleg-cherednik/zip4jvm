package net.lingala.zip4j;

import net.lingala.zip4j.model.CompressionLevel;
import net.lingala.zip4j.model.CompressionMethod;
import net.lingala.zip4j.model.ZipParameters;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static net.lingala.zip4j.assertj.Zip4jAssertions.assertThatDirectory;
import static net.lingala.zip4j.assertj.Zip4jAssertions.assertThatZipFile;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Oleg Cherednik
 * @since 15.03.2019
 */
@Test
@SuppressWarnings("FieldNamingConvention")
public class ZipMiscTest {

    private static final Path rootDir = Zip4jSuite.rootDir.resolve(ZipMiscTest.class.getSimpleName());

    @BeforeClass
    public static void createDir() throws IOException {
        Files.createDirectories(rootDir);
    }

    @AfterClass(enabled = Zip4jSuite.clear)
    public static void removeDir() throws IOException {
        Zip4jSuite.removeDir(rootDir);
    }

    public void shouldRetrieveAllEntryNamesForExistedZip() throws IOException {
        ZipMisc misc = ZipMisc.builder().zipFile(Zip4jSuite.noSplitZip).build();
        assertThat(misc.getEntryNames()).hasSize(15);
    }

    public void shouldRetrieveAllEntryNamesForExistedEncryptedZip() throws IOException {
        Path zipFile = Zip4jSuite.copy(rootDir, Zip4jSuite.noSplitAesZip);

        ZipMisc misc = ZipMisc.builder().zipFile(zipFile).build();
        assertThat(misc.isEncrypted()).isTrue();
        assertThat(misc.getEntryNames()).hasSize(15);
    }

    public void shouldRetrieveSingleFileWhenNoSplitZip() throws IOException {
        ZipMisc misc = ZipMisc.builder().zipFile(Zip4jSuite.noSplitZip).build();
        assertThat(misc.getFiles()).hasSize(1);
    }

    public void shouldRetrieveMultipleFilesWhenSplitZip() throws IOException {
        Path rootDir = Zip4jSuite.generateSubDirName(ZipMiscTest.rootDir, "shouldRetrieveMultipleFilesWhenSplitZip");

        ZipParameters parameters = ZipParameters.builder()
                                                .compressionMethod(CompressionMethod.DEFLATE)
                                                .compressionLevel(CompressionLevel.NORMAL)
                                                .splitLength(1024 * 1024).build();

        Path zipFile = rootDir.resolve("src.zip");
        ZipIt zip = ZipIt.builder().zipFile(zipFile).build();
        zip.add(Zip4jSuite.srcDir, parameters);
        assertThatDirectory(zipFile.getParent()).exists().hasSubDirectories(0).hasFiles(10);

        ZipMisc misc = ZipMisc.builder().zipFile(zipFile).build();
        List<Path> files = misc.getFiles();

        assertThat(files).hasSize(10);
        assertThat(files.get(0).getFileName().toString()).isEqualTo("src.zip");
        assertThat(files.get(1).getFileName().toString()).isEqualTo("src.z01");
        assertThat(files.get(2).getFileName().toString()).isEqualTo("src.z02");
        assertThat(files.get(3).getFileName().toString()).isEqualTo("src.z03");
        assertThat(files.get(4).getFileName().toString()).isEqualTo("src.z04");
        assertThat(files.get(5).getFileName().toString()).isEqualTo("src.z05");
        assertThat(files.get(6).getFileName().toString()).isEqualTo("src.z06");
        assertThat(files.get(7).getFileName().toString()).isEqualTo("src.z07");
        assertThat(files.get(8).getFileName().toString()).isEqualTo("src.z08");
        assertThat(files.get(9).getFileName().toString()).isEqualTo("src.z09");
    }

    public void shouldMergeSplitZip() throws IOException {
        ZipMisc misc = ZipMisc.builder().zipFile(Zip4jSuite.splitZip).build();
        assertThat(misc.isSplit()).isTrue();

        Path mergeDir = Zip4jSuite.generateSubDirName(rootDir, "shouldMergeSplitZip");
        Path mergeZipFle = mergeDir.resolve("src.zip");
        misc.merge(mergeZipFle);

        assertThatDirectory(mergeDir).exists().hasSubDirectories(0).hasFiles(1);
        assertThatZipFile(mergeZipFle).exists().rootEntry().matches(TestUtils.zipRootDirAssert);
    }
}
