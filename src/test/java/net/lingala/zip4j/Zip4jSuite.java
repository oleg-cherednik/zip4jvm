package net.lingala.zip4j;

import net.lingala.zip4j.model.AESStrength;
import net.lingala.zip4j.model.CompressionLevel;
import net.lingala.zip4j.model.CompressionMethod;
import net.lingala.zip4j.model.Encryption;
import net.lingala.zip4j.model.ZipParameters;
import org.apache.commons.io.FileUtils;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Oleg Cherednik
 * @since 23.03.2019
 */
@SuppressWarnings("FieldNamingConvention")
public class Zip4jSuite {

    public static final Path rootDir = Paths.get("d:/zip4j/foo");//Files.createTempDirectory("zip4j");
    public static final Path srcDir = rootDir.resolve("src");
    public static final Path carsDir = srcDir.resolve("cars");
    public static final Path noSplitZip = rootDir.resolve("no_split/src.zip");
    public static final Path noSplitAesZip = rootDir.resolve("no_split_aes/src.zip");

    /** Password for encrypted zip */
    public static final char[] password = "1".toCharArray();
    /** Clear resources */
    public static final boolean clear = false;

    @BeforeSuite
    public void beforeSuite() throws IOException {
        removeDir(rootDir);

        Files.createDirectories(srcDir);
        Files.createDirectories(srcDir.resolve("empty_dir"));
        copyTestData();
        createNoSplitZip();
        createEncryptedNoSplitZip();
    }

    @AfterSuite(enabled = clear)
    public void afterSuite() throws IOException {
        removeDir(rootDir);
    }

    private static void copyTestData() throws IOException {
        Path dataDir = Paths.get("src/test/resources/data").toAbsolutePath();

        Files.walk(dataDir).forEach(path -> {
            try {
                if (Files.isDirectory(path))
                    Files.createDirectories(srcDir.resolve(dataDir.relativize(path)));
                else if (Files.isRegularFile(path))
                    Files.copy(path, srcDir.resolve(dataDir.relativize(path)));
            } catch(IOException e) {
                e.printStackTrace();
            }
        });
    }

    private static void createNoSplitZip() throws IOException {
        ZipParameters parameters = ZipParameters.builder()
                                                .compressionMethod(CompressionMethod.DEFLATE)
                                                .compressionLevel(CompressionLevel.NORMAL).build();
        ZipIt zip = ZipIt.builder().zipFile(noSplitZip).build();
        zip.add(srcDir, parameters);

        assertThat(Files.exists(noSplitZip)).isTrue();
        assertThat(Files.isRegularFile(noSplitZip)).isTrue();
        TestUtils.checkDestinationDir(1, noSplitZip.getParent());
    }

    private static void createEncryptedNoSplitZip() throws IOException {
        ZipParameters parameters = ZipParameters.builder()
                                                .compressionMethod(CompressionMethod.DEFLATE)
                                                .compressionLevel(CompressionLevel.NORMAL)
                                                .encryption(Encryption.STANDARD)
                                                .aesKeyStrength(AESStrength.STRENGTH_256)
                                                .password(password).build();
        ZipIt zip = ZipIt.builder().zipFile(noSplitAesZip).build();
        zip.add(srcDir, parameters);

        assertThat(Files.exists(noSplitZip)).isTrue();
        assertThat(Files.isRegularFile(noSplitZip)).isTrue();
        TestUtils.checkDestinationDir(1, noSplitZip.getParent());
    }

    public static void removeDir(Path path) throws IOException {
        if (Files.exists(path))
            FileUtils.deleteQuietly(path.toFile());
    }

}
