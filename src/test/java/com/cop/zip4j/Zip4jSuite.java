package com.cop.zip4j;

import com.cop.zip4j.model.Compression;
import com.cop.zip4j.model.CompressionLevel;
import com.cop.zip4j.model.Encryption;
import com.cop.zip4j.model.ZipParameters;
import org.apache.commons.io.FileUtils;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.cop.zip4j.assertj.Zip4jAssertions.assertThatDirectory;
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
    public static final Path starWarsDir = srcDir.resolve("Star Wars");
    public static final Path emptyDir = srcDir.resolve("empty_dir");

    public static final Path noSplitZip = rootDir.resolve("no_split/src.zip");
    public static final Path noSplitPkwareZip = rootDir.resolve("no_split_pkware/src.zip");
    public static final Path splitZip = rootDir.resolve("split/src.zip");

    public static final Path winRarPkwareZip = Paths.get("src/test/resources/pkware.zip").toAbsolutePath();
    public static final Path winRarAesZip = Paths.get("src/test/resources/aes.zip").toAbsolutePath();

    /** Password for encrypted zip */
    public static final char[] password = "1".toCharArray();
    /** Clear resources */
    public static final boolean clear = false;

    private static final long time = System.currentTimeMillis();

    @BeforeSuite
    public void beforeSuite() throws IOException {
        removeDir(rootDir);

        copyTestData();
        createNoSplitZip();
        createEncryptedNoSplitZip();
        createSplitZip();
    }

    @AfterSuite(enabled = clear)
    public void afterSuite() throws IOException {
        removeDir(rootDir);
    }

    private static void copyTestData() throws IOException {
        Files.createDirectories(srcDir.resolve("empty_dir"));

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

        assertThatDirectory(srcDir).matches(TestUtils.dirAssert);
    }

    private static void createNoSplitZip() throws IOException {
        ZipParameters parameters = ZipParameters.builder()
                                                .compressionMethod(Compression.DEFLATE)
                                                .compressionLevel(CompressionLevel.NORMAL).build();
        ZipIt zip = ZipIt.builder().zipFile(noSplitZip).build();
        zip.add(srcDir, parameters);

        assertThat(Files.exists(noSplitZip)).isTrue();
        assertThat(Files.isRegularFile(noSplitZip)).isTrue();
        assertThatDirectory(noSplitZip.getParent()).exists().hasSubDirectories(0).hasFiles(1);
    }

    private static void createSplitZip() throws IOException {
        ZipParameters parameters = ZipParameters.builder()
                                                .compressionMethod(Compression.DEFLATE)
                                                .compressionLevel(CompressionLevel.NORMAL)
                                                .splitLength(1024 * 1024).build();
        ZipIt zip = ZipIt.builder().zipFile(splitZip).build();
        zip.add(srcDir, parameters);

        assertThat(Files.exists(splitZip)).isTrue();
        assertThat(Files.isRegularFile(splitZip)).isTrue();
        assertThatDirectory(splitZip.getParent()).exists().hasSubDirectories(0).hasFiles(10);
    }

    private static void createEncryptedNoSplitZip() throws IOException {
        ZipParameters parameters = ZipParameters.builder()
                                                .compressionMethod(Compression.DEFLATE)
                                                .compressionLevel(CompressionLevel.NORMAL)
                                                .encryption(Encryption.PKWARE)
                                                .comment("password: " + new String(password))
                                                .password(password).build();
        ZipIt zip = ZipIt.builder().zipFile(noSplitPkwareZip).build();
        zip.add(srcDir, parameters);

        assertThat(Files.exists(noSplitZip)).isTrue();
        assertThat(Files.isRegularFile(noSplitZip)).isTrue();
        assertThatDirectory(noSplitZip.getParent()).exists().hasSubDirectories(0).hasFiles(1);
    }

    public static void removeDir(Path path) throws IOException {
        if (Files.exists(path))
            FileUtils.deleteQuietly(path.toFile());
    }

    public static Path copy(Path rootDir, Path srcFile) throws IOException {
        Path zipFile = generateZipFileName(rootDir);
        Files.copy(srcFile, zipFile);
        return zipFile;
    }

    public static Path generateZipFileName(Path rootDir) {
        return rootDir.resolve("src_" + System.currentTimeMillis() + ".zip");
    }

    public static Path generateSubDirName(Class<?> cls) {
        return rootDir.resolve(cls.getSimpleName());
    }

    public static Path generateSubDirNameWithTime(Class<?> cls) {
        return rootDir.resolve(cls.getSimpleName()).resolve(Paths.get(String.valueOf(time)));
    }

    public static Path subDirNameAsMethodName(Path rootDir) {
        return rootDir.resolve(TestUtils.getMethodName()).resolve(Paths.get(String.valueOf(time)));
    }

}
