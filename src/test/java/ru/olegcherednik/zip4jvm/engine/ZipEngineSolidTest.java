package ru.olegcherednik.zip4jvm.engine;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.olegcherednik.zip4jvm.Zip4jvmSuite;
import ru.olegcherednik.zip4jvm.ZipFile;
import ru.olegcherednik.zip4jvm.ZipIt;
import ru.olegcherednik.zip4jvm.exception.EntryDuplicationException;
import ru.olegcherednik.zip4jvm.exception.EntryNotFoundException;
import ru.olegcherednik.zip4jvm.model.Compression;
import ru.olegcherednik.zip4jvm.model.CompressionLevel;
import ru.olegcherednik.zip4jvm.model.Encryption;
import ru.olegcherednik.zip4jvm.model.settings.ZipEntrySettings;
import ru.olegcherednik.zip4jvm.model.settings.ZipSettings;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static ru.olegcherednik.zip4jvm.TestData.dirBikes;
import static ru.olegcherednik.zip4jvm.TestData.dirCars;
import static ru.olegcherednik.zip4jvm.TestData.dirNameBikes;
import static ru.olegcherednik.zip4jvm.TestData.fileBentley;
import static ru.olegcherednik.zip4jvm.TestData.fileFerrari;
import static ru.olegcherednik.zip4jvm.TestData.fileHonda;
import static ru.olegcherednik.zip4jvm.TestData.fileKawasaki;
import static ru.olegcherednik.zip4jvm.TestData.fileNameBentley;
import static ru.olegcherednik.zip4jvm.TestData.fileNameDucati;
import static ru.olegcherednik.zip4jvm.TestData.fileNameFerrari;
import static ru.olegcherednik.zip4jvm.TestData.fileNameHonda;
import static ru.olegcherednik.zip4jvm.TestData.fileNameKawasaki;
import static ru.olegcherednik.zip4jvm.TestData.fileNameSuzuki;
import static ru.olegcherednik.zip4jvm.TestData.fileNameWiesmann;
import static ru.olegcherednik.zip4jvm.TestData.fileSuzuki;
import static ru.olegcherednik.zip4jvm.TestData.fileWiesmann;
import static ru.olegcherednik.zip4jvm.TestData.zipDirNameBikes;
import static ru.olegcherednik.zip4jvm.TestData.zipDirNameCars;
import static ru.olegcherednik.zip4jvm.TestData.zipStoreSolid;
import static ru.olegcherednik.zip4jvm.TestDataAssert.dirBikesAssert;
import static ru.olegcherednik.zip4jvm.TestDataAssert.dirCarsAssert;
import static ru.olegcherednik.zip4jvm.TestDataAssert.fileBentleyAssert;
import static ru.olegcherednik.zip4jvm.TestDataAssert.fileDucatiAssert;
import static ru.olegcherednik.zip4jvm.TestDataAssert.fileFerrariAssert;
import static ru.olegcherednik.zip4jvm.TestDataAssert.fileHondaAssert;
import static ru.olegcherednik.zip4jvm.TestDataAssert.fileKawasakiAssert;
import static ru.olegcherednik.zip4jvm.TestDataAssert.fileSuzukiAssert;
import static ru.olegcherednik.zip4jvm.TestDataAssert.fileWiesmannAssert;
import static ru.olegcherednik.zip4jvm.Zip4jvmSuite.password;
import static ru.olegcherednik.zip4jvm.assertj.Zip4jvmAssertions.assertThatDirectory;
import static ru.olegcherednik.zip4jvm.assertj.Zip4jvmAssertions.assertThatZipFile;

/**
 * @author Oleg Cherednik
 * @since 12.09.2019
 */
@Test
@SuppressWarnings("FieldNamingConvention")
public class ZipEngineSolidTest {

    private static final Path rootDir = Zip4jvmSuite.generateSubDirNameWithTime(ZipEngineSolidTest.class);
    private static final Path solidFile = rootDir.resolve("solid/src.zip");

