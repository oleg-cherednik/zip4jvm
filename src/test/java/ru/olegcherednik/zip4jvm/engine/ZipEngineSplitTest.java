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
import static ru.olegcherednik.zip4jvm.Zip4jvmSuite.SIZE_1MB;
import static ru.olegcherednik.zip4jvm.Zip4jvmSuite.password;
import static ru.olegcherednik.zip4jvm.assertj.Zip4jvmAssertions.assertThatDirectory;
import static ru.olegcherednik.zip4jvm.assertj.Zip4jvmAssertions.assertThatZipFile;

/**
 * @author Oleg Cherednik
 * @since 12.09.2019
 */
@Test
@SuppressWarnings("FieldNamingConvention")
public class ZipEngineSplitTest {

    private static final Path rootDir = Zip4jvmSuite.generateSubDirNameWithTime(ZipEngineSplitTest.class);
    private static final Path splitFile = rootDir.resolve("split/src.zip");

    @BeforeClass
    private static void createSplitArchive() throws IOException {
        ZipSettings settings = ZipSettings.builder()
                                          .entrySettingsProvider(entrySettingsProvider())
                                          .splitSize(SIZE_1MB).build();

        try (ZipFile.Writer zipFile = ZipIt.zip(splitFile).settings(settings).open()) {
            zipFile.add(fileBentley);
            zipFile.add(fileFerrari);
            zipFile.add(fileWiesmann);
            zipFile.add(fileHonda);
        }

        assertThatDirectory(splitFile.getParent()).exists().hasDirectories(0).hasFiles(3);
        assertThatZipFile(splitFile, password).exists().root().hasDirectories(0).hasFiles(4);
        assertThatZipFile(splitFile, password).file(fileNameBentley).matches(fileBentleyAssert);
        assertThatZipFile(splitFile, password).file(fileNameFerrari).matches(fileFerrariAssert);
        assertThatZipFile(splitFile, password).file(fileNameWiesmann).matches(fileWiesmannAssert);
        assertThatZipFile(splitFile, fileNameHonda.toCharArray()).file(fileNameHonda).matches(fileHondaAssert);
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
        Path zip = Zip4jvmSuite.copy(Zip4jvmSuite.subDirNameAsMethodName(rootDir), splitFile);

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

        ZipSettings settings = ZipSettings.builder()
                                          .entrySettingsProvider(entrySettingsProvider)
                                          .splitSize(SIZE_1MB).build();

        try (ZipFile.Writer zipFile = ZipIt.zip(zip).settings(settings).open()) {
            zipFile.add(fileKawasaki);
            zipFile.add(fileSuzuki);
        }

        assertThatDirectory(zip.getParent()).exists().hasDirectories(0).hasFiles(3);
        assertThatZipFile(zip, password).exists().root().hasDirectories(0).hasFiles(6);
        assertThatZipFile(zip, password).file(fileNameBentley).matches(fileBentleyAssert);
        assertThatZipFile(zip, password).file(fileNameFerrari).matches(fileFerrariAssert);
        assertThatZipFile(zip, fileNameHonda.toCharArray()).file(fileNameHonda).matches(fileHondaAssert);
        assertThatZipFile(zip, password).file(fileNameKawasaki).matches(fileKawasakiAssert);
        assertThatZipFile(zip, fileNameSuzuki.toCharArray()).file(fileNameSuzuki).matches(fileSuzukiAssert);
    }

    public void shouldThrowExceptionWhenAddDuplicateEntry() throws IOException {
        Path zip = Zip4jvmSuite.copy(Zip4jvmSuite.subDirNameAsMethodName(rootDir), splitFile);

        assertThatThrownBy(() -> {
            try (ZipFile.Writer zipFile = ZipIt.zip(zip).open()) {
                zipFile.add(fileBentley);
            }
        }).isExactlyInstanceOf(EntryDuplicationException.class);
    }

    public void shouldThrowExceptionWhenAddNullEntry() throws IOException {
        Path zip = Zip4jvmSuite.copy(Zip4jvmSuite.subDirNameAsMethodName(rootDir), splitFile);

        assertThatThrownBy(() -> {
            try (ZipFile.Writer zipFile = new ZipEngine(zip, ZipSettings.DEFAULT)) {
                zipFile.add((ZipFile.Entry)null);
            }
        }).isExactlyInstanceOf(NullPointerException.class);
    }

