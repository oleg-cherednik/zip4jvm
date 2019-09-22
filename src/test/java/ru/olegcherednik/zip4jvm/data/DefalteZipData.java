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
import static ru.olegcherednik.zip4jvm.TestData.deflateSolidAesZip;
import static ru.olegcherednik.zip4jvm.TestData.deflateSolidPkwareZip;
import static ru.olegcherednik.zip4jvm.TestData.deflateSolidZip;
import static ru.olegcherednik.zip4jvm.TestData.deflateSplitZip;
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
public final class DefalteZipData {

    public static void createDeflateZip() throws IOException {
        createDeflateSolidZip();
        createDeflateSplitZip();
        createDeflateSolidPkwareZip();
        createDeflateSolidAesZip();
    }

    private static void createDeflateSolidZip() throws IOException {
        ZipEntrySettings entrySettings = ZipEntrySettings.builder().compression(Compression.DEFLATE, CompressionLevel.NORMAL).build();
        ZipFileSettings settings = ZipFileSettings.builder().entrySettingsProvider(fileName -> entrySettings).build();

        ZipIt.add(deflateSolidZip, contentDirSrc, settings);
        assertThat(Files.exists(deflateSolidZip)).isTrue();
        assertThat(Files.isRegularFile(deflateSolidZip)).isTrue();
        assertThatDirectory(deflateSolidZip.getParent()).exists().hasSubDirectories(0).hasFiles(1);
        assertThatZipFile(deflateSolidZip).exists().root().matches(zipDirRootAssert);
    }

    private static void createDeflateSplitZip() throws IOException {
        ZipEntrySettings entrySettings = ZipEntrySettings.builder().compression(Compression.DEFLATE, CompressionLevel.NORMAL).build();
        ZipFileSettings settings = ZipFileSettings.builder().entrySettingsProvider(fileName -> entrySettings).splitSize(SIZE_1MB).build();

        ZipIt.add(deflateSplitZip, contentDirSrc, settings);
        assertThat(Files.exists(deflateSplitZip)).isTrue();
        assertThat(Files.isRegularFile(deflateSplitZip)).isTrue();
        assertThatDirectory(deflateSplitZip.getParent()).exists().hasSubDirectories(0).hasFiles(6);
    }

    private static void createDeflateSolidPkwareZip() throws IOException {
        ZipEntrySettings entrySettings = ZipEntrySettings.builder()
                                                         .compression(Compression.DEFLATE, CompressionLevel.NORMAL)
                                                         .encryption(Encryption.PKWARE, password).build();
        ZipFileSettings settings = ZipFileSettings.builder()
                                                  .entrySettingsProvider(fileName -> entrySettings)
                                                  .comment("password: " + passwordStr).build();

        ZipIt.add(deflateSolidPkwareZip, contentDirSrc, settings);
        assertThat(Files.exists(deflateSolidPkwareZip)).isTrue();
        assertThat(Files.isRegularFile(deflateSolidPkwareZip)).isTrue();
        assertThatDirectory(deflateSolidPkwareZip.getParent()).exists().hasSubDirectories(0).hasFiles(1);
        assertThatZipFile(deflateSolidPkwareZip, password).exists().root().matches(zipDirRootAssert);
    }

    private static void createDeflateSolidAesZip() throws IOException {
        ZipEntrySettings entrySettings = ZipEntrySettings.builder().compression(Compression.DEFLATE, CompressionLevel.NORMAL).build();
        ZipFileSettings settings = ZipFileSettings.builder()
                                                  .entrySettingsProvider(fileName ->
                                                          entrySettings.toBuilder().encryption(Encryption.AES_256, fileName.toCharArray()).build())
                                                  .comment("password: <fileName>").build();

        ZipIt.add(deflateSolidAesZip, contentDirSrc, settings);
        assertThat(Files.exists(deflateSolidAesZip)).isTrue();
        assertThat(Files.isRegularFile(deflateSolidAesZip)).isTrue();
        assertThatDirectory(deflateSolidAesZip.getParent()).exists().hasSubDirectories(0).hasFiles(1);
    }

}