    @BeforeClass
    private static void createSolidArchive() throws IOException {
        ZipSettings settings = ZipSettings.builder().entrySettingsProvider(entrySettingsProvider()).build();

        try (ZipFile.Writer zipFile = ZipIt.zip(solidFile).settings(settings).open()) {
            zipFile.add(fileBentley);
            zipFile.add(fileFerrari);
            zipFile.add(fileWiesmann);
            zipFile.add(fileHonda);
        }

        assertThatDirectory(solidFile.getParent()).exists().hasDirectories(0).hasFiles(1);
        assertThatZipFile(solidFile, password).exists().root().hasDirectories(0).hasFiles(4);
        assertThatZipFile(solidFile, password).file(fileNameBentley).matches(fileBentleyAssert);
        assertThatZipFile(solidFile, password).file(fileNameFerrari).matches(fileFerrariAssert);
        assertThatZipFile(solidFile, password).file(fileNameWiesmann).matches(fileWiesmannAssert);
        assertThatZipFile(solidFile, fileNameHonda.toCharArray()).file(fileNameHonda).matches(fileHondaAssert);
    }

    @AfterClass(enabled = Zip4jvmSuite.clear)
    public static void removeDir() throws IOException {
        Zip4jvmSuite.removeDir(rootDir);
    }

    private static Function<String, ZipEntrySettings> entrySettingsProvider() {
        return fileName -> {
            if (fileNameBentley.equals(fileName))
                return ZipEntrySettings.builder().compression(Compression.STORE, CompressionLevel.NORMAL).build();
            if (fileNameFerrari.equals(fileName))
                return ZipEntrySettings.builder().compression(Compression.DEFLATE, CompressionLevel.NORMAL).build();
            if (fileNameWiesmann.equals(fileName))
                return ZipEntrySettings.builder()
                                       .encryption(Encryption.PKWARE, password)
                                       .compression(Compression.DEFLATE, CompressionLevel.NORMAL).build();
            if (fileNameHonda.equals(fileName))
                return ZipEntrySettings.builder()
                                       .encryption(Encryption.AES_256, fileNameHonda.toCharArray())
                                       .compression(Compression.DEFLATE, CompressionLevel.NORMAL).build();
            return ZipEntrySettings.DEFAULT;
        };
    }

