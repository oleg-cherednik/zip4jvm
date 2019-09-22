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
import static ru.olegcherednik.zip4jvm.TestData.zipDeflateSolidAes;
import static ru.olegcherednik.zip4jvm.TestData.zipDeflateSolidPkware;
import static ru.olegcherednik.zip4jvm.TestData.zipDeflateSolid;
import static ru.olegcherednik.zip4jvm.TestData.zipDeflateSplit;
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

        ZipIt.add(zipDeflateSolid, contentDirSrc, settings);
        assertThat(Files.exists(zipDeflateSolid)).isTrue();
        assertThat(Files.isRegularFile(zipDeflateSolid)).isTrue();
        assertThatDirectory(zipDeflateSolid.getParent()).exists().hasSubDirectories(0).hasFiles(1);
        assertThatZipFile(zipDeflateSolid).exists().root().matches(zipDirRootAssert);
    }

    private static void createDeflateSplitZip() throws IOException {
        ZipEntrySettings entrySettings = ZipEntrySettings.builder().compression(Compression.DEFLATE, CompressionLevel.NORMAL).build();
        ZipFileSettings settings = ZipFileSettings.builder().entrySettingsProvider(fileName -> entrySettings).splitSize(SIZE_1MB).build();

        ZipIt.add(zipDeflateSplit, contentDirSrc, settings);
        assertThat(Files.exists(zipDeflateSplit)).isTrue();
        assertThat(Files.isRegularFile(zipDeflateSplit)).isTrue();
        assertThatDirectory(zipDeflateSplit.getParent()).exists().hasSubDirectories(0).hasFiles(6);
    }

    private static void createDeflateSolidPkwareZip() throws IOException {
        ZipEntrySettings entrySettings = ZipEntrySettings.builder()
                                                         .compression(Compression.DEFLATE, CompressionLevel.NORMAL)
                                                         .encryption(Encryption.PKWARE, password).build();
        ZipFileSettings settings = ZipFileSettings.builder()
                                                  .entrySettingsProvider(fileName -> entrySettings)
                                                  .comment("password: " + passwordStr).build();

        ZipIt.add(zipDeflateSolidPkware, contentDirSrc, settings);
        assertThat(Files.exists(zipDeflateSolidPkware)).isTrue();
        assertThat(Files.isRegularFile(zipDeflateSolidPkware)).isTrue();
        assertThatDirectory(zipDeflateSolidPkware.getParent()).exists().hasSubDirectories(0).hasFiles(1);
        assertThatZipFile(zipDeflateSolidPkware, password).exists().root().matches(zipDirRootAssert);
    }

    private static void createDeflateSolidAesZip() throws IOException {
        ZipEntrySettings entrySettings = ZipEntrySettings.builder().compression(Compression.DEFLATE, CompressionLevel.NORMAL).build();
        ZipFileSettings settings = ZipFileSettings.builder()
                                                  .entrySettingsProvider(fileName ->
                                                          entrySettings.toBuilder().encryption(Encryption.AES_256, fileName.toCharArray()).build())
                                                  .comment("password: <fileName>").build();

        ZipIt.add(zipDeflateSolidAes, contentDirSrc, settings);
        assertThat(Files.exists(zipDeflateSolidAes)).isTrue();
        assertThat(Files.isRegularFile(zipDeflateSolidAes)).isTrue();
        assertThatDirectory(zipDeflateSolidAes.getParent()).exists().hasSubDirectories(0).hasFiles(1);
    }

}
