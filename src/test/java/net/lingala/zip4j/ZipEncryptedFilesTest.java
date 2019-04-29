package net.lingala.zip4j;

import net.lingala.zip4j.model.AesStrength;
import net.lingala.zip4j.model.CompressionLevel;
import net.lingala.zip4j.model.CompressionMethod;
import net.lingala.zip4j.model.Encryption;
import net.lingala.zip4j.model.ZipParameters;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static net.lingala.zip4j.assertj.Zip4jAssertions.assertThatDirectory;
import static net.lingala.zip4j.assertj.Zip4jAssertions.assertThatZipFile;

/**
 * @author Oleg Cherednik
 * @since 15.03.2019
 */
@SuppressWarnings("FieldNamingConvention")
public class ZipEncryptedFilesTest {

    private static final Path rootDir = Zip4jSuite.rootDir.resolve(ZipEncryptedFilesTest.class.getSimpleName());
    private static final Path zipFile = rootDir.resolve("src.zip");

    @BeforeClass
    public static void createDir() throws IOException {
        Files.createDirectories(rootDir);
    }

    @AfterClass(enabled = Zip4jSuite.clear)
    public static void removeDir() throws IOException {
        Zip4jSuite.removeDir(rootDir);
    }

//    @Test
    public void shouldCreateEncryptedZip() throws IOException {
        ZipParameters parameters = ZipParameters.builder()
                                                .compressionMethod(CompressionMethod.DEFLATE)
                                                .compressionLevel(CompressionLevel.NORMAL)
                                                .encryption(Encryption.STANDARD)
                                                .aesStrength(AesStrength.STRENGTH_256)
                                                .password(Zip4jSuite.password).build();

        ZipIt zip = ZipIt.builder().zipFile(zipFile).build();
        zip.add(Zip4jSuite.srcDir, parameters);

        assertThatDirectory(zipFile.getParent()).exists().hasSubDirectories(0).hasFiles(1);
        assertThatZipFile(zipFile).exists().rootEntry().hasSubDirectories(1).hasFiles(0);
        assertThatZipFile(zipFile).directory("/").matches(TestUtils.zipRootDirAssert);
    }
}
