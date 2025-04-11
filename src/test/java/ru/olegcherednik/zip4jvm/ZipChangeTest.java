package ru.olegcherednik.zip4jvm;

import ru.olegcherednik.zip4jvm.engine.zip.ZipEngine;
import ru.olegcherednik.zip4jvm.exception.EntryDuplicationException;
import ru.olegcherednik.zip4jvm.model.Compression;
import ru.olegcherednik.zip4jvm.model.settings.ZipSettings;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static ru.olegcherednik.zip4jvm.TestData.fileBentley;
import static ru.olegcherednik.zip4jvm.TestData.fileNameBentley;

/**
 * @author Oleg Cherednik
 * @since 11.04.2025
 */
@Test
public class ZipChangeTest {

    private static final Path ROOT_DIR = Zip4jvmSuite.generateSubDirNameWithTime(ZipItTest.class);

    @BeforeClass
    public static void createDir() throws IOException {
        Files.createDirectories(ROOT_DIR);
    }

    @AfterClass(enabled = Zip4jvmSuite.clear)
    public static void removeDir() throws IOException {
        Zip4jvmSuite.removeDir(ROOT_DIR);
    }

    public void shouldNotChangeSrcZipWhenAddDuplicateEntry() throws IOException, InterruptedException {
        Path zip = Zip4jvmSuite.subDirNameAsMethodName(ROOT_DIR).resolve("src.zip");
        ZipIt.zip(zip).add(fileBentley);
        Thread.sleep(500);

        long expectedLastModifiedTime = Files.getLastModifiedTime(zip).toMillis();
        assertThatThrownBy(() -> ZipIt.zip(zip).add(fileBentley)).isExactlyInstanceOf(EntryDuplicationException.class);
        assertThat(Files.getLastModifiedTime(zip).toMillis()).isEqualTo(expectedLastModifiedTime);
    }

    public void shouldNotChangeSrcZipWhenNoChanges() throws IOException, InterruptedException {
        Path zip = Zip4jvmSuite.subDirNameAsMethodName(ROOT_DIR).resolve("src.zip");
        ZipIt.zip(zip).add(fileBentley);
        Thread.sleep(500);

        long expectedLastModifiedTime = Files.getLastModifiedTime(zip).toMillis();

        try (ZipEngine zipFile = ZipFile.writer(zip, ZipSettings.of(Compression.STORE))) {
            zipFile.markSuccess();
        }

        assertThat(Files.getLastModifiedTime(zip).toMillis()).isEqualTo(expectedLastModifiedTime);
    }

    public void shouldChangeSrcZipWhenRemoveEntry() throws IOException, InterruptedException {
        Path zip = Zip4jvmSuite.subDirNameAsMethodName(ROOT_DIR).resolve("src.zip");
        ZipIt.zip(zip).add(fileBentley);

        Thread.sleep(500);

        long lastModifiedTime = Files.getLastModifiedTime(zip).toMillis();

        try (ZipEngine zipFile = ZipFile.writer(zip, ZipSettings.of(Compression.STORE))) {
            zipFile.removeEntryByName(fileNameBentley);
            zipFile.markSuccess();
        }

        assertThat(Files.getLastModifiedTime(zip).toMillis()).isGreaterThan(lastModifiedTime);
    }

}
