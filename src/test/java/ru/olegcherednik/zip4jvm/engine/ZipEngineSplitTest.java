package ru.olegcherednik.zip4jvm.engine;

import org.apache.commons.io.IOUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.olegcherednik.zip4jvm.Zip4jvmSuite;
import ru.olegcherednik.zip4jvm.ZipFile;
import ru.olegcherednik.zip4jvm.ZipIt;
import ru.olegcherednik.zip4jvm.exception.EntryDuplicationException;
import ru.olegcherednik.zip4jvm.exception.EntryNotFoundException;
import ru.olegcherednik.zip4jvm.model.Charsets;
import ru.olegcherednik.zip4jvm.model.Compression;
import ru.olegcherednik.zip4jvm.model.CompressionLevel;
import ru.olegcherednik.zip4jvm.model.Encryption;
import ru.olegcherednik.zip4jvm.model.settings.ZipEntrySettings;
import ru.olegcherednik.zip4jvm.model.settings.ZipSettings;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static ru.olegcherednik.zip4jvm.TestData.dirBikes;
import static ru.olegcherednik.zip4jvm.TestData.dirCars;
import static ru.olegcherednik.zip4jvm.TestData.dirNameBikes;
import static ru.olegcherednik.zip4jvm.TestData.fileBentley;
import static ru.olegcherednik.zip4jvm.TestData.fileDucati;
import static ru.olegcherednik.zip4jvm.TestData.fileFerrari;
import static ru.olegcherednik.zip4jvm.TestData.fileHonda;
import static ru.olegcherednik.zip4jvm.TestData.fileKawasaki;
import static ru.olegcherednik.zip4jvm.TestData.fileNameBentley;
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
import static ru.olegcherednik.zip4jvm.Zip4jvmSuite.password;
import static ru.olegcherednik.zip4jvm.assertj.Zip4jvmAssertions.assertThatDirectory;
import static ru.olegcherednik.zip4jvm.assertj.ZipFileAssert.assertThatZipFile;

/**
 * @author Oleg Cherednik
 * @since 12.09.2019
 */
@Test
@SuppressWarnings("FieldNamingConvention")
public class ZipEngineSplitTest {

    private static final Path rootDir = Zip4jvmSuite.generateSubDirNameWithTime(ZipEngineSplitTest.class);
    private static final Path solidFile = rootDir.resolve("solid/src.zip");
    private static final Path splitFile = rootDir.resolve("split/src.zip");
    private static final Path supplierSolidFile = rootDir.resolve("supplier/split/src.zip");
    private static final Path memorySolidFile = rootDir.resolve("memory/split/src.zip");

    @BeforeClass
    public static void createDir() throws IOException {
        Files.createDirectories(rootDir);
    }

    @AfterClass(enabled = Zip4jvmSuite.clear)
    public static void removeDir() throws IOException {
        Zip4jvmSuite.removeDir(rootDir);
    }

