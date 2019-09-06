package ru.olegcherednik.zip4jvm;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.olegcherednik.zip4jvm.assertj.Zip4jAssertions;
import ru.olegcherednik.zip4jvm.exception.Zip4jException;
import ru.olegcherednik.zip4jvm.model.Compression;
import ru.olegcherednik.zip4jvm.model.CompressionLevel;
import ru.olegcherednik.zip4jvm.model.ZipParameters;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

/**
 * @author Oleg Cherednik
 * @since 15.03.2019
 */
@SuppressWarnings("FieldNamingConvention")
public class ZipFilesNoSplitTest {

    private static final Path rootDir = Zip4jSuite.generateSubDirNameWithTime(ZipFilesNoSplitTest.class);
    private static final Path zip = rootDir.resolve("src.zip");

    @BeforeClass
    public static void createDir() throws IOException {
        Files.createDirectories(rootDir);
    }

    @AfterClass(enabled = Zip4jSuite.clear)
    public static void removeDir() throws IOException {
        Zip4jSuite.removeDir(rootDir);
    }

    @Test
    public void shouldCreateNewZipWithFiles() throws IOException, Zip4jException {
        ZipParameters parameters = ZipParameters.builder()
                                                .compression(Compression.DEFLATE, CompressionLevel.NORMAL)
                                                .defaultFolderPath(Zip4jSuite.srcDir).build();

        Path bentley = Zip4jSuite.carsDir.resolve("bentley-continental.jpg");
        Path ferrari = Zip4jSuite.carsDir.resolve("ferrari-458-italia.jpg");
        Path wiesmann = Zip4jSuite.carsDir.resolve("wiesmann-gt-mf5.jpg");
        List<Path> files = Arrays.asList(bentley, ferrari, wiesmann);

        ZipIt zip = ZipIt.builder().zipFile(ZipFilesNoSplitTest.zip).build();
        zip.add(files, parameters);

        Zip4jAssertions.assertThatDirectory(ZipFilesNoSplitTest.zip.getParent()).exists().hasSubDirectories(0).hasFiles(1);
        Zip4jAssertions.assertThatZipFile(ZipFilesNoSplitTest.zip).exists().rootEntry().hasSubDirectories(1).hasFiles(0);
        Zip4jAssertions.assertThatZipFile(ZipFilesNoSplitTest.zip).directory("cars/").matches(TestUtils.zipCarsDirAssert);
    }

    // TODO Test to add files to existed no split zip
}
