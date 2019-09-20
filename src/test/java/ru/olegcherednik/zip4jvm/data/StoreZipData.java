package ru.olegcherednik.zip4jvm.data;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.olegcherednik.zip4jvm.TestDataAssert;
import ru.olegcherednik.zip4jvm.Zip4jvmSuite;
import ru.olegcherednik.zip4jvm.ZipIt;
import ru.olegcherednik.zip4jvm.model.Compression;
import ru.olegcherednik.zip4jvm.model.CompressionLevel;
import ru.olegcherednik.zip4jvm.model.Encryption;
import ru.olegcherednik.zip4jvm.model.settings.ZipEntrySettings;
import ru.olegcherednik.zip4jvm.model.settings.ZipFileSettings;

import java.io.IOException;
import java.nio.file.Files;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.olegcherednik.zip4jvm.TestData.storeSolidAesZip;
import static ru.olegcherednik.zip4jvm.TestData.storeSolidPkwareZip;
import static ru.olegcherednik.zip4jvm.TestData.storeSolidZip;
import static ru.olegcherednik.zip4jvm.TestData.storeSplitZip;
import static ru.olegcherednik.zip4jvm.Zip4jvmSuite.SIZE_1MB;
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
        createStoreSplitZip();
        createStoreSolidPkwareZip();
        createStoreSolidAesZip();
    }

    private static void createStoreSolidZip() throws IOException {
        ZipEntrySettings entrySettings = ZipEntrySettings.builder().compression(Compression.STORE, CompressionLevel.NORMAL).build();
        ZipFileSettings settings = ZipFileSettings.builder().entrySettingsProvider(fileName -> entrySettings).build();

        ZipIt.add(storeSolidZip, Zip4jvmSuite.contentSrcDir, settings);

        assertThat(Files.exists(storeSolidZip)).isTrue();
        assertThat(Files.isRegularFile(storeSolidZip)).isTrue();
        assertThatDirectory(storeSolidZip.getParent()).exists().hasSubDirectories(0).hasFiles(1);
        assertThatZipFile(storeSolidZip).exists().rootEntry().matches(TestDataAssert.zipRootDirAssert);
    }

    private static void createStoreSplitZip() throws IOException {
        ZipEntrySettings entrySettings = ZipEntrySettings.builder().compression(Compression.STORE, CompressionLevel.NORMAL).build();
        ZipFileSettings settings = ZipFileSettings.builder().entrySettingsProvider(fileName -> entrySettings).splitSize(SIZE_1MB).build();

        ZipIt.add(storeSplitZip, Zip4jvmSuite.contentSrcDir, settings);
        assertThat(Files.exists(storeSplitZip)).isTrue();
        assertThat(Files.isRegularFile(storeSplitZip)).isTrue();
        assertThatDirectory(storeSplitZip.getParent()).exists().hasSubDirectories(0).hasFiles(6);
    }

    private static void createStoreSolidPkwareZip() throws IOException {
        ZipEntrySettings entrySettings = ZipEntrySettings.builder()
                                                         .compression(Compression.STORE, CompressionLevel.NORMAL)
                                                         .encryption(Encryption.PKWARE, Zip4jvmSuite.password).build();
        ZipFileSettings settings = ZipFileSettings.builder()
                                                  .entrySettingsProvider(fileName -> entrySettings)
                                                  .comment("password: " + new String(Zip4jvmSuite.password)).build();

        ZipIt.add(storeSolidPkwareZip, Zip4jvmSuite.contentSrcDir, settings);
        assertThat(Files.exists(storeSolidPkwareZip)).isTrue();
        assertThat(Files.isRegularFile(storeSolidPkwareZip)).isTrue();
        assertThatDirectory(storeSolidPkwareZip.getParent()).exists().hasSubDirectories(0).hasFiles(1);
    }

    private static void createStoreSolidAesZip() throws IOException {
        ZipEntrySettings entrySettings = ZipEntrySettings.builder().compression(Compression.STORE, CompressionLevel.NORMAL).build();
        ZipFileSettings settings = ZipFileSettings.builder()
                                                  .entrySettingsProvider(fileName ->
                                                          entrySettings.toBuilder().encryption(Encryption.PKWARE, fileName.toCharArray()).build())
                                                  .comment("password: <fileName>").build();

        ZipIt.add(storeSolidAesZip, Zip4jvmSuite.contentSrcDir, settings);
        assertThat(Files.exists(storeSolidAesZip)).isTrue();
        assertThat(Files.isRegularFile(storeSolidAesZip)).isTrue();
        assertThatDirectory(storeSolidAesZip.getParent()).exists().hasSubDirectories(0).hasFiles(1);
    }

}
