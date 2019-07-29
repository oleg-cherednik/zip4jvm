package com.cop.zip4j.compression;

import com.cop.zip4j.TestUtils;
import com.cop.zip4j.Zip4jSuite;
import com.cop.zip4j.ZipIt;
import com.cop.zip4j.exception.Zip4jException;
import com.cop.zip4j.model.CompressionLevel;
import com.cop.zip4j.model.CompressionMethod;
import com.cop.zip4j.model.ZipParameters;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static com.cop.zip4j.assertj.Zip4jAssertions.assertThatDirectory;
import static com.cop.zip4j.assertj.Zip4jAssertions.assertThatZipFile;

/**
 * @author Oleg Cherednik
 * @since 15.03.2019
 */
@SuppressWarnings("FieldNamingConvention")
public class CompressionStoreTest {

    private static final Path rootDir = Zip4jSuite.generateSubDirNameWithTime(CompressionStoreTest.class);
    private static final Path zipFile = rootDir.resolve("src.zip");

    @BeforeClass
    public static void createDir() throws IOException {
        Files.createDirectories(rootDir);
    }

    @AfterClass(enabled = Zip4jSuite.clear)
    public static void removeDir() throws IOException {
        Zip4jSuite.removeDir(rootDir);
    }

    @Test
    public void shouldCreateNewZipWithFilesWhenStoreCompression() throws IOException, Zip4jException {
        ZipParameters parameters = ZipParameters.builder()
                                                .compressionMethod(CompressionMethod.STORE)
                                                .compressionLevel(CompressionLevel.NORMAL)
                                                .defaultFolderPath(Zip4jSuite.srcDir).build();

        Path bentley = Zip4jSuite.carsDir.resolve("bentley-continental.jpg");
        Path ferrari = Zip4jSuite.carsDir.resolve("ferrari-458-italia.jpg");
        Path wiesmann = Zip4jSuite.carsDir.resolve("wiesmann-gt-mf5.jpg");
        List<Path> files = Arrays.asList(bentley, ferrari, wiesmann);

        ZipIt zip = ZipIt.builder().zipFile(zipFile).build();
        zip.add(files, parameters);

        assertThatDirectory(zipFile.getParent()).exists().hasSubDirectories(0).hasFiles(1);
        assertThatZipFile(zipFile).exists().rootEntry().hasSubDirectories(1).hasFiles(0);
        assertThatZipFile(zipFile).directory("cars/").matches(TestUtils.zipCarsDirAssert);
    }
}
