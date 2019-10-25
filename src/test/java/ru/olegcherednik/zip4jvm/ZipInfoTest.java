package ru.olegcherednik.zip4jvm;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.olegcherednik.zip4jvm.model.Compression;
import ru.olegcherednik.zip4jvm.model.CompressionLevel;
import ru.olegcherednik.zip4jvm.model.Encryption;
import ru.olegcherednik.zip4jvm.model.settings.ZipEntrySettings;
import ru.olegcherednik.zip4jvm.model.settings.ZipSettings;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Function;

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

    public void foo() throws IOException {
        Path path = Paths.get("d:/zip4jvm/tmp/aes.zip");
//        Files.deleteIfExists(path);

        Function<String, ZipEntrySettings> entrySettingsProvider =
                fileName -> ZipEntrySettings.builder()
                                            .compression(Compression.DEFLATE, CompressionLevel.NORMAL)
                                            .encryption(Encryption.AES_256, "1".toCharArray()).build();
        ZipSettings settings = ZipSettings.builder().entrySettingsProvider(entrySettingsProvider).comment("password: 1").build();
//        ZipIt.zip(path).settings(settings).add(contentDirSrc);
//        ZipIt.zip(path).settings(settings).add(fileDucati);

//        ZipInfo.zip(zipDeflateSolidPkware).getShortInfo();
//        ZipInfo.zip(Paths.get("d:/zip4jvm/foo/deflate/solid/off/src.zip"))
//        ZipInfo.zip(Paths.get("d:/zip4jvm/tmp/pkware.zip"))
//        ZipInfo.zip(Paths.get("d:/zip4jvm/tmp/ferdinand.zip"))
//        ZipInfo.zip(Paths.get("d:/zip4jvm/tmp/aes.zip"))
//        ZipInfo.zip(Paths.get("d:/zip4jvm/tmp/split/src.zip"))
        ZipInfo.zip(Paths.get("d:/zip4jvm/tmp/macos_10.zip"))
//        ZipInfo.zip(Paths.get("d:/zip4jvm/tmp/ubuntu_18.zip"))

               .getShortInfo();
    }

}
