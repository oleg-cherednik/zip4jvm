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
import static ru.olegcherednik.zip4jvm.TestData.zipDeflateSolid;
import static ru.olegcherednik.zip4jvm.TestData.zipDeflateSolidAes;
import static ru.olegcherednik.zip4jvm.TestData.zipDeflateSolidPkware;
import static ru.olegcherednik.zip4jvm.TestData.zipDeflateSplit;
import static ru.olegcherednik.zip4jvm.TestDataAssert.rootAssert;
import static ru.olegcherednik.zip4jvm.Zip4jvmSuite.SIZE_1MB;
import static ru.olegcherednik.zip4jvm.Zip4jvmSuite.password;
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
        ZipSettings settings = ZipSettings.builder().entrySettingsProvider(fileName -> entrySettings).build();

        ZipIt.zip(zipDeflateSolid).settings(settings).add(contentDirSrc);
        assertThat(Files.exists(zipDeflateSolid)).isTrue();
        assertThat(Files.isRegularFile(zipDeflateSolid)).isTrue();
        assertThatDirectory(zipDeflateSolid.getParent()).exists().hasDirectories(0).hasFiles(1);
        assertThatZipFile(zipDeflateSolid).exists().root().matches(rootAssert);
    }

    private static void createDeflateSplitZip() throws IOException {
        ZipEntrySettings entrySettings = ZipEntrySettings.builder().compression(Compression.DEFLATE, CompressionLevel.NORMAL).build();
        ZipSettings settings = ZipSettings.builder().entrySettingsProvider(fileName -> entrySettings).splitSize(SIZE_1MB).build();

        ZipIt.zip(zipDeflateSplit).settings(settings).add(contentDirSrc);
        assertThat(Files.exists(zipDeflateSplit)).isTrue();
        assertThat(Files.isRegularFile(zipDeflateSplit)).isTrue();
        assertThatDirectory(zipDeflateSplit.getParent()).exists().hasDirectories(0).hasFiles(6);
    }

    private static void createDeflateSolidPkwareZip() throws IOException {
        ZipEntrySettings entrySettings = ZipEntrySettings.builder()
                                                         .compression(Compression.DEFLATE, CompressionLevel.NORMAL)
                                                         .encryption(Encryption.PKWARE, password).build();
        ZipSettings settings = ZipSettings.builder()
                                          .entrySettingsProvider(fileName -> entrySettings)
                                          // TODO temporary
                                          .comment("abcабвгдеёжзийклмнопрстуфхцчшщъыьэюя").build();
//                                          .comment("password: " + passwordStr).build();

        ZipIt.zip(zipDeflateSolidPkware).settings(settings).add(contentDirSrc);
        assertThat(Files.exists(zipDeflateSolidPkware)).isTrue();
        assertThat(Files.isRegularFile(zipDeflateSolidPkware)).isTrue();
        assertThatDirectory(zipDeflateSolidPkware.getParent()).exists().hasDirectories(0).hasFiles(1);
        assertThatZipFile(zipDeflateSolidPkware, password).exists().root().matches(rootAssert);
    }

    private static void createDeflateSolidAesZip() throws IOException {
        Function<String, ZipEntrySettings> entrySettingsProvider =
                fileName -> ZipEntrySettings.builder()
                                            .compression(Compression.DEFLATE, CompressionLevel.NORMAL)
                                            .encryption(Encryption.AES_256, fileName.toCharArray()).build();
        ZipSettings settings = ZipSettings.builder().entrySettingsProvider(entrySettingsProvider).comment("password: <fileName>").build();

        ZipIt.zip(zipDeflateSolidAes).settings(settings).add(contentDirSrc);
        assertThat(Files.exists(zipDeflateSolidAes)).isTrue();
        assertThat(Files.isRegularFile(zipDeflateSolidAes)).isTrue();
        assertThatDirectory(zipDeflateSolidAes.getParent()).exists().hasDirectories(0).hasFiles(1);
    }

}
