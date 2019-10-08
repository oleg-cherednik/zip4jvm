package ru.olegcherednik.zip4jvm.data;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.olegcherednik.zip4jvm.ZipIt;
import ru.olegcherednik.zip4jvm.model.Compression;
import ru.olegcherednik.zip4jvm.model.CompressionLevel;
import ru.olegcherednik.zip4jvm.model.Encryption;
import ru.olegcherednik.zip4jvm.model.settings.ZipEntrySettings;
import ru.olegcherednik.zip4jvm.model.settings.ZipSettings;

import java.io.IOException;
import java.nio.file.Files;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.olegcherednik.zip4jvm.TestData.contentDirSrc;
import static ru.olegcherednik.zip4jvm.TestData.zipStoreSolid;
import static ru.olegcherednik.zip4jvm.TestData.zipStoreSolidAes;
import static ru.olegcherednik.zip4jvm.TestData.zipStoreSolidPkware;
import static ru.olegcherednik.zip4jvm.TestData.zipStoreSplit;
import static ru.olegcherednik.zip4jvm.TestData.zipStoreSplitAes;
import static ru.olegcherednik.zip4jvm.TestData.zipStoreSplitPkware;
import static ru.olegcherednik.zip4jvm.TestDataAssert.rootAssert;
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
        ZipSettings settings = ZipSettings.builder().entrySettingsProvider(fileName -> entrySettings).build();

        ZipIt.zip(zipStoreSolid).settings(settings).add(contentDirSrc);

        assertThat(Files.exists(zipStoreSolid)).isTrue();
        assertThat(Files.isRegularFile(zipStoreSolid)).isTrue();
        assertThatDirectory(zipStoreSolid.getParent()).exists().hasDirectories(0).hasFiles(1);
        assertThatZipFile(zipStoreSolid).exists().root().matches(rootAssert);
    }

    private static void createStoreSplitZip() throws IOException {
        ZipEntrySettings entrySettings = ZipEntrySettings.builder().compression(Compression.STORE, CompressionLevel.NORMAL).build();
        ZipSettings settings = ZipSettings.builder().entrySettingsProvider(fileName -> entrySettings).splitSize(SIZE_1MB).build();

        ZipIt.zip(zipStoreSplit).settings(settings).add(contentDirSrc);
        assertThat(Files.exists(zipStoreSplit)).isTrue();
        assertThat(Files.isRegularFile(zipStoreSplit)).isTrue();
        assertThatDirectory(zipStoreSplit.getParent()).exists().hasDirectories(0).hasFiles(6);
        assertThatZipFile(zipStoreSplit).exists().root().matches(rootAssert);
    }

    private static void createStoreSolidPkwareZip() throws IOException {
        ZipEntrySettings entrySettings = ZipEntrySettings.builder()
                                                         .compression(Compression.STORE, CompressionLevel.NORMAL)
                                                         .encryption(Encryption.PKWARE, password).build();
        ZipSettings settings = ZipSettings.builder()
                                          .entrySettingsProvider(fileName -> entrySettings)
                                          .comment("password: " + passwordStr).build();

        ZipIt.zip(zipStoreSolidPkware).settings(settings).add(contentDirSrc);
        assertThat(Files.exists(zipStoreSolidPkware)).isTrue();
        assertThat(Files.isRegularFile(zipStoreSolidPkware)).isTrue();
        assertThatDirectory(zipStoreSolidPkware.getParent()).exists().hasDirectories(0).hasFiles(1);
        assertThatZipFile(zipStoreSolidPkware, password).exists().root().matches(rootAssert);
    }

    private static void createStoreSolidAesZip() throws IOException {
        Function<String, ZipEntrySettings> entrySettingsProvider =
                fileName -> ZipEntrySettings.builder()
                                            .compression(Compression.STORE, CompressionLevel.NORMAL)
                                            .encryption(Encryption.AES_192, fileName.toCharArray()).build();
        ZipSettings settings = ZipSettings.builder().entrySettingsProvider(entrySettingsProvider).comment("password: <fileName>").build();

        ZipIt.zip(zipStoreSolidAes).settings(settings).add(contentDirSrc);
        assertThat(Files.exists(zipStoreSolidAes)).isTrue();
        assertThat(Files.isRegularFile(zipStoreSolidAes)).isTrue();
        assertThatDirectory(zipStoreSolidAes.getParent()).exists().hasDirectories(0).hasFiles(1);
    }

    private static void createStoreSplitPkwareZip() throws IOException {
        ZipEntrySettings entrySettings = ZipEntrySettings.builder()
                                                         .compression(Compression.STORE, CompressionLevel.NORMAL)
                                                         .encryption(Encryption.PKWARE, password).build();
        ZipSettings settings = ZipSettings.builder()
                                          .entrySettingsProvider(fileName -> entrySettings)
                                          .splitSize(SIZE_1MB)
                                          .comment("password: " + passwordStr).build();

        ZipIt.zip(zipStoreSplitPkware).settings(settings).add(contentDirSrc);
        assertThat(Files.exists(zipStoreSplitPkware)).isTrue();
        assertThat(Files.isRegularFile(zipStoreSplitPkware)).isTrue();
        assertThatDirectory(zipStoreSplitPkware.getParent()).exists().hasDirectories(0).hasFiles(6);
        assertThatZipFile(zipStoreSplitPkware, password).exists().root().matches(rootAssert);
    }

    private static void createStoreSplitAesZip() throws IOException {
        Function<String, ZipEntrySettings> entrySettingsProvider =
                fileName -> ZipEntrySettings.builder()
                                            .compression(Compression.STORE, CompressionLevel.NORMAL)
                                            .encryption(Encryption.AES_128, fileName.toCharArray()).build();
        ZipSettings settings = ZipSettings.builder()
                                          .entrySettingsProvider(entrySettingsProvider)
                                          .splitSize(SIZE_1MB)
                                          .comment("password: <fileName>").build();

        ZipIt.zip(zipStoreSplitAes).settings(settings).add(contentDirSrc);
        assertThat(Files.exists(zipStoreSplitAes)).isTrue();
        assertThat(Files.isRegularFile(zipStoreSplitAes)).isTrue();
        assertThatDirectory(zipStoreSplitAes.getParent()).exists().hasDirectories(0).hasFiles(6);
    }

}
