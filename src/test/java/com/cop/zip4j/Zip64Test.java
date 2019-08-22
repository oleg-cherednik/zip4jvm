package com.cop.zip4j;

import com.cop.zip4j.model.Compression;
import com.cop.zip4j.model.ZipParameters;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author Oleg Cherednik
 * @since 06.04.2019
 */
@Test
@SuppressWarnings({ "FieldNamingConvention", "NewClassNamingConvention" })
public class Zip64Test {

    private static final Path rootDir = Zip4jSuite.generateSubDirNameWithTime(Zip64Test.class);

    @BeforeClass
    public static void createDir() throws IOException {
        Files.createDirectories(rootDir);
    }

    @AfterClass(enabled = Zip4jSuite.clear)
    public static void removeDir() throws IOException {
        Zip4jSuite.removeDir(rootDir);
    }

    public void shouldZipAndUnzipWhenZip64() throws IOException {
        ZipParameters parameters = ZipParameters.builder()
                                                .compressionMethod(Compression.STORE)
                                                .zip64(true)
//                                                .defaultFolderPath(Zip4jSuite.srcDir)
                                                .build();

        Path zipFile = Zip4jSuite.subDirNameAsMethodName(rootDir).resolve("src.zip");
        ZipIt zip = ZipIt.builder().zipFile(zipFile).build();
        zip.add(Zip4jSuite.srcDir.resolve("Oleg Cherednik.txt"), parameters);
//        zip.add(Zip4jSuite.filesCarsDir, parameters);

//        assertThatDirectory(zipFile.getParent()).exists().hasSubDirectories(0).hasFiles(1);
//        assertThatZipFile(zipFile).directory("/").matches(TestUtils.zipRootDirAssert);

//        Path dstDir = zipFile.getParent().resolve("unzip");
//        UnzipIt unzip = UnzipIt.builder()
//                               .zipFile(zipFile)
//                               .build();
//        unzip.extract(dstDir);
    }

    public void shouldUnzipReadZipWithZip64() throws IOException {
        Path dstDir = Zip4jSuite.subDirNameAsMethodName(rootDir);
        UnzipIt unzip = UnzipIt.builder()
//                               .zipFile(Paths.get("d:/zip4j/ferdinand_store.zip"))
//                               .zipFile(Paths.get("d:/zip4j/ferdinand_deflate.zip"))
                               .zipFile(Paths.get("d:/zip4j/zip64_winzip.zip"))
                               .build();
        unzip.extract(dstDir);

        int a = 0;
        a++;
    }

}
