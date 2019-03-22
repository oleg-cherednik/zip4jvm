package net.lingala.zip4j;

import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.AESStrength;
import net.lingala.zip4j.model.CompressionLevel;
import net.lingala.zip4j.model.CompressionMethod;
import net.lingala.zip4j.model.Encryption;
import net.lingala.zip4j.model.ZipParameters;
import org.testng.annotations.BeforeMethod;

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

    //    @Test
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

    //    @Test(dependsOnMethods = "shouldRetrieveAllEntryNamesForExistedZip")
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

    //    @Test(dependsOnMethods = "shouldRetrieveAllEntryNamesForExistedEncryptedZip")
    public void shouldRetrieveSingleFileWhenNoSplitZip() throws ZipException {
        Path zipFile = destDir.resolve("src.zip");
        assertThat(Files.exists(zipFile)).isTrue();
        assertThat(Files.isRegularFile(zipFile)).isTrue();

        ZipMisc misc = ZipMisc.builder().zipFile(zipFile).build();
        assertThat(misc.getFiles()).hasSize(1);
    }

    //    @Test(dependsOnMethods = "shouldRetrieveSingleFileWhenNoSplitZip")
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

    //    @Test(dependsOnMethods = "shouldRetrieveMultipleFilesWhenSplitZip")
    public void shouldMergeSplitZip() throws ZipException, IOException {
        Path zipFile = destDir.resolve("src.zip");
        assertThat(Files.exists(zipFile)).isTrue();
        assertThat(Files.isRegularFile(zipFile)).isTrue();

        ZipMisc misc = ZipMisc.builder().zipFile(zipFile).build();
        assertThat(misc.isSplit()).isTrue();

        Path mergeDir = destDir.resolve("merge");
        Path mergeZipFle = mergeDir.resolve("src.zip");
        // TODO temporary
        Files.createDirectories(mergeDir);
        misc.merge(mergeZipFle);

        assertThat(Files.exists(zipFile)).isTrue();
        assertThat(Files.isRegularFile(zipFile)).isTrue();
        TestUtils.checkDirectory(mergeDir, 0, 1);

        //---

        Path mergeResDir = mergeDir.resolve("res");
        UnzipIt unzip = UnzipIt.builder().zipFile(zipFile).build();
        unzip.extract(mergeResDir);
        TestUtils.checkResultDir(mergeResDir);
    }
}
