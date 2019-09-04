package ru.olegcherednik.zip4jvm;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author Oleg Cherednik
 * @since 01.09.2019
 */
//@Test
//@SuppressWarnings("FieldNamingConvention")
public class ZipFileTest {

    private static final Path rootDir = Zip4jSuite.generateSubDirNameWithTime(ZipFileTest.class);

    @BeforeClass
    public static void createDir() throws IOException {
        Files.createDirectories(rootDir);
    }

    @AfterClass(enabled = Zip4jSuite.clear)
    public static void removeDir() throws IOException {
        Zip4jSuite.removeDir(rootDir);
    }

//    public void shouldCreateZipFileWhenUseZipFile() {
//        Path zip = Zip4jSuite.subDirNameAsMethodName(rootDir).resolve("src.zip");
//
//        try(ZipFile zipFile = )
//    }
}