    public void shouldThrowExceptionWhenRemoveWithBlankName() throws IOException {
        Path zip = Zip4jvmSuite.copy(Zip4jvmSuite.subDirNameAsMethodName(rootDir), splitFile);

        for (String prefixEntryName : Arrays.asList(null, "", "  ")) {
            assertThatThrownBy(() -> {
                try (ZipFile.Writer zipFile = new ZipEngine(zip, ZipSettings.DEFAULT)) {
                    zipFile.removeEntryByName(prefixEntryName);
                }
            }).isExactlyInstanceOf(IllegalArgumentException.class);
        }
    }

    public void shouldAddDirectoryWhenZipExists() throws IOException {
        Path zip = Zip4jvmSuite.copy(Zip4jvmSuite.subDirNameAsMethodName(rootDir), splitFile);

        try (ZipFile.Writer zipFile = ZipIt.zip(zip).open()) {
            zipFile.add(dirBikes);
            zipFile.add(dirCars);
        }

        assertThatDirectory(zip.getParent()).exists().hasDirectories(0).hasFiles(6);
        assertThatZipFile(zip, password).exists().root().hasDirectories(2).hasFiles(4);
        assertThatZipFile(zip, password).file(fileNameBentley).matches(fileBentleyAssert);
        assertThatZipFile(zip, password).file(fileNameFerrari).matches(fileFerrariAssert);
        assertThatZipFile(zip, password).file(fileNameWiesmann).matches(fileWiesmannAssert);
        assertThatZipFile(zip, fileNameHonda.toCharArray()).file(fileNameHonda).matches(fileHondaAssert);
        assertThatZipFile(zip, password).directory(zipDirNameBikes).matches(dirBikesAssert);
        assertThatZipFile(zip, password).directory(zipDirNameCars).matches(dirCarsAssert);
    }

    public void shouldRemoveExistedEntityWhenNormalizeName() throws IOException {
        Path zip = Zip4jvmSuite.copy(Zip4jvmSuite.subDirNameAsMethodName(rootDir), splitFile);
        ZipIt.zip(zip).add(dirBikes);

        try (ZipFile.Writer zipFile = ZipIt.zip(zip).open()) {
            zipFile.removeEntryByNamePrefix(zipDirNameBikes + fileNameHonda);
        }

        assertThatDirectory(zip.getParent()).exists().hasDirectories(0).hasFiles(4);
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
        Path zip = Zip4jvmSuite.copy(Zip4jvmSuite.subDirNameAsMethodName(rootDir), splitFile);
        ZipIt.zip(zip).add(dirBikes);

        try (ZipFile.Writer zipFile = ZipIt.zip(zip).open()) {
            zipFile.removeEntryByName(dirNameBikes + '\\' + fileNameHonda);
        }

        assertThatDirectory(zip.getParent()).exists().hasDirectories(0).hasFiles(4);
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
        Path zip = Zip4jvmSuite.copy(Zip4jvmSuite.subDirNameAsMethodName(rootDir), splitFile);
        ZipIt.zip(zip).add(dirBikes);

        try (ZipFile.Writer zipFile = ZipIt.zip(zip).open()) {
            zipFile.removeEntryByNamePrefix(dirNameBikes);
        }

        assertThatDirectory(zip.getParent()).exists().hasDirectories(0).hasFiles(3);
        assertThatZipFile(zip, password).exists().root().hasDirectories(0).hasFiles(4);
        assertThatZipFile(zip, password).file(fileNameBentley).matches(fileBentleyAssert);
        assertThatZipFile(zip, password).file(fileNameFerrari).matches(fileFerrariAssert);
        assertThatZipFile(zip, password).file(fileNameWiesmann).matches(fileWiesmannAssert);
    }

    public void shouldThrowExceptionWhenRemoveNotExistedEntry() throws IOException {
        Path zip = Zip4jvmSuite.copy(Zip4jvmSuite.subDirNameAsMethodName(rootDir), splitFile);

        assertThatThrownBy(() -> {
            try (ZipFile.Writer zipFile = ZipIt.zip(zip).open()) {
                zipFile.removeEntryByName(fileNameKawasaki);
            }
        }).isExactlyInstanceOf(EntryNotFoundException.class);
    }

