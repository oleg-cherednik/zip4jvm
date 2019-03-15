package net.lingala.zip4j;

import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.AESStrength;
import net.lingala.zip4j.model.CompressionLevel;
import net.lingala.zip4j.model.CompressionMethod;
import net.lingala.zip4j.model.Encryption;
import net.lingala.zip4j.model.ZipParameters;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Oleg Cherednik
 * @since 15.03.2019
 */
public class ZipMiscTest {

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
    public void shouldRetrieveAllEntryNamesForExistedZip() throws ZipException, IOException {
        Path zipFile = destDir.resolve("src.zip");

        ZipParameters parameters = ZipParameters.builder()
                                                .compressionMethod(CompressionMethod.DEFLATE)
                                                .compressionLevel(CompressionLevel.NORMAL).build();
        ZipIt zip = ZipIt.builder().zipFile(zipFile).build();
        zip.add(srcDir, parameters);

        ZipMisc misc = ZipMisc.builder().zipFile(zipFile).build();
        assertThat(misc.getEntryNames()).hasSize(15);
    }

    @Test(dependsOnMethods = "shouldRetrieveAllEntryNamesForExistedZip")
    public void shouldRetrieveAllEntryNamesForExistedEncryptedZip() throws ZipException, IOException {
        Path zipFile = destDir.resolve("src.zip");
        Files.deleteIfExists(zipFile);

        ZipParameters parameters = ZipParameters.builder()
                                                .compressionMethod(CompressionMethod.DEFLATE)
                                                .compressionLevel(CompressionLevel.NORMAL)
                                                .encryption(Encryption.STANDARD)
                                                .aesKeyStrength(AESStrength.STRENGTH_256)
                                                .password("1".toCharArray())
                                                .defaultFolderPath(srcDir).build();

        ZipIt zip = ZipIt.builder().zipFile(zipFile).build();
        zip.add(srcDir, parameters);

        ZipMisc misc = ZipMisc.builder().zipFile(zipFile).build();
        assertThat(misc.isEncrypted()).isTrue();
        assertThat(misc.getEntryNames()).hasSize(15);
    }

    @Test(dependsOnMethods = "shouldRetrieveAllEntryNamesForExistedEncryptedZip")
    public void shouldRetrieveSingleFileWhenNoSplitZip() throws ZipException {
        Path zipFile = destDir.resolve("src.zip");
        assertThat(Files.exists(zipFile)).isTrue();
        assertThat(Files.isRegularFile(zipFile)).isTrue();

        ZipMisc misc = ZipMisc.builder().zipFile(zipFile).build();
        List<File> files = misc.getFiles();
        assertThat(files).hasSize(1);
        int a = 0;
        a++;
    }

    @Test(dependsOnMethods = "shouldRetrieveSingleFileWhenNoSplitZip")
    public void shouldRetrieveMultipleFilesWhenSplitZip() throws IOException, ZipException {
        Path zipFile = destDir.resolve("src.zip");
        Files.deleteIfExists(zipFile);

        ZipParameters parameters = ZipParameters.builder()
                                                .compressionMethod(CompressionMethod.DEFLATE)
                                                .compressionLevel(CompressionLevel.NORMAL)
                                                .splitLength(1024 * 1024).build();
        ZipIt zip = ZipIt.builder().zipFile(zipFile).build();
        zip.add(srcDir, parameters);
        TestUtils.checkDestinationDir(10, destDir);

        ZipMisc misc = ZipMisc.builder().zipFile(zipFile).build();
        List<File> files = misc.getFiles();

        assertThat(files).hasSize(10);
    }
}
