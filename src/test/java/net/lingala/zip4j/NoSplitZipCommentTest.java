package net.lingala.zip4j;

import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.AESStrength;
import net.lingala.zip4j.model.CompressionLevel;
import net.lingala.zip4j.model.CompressionMethod;
import net.lingala.zip4j.model.Encryption;
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
public class NoSplitZipCommentTest {

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

        final String comment = "Oleg Cherednik - Олег Чередник";
        ZipMisc misc = ZipMisc.builder().zipFile(zipFile).build();
        assertThat(misc.getComment()).isNull();

        misc.setComment(comment);
        assertThat(misc.getComment()).isEqualTo(comment);
    }

    @Test(dependsOnMethods = "shouldAddCommentToExistedNoSplitZip")
    public void shouldClearCommentForExistedZip() throws ZipException {
        Path zipFile = destDir.resolve("src.zip");
        assertThat(Files.exists(zipFile)).isTrue();
        assertThat(Files.isRegularFile(zipFile)).isTrue();

        ZipMisc misc = ZipMisc.builder().zipFile(zipFile).build();
        assertThat(misc.getComment()).isNotBlank();

        misc.clearComment();
        assertThat(misc.getComment()).isNull();
    }

    @Test(dependsOnMethods = "shouldClearCommentForExistedZip")
    public void shouldAddCommentToEncryptedZip() throws ZipException, IOException {
        Path zipFile = destDir.resolve("src.zip");
        Files.deleteIfExists(zipFile);

        Path carsDir = srcDir.resolve("cars");
        ZipParameters parameters = ZipParameters.builder()
                                                .compressionMethod(CompressionMethod.DEFLATE)
                                                .compressionLevel(CompressionLevel.NORMAL)
                                                .encryption(Encryption.STANDARD)
                                                .aesKeyStrength(AESStrength.STRENGTH_256)
                                                .password("1".toCharArray())
                                                .defaultFolderPath(srcDir).build();

        ZipIt zip = ZipIt.builder().zipFile(zipFile).build();
        zip.add(carsDir, parameters);

        ZipMisc misc = ZipMisc.builder().zipFile(zipFile).build();
        assertThat(misc.isEncrypted()).isTrue();

        final String comment = "Oleg Cherednik - Олег Чередник";

        misc.setComment(comment);
        assertThat(misc.getComment()).isEqualTo(comment);
    }

}
