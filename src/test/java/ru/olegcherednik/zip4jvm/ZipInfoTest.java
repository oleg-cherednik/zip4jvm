package ru.olegcherednik.zip4jvm;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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
//        ZipInfo.zip(zipDeflateSolidPkware).getShortInfo();
//        ZipInfo.zip(Paths.get("d:/zip4jvm/tmp/src.zip"))
        ZipInfo.zip(Paths.get("d:/zip4jvm/tmp/pkware.zip"))
//        ZipInfo.zip(Paths.get("d:/zip4jvm/tmp/ferdinand.zip"))
//        ZipInfo.zip(Paths.get("d:/zip4jvm/tmp/aa.zip"))
               .getShortInfo();
    }

}
