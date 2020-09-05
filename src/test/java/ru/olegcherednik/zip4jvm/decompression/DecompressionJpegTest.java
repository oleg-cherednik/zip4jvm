package ru.olegcherednik.zip4jvm.decompression;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.olegcherednik.zip4jvm.UnzipIt;
import ru.olegcherednik.zip4jvm.Zip4jvmSuite;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author Oleg Cherednik
 * @since 04.09.2020
 */
@Test
@SuppressWarnings("FieldNamingConvention")
public class DecompressionJpegTest {

    private static final Path rootDir = Zip4jvmSuite.generateSubDirNameWithTime(DecompressionJpegTest.class);

    @BeforeClass
    public static void createDir() throws IOException {
        Files.createDirectories(rootDir);
    }

    @AfterClass(enabled = Zip4jvmSuite.clear)
    public static void removeDir() throws IOException {
        Zip4jvmSuite.removeDir(rootDir);
    }

    public void shouldDecompressSingleZipWithFilesWhenJpegCompression() throws IOException {
        Path zip = Paths.get("src/test/resources/zip/jpeg.zip").toAbsolutePath();
        Path destDir = Paths.get("d:/zip4jvm/jpeg/decompose");

//        ZipInfo.zip(zip)
//               .settings(ZipInfoSettings.builder()
//                                        .copyPayload(true)
//                                        .build())
//               .decompose(destDir);
        UnzipIt.zip(zip).destDir(destDir).extract();


//        ZipEntrySettings entrySettings = ZipEntrySettings.builder()
//                                                         .compression(Compression.LZMA, CompressionLevel.NORMAL)
//                                                         .lzmaEosMarker(true).build();
//        ZipSettings settings = ZipSettings.builder().entrySettingsProvider(fileName -> entrySettings).build();
//
//        Path zip = Zip4jvmSuite.subDirNameAsMethodName(rootDir).resolve("src.zip");
//
//        ZipIt.zip(zip).settings(settings).add(filesDirBikes);
//        assertThatDirectory(zip.getParent()).exists().hasDirectories(0).hasFiles(1);
//        assertThatZipFile(zip).root().matches(dirBikesAssert);
    }

}
