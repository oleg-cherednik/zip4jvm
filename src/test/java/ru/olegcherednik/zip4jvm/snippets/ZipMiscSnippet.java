package ru.olegcherednik.zip4jvm.snippets;

import org.apache.commons.io.FileUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.olegcherednik.zip4jvm.Zip4jvmSuite;
import ru.olegcherednik.zip4jvm.ZipFile;
import ru.olegcherednik.zip4jvm.ZipMisc;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.olegcherednik.zip4jvm.TestData.dirNameBikes;
import static ru.olegcherednik.zip4jvm.TestData.dirNameCars;
import static ru.olegcherednik.zip4jvm.TestData.fileNameFerrari;
import static ru.olegcherednik.zip4jvm.TestData.fileNameHonda;
import static ru.olegcherednik.zip4jvm.TestData.zipDeflateSolid;
import static ru.olegcherednik.zip4jvm.TestData.zipDeflateSplit;

/**
 * @author Oleg Cherednik
 * @since 07.10.2019
 */
@Test
@SuppressWarnings({ "FieldNamingConvention", "NewClassNamingConvention" })
public class ZipMiscSnippet {

    private static final Path rootDir = Zip4jvmSuite.generateSubDirNameWithTime(ZipMiscSnippet.class);
    private static final Path zip = rootDir.resolve("filename.zip");

    @BeforeClass
    public static void createDir() throws IOException {
        Files.createDirectories(rootDir);
        FileUtils.copyFile(zipDeflateSolid.toFile(), zip.toFile());
    }

    @AfterClass(enabled = Zip4jvmSuite.clear)
    public static void removeDir() throws IOException {
        Zip4jvmSuite.removeDir(rootDir);
    }

    public void modifyZipArchiveComment() throws IOException {
        Path zip = Zip4jvmSuite.subDirNameAsMethodName(rootDir).resolve("filename.zip");
        FileUtils.copyFile(zipDeflateSolid.toFile(), zip.toFile());

        ZipMisc zipFile = ZipMisc.zip(zip);

        assertThat(zipFile.getComment()).isNull();

        zipFile.setComment("new comment");
        assertThat(zipFile.getComment()).isEqualTo("new comment");

        zipFile.setComment("  ");
        assertThat(zipFile.getComment()).isEqualTo("  ");

        zipFile.setComment("");
        assertThat(zipFile.getComment()).isNull();

        zipFile.setComment(null);
        assertThat(zipFile.getComment()).isNull();
    }

    public void getAllEntries() throws IOException {
        ZipMisc zipFile = ZipMisc.zip(zip);
        List<ZipFile.Entry> entries = zipFile.getEntries().collect(Collectors.toList());
    }

    public void removeEntryByName() throws IOException {
        Path zip = Zip4jvmSuite.subDirNameAsMethodName(rootDir).resolve("filename.zip");
        FileUtils.copyFile(zipDeflateSolid.toFile(), zip.toFile());

        ZipMisc zipFile = ZipMisc.zip(zip);
        zipFile.entryName(dirNameCars + '/' + fileNameFerrari).removeEntryByName();
    }

    public void removeSomeEntriesByName() throws IOException {
        Path zip = Zip4jvmSuite.subDirNameAsMethodName(rootDir).resolve("filename.zip");
        FileUtils.copyFile(zipDeflateSolid.toFile(), zip.toFile());

        Collection<String> entryNames = Arrays.asList(dirNameCars + '/' + fileNameFerrari, dirNameBikes + '/' + fileNameHonda);

        ZipMisc zipFile = ZipMisc.zip(zip);
        zipFile.entryName(entryNames).removeEntryByName();

        int a = 0;
        a++;
    }

    public void removeEntryByNamePrefix() throws IOException {
        Path zip = Zip4jvmSuite.subDirNameAsMethodName(rootDir).resolve("filename.zip");
        FileUtils.copyFile(zipDeflateSolid.toFile(), zip.toFile());

        ZipMisc zipFile = ZipMisc.zip(zip);
        zipFile.entryName(dirNameCars).removeEntryByNamePrefix();

        int a = 0;
        a++;
    }

    public void checkWhetherZipArchiveSplitOrNot() throws IOException {
        ZipMisc zipFile = ZipMisc.zip(zip);
        assertThat(ZipMisc.zip(zip).isSplit()).isFalse();
    }

    public void mergeSplitArchiveIntoSolidOne() throws IOException {
        Path zip = Zip4jvmSuite.subDirNameAsMethodName(rootDir).resolve("filename.zip");
        ZipMisc zipFile = ZipMisc.zip(zipDeflateSplit);
        zipFile.merge(zip);
    }
}