    public void shouldThrowExceptionWhenCopyNullEntry() throws IOException {
        Path zip = Zip4jvmSuite.copy(Zip4jvmSuite.subDirNameAsMethodName(rootDir), splitFile);

        assertThatThrownBy(() -> {
            try (ZipFile.Writer zipFile = new ZipEngine(zip, ZipSettings.DEFAULT)) {
                zipFile.copy(null);
            }
        }).isExactlyInstanceOf(IllegalArgumentException.class);
    }

//    public void shouldThrowNullPointerExceptionWhenArgumentIsNull() {
//        assertThatThrownBy(() -> new ZipEngine(null, ZipSettings.DEFAULT)).isExactlyInstanceOf(NullPointerException.class);
//        assertThatThrownBy(() -> new ZipEngine(zipStoreSolid, null)).isExactlyInstanceOf(NullPointerException.class);
//    }
//
//    @Test(enabled = false)
//    public void shouldCreateZipFileWhenSolidAndSeparateSettingsForEachFile() throws IOException {
//        Function<String, ZipEntrySettings> entrySettingsProvider = fileName -> {
//            if (fileNameBentley.equals(fileName))
//                return ZipEntrySettings.builder().compression(Compression.STORE, CompressionLevel.NORMAL).build();
//            if (fileNameFerrari.equals(fileName))
//                return ZipEntrySettings.builder().compression(Compression.DEFLATE, CompressionLevel.NORMAL).build();
//            if (fileNameWiesmann.equals(fileName))
//                return ZipEntrySettings.builder()
//                                       .encryption(Encryption.PKWARE, password)
//                                       .compression(Compression.DEFLATE, CompressionLevel.NORMAL).build();
//            if (fileNameHonda.equals(fileName))
//                return ZipEntrySettings.builder()
//                                       .encryption(Encryption.AES_256, fileNameHonda.toCharArray())
//                                       .compression(Compression.DEFLATE, CompressionLevel.NORMAL).build();
//            return ZipEntrySettings.DEFAULT;
//        };
//
//        ZipSettings settings = ZipSettings.builder().entrySettingsProvider(entrySettingsProvider).build();
//
//        try (ZipFile.Writer zipFile = ZipIt.zip(solidFile).settings(settings).open()) {
//            zipFile.add(fileBentley);
//            zipFile.add(fileFerrari);
//            zipFile.add(fileWiesmann);
//            zipFile.add(fileHonda);
//        }
//
//        assertThatDirectory(solidFile.getParent()).exists().hasDirectories(0).hasFiles(1);
//        assertThatZipFile(solidFile, password).exists().root().hasDirectories(0).hasFiles(4);
//        assertThatZipFile(solidFile, password).file(fileNameBentley).matches(fileBentleyAssert);
//        assertThatZipFile(solidFile, password).file(fileNameFerrari).matches(fileFerrariAssert);
//        assertThatZipFile(solidFile, password).file(fileNameWiesmann).matches(fileWiesmannAssert);
//        assertThatZipFile(solidFile, fileNameHonda.toCharArray()).file(fileNameHonda).matches(fileHondaAssert);
//    }
//
//    @Test(dependsOnMethods = "shouldCreateZipFileWhenSolidAndSeparateSettingsForEachFile", enabled = false)
//    public void shouldAddFilesToExistedZipWhenUseZipFile() throws IOException {
//        Function<String, ZipEntrySettings> entrySettingsProvider = fileName -> {
//            if (fileNameKawasaki.equals(fileName))
//                return ZipEntrySettings.builder()
//                                       .compression(Compression.STORE, CompressionLevel.NORMAL)
//                                       .encryption(Encryption.PKWARE, password).build();
//            if (fileNameSuzuki.equals(fileName))
//                return ZipEntrySettings.builder()
//                                       .compression(Compression.DEFLATE, CompressionLevel.NORMAL)
//                                       .encryption(Encryption.AES_256, fileNameSuzuki.toCharArray()).build();
//            return ZipEntrySettings.DEFAULT;
//        };
//
//        ZipSettings settings = ZipSettings.builder().entrySettingsProvider(entrySettingsProvider).build();
//
//        try (ZipFile.Writer zipFile = ZipIt.zip(solidFile).settings(settings).open()) {
//            zipFile.add(fileKawasaki);
//            zipFile.add(fileSuzuki);
//        }
//
//        assertThatDirectory(solidFile.getParent()).exists().hasDirectories(0).hasFiles(1);
//        assertThatZipFile(solidFile, password).exists().root().hasDirectories(0).hasFiles(6);
//        assertThatZipFile(solidFile, password).file(fileNameBentley).matches(fileBentleyAssert);
//        assertThatZipFile(solidFile, password).file(fileNameFerrari).matches(fileFerrariAssert);
//        assertThatZipFile(solidFile, fileNameHonda.toCharArray()).file(fileNameHonda).matches(fileHondaAssert);
//        assertThatZipFile(solidFile, password).file(fileNameKawasaki).matches(fileKawasakiAssert);
//        assertThatZipFile(solidFile, fileNameSuzuki.toCharArray()).file(fileNameSuzuki).matches(fileSuzukiAssert);
//    }
//
//    @Test(dependsOnMethods = "shouldAddFilesToExistedZipWhenUseZipFile", enabled = false)
//    public void shouldThrowExceptionWhenAddDuplicateEntry() throws IOException {
//        Function<String, ZipEntrySettings> entrySettingsProvider = fileName -> {
//            if (fileNameKawasaki.equals(fileName))
//                return ZipEntrySettings.builder()
//                                       .compression(Compression.STORE, CompressionLevel.NORMAL)
//                                       .encryption(Encryption.PKWARE, password).build();
//            return ZipEntrySettings.DEFAULT;
//        };
//
//        ZipSettings settings = ZipSettings.builder().entrySettingsProvider(entrySettingsProvider).build();
//
//        assertThatThrownBy(() -> {
//            try (ZipFile.Writer zipFile = ZipIt.zip(solidFile).settings(settings).open()) {
//                zipFile.add(fileKawasaki);
//            }
//        }).isExactlyInstanceOf(EntryDuplicationException.class);
//    }
//
//    @SuppressWarnings("ConstantConditions")
//    @Test(dependsOnMethods = "shouldThrowExceptionWhenAddDuplicateEntry", enabled = false)
//    public void shouldThrowExceptionWhenAddNullEntry() throws IOException {
//        assertThatThrownBy(() -> {
//            try (ZipEngine engine = new ZipEngine(solidFile, ZipSettings.DEFAULT)) {
//                engine.add((ZipFile.Entry)null);
//            }
//        }).isExactlyInstanceOf(NullPointerException.class);
//    }
//
//    @Test(dependsOnMethods = "shouldThrowExceptionWhenAddNullEntry", enabled = false)
//    public void shouldThrowExceptionWhenRemoveWithNullPrefix() throws IOException {
//        assertThatThrownBy(() -> {
//            try (ZipEngine engine = new ZipEngine(solidFile, ZipSettings.DEFAULT)) {
//                engine.removeEntryByNamePrefix(null);
//            }
//        }).isExactlyInstanceOf(IllegalArgumentException.class);
//    }
//
//    @Test(dependsOnMethods = "shouldThrowExceptionWhenRemoveWithNullPrefix", enabled = false)
//    public void shouldThrowExceptionWhenRemoveWithBlankPrefix() throws IOException {
//        for (String prefixEntryName : Arrays.asList("", "  ")) {
//            assertThatThrownBy(() -> {
//                try (ZipEngine engine = new ZipEngine(solidFile, ZipSettings.DEFAULT)) {
//                    engine.removeEntryByNamePrefix(prefixEntryName);
//                }
//            }).isExactlyInstanceOf(IllegalArgumentException.class);
//        }
//    }
//
//    @Test(dependsOnMethods = "shouldThrowExceptionWhenRemoveWithBlankPrefix", enabled = false)
//    public void shouldRemoveExistedEntityWhenNormalizePrefix() throws IOException {
//        try (ZipFile.Writer zipFile = ZipIt.zip(solidFile).open()) {
//            zipFile.removeEntryByNamePrefix(fileNameKawasaki);
//        }
//
//        assertThatDirectory(solidFile.getParent()).exists().hasDirectories(0).hasFiles(1);
//        assertThatZipFile(solidFile, password).exists().root().hasDirectories(0).hasFiles(5);
//    }
//
//    @Test(dependsOnMethods = "shouldRemoveExistedEntityWhenNormalizePrefix", enabled = false)
//    public void shouldAddDirectoryWhenZipExists() throws IOException {
//        try (ZipFile.Writer zipFile = ZipIt.zip(solidFile).open()) {
//            zipFile.add(dirBikes);
//            zipFile.add(dirCars);
//        }
//
//        assertThatDirectory(solidFile.getParent()).exists().hasDirectories(0).hasFiles(1);
//        assertThatZipFile(solidFile, password).exists().directory(zipDirNameBikes).matches(dirBikesAssert);
//        assertThatZipFile(solidFile, password).exists().directory(zipDirNameCars).matches(dirCarsAssert);
//    }
//
//    @Test(dependsOnMethods = "shouldAddDirectoryWhenZipExists", enabled = false)
//    public void shouldRemoveEntryWhenNormalizePrefix() throws IOException {
//        try (ZipFile.Writer zipFile = ZipIt.zip(solidFile).open()) {
//            zipFile.removeEntryByNamePrefix(dirNameBikes + '/' + fileNameHonda);
//        }
//
//        assertThatDirectory(solidFile.getParent()).exists().hasDirectories(0).hasFiles(1);
//        assertThatZipFile(solidFile, password).exists().directory(zipDirNameBikes).hasFiles(3);
//    }
//
//    @Test(dependsOnMethods = "shouldRemoveEntryWhenNormalizePrefix", enabled = false)
//    public void shouldRemoveEntryWhenNotNormalizePrefix() throws IOException {
//        try (ZipFile.Writer zipFile = ZipIt.zip(solidFile).open()) {
//            zipFile.removeEntryByNamePrefix(dirNameBikes + '\\' + fileNameKawasaki);
//        }
//
//        assertThatDirectory(solidFile.getParent()).exists().hasDirectories(0).hasFiles(1);
//        assertThatZipFile(solidFile, password).exists().directory(zipDirNameBikes).hasFiles(2);
//    }
//
//    @Test(dependsOnMethods = "shouldRemoveEntryWhenNotNormalizePrefix", enabled = false)
//    public void shouldRemoveDirectoryWhenNoDirectoryMarker() throws IOException {
//        try (ZipFile.Writer zipFile = ZipIt.zip(solidFile).open()) {
//            zipFile.removeEntryByNamePrefix(dirNameBikes);
//        }
//
//        assertThatDirectory(solidFile.getParent()).exists().hasDirectories(0).hasFiles(1);
//        assertThatZipFile(solidFile, password).exists().directory(zipDirNameBikes).notExists();
//    }
//
//    @Test(dependsOnMethods = "shouldRemoveDirectoryWhenNoDirectoryMarker", enabled = false)
//    public void shouldThrowExceptionWhenRemoveNotExistedEntry() throws IOException {
//        assertThatThrownBy(() -> {
//            try (ZipFile.Writer zipFile = ZipIt.zip(solidFile).open()) {
//                zipFile.removeEntryByName(dirNameBikes);
//            }
//        }).isExactlyInstanceOf(EntryNotFoundException.class);
//    }
//
//    @Test(dependsOnMethods = "shouldThrowExceptionWhenRemoveNotExistedEntry", enabled = false)
//    public void shouldThrowExceptionWhenCopyNullEntry() throws IOException {
//        assertThatThrownBy(() -> {
//            try (ZipEngine engine = new ZipEngine(solidFile, ZipSettings.DEFAULT)) {
//                engine.copy(null);
//            }
//        }).isExactlyInstanceOf(IllegalArgumentException.class);
//    }
//
//    public void shouldCreateZipFileWhenUseZipFileAndAddFilesSplit() throws IOException {
//        Function<String, ZipEntrySettings> entrySettingsProvider =
//                fileName -> ZipEntrySettings.builder().compression(Compression.STORE, CompressionLevel.NORMAL).build();
//
//        ZipSettings settings = ZipSettings.builder()
//                                          .entrySettingsProvider(entrySettingsProvider)
//                                          .splitSize(SIZE_1MB).build();
//
//        try (ZipFile.Writer zipFile = ZipIt.zip(splitFile).settings(settings).open()) {
//            zipFile.add(fileBentley);
//            zipFile.add(fileFerrari);
//            zipFile.add(fileWiesmann);
//        }
//
//        assertThatDirectory(splitFile.getParent()).exists().hasDirectories(0).hasFiles(3);
//        assertThatZipFile(splitFile).exists().root().hasDirectories(0).hasFiles(3);
//        assertThatZipFile(splitFile).file(fileNameBentley).matches(fileBentleyAssert);
//        assertThatZipFile(splitFile).file(fileNameFerrari).matches(fileFerrariAssert);
//        assertThatZipFile(splitFile).file(fileNameWiesmann).matches(fileWiesmannAssert);
//    }
//
//    @SuppressWarnings({ "unused", "EmptyTryBlock" })
//    public void shouldCreateArchiveWithExistedEntriesWhenSolidNoChangeSettings() throws IOException {
//        Path zip = Zip4jvmSuite.copy(Zip4jvmSuite.subDirNameAsMethodName(rootDir), solidFile);
//        Function<String, ZipEntrySettings> entrySettingsProvider =
//                fileName -> ZipEntrySettings.builder().compression(Compression.STORE, CompressionLevel.NORMAL).build();
//
//        ZipSettings settings = ZipSettings.builder().entrySettingsProvider(entrySettingsProvider).build();
//
//        try (ZipFile.Writer zipFile = ZipIt.zip(zip).settings(settings).open()) {
//        }
//
//        assertThatDirectory(solidFile.getParent()).exists().hasDirectories(0).hasFiles(1);
//        assertThatZipFile(solidFile, password).exists().root().hasDirectories(0).hasFiles(4);
//        assertThatZipFile(solidFile, password).file(fileNameBentley).matches(fileBentleyAssert);
//        assertThatZipFile(solidFile, password).file(fileNameFerrari).matches(fileFerrariAssert);
//        assertThatZipFile(solidFile, password).file(fileNameWiesmann).matches(fileWiesmannAssert);
//        assertThatZipFile(solidFile, fileNameHonda.toCharArray()).file(fileNameHonda).matches(fileHondaAssert);
//    }
//
//    public void shouldCreateAddFilesToExistedSplitArchiveWhenNoChangeSetting() throws IOException {
//        Path zip = Zip4jvmSuite.copy(Zip4jvmSuite.subDirNameAsMethodName(rootDir), solidFile);
//        Function<String, ZipEntrySettings> entrySettingsProvider =
//                fileName -> ZipEntrySettings.builder().compression(Compression.STORE, CompressionLevel.NORMAL).build();
//
//        ZipSettings settings = ZipSettings.builder().entrySettingsProvider(entrySettingsProvider).build();
//
//        try (ZipFile.Writer zipFile = ZipIt.zip(zip).settings(settings).open()) {
//        }
//
////        assertThatDirectory(splitFile.getParent()).exists().hasDirectories(0).hasFiles(4);
////        assertThatZipFile(splitFile).exists().root().hasDirectories(0).hasFiles(7);
////        assertThatZipFile(splitFile).file(fileNameBentley).matches(fileBentleyAssert);
////        assertThatZipFile(splitFile).file(fileNameFerrari).matches(fileFerrariAssert);
////        assertThatZipFile(splitFile).file(fileNameWiesmann).matches(fileWiesmannAssert);
////        assertThatZipFile(splitFile).file("one.jpg").exists().isImage().hasSize(2_204_448);
////        assertThatZipFile(splitFile).file("two.jpg").exists().isImage().hasSize(277_857);
////        assertThatZipFile(splitFile).file("three.jpg").exists().isImage().hasSize(1_601_879);
////        assertThatZipFile(splitFile).file("four.jpg").exists().isImage().hasSize(1_916_776);
//    }
//
//    // TODO add test: should add files to existed split and change split size
//    // TODO add test: should add files to existed split and convert to solid
//
//    @Test(enabled = false)
//    public void shouldCreateZipFileWhenUseZipFileAndAddFilesUsingSupplier() throws IOException {
//        Function<String, ZipEntrySettings> entrySettingsProvider = fileName -> {
//            if (fileNameBentley.equals(fileName))
//                return ZipEntrySettings.builder().compression(Compression.STORE, CompressionLevel.NORMAL).build();
//            if (fileNameFerrari.equals(fileName))
//                return ZipEntrySettings.builder().compression(Compression.DEFLATE, CompressionLevel.NORMAL).build();
//            if (fileNameWiesmann.equals(fileName))
//                return ZipEntrySettings.builder()
//                                       .encryption(Encryption.PKWARE, password)
//                                       .compression(Compression.DEFLATE, CompressionLevel.NORMAL).build();
//            if (fileNameHonda.equals(fileName))
//                return ZipEntrySettings.builder()
//                                       .encryption(Encryption.AES_256, password)
//                                       .compression(Compression.DEFLATE, CompressionLevel.NORMAL).build();
//            return ZipEntrySettings.DEFAULT;
//        };
//
//        ZipSettings settings = ZipSettings.builder().entrySettingsProvider(entrySettingsProvider).build();
//
//        try (ZipFile.Writer zipFile = ZipIt.zip(supplierSolidFile).settings(settings).open()) {
//            zipFile.add(ZipFile.Entry.of(fileBentley, fileNameBentley));
//            zipFile.add(ZipFile.Entry.of(fileFerrari, fileNameFerrari));
//            zipFile.add(ZipFile.Entry.of(fileWiesmann, fileNameWiesmann));
//            zipFile.add(ZipFile.Entry.of(fileHonda, fileNameHonda));
//        }
//
//        assertThatDirectory(supplierSolidFile.getParent()).exists().hasDirectories(0).hasFiles(1);
//        assertThatZipFile(supplierSolidFile, password).exists().root().hasDirectories(0).hasFiles(4);
//        assertThatZipFile(supplierSolidFile, password).file(fileNameBentley).matches(fileBentleyAssert);
//        assertThatZipFile(supplierSolidFile, password).file(fileNameFerrari).matches(fileFerrariAssert);
//        assertThatZipFile(supplierSolidFile, password).file(fileNameWiesmann).matches(fileWiesmannAssert);
//        assertThatZipFile(supplierSolidFile, password).file(fileNameHonda).matches(fileHondaAssert);
//    }
//
//    @Test(enabled = false)
//    public void shouldCreateZipFileWhenUseZipFileAndAddFilesWithText() throws IOException {
//        Function<String, ZipEntrySettings> entrySettingsProvider = fileName -> {
//            if ("one.txt".equals(fileName))
//                return ZipEntrySettings.builder().compression(Compression.STORE, CompressionLevel.NORMAL).build();
//            if ("two.txt".equals(fileName))
//                return ZipEntrySettings.builder().compression(Compression.DEFLATE, CompressionLevel.NORMAL).build();
//            if ("three.txt".equals(fileName))
//                return ZipEntrySettings.builder()
//                                       .encryption(Encryption.PKWARE, password)
//                                       .compression(Compression.DEFLATE, CompressionLevel.NORMAL).build();
//            if ("four.txt".equals(fileName))
//                return ZipEntrySettings.builder()
//                                       .encryption(Encryption.AES_256, password)
//                                       .compression(Compression.DEFLATE, CompressionLevel.NORMAL).build();
//            return ZipEntrySettings.DEFAULT;
//        };
//
//        ZipFile.Entry entryOne = ZipFile.Entry.builder()
//                                              .inputStreamSupplier(() -> IOUtils.toInputStream("one.txt", Charsets.UTF_8))
//                                              .fileName("one.txt").build();
//        ZipFile.Entry entryTwo = ZipFile.Entry.builder()
//                                              .inputStreamSupplier(() -> IOUtils.toInputStream("two.txt", Charsets.UTF_8))
//                                              .fileName("two.txt").build();
//        ZipFile.Entry entryThree = ZipFile.Entry.builder()
//                                                .inputStreamSupplier(() -> IOUtils.toInputStream("three.txt", Charsets.UTF_8))
//                                                .fileName("three.txt").build();
//        ZipFile.Entry entryFour = ZipFile.Entry.builder()
//                                               .inputStreamSupplier(() -> IOUtils.toInputStream("four.txt", Charsets.UTF_8))
//                                               .fileName("four.txt").build();
//
//        Path memorySolidFile = rootDir.resolve("memory/split/src.zip");
//        ZipSettings settings = ZipSettings.builder().entrySettingsProvider(entrySettingsProvider).build();
//
//        try (ZipFile.Writer zipFile = ZipIt.zip(memorySolidFile).settings(settings).open()) {
//            zipFile.add(entryOne);
//            zipFile.add(entryTwo);
//            zipFile.add(entryThree);
//            zipFile.add(entryFour);
//        }
//
//        assertThatDirectory(memorySolidFile.getParent()).exists().hasDirectories(0).hasFiles(1);
//        assertThatZipFile(memorySolidFile, password).exists().root().hasDirectories(0).hasFiles(4);
//        assertThatZipFile(memorySolidFile, password).file(fileNameBentley).matches(fileBentleyAssert);
//        assertThatZipFile(memorySolidFile, password).file(fileNameFerrari).matches(fileFerrariAssert);
//        assertThatZipFile(memorySolidFile, password).file(fileNameWiesmann).matches(fileWiesmannAssert);
//        assertThatZipFile(memorySolidFile, password).file("one.jpg").exists().isImage().hasSize(2_204_448);
//    }
}
