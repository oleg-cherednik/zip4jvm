package ru.olegcherednik.zip4jvm.compression;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.olegcherednik.zip4jvm.Zip4jvmSuite;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author Oleg Cherednik
 * @since 15.04.2020
 */
@Test
@SuppressWarnings("FieldNamingConvention")
public class CompressionEnhancedDeflateTest {

    private static final Path rootDir = Zip4jvmSuite.generateSubDirNameWithTime(CompressionEnhancedDeflateTest.class);

    @BeforeClass
    public static void createDir() throws IOException {
        Files.createDirectories(rootDir);
    }

    @AfterClass(enabled = Zip4jvmSuite.clear)
    public static void removeDir() throws IOException {
        Zip4jvmSuite.removeDir(rootDir);
    }

//    public void shouldCreateSingleZipWithFilesWhenBzip2CompressionNormalLevel() throws IOException {
//        ZipEntrySettings entrySettings = ZipEntrySettings.builder()
//                                                         .compression(Compression.BZIP2, CompressionLevel.NORMAL)
//                                                         .lzmaEosMarker(true).build();
//        ZipSettings settings = ZipSettings.builder().entrySettingsProvider(fileName -> entrySettings).build();
//
//        Path zip = Zip4jvmSuite.subDirNameAsMethodName(rootDir).resolve("src.zip");
//        ZipIt.zip(zip).settings(settings).add(filesDirBikes);
//        assertThatDirectory(zip.getParent()).exists().hasDirectories(0).hasFiles(1);
//        assertThatZipFile(zip).root().matches(dirBikesAssert);
//    }
//
//    public void shouldCreateSingleZipWithFilesWhenBzip2CompressionSuperFast() throws IOException {
//        ZipEntrySettings entrySettings = ZipEntrySettings.builder()
//                                                         .compression(Compression.BZIP2, CompressionLevel.SUPER_FAST)
//                                                         .lzmaEosMarker(true).build();
//        ZipSettings settings = ZipSettings.builder().entrySettingsProvider(fileName -> entrySettings).build();
//
//        Path zip = Zip4jvmSuite.subDirNameAsMethodName(rootDir).resolve("src.zip");
//        ZipIt.zip(zip).settings(settings).add(filesDirBikes);
//        assertThatDirectory(zip.getParent()).exists().hasDirectories(0).hasFiles(1);
//        assertThatZipFile(zip).root().matches(dirBikesAssert);
//    }
//
//    public void shouldUseCompressStoreWhenFileEmpty() throws IOException {
//        ZipEntrySettings entrySettings = ZipEntrySettings.builder().compression(Compression.BZIP2, CompressionLevel.NORMAL).build();
//        ZipSettings settings = ZipSettings.builder().entrySettingsProvider(fileName -> entrySettings).build();
//
//        Path zip = Zip4jvmSuite.subDirNameAsMethodName(rootDir).resolve("src.zip");
//
//        ZipIt.zip(zip).settings(settings).add(fileEmpty);
//        CentralDirectory.FileHeader fileHeader = ZipInfo.zip(zip).getFileHeader(fileNameEmpty);
//        assertThat(fileHeader.getCompressionMethod()).isSameAs(CompressionMethod.STORE);
//    }

}
