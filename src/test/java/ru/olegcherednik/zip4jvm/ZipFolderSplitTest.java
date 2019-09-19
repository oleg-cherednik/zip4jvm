package ru.olegcherednik.zip4jvm;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.olegcherednik.zip4jvm.model.Compression;
import ru.olegcherednik.zip4jvm.model.CompressionLevel;
import ru.olegcherednik.zip4jvm.model.settings.ZipEntrySettings;
import ru.olegcherednik.zip4jvm.model.settings.ZipFileSettings;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.olegcherednik.zip4jvm.assertj.Zip4jvmAssertionsForClassTypes.assertThatDirectory;

/**
 * @author Oleg Cherednik
 * @since 14.03.2019
 */
@SuppressWarnings("FieldNamingConvention")
public class ZipFolderSplitTest {

    private static final Path rootDir = Zip4jvmSuite.generateSubDirNameWithTime(ZipFolderSplitTest.class);
    private static final Path zip = rootDir.resolve("src.zip");

    @BeforeClass
    public static void createDir() throws IOException {
        Files.createDirectories(rootDir);
    }

    @AfterClass(enabled = Zip4jvmSuite.clear)
    public static void removeDir() throws IOException {
        Zip4jvmSuite.removeDir(rootDir);
    }

    @Test
    public void shouldCreateNewZipWithFolder() throws IOException {
        ZipFileSettings settings = ZipFileSettings.builder()
                                                  .entrySettingsProvider(fileName ->
                                                          ZipEntrySettings.builder()
                                                                          .compression(Compression.DEFLATE, CompressionLevel.NORMAL).build())
                                                  .splitSize(1024 * 1024).build();
        ZipIt.add(zip, Zip4jvmSuite.contentSrcDir, settings);

        assertThatDirectory(zip.getParent()).exists().hasSubDirectories(0).hasFiles(10);
        assertThat(Files.exists(zip)).isTrue();
        assertThat(Files.isRegularFile(zip)).isTrue();
        // TODO ZipFile does not read split archive
//        assertThatZipFile(zipFile).directory("/").matches(TestUtils.zipRootDirAssert);
    }

//    @Test(dependsOnMethods = "shouldCreateNewZipWithFolder")
//    public void shouldThrowExceptionWhenModifySplitZip() {
//        ZipFileWriterSettings settings = ZipFileWriterSettings.builder()
//                                                  .entrySettings(
//                                                          ZipEntrySettings.builder()
//                                                                          .compression(Compression.DEFLATE, CompressionLevel.NORMAL).build())
//                                                  .splitSize(2014 * 1024).build();
//
//        assertThatThrownBy(() -> ZipIt.add(zip, Zip4jSuite.starWarsDir, settings)).isExactlyInstanceOf(Zip4jException.class);
//    }
}
