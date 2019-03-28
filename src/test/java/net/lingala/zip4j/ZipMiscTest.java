package net.lingala.zip4j;

import net.lingala.zip4j.model.AESStrength;
import net.lingala.zip4j.model.CompressionLevel;
import net.lingala.zip4j.model.CompressionMethod;
import net.lingala.zip4j.model.Encryption;
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
@SuppressWarnings("FieldNamingConvention")
public class ZipMiscTest {

    private static final Path rootDir = Zip4jSuite.rootDir.resolve(ZipMiscTest.class.getSimpleName());
    private static final Path zipFile = rootDir.resolve("src.zip");

    @BeforeClass
    public static void createDir() throws IOException {
        Files.createDirectories(rootDir);
    }

    @AfterClass(enabled = Zip4jSuite.clear)
    public static void removeDir() throws IOException {
        Zip4jSuite.removeDir(rootDir);
    }

    @Test
    public void shouldRetrieveAllEntryNamesForExistedZip() throws IOException {
        ZipParameters parameters = ZipParameters.builder()
                                                .compressionMethod(CompressionMethod.DEFLATE)
                                                .compressionLevel(CompressionLevel.NORMAL).build();

        ZipIt zip = ZipIt.builder().zipFile(zipFile).build();
        zip.add(Zip4jSuite.srcDir, parameters);

        ZipMisc misc = ZipMisc.builder().zipFile(zipFile).build();
        assertThat(misc.getEntryNames()).hasSize(15);
    }

    @Test(dependsOnMethods = "shouldRetrieveAllEntryNamesForExistedZip")
    public void shouldRetrieveAllEntryNamesForExistedEncryptedZip() throws IOException {
        Files.deleteIfExists(zipFile);

        ZipParameters parameters = ZipParameters.builder()
                                                .compressionMethod(CompressionMethod.DEFLATE)
                                                .compressionLevel(CompressionLevel.NORMAL)
                                                .encryption(Encryption.STANDARD)
                                                .aesKeyStrength(AESStrength.STRENGTH_256)
                                                .password("1".toCharArray())
                                                .defaultFolderPath(Zip4jSuite.srcDir).build();

        ZipIt zip = ZipIt.builder().zipFile(zipFile).build();
        zip.add(Zip4jSuite.srcDir, parameters);

        ZipMisc misc = ZipMisc.builder().zipFile(zipFile).build();
        assertThat(misc.isEncrypted()).isTrue();
        assertThat(misc.getEntryNames()).hasSize(15);
    }

    @Test(dependsOnMethods = "shouldRetrieveAllEntryNamesForExistedEncryptedZip")
    public void shouldRetrieveSingleFileWhenNoSplitZip() throws IOException {
        assertThatZipFile(zipFile).exists();

        ZipMisc misc = ZipMisc.builder().zipFile(zipFile).build();
        assertThat(misc.getFiles()).hasSize(1);
    }

    @Test(dependsOnMethods = "shouldRetrieveSingleFileWhenNoSplitZip")
    public void shouldRetrieveMultipleFilesWhenSplitZip() throws IOException {
        Files.deleteIfExists(zipFile);

        ZipParameters parameters = ZipParameters.builder()
                                                .compressionMethod(CompressionMethod.DEFLATE)
                                                .compressionLevel(CompressionLevel.NORMAL)
                                                .splitLength(1024 * 1024).build();

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

    @Test(dependsOnMethods = "shouldRetrieveMultipleFilesWhenSplitZip")
    public void shouldMergeSplitZip() throws IOException {
        assertThatZipFile(zipFile).exists();

        ZipMisc misc = ZipMisc.builder().zipFile(zipFile).build();
        assertThat(misc.isSplit()).isTrue();

        Path mergeDir = rootDir.resolve("merge");
        Path mergeZipFle = mergeDir.resolve("src.zip");
        // TODO temporary, should be created automatically
        Files.createDirectories(mergeDir);
        misc.merge(mergeZipFle);

        assertThatDirectory(mergeDir).exists().hasSubDirectories(0).hasFiles(1);
        assertThatZipFile(mergeZipFle).exists().directory("/").matches(TestUtils.rootDirAssert);
    }
}
