package ru.olegcherednik.zip4jvm;

import org.apache.commons.io.IOUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.olegcherednik.zip4jvm.model.Compression;
import ru.olegcherednik.zip4jvm.model.CompressionLevel;
import ru.olegcherednik.zip4jvm.model.Encryption;
import ru.olegcherednik.zip4jvm.model.ZipModel;
import ru.olegcherednik.zip4jvm.model.builders.ZipModelBuilder;
import ru.olegcherednik.zip4jvm.model.settings.ZipEntrySettings;
import ru.olegcherednik.zip4jvm.model.settings.ZipFileSettings;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.olegcherednik.zip4jvm.TestData.contentDirSrc;
import static ru.olegcherednik.zip4jvm.TestDataAssert.dirSrcAssert;
import static ru.olegcherednik.zip4jvm.TestDataAssert.zipDirRootAssert;
import static ru.olegcherednik.zip4jvm.Zip4jvmSuite.SIZE_1MB;
import static ru.olegcherednik.zip4jvm.Zip4jvmSuite.password;
import static ru.olegcherednik.zip4jvm.Zip4jvmSuite.passwordStr;
import static ru.olegcherednik.zip4jvm.assertj.Zip4jvmAssertions.assertThatDirectory;
import static ru.olegcherednik.zip4jvm.assertj.Zip4jvmAssertions.assertThatZipFile;

/**
 * @author Oleg Cherednik
 * @since 06.04.2019
 */
@Test
@SuppressWarnings({ "FieldNamingConvention", "NewClassNamingConvention" })
public class Zip64Test {

    private static final Path rootDir = Zip4jvmSuite.generateSubDirNameWithTime(Zip64Test.class);

    private Path zipSimple;
    private Path zipAes;
    private Path zipSplit;
    private Path zipManyEntries;
    private Path zipHugeEntry;

    @BeforeClass
    public static void createDir() throws IOException {
        Files.createDirectories(rootDir);
    }

    @AfterClass(enabled = Zip4jvmSuite.clear)
    public static void removeDir() throws IOException {
        Zip4jvmSuite.removeDir(rootDir);
    }

    public void shouldZipWhenZip64() throws IOException {
        ZipFileSettings settings = ZipFileSettings.builder().zip64(true).build();

        zipSimple = Zip4jvmSuite.subDirNameAsMethodName(rootDir).resolve("src.zip");
        ZipIt.add(zipSimple, contentDirSrc, settings);

        assertThatDirectory(zipSimple.getParent()).exists().hasDirectories(0).hasFiles(1);
        assertThatZipFile(zipSimple).root().matches(zipDirRootAssert);
    }

    @Test(dependsOnMethods = "shouldZipWhenZip64")
    public void shouldUnzipWhenZip64() throws IOException {
        Path destDir = Zip4jvmSuite.subDirNameAsMethodName(rootDir);
        UnzipIt.extract(zipSimple, destDir);
        assertThatDirectory(destDir).matches(dirSrcAssert);
    }

    public void shouldZipWhenZip64AndAesEncryption() throws IOException {
        ZipEntrySettings entrySettings = ZipEntrySettings.builder().encryption(Encryption.AES_256, password).build();
        ZipFileSettings settings = ZipFileSettings.builder()
                                                  .entrySettingsProvider(fileName -> entrySettings)
                                                  .comment("password: " + passwordStr)
                                                  .zip64(true).build();

        zipAes = Zip4jvmSuite.subDirNameAsMethodName(rootDir).resolve("src.zip");
        ZipIt.add(zipAes, contentDirSrc, settings);

        assertThatDirectory(zipAes.getParent()).exists().hasDirectories(0).hasFiles(1);
        assertThatZipFile(zipAes, password).root().matches(zipDirRootAssert);
    }

    @Test(dependsOnMethods = "shouldZipWhenZip64AndAesEncryption")
    public void shouldUnzipWhenZip64AndAesEncryption() throws IOException {
        Path destDir = Zip4jvmSuite.subDirNameAsMethodName(rootDir);
        UnzipIt.extract(zipAes, destDir, fileName -> password);
        assertThatDirectory(destDir).matches(dirSrcAssert);
    }

