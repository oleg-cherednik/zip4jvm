package ru.olegcherednik.zip4jvm;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.olegcherednik.zip4jvm.io.readers.ZipModelReader;
import ru.olegcherednik.zip4jvm.model.Compression;
import ru.olegcherednik.zip4jvm.model.CompressionLevel;
import ru.olegcherednik.zip4jvm.model.EndCentralDirectory;
import ru.olegcherednik.zip4jvm.model.Zip64;
import ru.olegcherednik.zip4jvm.model.ZipModel;
import ru.olegcherednik.zip4jvm.model.settings.ZipEntrySettings;
import ru.olegcherednik.zip4jvm.model.settings.ZipSettings;
import ru.olegcherednik.zip4jvm.model.src.SrcZip;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.olegcherednik.zip4jvm.TestData.fileBentley;
import static ru.olegcherednik.zip4jvm.TestData.fileFerrari;
import static ru.olegcherednik.zip4jvm.TestData.fileWiesmann;
import static ru.olegcherednik.zip4jvm.Zip4jvmSuite.SIZE_1MB;

/**
 * @author Oleg Cherednik
 * @since 27.04.2019
 */
@Test
@SuppressWarnings("FieldNamingConvention")
public class ZipFilesSplitTest {

    private static final Path rootDir = Zip4jvmSuite.generateSubDirNameWithTime(ZipFilesSplitTest.class);

    @BeforeClass
    public static void createDir() throws IOException {
        Files.createDirectories(rootDir);
    }

    @AfterClass(enabled = Zip4jvmSuite.clear)
    public static void removeDir() throws IOException {
        Zip4jvmSuite.removeDir(rootDir);
    }

    public void shouldCreateNewSplitZipWithFiles() throws IOException {
        Path zip = Zip4jvmSuite.subDirNameAsMethodName(rootDir).resolve("src.zip");
        ZipSettings settings = ZipSettings.builder()
                                          .entrySettingsProvider(fileName ->
                                                  ZipEntrySettings.builder()
                                                                  .compression(Compression.DEFLATE, CompressionLevel.NORMAL).build())
                                          .splitSize(SIZE_1MB).build();
        List<Path> files = Arrays.asList(fileBentley, fileFerrari, fileWiesmann);
        ZipIt.zip(zip).settings(settings).add(files);
//    TODO commented tests
//        assertThatDirectory(zipFile.getParent()).exists().hasSubDirectories(0).hasFiles(1);
//        assertThatZipFile(zipFile).exists().rootEntry().hasSubDirectories(1).hasFiles(0);
//        assertThatZipFile(zipFile).directory("/").matches(TestUtils.zipCarsDirAssert);
    }

    @SuppressWarnings("LocalVariableNamingConvention")
    public void shouldSetTotalDiskWhenSplitZip64() throws IOException {
        Path zip = Zip4jvmSuite.subDirNameAsMethodName(rootDir).resolve("src.zip");
        ZipSettings settings = ZipSettings.builder()
                                          .zip64(true)
                                          .entrySettingsProvider(fileName ->
                                                  ZipEntrySettings.builder()
                                                                  .compression(Compression.DEFLATE, CompressionLevel.NORMAL).build())
                                          .splitSize(SIZE_1MB).build();
        List<Path> files = Arrays.asList(fileBentley, fileFerrari, fileWiesmann);
        ZipIt.zip(zip).settings(settings).add(files);

        SrcZip srcZip = SrcZip.of(zip);
        ZipModelReader reader = new ZipModelReader(srcZip);
        reader.readCentralData();

        EndCentralDirectory endCentralDirectory = reader.getEndCentralDirectory();
        Zip64.EndCentralDirectoryLocator zip64EndCentralDirectoryLocator = reader.getZip64().getEndCentralDirectoryLocator();

        assertThat(endCentralDirectory.getMainDiskNo()).isEqualTo(ZipModel.MAX_TOTAL_DISKS);
        assertThat(endCentralDirectory.getTotalDisks()).isEqualTo(ZipModel.MAX_TOTAL_DISKS);
        assertThat(zip64EndCentralDirectoryLocator.getMainDiskNo()).isEqualTo(2);
        assertThat(zip64EndCentralDirectoryLocator.getTotalDisks()).isEqualTo(3);

        assertThat(ZipModelReader.getTotalDisks(srcZip)).isEqualTo(3);
    }

    public void shouldSetTotalDiskWhenSplit() throws IOException {
        Path zip = Zip4jvmSuite.subDirNameAsMethodName(rootDir).resolve("src.zip");
        ZipSettings settings = ZipSettings.builder()
                                          .entrySettingsProvider(fileName ->
                                                  ZipEntrySettings.builder()
                                                                  .compression(Compression.DEFLATE, CompressionLevel.NORMAL).build())
                                          .splitSize(SIZE_1MB).build();
        List<Path> files = Arrays.asList(fileBentley, fileFerrari, fileWiesmann);
        ZipIt.zip(zip).settings(settings).add(files);

        SrcZip srcZip = SrcZip.of(zip);
        ZipModelReader reader = new ZipModelReader(srcZip);
        reader.readCentralData();

        EndCentralDirectory endCentralDirectory = reader.getEndCentralDirectory();

        assertThat(endCentralDirectory.getMainDiskNo()).isEqualTo(2);
        assertThat(endCentralDirectory.getTotalDisks()).isEqualTo(2);
        assertThat(reader.getZip64()).isSameAs(Zip64.NULL);

        assertThat(ZipModelReader.getTotalDisks(srcZip)).isEqualTo(3);
    }

}
