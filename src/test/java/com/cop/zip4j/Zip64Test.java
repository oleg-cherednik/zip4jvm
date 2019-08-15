package com.cop.zip4j;

import com.cop.zip4j.exception.Zip4jException;
import com.cop.zip4j.model.Compression;
import com.cop.zip4j.model.ZipParameters;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.cop.zip4j.assertj.Zip4jAssertions.assertThatDirectory;
import static com.cop.zip4j.assertj.Zip4jAssertions.assertThatZipFile;

/**
 * @author Oleg Cherednik
 * @since 06.04.2019
 */
//@Test
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

    public void shouldCreateNewZipWithZip64() throws IOException, Zip4jException {
        ZipParameters parameters = ZipParameters.builder()
                                                .compressionMethod(Compression.STORE)
                                                .zip64(true)
                                                .defaultFolderPath(Zip4jSuite.srcDir).build();

        Path zipFile = Zip4jSuite.subDirNameAsMethodName(rootDir).resolve("src.zip");
        ZipIt zip = ZipIt.builder().zipFile(zipFile).build();
        zip.add(Zip4jSuite.srcDir, parameters);

        assertThatDirectory(zipFile.getParent()).exists().hasSubDirectories(0).hasFiles(1);
        assertThatZipFile(zipFile).directory("/").matches(TestUtils.zipRootDirAssert);
    }

}
