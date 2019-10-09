package ru.olegcherednik.zip4jvm.encryption;

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

import static ru.olegcherednik.zip4jvm.TestData.fileOlegCherednik;

/**
 * @author Oleg Cherednik
 * @since 08.10.2019
 */
@Test
@SuppressWarnings("FieldNamingConvention")
public class EncryptionCentralDirectoryTest {

    private static final Path rootDir = Zip4jvmSuite.generateSubDirNameWithTime(EncryptionCentralDirectoryTest.class);

    @BeforeClass
    public static void createDir() throws IOException {
        Files.createDirectories(rootDir);
    }

    @AfterClass(enabled = Zip4jvmSuite.clear)
    public static void removeDir() throws IOException {
        Zip4jvmSuite.removeDir(rootDir);
    }

    public void shouldEncryptCentralDirectory() throws IOException {
        ZipEntrySettings entrySettings = ZipEntrySettings.builder().compression(Compression.STORE, CompressionLevel.NORMAL).build();
        ZipSettings settings = ZipSettings.builder().entrySettings(entrySettings).encryptFileNames(true).build();

        Path zip = Zip4jvmSuite.subDirNameAsMethodName(rootDir).resolve("src.zip");
        ZipIt.zip(zip).settings(settings).add(fileOlegCherednik);
    }
}
