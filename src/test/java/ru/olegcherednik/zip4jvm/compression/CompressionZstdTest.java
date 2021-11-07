package ru.olegcherednik.zip4jvm.compression;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.olegcherednik.zip4jvm.Zip4jvmSuite;
import ru.olegcherednik.zip4jvm.ZipIt;
import ru.olegcherednik.zip4jvm.model.Compression;
import ru.olegcherednik.zip4jvm.model.CompressionLevel;
import ru.olegcherednik.zip4jvm.model.settings.ZipEntrySettings;
import ru.olegcherednik.zip4jvm.model.settings.ZipSettings;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static ru.olegcherednik.zip4jvm.TestData.filesDirBikes;
import static ru.olegcherednik.zip4jvm.TestDataAssert.dirBikesAssert;
import static ru.olegcherednik.zip4jvm.assertj.Zip4jvmAssertions.assertThatDirectory;
import static ru.olegcherednik.zip4jvm.assertj.Zip4jvmAssertions.assertThatZipFile;

/**
 * @author Oleg Cherednik
 * @since 07.11.2021
 */
@Test
@SuppressWarnings("FieldNamingConvention")
public class CompressionZstdTest {

    private static final Path rootDir = Zip4jvmSuite.generateSubDirNameWithTime(CompressionZstdTest.class);

    @BeforeClass
    public static void createDir() throws IOException {
        Files.createDirectories(rootDir);
    }

    @AfterClass(enabled = Zip4jvmSuite.clear)
    public static void removeDir() throws IOException {
        Zip4jvmSuite.removeDir(rootDir);
    }

    public void shouldCreateSingleZipWithFilesWhenZstdCompressionNormalLevelEosMarker() throws IOException {
        ZipEntrySettings entrySettings = ZipEntrySettings.builder()
                                                         .compression(Compression.ZSTD, CompressionLevel.NORMAL)
                                                         .lzmaEosMarker(true).build();
        ZipSettings settings = ZipSettings.builder().entrySettingsProvider(fileName -> entrySettings).build();

        Path zip = Zip4jvmSuite.subDirNameAsMethodName(rootDir).resolve("src.zip");

        ZipIt.zip(zip).settings(settings).add(filesDirBikes);
        assertThatDirectory(zip.getParent()).exists().hasDirectories(0).hasFiles(1);
        assertThatZipFile(zip).root().matches(dirBikesAssert);
    }

//    public void shouldCreateSingleZipWithFilesWhenLzmaCompressionNormalLevelEosNoMarker() throws IOException {
//        ZipEntrySettings entrySettings = ZipEntrySettings.builder()
//                                                         .compression(Compression.LZMA, CompressionLevel.NORMAL)
//                                                         .lzmaEosMarker(false).build();
//        ZipSettings settings = ZipSettings.builder().entrySettingsProvider(fileName -> entrySettings).build();
//
//        Path zip = Zip4jvmSuite.subDirNameAsMethodName(rootDir).resolve("src.zip");
//
//        ZipIt.zip(zip).settings(settings).add(filesDirBikes);
//        assertThatDirectory(zip.getParent()).exists().hasDirectories(0).hasFiles(1);
//        assertThatZipFile(zip).root().matches(dirBikesAssert);
//    }
//
//    public void shouldCreateSingleZipWithFilesWhenLzmaCompressionSuperFastLevelEosMarker() throws IOException {
//        ZipEntrySettings entrySettings = ZipEntrySettings.builder()
//                                                         .compression(Compression.LZMA, CompressionLevel.SUPER_FAST)
//                                                         .lzmaEosMarker(true).build();
//        ZipSettings settings = ZipSettings.builder().entrySettingsProvider(fileName -> entrySettings).build();
//
//        Path zip = Zip4jvmSuite.subDirNameAsMethodName(rootDir).resolve("src.zip");
//
//        ZipIt.zip(zip).settings(settings).add(filesDirBikes);
//        assertThatDirectory(zip.getParent()).exists().hasDirectories(0).hasFiles(1);
//        assertThatZipFile(zip).root().matches(dirBikesAssert);
//    }
//
//    public void shouldCreateSingleZipWithFilesWhenLzmaCompressionSuperFastLevelNoEosMarker() throws IOException {
//        ZipEntrySettings entrySettings = ZipEntrySettings.builder()
//                                                         .compression(Compression.LZMA, CompressionLevel.SUPER_FAST)
//                                                         .lzmaEosMarker(false).build();
//        ZipSettings settings = ZipSettings.builder().entrySettingsProvider(fileName -> entrySettings).build();
//
//        Path zip = Zip4jvmSuite.subDirNameAsMethodName(rootDir).resolve("src.zip");
//
//        ZipIt.zip(zip).settings(settings).add(filesDirBikes);
//        assertThatDirectory(zip.getParent()).exists().hasDirectories(0).hasFiles(1);
//        assertThatZipFile(zip).root().matches(dirBikesAssert);
//    }
//
//    public void shouldUseCompressStoreWhenFileEmpty() throws IOException {
//        ZipEntrySettings entrySettings = ZipEntrySettings.builder().compression(Compression.LZMA, CompressionLevel.NORMAL).build();
//        ZipSettings settings = ZipSettings.builder().entrySettingsProvider(fileName -> entrySettings).build();
//
//        Path zip = Zip4jvmSuite.subDirNameAsMethodName(rootDir).resolve("src.zip");
//
//        ZipIt.zip(zip).settings(settings).add(fileEmpty);
//        CentralDirectory.FileHeader fileHeader = ZipInfo.zip(zip).getFileHeader(fileNameEmpty);
//        assertThat(fileHeader.getCompressionMethod()).isSameAs(CompressionMethod.STORE);
//    }

}
