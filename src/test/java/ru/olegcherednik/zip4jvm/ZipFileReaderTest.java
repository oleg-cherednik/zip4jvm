package ru.olegcherednik.zip4jvm;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

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
        UnzipIt.extract(Zip4jSuite.storeSolidPkwareZip, destDir, fileName -> Zip4jSuite.password);

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
        UnzipIt.extract(Zip4jSuite.storeSolidAesZip, destDir, String::toCharArray);

//        Zip4jAssertions.assertThatDirectory(destDir).exists().hasSubDirectories(1).hasFiles(0);
//
//        Path starWarsDir = destDir.resolve("Star Wars/");
//        Zip4jAssertions.assertThatDirectory(starWarsDir).exists().hasSubDirectories(0).hasFiles(4);
//        Zip4jAssertions.assertThatFile(starWarsDir.resolve("one.jpg")).isImage().hasSize(2_204_448);
//        Zip4jAssertions.assertThatFile(starWarsDir.resolve("two.jpg")).isImage().hasSize(277_857);
//        Zip4jAssertions.assertThatFile(starWarsDir.resolve("three.jpg")).isImage().hasSize(1_601_879);
//        Zip4jAssertions.assertThatFile(starWarsDir.resolve("four.jpg")).isImage().hasSize(1_916_776);
    }

    public void shouldIterateOverAllEntriesWhenStoreSolidPkware() throws IOException {
        List<String> entryNames = new ArrayList<>();

        for (ZipFile.Entry entry : ZipFile.read(Zip4jSuite.storeSolidPkwareZip))
            entryNames.add(entry.getFileName());

        assertThat(entryNames).containsExactlyInAnyOrder(
                "cars/bentley-continental.jpg",
                "cars/ferrari-458-italia.jpg",
                "cars/wiesmann-gt-mf5.jpg",
                "Star Wars/one.jpg",
                "Star Wars/two.jpg",
                "Star Wars/three.jpg",
                "Star Wars/four.jpg",
                "empty_dir",
                "empty_file.txt",
                "mcdonnell-douglas-f15-eagle.jpg",
                "Oleg Cherednik.txt",
                "saint-petersburg.jpg",
                "sig-sauer-pistol.jpg");
    }

    public void shouldRetrieveStreamWithAllEntriesWhenStoreSolidPkware() throws IOException {
        List<String> entryNames = ZipFile.read(Zip4jSuite.storeSolidPkwareZip).stream()
                                         .map(ZipFile.Entry::getFileName)
                                         .collect(Collectors.toList());

        assertThat(entryNames).containsExactlyInAnyOrder(
                "cars/bentley-continental.jpg",
                "cars/ferrari-458-italia.jpg",
                "cars/wiesmann-gt-mf5.jpg",
                "Star Wars/one.jpg",
                "Star Wars/two.jpg",
                "Star Wars/three.jpg",
                "Star Wars/four.jpg",
                "empty_dir",
                "empty_file.txt",
                "mcdonnell-douglas-f15-eagle.jpg",
                "Oleg Cherednik.txt",
                "saint-petersburg.jpg",
                "sig-sauer-pistol.jpg");
    }
}
