package com.cop.zip4j.compression;

import com.cop.zip4j.Zip4jSuite;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author Oleg Cherednik
 * @since 06.08.2019
 */
//@Test
@SuppressWarnings("FieldNamingConvention")
public class CompressionDeflateTest {

    private static final Path rootDir = Zip4jSuite.generateSubDirNameWithTime(CompressionDeflateTest.class);

    @BeforeClass
    public static void createDir() throws IOException {
        Files.createDirectories(rootDir);
    }

    @AfterClass(enabled = Zip4jSuite.clear)
    public static void removeDir() throws IOException {
        Zip4jSuite.removeDir(rootDir);
    }

//    @Test
//    public void shouldCreateNewZipWithFolder() throws IOException {
//        ZipParameters parameters = ZipParameters.builder()
//                                                .compressionMethod(Compression.DEFLATE)
//                                                .compressionLevel(CompressionLevel.NORMAL)
//                                                .defaultFolderPath(Zip4jSuite.srcDir).build();
//
//        ZipIt zipIt = ZipIt.builder().zipFile(zipFile).build();
//        zipIt.add(Zip4jSuite.carsDir, parameters);
//
//        assertThatDirectory(zipFile.getParent()).exists().hasSubDirectories(0).hasFiles(1);
//        assertThatZipFile(zipFile).exists().rootEntry().hasSubDirectories(1).hasFiles(0);
//        assertThatZipFile(zipFile).directory("cars/").matches(TestUtils.zipCarsDirAssert);
//    }

}
