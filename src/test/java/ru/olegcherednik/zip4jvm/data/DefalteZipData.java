package ru.olegcherednik.zip4jvm.data;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
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
        ZipFileSettings settings = ZipFileSettings.builder()
                                                  .entrySettingsProvider(fileName ->
                                                          ZipEntrySettings.builder()
                                                                          .compression(Compression.DEFLATE, CompressionLevel.NORMAL).build())
                                                  .build();

        ZipIt.add(Zip4jvmSuite.deflateSolidZip, Zip4jvmSuite.contentSrcDir, settings);

        assertThat(Files.exists(Zip4jvmSuite.deflateSolidZip)).isTrue();
        assertThat(Files.isRegularFile(Zip4jvmSuite.deflateSolidZip)).isTrue();
        assertThatDirectory(Zip4jvmSuite.deflateSolidZip.getParent()).exists().hasSubDirectories(0).hasFiles(1);
    }

    private static void createDeflateSplitZip() throws IOException {
        ZipFileSettings settings = ZipFileSettings.builder()
                                                  .entrySettingsProvider(fileName ->
                                                          ZipEntrySettings.builder()
                                                                          .compression(Compression.DEFLATE, CompressionLevel.NORMAL).build())
                                                  .splitSize(1024 * 1024).build();
        ZipIt.add(Zip4jvmSuite.deflateSplitZip, Zip4jvmSuite.contentSrcDir, settings);

        assertThat(Files.exists(Zip4jvmSuite.deflateSplitZip)).isTrue();
        assertThat(Files.isRegularFile(Zip4jvmSuite.deflateSplitZip)).isTrue();
        assertThatDirectory(Zip4jvmSuite.deflateSplitZip.getParent()).exists().hasSubDirectories(0).hasFiles(10);
    }

    private static void createDeflateSolidPkwareZip() throws IOException {
        ZipFileSettings settings = ZipFileSettings.builder()
                                                  .entrySettingsProvider(fileName ->
                                                          ZipEntrySettings.builder()
                                                                          .compression(Compression.DEFLATE, CompressionLevel.NORMAL)
                                                                          .encryption(Encryption.PKWARE, Zip4jvmSuite.password).build())
                                                  .comment("password: " + new String(Zip4jvmSuite.password)).build();
        ZipIt.add(Zip4jvmSuite.deflateSolidPkwareZip, Zip4jvmSuite.contentSrcDir, settings);

        assertThat(Files.exists(Zip4jvmSuite.deflateSolidPkwareZip)).isTrue();
        assertThat(Files.isRegularFile(Zip4jvmSuite.deflateSolidPkwareZip)).isTrue();
        assertThatDirectory(Zip4jvmSuite.deflateSolidPkwareZip.getParent()).exists().hasSubDirectories(0).hasFiles(1);
    }

    private static void createDeflateSolidAesZip() throws IOException {
        ZipFileSettings settings = ZipFileSettings.builder()
                                                  .entrySettingsProvider(fileName ->
                                                          ZipEntrySettings.builder()
                                                                          .compression(Compression.DEFLATE, CompressionLevel.NORMAL)
                                                                          .encryption(Encryption.AES_256, fileName.toCharArray()).build())
                                                  .comment("password: fileName").build();
        ZipIt.add(Zip4jvmSuite.deflateSolidAesZip, Zip4jvmSuite.contentSrcDir, settings);

        assertThat(Files.exists(Zip4jvmSuite.deflateSolidAesZip)).isTrue();
        assertThat(Files.isRegularFile(Zip4jvmSuite.deflateSolidAesZip)).isTrue();
        assertThatDirectory(Zip4jvmSuite.deflateSolidAesZip.getParent()).exists().hasSubDirectories(0).hasFiles(1);
    }

}
