package ru.olegcherednik.zip4jvm.encryption;

import ru.olegcherednik.zip4jvm.UnzipIt;
import ru.olegcherednik.zip4jvm.Zip4jvmSuite;
import ru.olegcherednik.zip4jvm.model.settings.UnzipSettings;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static ru.olegcherednik.zip4jvm.Zip4jvmSuite.password;


/**
 * @author Oleg Cherednik
 * @since 21.07.2026
 */
@Test
@SuppressWarnings("FieldNamingConvention")
public class Encryption3DesTest {

    private static final Path DIR_ROOT = Zip4jvmSuite.generateSubDirNameWithTime();

    @BeforeClass
    public static void createDir() throws IOException {
        Zip4jvmSuite.createDir(DIR_ROOT);
    }

    @AfterClass(enabled = Zip4jvmSuite.clear)
    public static void removeDir() throws IOException {
        Zip4jvmSuite.removeDir(DIR_ROOT);
    }

    public void shouldUnzipWhenStoreSolidAes() throws IOException {
        Path destDir = Zip4jvmSuite.subDirNameAsMethodName(DIR_ROOT);

        UnzipSettings settings = UnzipSettings.builder().password(password).build();

        UnzipIt.zip(Paths.get("d:/zip4jvm/3des/3des_lk.zip")).dstDir(destDir).settings(settings).extract();
//        UnzipIt.zip(Paths.get("d:/zip4jvm/securezip/aes/aes128.zip")).destDir(destDir).settings(settings).extract();
//        UnzipIt.zip(Paths.get("d:/zip4jvm/securezip/aes/aes192.zip")).destDir(destDir).settings(settings).extract();
//        UnzipIt.zip(Paths.get("d:/zip4jvm/securezip/aes/aes256.zip")).destDir(destDir).settings(settings).extract();
//        assertThatDirectory(destDir).matches(rootAssert);
    }

    public static void main(String[] args) throws IOException {
        Path zip = Paths.get("d:/zip4jvm/3des/3des_store_168.zip");
        Path destDir = Paths.get("d:/zip4jvm/3des/3des_store_168");
        UnzipIt.zip(zip).dstDir(destDir).password("5oquil2oo2vb63e8ionujny6".toCharArray()).extract();

    }


}