    public void shouldZipWhenZip64AndSplit() throws IOException {
        ZipFileSettings settings = ZipFileSettings.builder().splitSize(SIZE_1MB).zip64(true).build();

        zipSplit = Zip4jvmSuite.subDirNameAsMethodName(rootDir).resolve("src.zip");
        ZipIt.add(zipSplit, contentDirSrc, settings);

        // TODO it seems it could be checked with commons-compress
//        assertThatDirectory(zipFile.getParent()).exists().hasSubDirectories(0).hasFiles(1);
//        assertThatZipFile(zipFile).directory("/").matches(TestUtils.zipRootDirAssert);
    }

    @Test(dependsOnMethods = "shouldZipWhenZip64AndSplit")
    public void shouldUnzipWhenZip64AndSplit() throws IOException {
        Path destDir = Zip4jvmSuite.subDirNameAsMethodName(rootDir);
        UnzipIt.extract(zipSplit, destDir);
        assertThatDirectory(destDir).matches(dirSrcAssert);
    }

    public void shouldUseZip64WhenTotalEntriesOverFFFF() throws IOException {
        zipManyEntries = Zip4jvmSuite.subDirNameAsMethodName(rootDir).resolve("src.zip");

        try (ZipFile.Writer zipFile = ZipFile.write(zipManyEntries)) {
            IntStream.rangeClosed(1, ZipModel.MAX_TOTAL_ENTRIES + 1)
                     .mapToObj(i -> "file_" + i + ".txt")
                     .map(fileName -> ZipFile.Entry.builder()
                                                   .inputStreamSup(() -> IOUtils.toInputStream(fileName, StandardCharsets.UTF_8))
                                                   .fileName(fileName).build())
                     .forEach(zipFile::addEntry);
        }

        ZipModel zipModel = ZipModelBuilder.read(zipManyEntries);

        assertThatDirectory(zipManyEntries.getParent()).exists().hasDirectories(0).hasFiles(1);
        assertThat(zipModel.getEntryNames()).hasSize(ZipModel.MAX_TOTAL_ENTRIES + 1);
        assertThat(zipModel.isZip64()).isTrue();
    }

//    // TODO it works but it's too slow
//    @Test(dependsOnMethods = "shouldUseZip64WhenTotalEntriesOverFFFF")
//    public void shouldUnzipZip64WhenTotalEntriesOverFFFF() throws IOException {
//        Path destDir = Zip4jvmSuite.subDirNameAsMethodName(rootDir);
//        UnzipIt.extract(zipManyEntries, destDir);
//        assertThatDirectory(destDir).hasDirectories(0).hasFiles(ZipModel.MAX_TOTAL_ENTRIES + 1);
//    }

    public void shouldUseZip64WhenEntrySizeOverFFFFFFFF() throws IOException {
        Path dir = Zip4jvmSuite.subDirNameAsMethodName(rootDir);
        Files.createDirectories(dir);

        Path file = dir.resolve("file.txt");

        try (RandomAccessFile f = new RandomAccessFile(file.toFile(), "rw")) {
            f.setLength(ZipModel.MAX_ENTRY_SIZE + 1);
        }

        zipHugeEntry = dir.resolve("src.zip");
        ZipEntrySettings entrySettings = ZipEntrySettings.builder().compression(Compression.STORE, CompressionLevel.NORMAL).build();
        ZipFileSettings settings = ZipFileSettings.builder().entrySettingsProvider(fileNam -> entrySettings).build();
        ZipIt.add(zipHugeEntry, file, settings);

        ZipModel zipModel = ZipModelBuilder.read(zipHugeEntry);
        assertThat(zipModel.getEntryByFileName("file.txt").getUncompressedSize()).isEqualTo(ZipModel.MAX_ENTRY_SIZE + 1);
    }

}
