package ru.olegcherednik.zip4jvm;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.olegcherednik.zip4jvm.assertj.Zip4jvmAssertions;
import ru.olegcherednik.zip4jvm.model.Compression;
import ru.olegcherednik.zip4jvm.model.CompressionLevel;
import ru.olegcherednik.zip4jvm.model.settings.ZipEntrySettings;
import ru.olegcherednik.zip4jvm.model.settings.ZipFileSettings;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

/**
 * @author Oleg Cherednik
 * @since 15.03.2019
 */
@Test
@SuppressWarnings("FieldNamingConvention")
public class ZipFilesNoSplitTest {

    private static final Path rootDir = Zip4jvmSuite.generateSubDirNameWithTime(ZipFilesNoSplitTest.class);
    private static final Path zip = rootDir.resolve("src.zip");

    @BeforeClass
    public static void createDir() throws IOException {
        Files.createDirectories(rootDir);
    }

    @AfterClass(enabled = Zip4jvmSuite.clear)
    public static void removeDir() throws IOException {
        Zip4jvmSuite.removeDir(rootDir);
    }

    public void shouldCreateNewZipWithFiles() throws IOException {
        ZipFileSettings settings = ZipFileSettings.builder()
                                                  .entrySettingsProvider(fileName ->
                                                          ZipEntrySettings.builder()
                                                                          .compression(Compression.DEFLATE, CompressionLevel.NORMAL).build())
                                                  .build();

        Path bentley = Zip4jvmSuite.carsDir.resolve("bentley-continental.jpg");
        Path ferrari = Zip4jvmSuite.carsDir.resolve("ferrari-458-italia.jpg");
        Path wiesmann = Zip4jvmSuite.carsDir.resolve("wiesmann-gt-mf5.jpg");
        List<Path> files = Arrays.asList(bentley, ferrari, wiesmann);

        ZipIt.add(zip, files, settings);

        Zip4jvmAssertions.assertThatDirectory(zip.getParent()).exists().hasSubDirectories(0).hasFiles(1);
        Zip4jvmAssertions.assertThatZipFile(zip).directory("/").matches(TestUtils.zipCarsDirAssert);
    }

    // TODO Test to add files to existed no split zip
}
