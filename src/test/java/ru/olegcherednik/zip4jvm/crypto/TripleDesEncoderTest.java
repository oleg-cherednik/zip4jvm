package ru.olegcherednik.zip4jvm.crypto;

import ru.olegcherednik.zip4jvm.Zip4jvmSuite;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

import java.nio.file.Path;

/**
 * @author Oleg Cherednik
 * @since 24.07.2026
 */
//@Test
public class TripleDesEncoderTest {

    private static final Path DIR_ROOT = Zip4jvmSuite.generateSubDirNameWithTime();

    @BeforeClass
    public static void createDir() {
        Zip4jvmSuite.createDir(DIR_ROOT);
    }

    @AfterClass(enabled = Zip4jvmSuite.clear)
    public static void removeDir() {
        Zip4jvmSuite.removeDir(DIR_ROOT);
    }

    //    public void shouldEncodeUsing3des() {
    //        ZipSettings settings =
    //                ZipSettings.builder()
    //                           .entrySettings(
    //                                   ZipEntrySettings.builder()
    //                                                   .compression(CompressionEnum.STORE)
    //                                                   .encryption(EncryptionEnum.TRIPLE_DES_168, password)
    //                                                   .build())
    //                           .build();
    //
    //        Path dir = Zip4jvmSuite.subDirNameAsMethodName(DIR_ROOT);
    //        Path zip = dir.resolve(fileNameZipSrc);
    //        ZipIt.zip(zip).settings(settings).add(fileOlegCherednik);
    //
    //        ZipInfo.zip(zip).decompose(dir.resolve("decompose"));
    //
    ////        UnzipIt.zip(zip).dstDir(dir).password(password).extract();
    //    }

    //    public void shouldCheckPkware() {
    //        Path dir = Paths.get("f:/zip4jvm/foo/aa");
    //        Path zip = dir.resolve("src.zip");
    //        ZipInfo.zip(zip).decompose(dir.resolve("decompose"));
    ////        UnzipIt.zip(zip).dstDir(dir).password(password).extract();
    //    }

}
