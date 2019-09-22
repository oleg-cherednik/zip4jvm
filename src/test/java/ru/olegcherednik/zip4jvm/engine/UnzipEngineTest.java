package ru.olegcherednik.zip4jvm.engine;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.olegcherednik.zip4jvm.UnzipIt;
import ru.olegcherednik.zip4jvm.Zip4jvmSuite;
import ru.olegcherednik.zip4jvm.ZipFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.olegcherednik.zip4jvm.TestData.fileNameDucati;
import static ru.olegcherednik.zip4jvm.TestData.fileNameHonda;
import static ru.olegcherednik.zip4jvm.TestData.fileNameKawasaki;
import static ru.olegcherednik.zip4jvm.TestData.fileNameSuzuki;
import static ru.olegcherednik.zip4jvm.TestData.zipStoreSolidAes;
import static ru.olegcherednik.zip4jvm.TestData.zipStoreSolidPkware;

/**
 * @author Oleg Cherednik
 * @since 07.09.2019
 */
@Test
@SuppressWarnings("FieldNamingConvention")
public class UnzipEngineTest {

    private static final Path rootDir = Zip4jvmSuite.generateSubDirNameWithTime(UnzipEngineTest.class);

    @BeforeClass
    public static void createDir() throws IOException {
        Files.createDirectories(rootDir);
    }

    @AfterClass(enabled = Zip4jvmSuite.clear)
    public static void removeDir() throws IOException {
        Zip4jvmSuite.removeDir(rootDir);
    }

//    public void shouldUnzipZipFileIntoDestinationFolderWhenDeflateSolid() throws IOException {
//        Path destDir = Zip4jSuite.subDirNameAsMethodName(rootDir);
//        ZipFileReadSettings settings = ZipFileReadSettings.builder().build();
//        ZipFileReader zipFile = new ZipFileReader(Zip4jSuite.deflateSolidZip, settings);
//        zipFile.extract(destDir);
//
////        Zip4jvmAssertions.assertThatDirectory(destDir).exists().hasSubDirectories(1).hasFiles(0);
////
////        Path starWarsDir = destDir.resolve("Star Wars/");
////        Zip4jvmAssertions.assertThatDirectory(starWarsDir).exists().hasSubDirectories(0).hasFiles(4);
////        Zip4jvmAssertions.assertThatFile(starWarsDir.resolve("one.jpg")).isImage().hasSize(2_204_448);
////        Zip4jvmAssertions.assertThatFile(starWarsDir.resolve("two.jpg")).isImage().hasSize(277_857);
////        Zip4jvmAssertions.assertThatFile(starWarsDir.resolve("three.jpg")).isImage().hasSize(1_601_879);
////        Zip4jvmAssertions.assertThatFile(starWarsDir.resolve("four.jpg")).isImage().hasSize(1_916_776);
//    }

    public void shouldUnzipZipFileIntoDestinationFolderWhenStoreSolidPkware() throws IOException {
        Path destDir = Zip4jvmSuite.subDirNameAsMethodName(rootDir);
        UnzipIt.extract(zipStoreSolidPkware, destDir, fileName -> Zip4jvmSuite.password);

//        Zip4jvmAssertions.assertThatDirectory(destDir).exists().hasSubDirectories(1).hasFiles(0);
//
//        Path starWarsDir = destDir.resolve("Star Wars/");
//        Zip4jvmAssertions.assertThatDirectory(starWarsDir).exists().hasSubDirectories(0).hasFiles(4);
//        Zip4jvmAssertions.assertThatFile(starWarsDir.resolve("one.jpg")).isImage().hasSize(2_204_448);
//        Zip4jvmAssertions.assertThatFile(starWarsDir.resolve("two.jpg")).isImage().hasSize(277_857);
//        Zip4jvmAssertions.assertThatFile(starWarsDir.resolve("three.jpg")).isImage().hasSize(1_601_879);
//        Zip4jvmAssertions.assertThatFile(starWarsDir.resolve("four.jpg")).isImage().hasSize(1_916_776);
    }

    public void shouldUnzipZipFileIntoDestinationFolderWhenStoreSolidAes() throws IOException {
        Path destDir = Zip4jvmSuite.subDirNameAsMethodName(rootDir);
        UnzipIt.extract(zipStoreSolidAes, destDir, String::toCharArray);

//        Zip4jvmAssertions.assertThatDirectory(destDir).exists().hasSubDirectories(1).hasFiles(0);
//
//        Path starWarsDir = destDir.resolve("Star Wars/");
//        Zip4jvmAssertions.assertThatDirectory(starWarsDir).exists().hasSubDirectories(0).hasFiles(4);
//        Zip4jvmAssertions.assertThatFile(starWarsDir.resolve("one.jpg")).isImage().hasSize(2_204_448);
//        Zip4jvmAssertions.assertThatFile(starWarsDir.resolve("two.jpg")).isImage().hasSize(277_857);
//        Zip4jvmAssertions.assertThatFile(starWarsDir.resolve("three.jpg")).isImage().hasSize(1_601_879);
//        Zip4jvmAssertions.assertThatFile(starWarsDir.resolve("four.jpg")).isImage().hasSize(1_916_776);
    }

    // TODO use constants
    public void shouldIterateOverAllEntriesWhenStoreSolidPkware() throws IOException {
        List<String> entryNames = new ArrayList<>();

        for (ZipFile.Entry entry : ZipFile.read(zipStoreSolidPkware))
            entryNames.add(entry.getFileName());

        assertThat(entryNames).containsExactlyInAnyOrder(
                "bikes/" + fileNameDucati,
                "bikes/" + fileNameHonda,
                "bikes/" + fileNameKawasaki,
                "bikes/" + fileNameSuzuki,
                "cars/bentley-continental.jpg",
                "cars/ferrari-458-italia.jpg",
                "cars/wiesmann-gt-mf5.jpg",
                "empty_dir",
                "empty_file.txt",
                "mcdonnell-douglas-f15-eagle.jpg",
                "Oleg Cherednik.txt",
                "saint-petersburg.jpg",
                "sig-sauer-pistol.jpg");
    }

    public void shouldRetrieveStreamWithAllEntriesWhenStoreSolidPkware() throws IOException {
        List<String> entryNames = ZipFile.read(zipStoreSolidPkware).stream()
                                         .map(ZipFile.Entry::getFileName)
                                         .collect(Collectors.toList());

        assertThat(entryNames).containsExactlyInAnyOrder(
                "bikes/" + fileNameDucati,
                "bikes/" + fileNameHonda,
                "bikes/" + fileNameKawasaki,
                "bikes/" + fileNameSuzuki,
                "cars/bentley-continental.jpg",
                "cars/ferrari-458-italia.jpg",
                "cars/wiesmann-gt-mf5.jpg",
                "empty_dir",
                "empty_file.txt",
                "mcdonnell-douglas-f15-eagle.jpg",
                "Oleg Cherednik.txt",
                "saint-petersburg.jpg",
                "sig-sauer-pistol.jpg");
    }
}
