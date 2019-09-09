package ru.olegcherednik.zip4jvm;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.olegcherednik.zip4jvm.model.settings.ZipFileReaderSettings;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author Oleg Cherednik
 * @since 07.09.2019
 */
@Test
@SuppressWarnings("FieldNamingConvention")
public class ZipFileReaderTest {

    private static final Path rootDir = Zip4jSuite.generateSubDirNameWithTime(ZipFileReaderTest.class);

    @BeforeClass
    public static void createDir() throws IOException {
        Files.createDirectories(rootDir);
    }

    @AfterClass(enabled = Zip4jSuite.clear)
    public static void removeDir() throws IOException {
        Zip4jSuite.removeDir(rootDir);
    }

//    public void shouldUnzipZipFileIntoDestinationFolderWhenDeflateSolid() throws IOException {
//        Path destDir = Zip4jSuite.subDirNameAsMethodName(rootDir);
//        ZipFileReadSettings settings = ZipFileReadSettings.builder().build();
//        ZipFileReader zipFile = new ZipFileReader(Zip4jSuite.deflateSolidZip, settings);
//        zipFile.extract(destDir);
//
////        Zip4jAssertions.assertThatDirectory(destDir).exists().hasSubDirectories(1).hasFiles(0);
////
////        Path starWarsDir = destDir.resolve("Star Wars/");
////        Zip4jAssertions.assertThatDirectory(starWarsDir).exists().hasSubDirectories(0).hasFiles(4);
////        Zip4jAssertions.assertThatFile(starWarsDir.resolve("one.jpg")).isImage().hasSize(2_204_448);
////        Zip4jAssertions.assertThatFile(starWarsDir.resolve("two.jpg")).isImage().hasSize(277_857);
////        Zip4jAssertions.assertThatFile(starWarsDir.resolve("three.jpg")).isImage().hasSize(1_601_879);
////        Zip4jAssertions.assertThatFile(starWarsDir.resolve("four.jpg")).isImage().hasSize(1_916_776);
//    }

    public void shouldUnzipZipFileIntoDestinationFolderWhenStoreSolidPkware() throws IOException {
        Path destDir = Zip4jSuite.subDirNameAsMethodName(rootDir);
        ZipFileReaderSettings settings = ZipFileReaderSettings.builder()
                                                              .password(fileName -> Zip4jSuite.password)
                                                              .build();
        ZipFile.Reader zipFile = ZipFile.read(Zip4jSuite.storeSolidPkwareZip, settings);
        zipFile.extract(destDir);

//        Zip4jAssertions.assertThatDirectory(destDir).exists().hasSubDirectories(1).hasFiles(0);
//
//        Path starWarsDir = destDir.resolve("Star Wars/");
//        Zip4jAssertions.assertThatDirectory(starWarsDir).exists().hasSubDirectories(0).hasFiles(4);
//        Zip4jAssertions.assertThatFile(starWarsDir.resolve("one.jpg")).isImage().hasSize(2_204_448);
//        Zip4jAssertions.assertThatFile(starWarsDir.resolve("two.jpg")).isImage().hasSize(277_857);
//        Zip4jAssertions.assertThatFile(starWarsDir.resolve("three.jpg")).isImage().hasSize(1_601_879);
//        Zip4jAssertions.assertThatFile(starWarsDir.resolve("four.jpg")).isImage().hasSize(1_916_776);
    }

    public void shouldUnzipZipFileIntoDestinationFolderWhenStoreSolidAes() throws IOException {
        Path destDir = Zip4jSuite.subDirNameAsMethodName(rootDir);
        ZipFileReaderSettings settings = ZipFileReaderSettings.builder()
                                                              .password(String::toCharArray)
                                                              .build();
        ZipFile.Reader zipFile = ZipFile.read(Zip4jSuite.storeSolidAesZip, settings);
        zipFile.extract(destDir);

//        Zip4jAssertions.assertThatDirectory(destDir).exists().hasSubDirectories(1).hasFiles(0);
//
//        Path starWarsDir = destDir.resolve("Star Wars/");
//        Zip4jAssertions.assertThatDirectory(starWarsDir).exists().hasSubDirectories(0).hasFiles(4);
//        Zip4jAssertions.assertThatFile(starWarsDir.resolve("one.jpg")).isImage().hasSize(2_204_448);
//        Zip4jAssertions.assertThatFile(starWarsDir.resolve("two.jpg")).isImage().hasSize(277_857);
//        Zip4jAssertions.assertThatFile(starWarsDir.resolve("three.jpg")).isImage().hasSize(1_601_879);
//        Zip4jAssertions.assertThatFile(starWarsDir.resolve("four.jpg")).isImage().hasSize(1_916_776);
    }
}
