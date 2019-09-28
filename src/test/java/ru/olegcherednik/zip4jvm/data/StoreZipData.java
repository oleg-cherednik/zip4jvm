package ru.olegcherednik.zip4jvm.data;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.olegcherednik.zip4jvm.ZipIt;
import ru.olegcherednik.zip4jvm.model.Compression;
import ru.olegcherednik.zip4jvm.model.CompressionLevel;
import ru.olegcherednik.zip4jvm.model.Encryption;
import ru.olegcherednik.zip4jvm.model.settings.ZipEntrySettings;
import ru.olegcherednik.zip4jvm.model.settings.ZipFileSettings;

import java.io.IOException;
import java.nio.file.Files;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.olegcherednik.zip4jvm.TestData.contentDirSrc;
import static ru.olegcherednik.zip4jvm.TestData.zipStoreSolidAes;
import static ru.olegcherednik.zip4jvm.TestData.zipStoreSolidPkware;
import static ru.olegcherednik.zip4jvm.TestData.zipStoreSolid;
import static ru.olegcherednik.zip4jvm.TestData.zipStoreSplitAes;
import static ru.olegcherednik.zip4jvm.TestData.zipStoreSplitPkware;
import static ru.olegcherednik.zip4jvm.TestData.zipStoreSplit;
import static ru.olegcherednik.zip4jvm.TestDataAssert.zipDirRootAssert;
import static ru.olegcherednik.zip4jvm.Zip4jvmSuite.SIZE_1MB;
import static ru.olegcherednik.zip4jvm.Zip4jvmSuite.password;
import static ru.olegcherednik.zip4jvm.Zip4jvmSuite.passwordStr;
import static ru.olegcherednik.zip4jvm.assertj.Zip4jvmAssertions.assertThatDirectory;
import static ru.olegcherednik.zip4jvm.assertj.Zip4jvmAssertions.assertThatZipFile;

/**
 * @author Oleg Cherednik
 * @since 06.08.2019
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class StoreZipData {

    public static void createStoreZip() throws IOException {
        createStoreSolidZip();
        createStoreSolidPkwareZip();
        createStoreSolidAesZip();

        createStoreSplitZip();
        createStoreSplitPkwareZip();
        createStoreSplitAesZip();
    }

    private static void createStoreSolidZip() throws IOException {
        ZipEntrySettings entrySettings = ZipEntrySettings.builder().compression(Compression.STORE, CompressionLevel.NORMAL).build();
        ZipFileSettings settings = ZipFileSettings.builder().entrySettingsProvider(fileName -> entrySettings).build();

        ZipIt.add(zipStoreSolid, contentDirSrc, settings);

        assertThat(Files.exists(zipStoreSolid)).isTrue();
        assertThat(Files.isRegularFile(zipStoreSolid)).isTrue();
        assertThatDirectory(zipStoreSolid.getParent()).exists().hasDirectories(0).hasFiles(1);
        assertThatZipFile(zipStoreSolid).exists().root().matches(zipDirRootAssert);
    }

    private static void createStoreSplitZip() throws IOException {
        ZipEntrySettings entrySettings = ZipEntrySettings.builder().compression(Compression.STORE, CompressionLevel.NORMAL).build();
        ZipFileSettings settings = ZipFileSettings.builder().entrySettingsProvider(fileName -> entrySettings).splitSize(SIZE_1MB).build();

        ZipIt.add(zipStoreSplit, contentDirSrc, settings);
        assertThat(Files.exists(zipStoreSplit)).isTrue();
        assertThat(Files.isRegularFile(zipStoreSplit)).isTrue();
        assertThatDirectory(zipStoreSplit.getParent()).exists().hasDirectories(0).hasFiles(6);
        assertThatZipFile(zipStoreSplit).exists().root().matches(zipDirRootAssert);
    }

    private static void createStoreSolidPkwareZip() throws IOException {
        ZipEntrySettings entrySettings = ZipEntrySettings.builder()
                                                         .compression(Compression.STORE, CompressionLevel.NORMAL)
                                                         .encryption(Encryption.PKWARE, password).build();
        ZipFileSettings settings = ZipFileSettings.builder()
                                                  .entrySettingsProvider(fileName -> entrySettings)
                                                  .comment("password: " + passwordStr).build();

        ZipIt.add(zipStoreSolidPkware, contentDirSrc, settings);
        assertThat(Files.exists(zipStoreSolidPkware)).isTrue();
        assertThat(Files.isRegularFile(zipStoreSolidPkware)).isTrue();
        assertThatDirectory(zipStoreSolidPkware.getParent()).exists().hasDirectories(0).hasFiles(1);
        assertThatZipFile(zipStoreSolidPkware, password).exists().root().matches(zipDirRootAssert);
    }

    private static void createStoreSolidAesZip() throws IOException {
        ZipEntrySettings entrySettings = ZipEntrySettings.builder().compression(Compression.STORE, CompressionLevel.NORMAL).build();
        ZipFileSettings settings = ZipFileSettings.builder()
                                                  .entrySettingsProvider(fileName ->
                                                          entrySettings.toBuilder().encryption(Encryption.AES_256, fileName.toCharArray()).build())
                                                  .comment("password: <fileName>").build();

        ZipIt.add(zipStoreSolidAes, contentDirSrc, settings);
        assertThat(Files.exists(zipStoreSolidAes)).isTrue();
        assertThat(Files.isRegularFile(zipStoreSolidAes)).isTrue();
        assertThatDirectory(zipStoreSolidAes.getParent()).exists().hasDirectories(0).hasFiles(1);
    }

    private static void createStoreSplitPkwareZip() throws IOException {
        ZipEntrySettings entrySettings = ZipEntrySettings.builder()
                                                         .compression(Compression.STORE, CompressionLevel.NORMAL)
                                                         .encryption(Encryption.PKWARE, password).build();
        ZipFileSettings settings = ZipFileSettings.builder()
                                                  .entrySettingsProvider(fileName -> entrySettings)
                                                  .splitSize(SIZE_1MB)
                                                  .comment("password: " + passwordStr).build();

        ZipIt.add(zipStoreSplitPkware, contentDirSrc, settings);
        assertThat(Files.exists(zipStoreSplitPkware)).isTrue();
        assertThat(Files.isRegularFile(zipStoreSplitPkware)).isTrue();
        assertThatDirectory(zipStoreSplitPkware.getParent()).exists().hasDirectories(0).hasFiles(6);
        // TODO should be implemented
//        assertThatZipFile(storeSplitPkwareZip, password).exists().root().matches(zipDirRootAssert);
    }

    private static void createStoreSplitAesZip() throws IOException {
        ZipEntrySettings entrySettings = ZipEntrySettings.builder().compression(Compression.STORE, CompressionLevel.NORMAL).build();
        ZipFileSettings settings = ZipFileSettings.builder()
                                                  .entrySettingsProvider(fileName ->
                                                          entrySettings.toBuilder().encryption(Encryption.AES_256, fileName.toCharArray()).build())
                                                  .splitSize(SIZE_1MB)
                                                  .comment("password: <fileName>").build();

        ZipIt.add(zipStoreSplitAes, contentDirSrc, settings);
        assertThat(Files.exists(zipStoreSplitAes)).isTrue();
        assertThat(Files.isRegularFile(zipStoreSplitAes)).isTrue();
        assertThatDirectory(zipStoreSplitAes.getParent()).exists().hasDirectories(0).hasFiles(6);
    }

}
