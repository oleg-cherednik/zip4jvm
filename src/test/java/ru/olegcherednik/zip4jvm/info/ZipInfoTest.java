package ru.olegcherednik.zip4jvm.info;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.olegcherednik.zip4jvm.TestData;
import ru.olegcherednik.zip4jvm.Zip4jvmSuite;
import ru.olegcherednik.zip4jvm.ZipInfo;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static ru.olegcherednik.zip4jvm.assertj.Zip4jvmAssertions.assertThatFile;

/**
 * @author Oleg Cherednik
 * @since 11.10.2019
 */
@Test
@SuppressWarnings("FieldNamingConvention")
public class ZipInfoTest {

    private static final Path rootDir = Zip4jvmSuite.generateSubDirNameWithTime(ZipInfoTest.class);

    @BeforeClass
    public static void createDir() throws IOException {
        Files.createDirectories(rootDir);
    }

    @AfterClass(enabled = Zip4jvmSuite.clear)
    public static void removeDir() throws IOException {
        Zip4jvmSuite.removeDir(rootDir);
    }

    public void shouldRetrieveInfoWhenStoreSolid() throws IOException {
        Path file = Zip4jvmSuite.subDirNameAsMethodName(rootDir).resolve("actual.txt");
        Files.createDirectories(file.getParent());

        try (PrintStream out = new PrintStream(file.toFile())) {
            ZipInfo.zip(TestData.zipStoreSolid).getShortInfo(out);
        }

        assertThatFile(file).matchesResourceLines("/info/store_solid.txt");
    }

    public void shouldRetrieveInfoWhenStoreSolidPkware() throws IOException {
        Path file = Zip4jvmSuite.subDirNameAsMethodName(rootDir).resolve("actual.txt");
        Files.createDirectories(file.getParent());

        try (PrintStream out = new PrintStream(file.toFile())) {
            ZipInfo.zip(TestData.zipStoreSolidPkware).getShortInfo(out);
        }

        assertThatFile(file).matchesResourceLines("/info/store_solid_pkware.txt");
    }

    public void shouldRetrieveInfoWhenStoreSolidAes() throws IOException {
        Path file = Zip4jvmSuite.subDirNameAsMethodName(rootDir).resolve("actual.txt");
        Files.createDirectories(file.getParent());

        try (PrintStream out = new PrintStream(file.toFile())) {
            ZipInfo.zip(TestData.zipStoreSolidAes).getShortInfo(out);
        }

        assertThatFile(file).matchesResourceLines("/info/store_solid_aes.txt");
    }

    //    @Test(enabled = false)
    public void foo() throws IOException {
        Path path = Paths.get("d:/zip4jvm/tmp/aes.zip");
//        Files.deleteIfExists(path);

//        Function<String, ZipEntrySettings> entrySettingsProvider =
//                fileName -> ZipEntrySettings.builder()
//                                            .compression(Compression.DEFLATE, CompressionLevel.NORMAL)
//                                            .encryption(Encryption.AES_256, "1".toCharArray())
//                                            .build();
//        ZipSettings settings = ZipSettings.builder().entrySettingsProvider(entrySettingsProvider).comment("password: 1").build();
//        ZipIt.zip(path).settings(settings).add(contentDirSrc);
//        ZipIt.zip(path).settings(settings).add(fileDucati);

//        ZipInfo.zip(zipDeflateSolidPkware).getShortInfo();
//        ZipInfo.zip(Paths.get("d:/zip4jvm/foo/deflate/solid/off/src.zip"))
//        ZipInfo.zip(Paths.get("d:/zip4jvm/tmp/pkware.zip"))
        ZipInfo.zip(Paths.get("d:/zip4jvm/tmp/ferdinand.zip"))
//        ZipInfo.zip(Paths.get("d:/zip4jvm/tmp/aes.zip"))
//        ZipInfo.zip(Paths.get("d:/zip4jvm/tmp/split/src.zip"))
//        ZipInfo.zip(Paths.get("d:/zip4jvm/tmp/macos_10.zip"))
//        ZipInfo.zip(Paths.get("d:/zip4jvm/tmp/ubuntu_18.zip"))
//        ZipInfo.zip(Paths.get("d:/zip4jvm/securezip/aes128.zip"))
//        ZipInfo.zip(TestData.zipStoreSolid)
               .getShortInfo(System.out);
    }

    //    @Test(enabled = false)
    public void decompose() throws IOException {
        Path dir = Zip4jvmSuite.subDirNameAsMethodName(rootDir);

//        ZipInfo.zip(Paths.get("d:/zip4jvm/tmp/aes.zip"))
//        ZipInfo.zip(Paths.get("d:/zip4jvm/tmp/aes1.zip"))
        ZipInfo.zip(Paths.get("d:/zip4jvm/tmp/ferdinand.zip"))
//        ZipInfo.zip(Paths.get("d:/zip4jvm/tmp/pkware.zip"))
//        ZipInfo.zip(Paths.get("d:/zip4jvm/tmp/macos_10.zip"))
//        ZipInfo.zip(Paths.get("d:/zip4jvm/tmp/ubuntu_18.zip"))
//        ZipInfo.zip(Paths.get("d:/zip4jvm/securezip/aes128.zip"))
//        ZipInfo.zip(Paths.get("d:/Programming/GitHub/zip4jvm/src/test/resources/zip/zip64_crc1byte_check.zip"))
               .decompose(dir);
    }

}
