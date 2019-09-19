package ru.olegcherednik.zip4jvm.data;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.olegcherednik.zip4jvm.TestUtils;
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
        ZipFileSettings settings = ZipFileSettings.builder()
                                                  .entrySettingsProvider(fileName ->
                                                          ZipEntrySettings.builder()
                                                                          .compression(Compression.STORE, CompressionLevel.NORMAL).build())
                                                  .build();

        ZipIt.add(Zip4jvmSuite.storeSolidZip, Zip4jvmSuite.contentSrcDir, settings);

        assertThat(Files.exists(Zip4jvmSuite.storeSolidZip)).isTrue();
        assertThat(Files.isRegularFile(Zip4jvmSuite.storeSolidZip)).isTrue();
        assertThatDirectory(Zip4jvmSuite.storeSolidZip.getParent()).exists().hasSubDirectories(0).hasFiles(1);
        assertThatZipFile(Zip4jvmSuite.storeSolidZip).exists().rootEntry().matches(TestUtils.zipRootDirAssert);
    }

    private static void createStoreSplitZip() throws IOException {
        ZipFileSettings settings = ZipFileSettings.builder()
                                                  .entrySettingsProvider(fileName ->
                                                          ZipEntrySettings.builder()
                                                                          .compression(Compression.STORE, CompressionLevel.NORMAL).build())
                                                  .splitSize(1024 * 1024).build();
        ZipIt.add(Zip4jvmSuite.storeSplitZip, Zip4jvmSuite.contentSrcDir, settings);

        assertThat(Files.exists(Zip4jvmSuite.storeSplitZip)).isTrue();
        assertThat(Files.isRegularFile(Zip4jvmSuite.storeSplitZip)).isTrue();
        assertThatDirectory(Zip4jvmSuite.storeSplitZip.getParent()).exists().hasSubDirectories(0).hasFiles(11);
    }

    private static void createStoreSolidPkwareZip() throws IOException {
        ZipFileSettings settings = ZipFileSettings.builder()
                                                  .entrySettingsProvider(fileName ->
                                                          ZipEntrySettings.builder()
                                                                          .compression(Compression.STORE, CompressionLevel.NORMAL)
                                                                          .encryption(Encryption.PKWARE, Zip4jvmSuite.password).build())
                                                  .comment("password: " + new String(Zip4jvmSuite.password)).build();
        ZipIt.add(Zip4jvmSuite.storeSolidPkwareZip, Zip4jvmSuite.contentSrcDir, settings);

        assertThat(Files.exists(Zip4jvmSuite.storeSolidPkwareZip)).isTrue();
        assertThat(Files.isRegularFile(Zip4jvmSuite.storeSolidPkwareZip)).isTrue();
        assertThatDirectory(Zip4jvmSuite.storeSolidPkwareZip.getParent()).exists().hasSubDirectories(0).hasFiles(1);
    }

    private static void createStoreSolidAesZip() throws IOException {
        ZipFileSettings settings = ZipFileSettings.builder()
                                                  .entrySettingsProvider(fileName ->
                                                          ZipEntrySettings.builder()
                                                                          .compression(Compression.STORE, CompressionLevel.NORMAL)
                                                                          .encryption(Encryption.PKWARE, fileName.toCharArray()).build())
                                                  .comment("password: fileName").build();
        ZipIt.add(Zip4jvmSuite.storeSolidAesZip, Zip4jvmSuite.contentSrcDir, settings);

        assertThat(Files.exists(Zip4jvmSuite.storeSolidAesZip)).isTrue();
        assertThat(Files.isRegularFile(Zip4jvmSuite.storeSolidAesZip)).isTrue();
        assertThatDirectory(Zip4jvmSuite.storeSolidAesZip.getParent()).exists().hasSubDirectories(0).hasFiles(1);
    }

}
