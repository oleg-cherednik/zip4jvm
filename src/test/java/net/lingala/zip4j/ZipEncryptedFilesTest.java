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

import static net.lingala.zip4j.assertj.Zip4jAssertions.assertThatDirectory;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Oleg Cherednik
 * @since 15.03.2019
 */
public class ZipEncryptedFilesTest {

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
    public void shouldCreateEncryptedZip() throws ZipException, IOException {
        final char[] password = "1".toCharArray();
        Path zipFile = destDir.resolve("src.zip");
        Path carsDir = srcDir.resolve("cars");

        ZipParameters parameters = ZipParameters.builder()
                                                .compressionMethod(CompressionMethod.DEFLATE)
                                                .compressionLevel(CompressionLevel.NORMAL)
                                                .encryption(Encryption.AES)
                                                .aesKeyStrength(AESStrength.STRENGTH_256)
                                                .password(password)
                                                .defaultFolderPath(srcDir).build();

        ZipIt zip = ZipIt.builder().zipFile(zipFile).build();
        zip.add(carsDir, parameters);

        assertThat(Files.exists(zipFile)).isTrue();
        assertThat(Files.isRegularFile(zipFile)).isTrue();
        assertThatDirectory(destDir).exists().hasSubDirectories(0).hasFiles(1);

        // ---

        UnzipIt unzip = UnzipIt.builder()
                               .zipFile(zipFile)
                               .password(password).build();
        unzip.extract(resDir);

        TestUtils.checkDirectory(resDir, 1, 0);
        TestUtils.checkCarsDirectory(resDir.resolve("cars"));
    }
}
