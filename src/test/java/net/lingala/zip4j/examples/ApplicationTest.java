package net.lingala.zip4j.examples;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.core.ZipFileDir;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.AESStrength;
import net.lingala.zip4j.model.CompressionLevel;
import net.lingala.zip4j.model.CompressionMethod;
import net.lingala.zip4j.model.Encryption;
import net.lingala.zip4j.model.InputStreamMeta;
import net.lingala.zip4j.model.ZipParameters;
import org.apache.commons.io.FileUtils;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Oleg Cherednik
 * @since 28.02.2019
 */
@Test
public class ApplicationTest {

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
        Files.createDirectories(resDir);
    }

    //    @BeforeMethod(dependsOnMethods = "createDirectory")
    public void copyTestData() throws IOException {
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

    //    @AfterMethod
    public void removeDirectory() throws IOException {
        FileUtils.deleteQuietly(root.toFile());
    }

    public void addFilesStoreCompNew() throws ZipException, IOException {
        ZipFile zipFile = new ZipFile(destDir.resolve("src.zip"));

        List<Path> files = Arrays.asList(
                srcDir.resolve("mcdonnell-douglas-f15-eagle.jpg"),
                srcDir.resolve("saint-petersburg.jpg"),
                srcDir.resolve("sig-sauer-pistol.jpg"),
                srcDir.resolve("cars/bentley-continental.jpg"),
                srcDir.resolve("cars/ferrari-458-italia.jpg"),
                srcDir.resolve("cars/wiesmann-gt-mf5.jpg"));

        ZipParameters parameters = ZipParameters.builder()
                                                .compressionMethod(CompressionMethod.DEFLATE)
                                                .encryption(Encryption.AES)
                                                .aesKeyStrength(AESStrength.STRENGTH_256)
                                                .password("1".toCharArray())
                                                .defaultFolderPath(srcDir.toString()).build();

        // Now add files to the zip file
        // Note: To add a single file, the method addFile can be used
        // Note: If the zip file already exists and if this zip file is a split file
        // then this method throws an exception as Zip Format Specification does not
        // allow updating split zip files
        zipFile.addFiles(files, parameters);

        checkDestinationDir(1);
//        checkResultDir();
    }

    public void addFileEncryption() throws ZipException, IOException {
        ZipFile zipFile = new ZipFile(destDir.resolve("src.zip"));
        List<Path> files = Collections.singletonList(srcDir.resolve("mcdonnell-douglas-f15-eagle.jpg"));

        ZipParameters parameters = ZipParameters.builder()
                                                .compressionMethod(CompressionMethod.DEFLATE)
                                                .compressionLevel(CompressionLevel.NORMAL)
                                                .encryption(Encryption.STANDARD)
                                                .aesKeyStrength(AESStrength.STRENGTH_256)
                                                .password("1".toCharArray())
                                                .defaultFolderPath(srcDir.toString()).build();

        // Now add files to the zip file
        // Note: To add a single file, the method addFile can be used
        // Note: If the zip file already exists and if this zip file is a split file
        // then this method throws an exception as Zip Format Specification does not
        // allow updating split zip files
        zipFile.addFiles(files, parameters);

        checkDestinationDir(1);
//        checkResultDir();
    }

    public void addFolderNew() throws ZipException, IOException {
        ZipParameters parameters = ZipParameters.builder()
                                                .compressionMethod(CompressionMethod.DEFLATE)
                                                .compressionLevel(CompressionLevel.NORMAL)
                                                .defaultFolderPath(srcDir.toString())
                                                .splitLength(1024 * 1024).build();
        new ZipFileDir(destDir.resolve("src.zip")).addFolder(srcDir, parameters);

        checkDestinationDir(10);
//        checkResultDir();
    }

    public void addFilesWithStream() throws IOException, ZipException {
        ZipFile zipFile = new ZipFile(destDir.resolve("src.zip"));
        ZipParameters parameters = ZipParameters.builder()
                                                .compressionMethod(CompressionMethod.DEFLATE)
                                                .compressionLevel(CompressionLevel.NORMAL)
                                                .isSourceExternalStream(true).build();

        List<InputStreamMeta> files = Arrays.asList(
                new InputStreamMeta(new FileInputStream(srcDir.resolve("saint-petersburg.jpg").toFile()), "one_one/one.jpg"),
                new InputStreamMeta(new FileInputStream(srcDir.resolve("cars/bentley-continental.jpg").toFile()), "two_two/two.jpg"));
        zipFile.addStream(files, parameters);
    }

//    public void shouldZipAndUnzipWithSinglePart() throws Exception {
//        Application.main("--add", srcDir.toString(), destDir.toString());
//        Application.main("--extract", destDir.resolve("src.zip").toString(), destDir.resolve("res").toString());
//
//        checkDestinationDir(1);
//        checkResultDir();
//    }

//    public void shouldZipAndUnzipWithMultipleParts() throws Exception {
//        Application.main("--add", srcDir.toString(), destDir.toString(), "1MB");
//        Application.main("--extract", destDir.resolve("src.zip").toString(), destDir.resolve("res").toString());
//
//        checkDestinationDir(10);
//        checkResultDir();
//    }

    private void checkDestinationDir(int totalParts) throws IOException {
        assertThat(Files.exists(destDir)).isTrue();
        assertThat(Files.isDirectory(destDir)).isTrue();
        assertThat(getRegularFilesAmount(destDir)).isEqualTo(totalParts);
        assertThat(getFoldersAmount(destDir)).isOne();

        assertThat(Files.exists(resDir)).isTrue();
        assertThat(getRegularFilesAmount(resDir)).isZero();
//        assertThat(getFoldersAmount(resDir)).isOne();
    }

    private void checkResultDir() throws IOException {
        Path srcDir = resDir.resolve("src");
        assertThat(Files.exists(srcDir)).isTrue();
        assertThat(Files.isDirectory(srcDir)).isTrue();
        assertThat(getRegularFilesAmount(srcDir)).isEqualTo(5);
        assertThat(getFoldersAmount(srcDir)).isEqualTo(3);

        Path carDir = srcDir.resolve("car");
        assertThat(Files.exists(carDir)).isTrue();
        assertThat(Files.isDirectory(carDir)).isTrue();
        assertThat(getRegularFilesAmount(carDir)).isEqualTo(3);
        assertThat(getFoldersAmount(carDir)).isZero();

        Path starWarsDir = srcDir.resolve("Star Wars");
        assertThat(Files.exists(starWarsDir)).isTrue();
        assertThat(Files.isDirectory(starWarsDir)).isTrue();
        assertThat(getRegularFilesAmount(starWarsDir)).isEqualTo(4);
        assertThat(getFoldersAmount(starWarsDir)).isZero();

        Path emptyDir = srcDir.resolve("empty folder");
        assertThat(Files.exists(emptyDir)).isTrue();
        assertThat(Files.isDirectory(emptyDir)).isTrue();
        assertThat(getRegularFilesAmount(emptyDir)).isZero();
        assertThat(getFoldersAmount(emptyDir)).isZero();
    }

    private static long getRegularFilesAmount(Path dir) throws IOException {
        return Files.list(dir).filter(path -> Files.isRegularFile(path)).count();
    }

    private static long getFoldersAmount(Path dir) throws IOException {
        return Files.list(dir).filter(path -> Files.isDirectory(path)).count();
    }

}