    public void shouldThrowNullPointerExceptionWhenArgumentIsNull() {
        assertThatThrownBy(() -> new ZipEngine(null, ZipSettings.DEFAULT)).isExactlyInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new ZipEngine(zipStoreSolid, null)).isExactlyInstanceOf(NullPointerException.class);
    }

    public void shouldCreateZipFileWhenSeparateSettingsForEachFile() throws IOException {
        Function<String, ZipEntrySettings> entrySettingsProvider = fileName -> {
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

        ZipSettings settings = ZipSettings.builder().entrySettingsProvider(entrySettingsProvider).build();

        try (ZipFile.Writer zipFile = ZipIt.zip(solidFile).settings(settings).open()) {
            zipFile.add(fileBentley);
            zipFile.add(fileFerrari);
            zipFile.add(fileWiesmann);
            zipFile.add(fileHonda);
        }

        assertThatDirectory(solidFile.getParent()).exists().hasDirectories(0).hasFiles(1);
        assertThatZipFile(solidFile, password).exists().root().hasDirectories(0).hasFiles(4);
        assertThatZipFile(solidFile, password).file(fileNameBentley).exists().hasSize(1_395_362);
        assertThatZipFile(solidFile, password).file(fileNameFerrari).exists().hasSize(320_894);
        assertThatZipFile(solidFile, password).file(fileNameWiesmann).exists().hasSize(729_633);
        assertThatZipFile(solidFile, fileNameHonda.toCharArray()).file(fileNameHonda).exists().hasSize(154_591);
    }

    @Test(dependsOnMethods = "shouldCreateZipFileWhenSeparateSettingsForEachFile")
    public void shouldAddFilesToExistedZipWhenUseZipFile() throws IOException {
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

        try (ZipFile.Writer zipFile = ZipIt.zip(solidFile).settings(settings).open()) {
            zipFile.add(fileKawasaki);
            zipFile.add(fileSuzuki);
        }

        assertThatDirectory(solidFile.getParent()).exists().hasDirectories(0).hasFiles(1);
        assertThatZipFile(solidFile, password).exists().root().hasDirectories(0).hasFiles(6);
        assertThatZipFile(solidFile, password).file(fileNameBentley).exists().hasSize(1_395_362);
        assertThatZipFile(solidFile, password).file(fileNameFerrari).exists().hasSize(320_894);
        assertThatZipFile(solidFile, fileNameHonda.toCharArray()).file(fileNameHonda).exists().hasSize(154_591);
        assertThatZipFile(solidFile, password).file(fileNameKawasaki).exists().hasSize(167_026);
        assertThatZipFile(solidFile, fileNameSuzuki.toCharArray()).file(fileNameSuzuki).exists().hasSize(287_349);
    }

    @Test(dependsOnMethods = "shouldAddFilesToExistedZipWhenUseZipFile")
    public void shouldThrowExceptionWhenAddDuplicateEntry() throws IOException {
        Function<String, ZipEntrySettings> entrySettingsProvider = fileName -> {
            if (fileNameKawasaki.equals(fileName))
                return ZipEntrySettings.builder()
                                       .compression(Compression.STORE, CompressionLevel.NORMAL)
                                       .encryption(Encryption.PKWARE, password).build();
            return ZipEntrySettings.DEFAULT;
        };

        ZipSettings settings = ZipSettings.builder().entrySettingsProvider(entrySettingsProvider).build();

        assertThatThrownBy(() -> {
            try (ZipFile.Writer zipFile = ZipIt.zip(solidFile).settings(settings).open()) {
                zipFile.add(fileKawasaki);
            }
        }).isExactlyInstanceOf(EntryDuplicationException.class);
    }

    @SuppressWarnings("ConstantConditions")
    @Test(dependsOnMethods = "shouldThrowExceptionWhenAddDuplicateEntry")
    public void shouldThrowExceptionWhenAddNullEntry() throws IOException {
        assertThatThrownBy(() -> {
            try (ZipEngine engine = new ZipEngine(solidFile, ZipSettings.DEFAULT)) {
                engine.add((ZipFile.Entry)null);
            }
        }).isExactlyInstanceOf(NullPointerException.class);
    }

    @Test(dependsOnMethods = "shouldThrowExceptionWhenAddNullEntry")
    public void shouldThrowExceptionWhenRemoveWithNullPrefix() throws IOException {
        assertThatThrownBy(() -> {
            try (ZipEngine engine = new ZipEngine(solidFile, ZipSettings.DEFAULT)) {
                engine.removeEntryByNamePrefix(null);
            }
        }).isExactlyInstanceOf(IllegalArgumentException.class);
    }

    @Test(dependsOnMethods = "shouldThrowExceptionWhenRemoveWithNullPrefix")
    public void shouldThrowExceptionWhenRemoveWithBlankPrefix() throws IOException {
        for (String prefixEntryName : Arrays.asList("", "  ")) {
            assertThatThrownBy(() -> {
                try (ZipEngine engine = new ZipEngine(solidFile, ZipSettings.DEFAULT)) {
                    engine.removeEntryByNamePrefix(prefixEntryName);
                }
            }).isExactlyInstanceOf(IllegalArgumentException.class);
        }
    }

    @Test(dependsOnMethods = "shouldThrowExceptionWhenRemoveWithBlankPrefix")
    public void shouldRemoveExistedEntityWhenNormalizePrefix() throws IOException {
        try (ZipFile.Writer zipFile = ZipIt.zip(solidFile).open()) {
            zipFile.removeEntryByNamePrefix(fileNameKawasaki);
        }

        assertThatDirectory(solidFile.getParent()).exists().hasDirectories(0).hasFiles(1);
        assertThatZipFile(solidFile, password).exists().root().hasDirectories(0).hasFiles(5);
    }

    @Test(dependsOnMethods = "shouldRemoveExistedEntityWhenNormalizePrefix")
    public void shouldAddDirectoryWhenZipExists() throws IOException {
        try (ZipFile.Writer zipFile = ZipIt.zip(solidFile).open()) {
            zipFile.add(dirBikes);
            zipFile.add(dirCars);
        }

        assertThatDirectory(solidFile.getParent()).exists().hasDirectories(0).hasFiles(1);
        assertThatZipFile(solidFile, password).exists().directory(zipDirNameBikes).matches(dirBikesAssert);
        assertThatZipFile(solidFile, password).exists().directory(zipDirNameCars).matches(dirCarsAssert);
    }

    @Test(dependsOnMethods = "shouldAddDirectoryWhenZipExists")
    public void shouldRemoveEntryWhenNormalizePrefix() throws IOException {
        try (ZipFile.Writer zipFile = ZipIt.zip(solidFile).open()) {
            zipFile.removeEntryByNamePrefix(dirNameBikes + '/' + fileNameHonda);
        }

        assertThatDirectory(solidFile.getParent()).exists().hasDirectories(0).hasFiles(1);
        assertThatZipFile(solidFile, password).exists().directory(zipDirNameBikes).hasFiles(3);
    }

    @Test(dependsOnMethods = "shouldRemoveEntryWhenNormalizePrefix")
    public void shouldRemoveEntryWhenNotNormalizePrefix() throws IOException {
        try (ZipFile.Writer zipFile = ZipIt.zip(solidFile).open()) {
            zipFile.removeEntryByNamePrefix(dirNameBikes + '\\' + fileNameKawasaki);
        }

        assertThatDirectory(solidFile.getParent()).exists().hasDirectories(0).hasFiles(1);
        assertThatZipFile(solidFile, password).exists().directory(zipDirNameBikes).hasFiles(2);
    }

    @Test(dependsOnMethods = "shouldRemoveEntryWhenNotNormalizePrefix")
    public void shouldRemoveDirectoryWhenNoDirectoryMarker() throws IOException {
        try (ZipFile.Writer zipFile = ZipIt.zip(solidFile).open()) {
            zipFile.removeEntryByNamePrefix(dirNameBikes);
        }

        assertThatDirectory(solidFile.getParent()).exists().hasDirectories(0).hasFiles(1);
        assertThatZipFile(solidFile, password).exists().directory(zipDirNameBikes).notExists();
    }

    @Test(dependsOnMethods = "shouldRemoveDirectoryWhenNoDirectoryMarker")
    public void shouldThrowExceptionWhenRemoveNotExistedEntry() throws IOException {
        assertThatThrownBy(() -> {
            try (ZipFile.Writer zipFile = ZipIt.zip(solidFile).open()) {
                zipFile.removeEntryByName(dirNameBikes);
            }
        }).isExactlyInstanceOf(EntryNotFoundException.class);
    }

    @Test(dependsOnMethods = "shouldThrowExceptionWhenRemoveNotExistedEntry")
    public void shouldThrowExceptionWhenCopyNullEntry() throws IOException {
        assertThatThrownBy(() -> {
            try (ZipEngine engine = new ZipEngine(solidFile, ZipSettings.DEFAULT)) {
                engine.copy(null);
            }
        }).isExactlyInstanceOf(IllegalArgumentException.class);
    }

    public void shouldCreateZipFileWhenUseZipFileAndAddFilesSplit() throws IOException {
        Function<String, ZipEntrySettings> entrySettingsProvider =
                fileName -> ZipEntrySettings.builder().compression(Compression.STORE, CompressionLevel.NORMAL).build();

        ZipSettings settings = ZipSettings.builder()
                                          .entrySettingsProvider(entrySettingsProvider)
                                          .splitSize(1024 * 1024).build();

        try (ZipFile.Writer zipFile = ZipIt.zip(splitFile).settings(settings).open()) {
            zipFile.add(fileBentley);
            zipFile.add(fileFerrari);
            zipFile.add(fileWiesmann);
        }

        assertThatDirectory(splitFile.getParent()).exists().hasDirectories(0).hasFiles(3);

// TODO ignored tests
//        assertThatZipFile(splitFile).exists().root().hasDirectories(0).hasFiles(3);
//        assertThatZipFile(splitFile).file("bentley-continental.jpg").exists().isImage().hasSize(1_395_362);
//        assertThatZipFile(splitFile).file("ferrari-458-italia.jpg").exists().isImage().hasSize(320_894);
//        assertThatZipFile(splitFile).file("wiesmann-gt-mf5.jpg").exists().isImage().hasSize(729_633);
    }

    @Test(dependsOnMethods = "shouldCreateZipFileWhenUseZipFileAndAddFilesSplit")
    public void shouldAddFilesToExistedZipWhenUseZipFileSplit() throws IOException {
        Function<String, ZipEntrySettings> entrySettingsProvider =
                fileName -> ZipEntrySettings.builder().compression(Compression.STORE, CompressionLevel.NORMAL).build();

        ZipSettings settings = ZipSettings.builder()
                                          .entrySettingsProvider(entrySettingsProvider)
                                          .splitSize(1024 * 1024).build();

        try (ZipFile.Writer zipFile = ZipIt.zip(splitFile).settings(settings).open()) {
            zipFile.add(fileDucati);
            zipFile.add(fileHonda);
            zipFile.add(fileKawasaki);
            zipFile.add(fileSuzuki);
        }

        assertThatDirectory(splitFile.getParent()).exists().hasDirectories(0).hasFiles(4);
// TODO ignored tests
//        assertThatZipFile(splitFile).exists().root().hasDirectories(0).hasFiles(7);
//        assertThatZipFile(splitFile).file("bentley-continental.jpg").exists().isImage().hasSize(1_395_362);
//        assertThatZipFile(splitFile).file("ferrari-458-italia.jpg").exists().isImage().hasSize(320_894);
//        assertThatZipFile(splitFile).file("wiesmann-gt-mf5.jpg").exists().isImage().hasSize(729_633);
//        assertThatZipFile(splitFile).file("one.jpg").exists().isImage().hasSize(2_204_448);
//        assertThatZipFile(splitFile).file("two.jpg").exists().isImage().hasSize(277_857);
//        assertThatZipFile(splitFile).file("three.jpg").exists().isImage().hasSize(1_601_879);
//        assertThatZipFile(splitFile).file("four.jpg").exists().isImage().hasSize(1_916_776);
    }

    public void shouldCreateZipFileWhenUseZipFileAndAddFilesUsingSupplier() throws IOException {
        Function<String, ZipEntrySettings> entrySettingsProvider = fileName -> {
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
                                       .encryption(Encryption.AES_256, password)
                                       .compression(Compression.DEFLATE, CompressionLevel.NORMAL).build();
            return ZipEntrySettings.DEFAULT;
        };

        ZipSettings settings = ZipSettings.builder().entrySettingsProvider(entrySettingsProvider).build();

        try (ZipFile.Writer zipFile = ZipIt.zip(supplierSolidFile).settings(settings).open()) {
            zipFile.add(ZipFile.Entry.of(fileBentley, fileNameBentley));
            zipFile.add(ZipFile.Entry.of(fileFerrari, fileNameFerrari));
            zipFile.add(ZipFile.Entry.of(fileWiesmann, fileNameWiesmann));
            zipFile.add(ZipFile.Entry.of(fileHonda, fileNameHonda));
        }

        assertThatDirectory(supplierSolidFile.getParent()).exists().hasDirectories(0).hasFiles(1);
        assertThatZipFile(supplierSolidFile, password).exists().root().hasDirectories(0).hasFiles(4);
        assertThatZipFile(supplierSolidFile, password).file(fileNameBentley).exists().hasSize(1_395_362);
        assertThatZipFile(supplierSolidFile, password).file(fileNameFerrari).exists().hasSize(320_894);
        assertThatZipFile(supplierSolidFile, password).file(fileNameWiesmann).exists().hasSize(729_633);
        assertThatZipFile(supplierSolidFile, password).file(fileNameHonda).exists().hasSize(154_591);
    }

    public void shouldCreateZipFileWhenUseZipFileAndAddFilesWithText() throws IOException {
        Function<String, ZipEntrySettings> entrySettingsProvider = fileName -> {
            if ("one.txt".equals(fileName))
                return ZipEntrySettings.builder().compression(Compression.STORE, CompressionLevel.NORMAL).build();
            if ("two.txt".equals(fileName))
                return ZipEntrySettings.builder().compression(Compression.DEFLATE, CompressionLevel.NORMAL).build();
            if ("three.txt".equals(fileName))
                return ZipEntrySettings.builder()
                                       .encryption(Encryption.PKWARE, password)
                                       .compression(Compression.DEFLATE, CompressionLevel.NORMAL).build();
            if ("four.txt".equals(fileName))
                return ZipEntrySettings.builder()
                                       .encryption(Encryption.AES_256, password)
                                       .compression(Compression.DEFLATE, CompressionLevel.NORMAL).build();
            return ZipEntrySettings.DEFAULT;
        };

        ZipFile.Entry entryOne = ZipFile.Entry.builder()
                                              .inputStreamSupplier(() -> IOUtils.toInputStream("one.txt", Charsets.UTF_8))
                                              .fileName("one.txt").build();
        ZipFile.Entry entryTwo = ZipFile.Entry.builder()
                                              .inputStreamSupplier(() -> IOUtils.toInputStream("two.txt", Charsets.UTF_8))
                                              .fileName("two.txt").build();
        ZipFile.Entry entryThree = ZipFile.Entry.builder()
                                                .inputStreamSupplier(() -> IOUtils.toInputStream("three.txt", Charsets.UTF_8))
                                                .fileName("three.txt").build();
        ZipFile.Entry entryFour = ZipFile.Entry.builder()
                                               .inputStreamSupplier(() -> IOUtils.toInputStream("four.txt", Charsets.UTF_8))
                                               .fileName("four.txt").build();

        ZipSettings settings = ZipSettings.builder().entrySettingsProvider(entrySettingsProvider).build();

        try (ZipFile.Writer zipFile = ZipIt.zip(memorySolidFile).settings(settings).open()) {
            zipFile.add(entryOne);
            zipFile.add(entryTwo);
            zipFile.add(entryThree);
            zipFile.add(entryFour);
        }

        assertThatDirectory(memorySolidFile.getParent()).exists().hasDirectories(0).hasFiles(1);
        assertThatZipFile(memorySolidFile, password).exists().root().hasDirectories(0).hasFiles(4);
// TODO ignored tests
//        assertThatZipFile(memorySolidFile, password).file("bentley-continental.jpg").exists().isImage().hasSize(1_395_362);
//        assertThatZipFile(memorySolidFile, password).file("ferrari-458-italia.jpg").exists().isImage().hasSize(320_894);
//        assertThatZipFile(memorySolidFile, password).file("wiesmann-gt-mf5.jpg").exists().isImage().hasSize(729_633);
//        assertThatZipFile(memorySolidFile, password).file("one.jpg").exists().isImage().hasSize(2_204_448);
    }
}