    public void shouldThrowNullPointerExceptionWhenArgumentIsNull() {
        assertThatThrownBy(() -> new ZipEngine(null, ZipSettings.DEFAULT)).isExactlyInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new ZipEngine(zipStoreSolid, null)).isExactlyInstanceOf(NullPointerException.class);
    }

    public void shouldAddFilesToExistedZipWhenUseZipFile() throws IOException {
        Path zip = Zip4jvmSuite.copy(Zip4jvmSuite.subDirNameAsMethodName(rootDir), solidFile);

        Function<String, ZipEntrySettings> entrySettingsProvider = fileName -> {
            if (fileNameKawasaki.equals(fileName))
                return ZipEntrySettings.builder()
                                       .compression(Compression.STORE, CompressionLevel.NORMAL)
                                       .encryption(Encryption.PKWARE, password).build();
            if (fileNameSuzuki.equals(fileName))
                return ZipEntrySettings.builder()
                                       .compression(Compression.DEFLATE, CompressionLevel.NORMAL)
                                       .encryption(Encryption.AES_256, fileNameSuzuki.toCharArray()).build();
            return ZipEntrySettings.DEFAULT;
        };

        ZipSettings settings = ZipSettings.builder().entrySettingsProvider(entrySettingsProvider).build();

        try (ZipFile.Writer zipFile = ZipIt.zip(zip).settings(settings).open()) {
            zipFile.add(fileKawasaki);
            zipFile.add(fileSuzuki);
        }

        assertThatDirectory(zip.getParent()).exists().hasDirectories(0).hasFiles(1);
        assertThatZipFile(zip, password).exists().root().hasDirectories(0).hasFiles(6);
        assertThatZipFile(zip, password).file(fileNameBentley).matches(fileBentleyAssert);
        assertThatZipFile(zip, password).file(fileNameFerrari).matches(fileFerrariAssert);
        assertThatZipFile(zip, fileNameHonda.toCharArray()).file(fileNameHonda).matches(fileHondaAssert);
        assertThatZipFile(zip, password).file(fileNameKawasaki).matches(fileKawasakiAssert);
        assertThatZipFile(zip, fileNameSuzuki.toCharArray()).file(fileNameSuzuki).matches(fileSuzukiAssert);
    }

    public void shouldThrowExceptionWhenAddDuplicateEntry() throws IOException {
        Path zip = Zip4jvmSuite.copy(Zip4jvmSuite.subDirNameAsMethodName(rootDir), solidFile);

        assertThatThrownBy(() -> {
            try (ZipFile.Writer zipFile = ZipIt.zip(zip).open()) {
                zipFile.add(fileBentley);
            }
        }).isExactlyInstanceOf(EntryDuplicationException.class);
    }

    public void shouldThrowExceptionWhenAddNullEntry() throws IOException {
        Path zip = Zip4jvmSuite.copy(Zip4jvmSuite.subDirNameAsMethodName(rootDir), solidFile);

        assertThatThrownBy(() -> {
            try (ZipFile.Writer zipFile = new ZipEngine(zip, ZipSettings.DEFAULT)) {
                zipFile.add((ZipFile.Entry)null);
            }
        }).isExactlyInstanceOf(NullPointerException.class);
    }

    public void shouldThrowExceptionWhenRemoveWithBlankName() throws IOException {
        Path zip = Zip4jvmSuite.copy(Zip4jvmSuite.subDirNameAsMethodName(rootDir), solidFile);

        for (String prefixEntryName : Arrays.asList(null, "", "  ")) {
            assertThatThrownBy(() -> {
                try (ZipFile.Writer zipFile = new ZipEngine(zip, ZipSettings.DEFAULT)) {
                    zipFile.removeEntryByName(prefixEntryName);
                }
            }).isExactlyInstanceOf(IllegalArgumentException.class);
        }
    }

    public void shouldAddDirectoryWhenZipExists() throws IOException {
        Path zip = Zip4jvmSuite.copy(Zip4jvmSuite.subDirNameAsMethodName(rootDir), solidFile);

        try (ZipFile.Writer zipFile = ZipIt.zip(zip).open()) {
            zipFile.add(dirBikes);
            zipFile.add(dirCars);
        }

        assertThatDirectory(zip.getParent()).exists().hasDirectories(0).hasFiles(1);
        assertThatZipFile(zip, password).exists().root().hasDirectories(2).hasFiles(4);
        assertThatZipFile(zip, password).file(fileNameBentley).matches(fileBentleyAssert);
        assertThatZipFile(zip, password).file(fileNameFerrari).matches(fileFerrariAssert);
        assertThatZipFile(zip, password).file(fileNameWiesmann).matches(fileWiesmannAssert);
        assertThatZipFile(zip, fileNameHonda.toCharArray()).file(fileNameHonda).matches(fileHondaAssert);
        assertThatZipFile(zip, password).directory(zipDirNameBikes).matches(dirBikesAssert);
        assertThatZipFile(zip, password).directory(zipDirNameCars).matches(dirCarsAssert);
    }

    public void shouldRemoveExistedEntityWhenNormalizeName() throws IOException {
        Path zip = Zip4jvmSuite.copy(Zip4jvmSuite.subDirNameAsMethodName(rootDir), solidFile);
        ZipIt.zip(zip).add(dirBikes);

        try (ZipFile.Writer zipFile = ZipIt.zip(zip).open()) {
            zipFile.removeEntryByName(zipDirNameBikes + fileNameHonda);
        }

        assertThatDirectory(zip.getParent()).exists().hasDirectories(0).hasFiles(1);
        assertThatZipFile(zip, password).exists().root().hasDirectories(1).hasFiles(4);
        assertThatZipFile(zip, password).file(fileNameBentley).matches(fileBentleyAssert);
        assertThatZipFile(zip, password).file(fileNameFerrari).matches(fileFerrariAssert);
        assertThatZipFile(zip, password).file(fileNameWiesmann).matches(fileWiesmannAssert);
        assertThatZipFile(zip, fileNameHonda.toCharArray()).file(fileNameHonda).matches(fileHondaAssert);
        assertThatZipFile(zip, password).directory(zipDirNameBikes).hasDirectories(0).hasFiles(3);
        assertThatZipFile(zip, password).file(zipDirNameBikes + fileNameDucati).matches(fileDucatiAssert);
        assertThatZipFile(zip, password).file(zipDirNameBikes + fileNameKawasaki).matches(fileKawasakiAssert);
        assertThatZipFile(zip, password).file(zipDirNameBikes + fileNameSuzuki).matches(fileSuzukiAssert);
    }

    public void shouldRemoveEntryWhenNotNormalizeName() throws IOException {
        Path zip = Zip4jvmSuite.copy(Zip4jvmSuite.subDirNameAsMethodName(rootDir), solidFile);
        ZipIt.zip(zip).add(dirBikes);

        try (ZipFile.Writer zipFile = ZipIt.zip(zip).open()) {
            zipFile.removeEntryByName(dirNameBikes + '\\' + fileNameHonda);
        }

        assertThatDirectory(zip.getParent()).exists().hasDirectories(0).hasFiles(1);
        assertThatZipFile(zip, password).exists().root().hasDirectories(1).hasFiles(4);
        assertThatZipFile(zip, password).file(fileNameBentley).matches(fileBentleyAssert);
        assertThatZipFile(zip, password).file(fileNameFerrari).matches(fileFerrariAssert);
        assertThatZipFile(zip, password).file(fileNameWiesmann).matches(fileWiesmannAssert);
        assertThatZipFile(zip, fileNameHonda.toCharArray()).file(fileNameHonda).matches(fileHondaAssert);
        assertThatZipFile(zip, password).directory(zipDirNameBikes).hasDirectories(0).hasFiles(3);
        assertThatZipFile(zip, password).file(zipDirNameBikes + fileNameDucati).matches(fileDucatiAssert);
        assertThatZipFile(zip, password).file(zipDirNameBikes + fileNameKawasaki).matches(fileKawasakiAssert);
        assertThatZipFile(zip, password).file(zipDirNameBikes + fileNameSuzuki).matches(fileSuzukiAssert);
    }

    public void shouldRemoveDirectoryWhenNoDirectoryMarker() throws IOException {
        Path zip = Zip4jvmSuite.copy(Zip4jvmSuite.subDirNameAsMethodName(rootDir), solidFile);
        ZipIt.zip(zip).add(dirBikes);

        try (ZipFile.Writer zipFile = ZipIt.zip(zip).open()) {
            zipFile.removeEntryByNamePrefix(dirNameBikes);
        }

        assertThatDirectory(zip.getParent()).exists().hasDirectories(0).hasFiles(1);
        assertThatZipFile(zip, password).exists().root().hasDirectories(0).hasFiles(4);
        assertThatZipFile(zip, password).file(fileNameBentley).matches(fileBentleyAssert);
        assertThatZipFile(zip, password).file(fileNameFerrari).matches(fileFerrariAssert);
        assertThatZipFile(zip, password).file(fileNameWiesmann).matches(fileWiesmannAssert);
    }

    public void shouldThrowExceptionWhenRemoveNotExistedEntry() throws IOException {
        Path zip = Zip4jvmSuite.copy(Zip4jvmSuite.subDirNameAsMethodName(rootDir), solidFile);

        assertThatThrownBy(() -> {
            try (ZipFile.Writer zipFile = ZipIt.zip(zip).open()) {
                zipFile.removeEntryByName(fileNameKawasaki);
            }
        }).isExactlyInstanceOf(EntryNotFoundException.class);
    }

    public void shouldThrowExceptionWhenCopyNullEntry() throws IOException {
        Path zip = Zip4jvmSuite.copy(Zip4jvmSuite.subDirNameAsMethodName(rootDir), solidFile);

        assertThatThrownBy(() -> {
            try (ZipFile.Writer zipFile = new ZipEngine(zip, ZipSettings.DEFAULT)) {
                zipFile.copy(null);
            }
        }).isExactlyInstanceOf(IllegalArgumentException.class);
    }

}
