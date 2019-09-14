package ru.olegcherednik.zip4jvm.data;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.olegcherednik.zip4jvm.Zip4jSuite;
import ru.olegcherednik.zip4jvm.ZipIt;
import ru.olegcherednik.zip4jvm.model.Compression;
import ru.olegcherednik.zip4jvm.model.CompressionLevel;
import ru.olegcherednik.zip4jvm.model.Encryption;
import ru.olegcherednik.zip4jvm.model.settings.ZipEntrySettings;
import ru.olegcherednik.zip4jvm.model.settings.ZipFileSettings;

import java.io.IOException;
import java.nio.file.Files;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.olegcherednik.zip4jvm.assertj.Zip4jAssertions.assertThatDirectory;

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
                                                  .entrySettings(
                                                          ZipEntrySettings.builder()
                                                                          .compression(Compression.DEFLATE, CompressionLevel.NORMAL).build())
                                                  .build();

        ZipIt.add(Zip4jSuite.deflateSolidZip, Zip4jSuite.contentSrcDir, settings);

        assertThat(Files.exists(Zip4jSuite.deflateSolidZip)).isTrue();
        assertThat(Files.isRegularFile(Zip4jSuite.deflateSolidZip)).isTrue();
        assertThatDirectory(Zip4jSuite.deflateSolidZip.getParent()).exists().hasSubDirectories(0).hasFiles(1);
    }

    private static void createDeflateSplitZip() throws IOException {
        ZipFileSettings settings = ZipFileSettings.builder()
                                                  .entrySettings(
                                                          ZipEntrySettings.builder()
                                                                          .compression(Compression.DEFLATE, CompressionLevel.NORMAL).build())
                                                  .splitSize(1024 * 1024).build();
        ZipIt.add(Zip4jSuite.deflateSplitZip, Zip4jSuite.contentSrcDir, settings);

        assertThat(Files.exists(Zip4jSuite.deflateSplitZip)).isTrue();
        assertThat(Files.isRegularFile(Zip4jSuite.deflateSplitZip)).isTrue();
        assertThatDirectory(Zip4jSuite.deflateSplitZip.getParent()).exists().hasSubDirectories(0).hasFiles(10);
    }

    private static void createDeflateSolidPkwareZip() throws IOException {
        ZipFileSettings settings = ZipFileSettings.builder()
                                                  .entrySettings(
                                                          ZipEntrySettings.builder()
                                                                          .compression(Compression.DEFLATE, CompressionLevel.NORMAL)
                                                                          .encryption(Encryption.PKWARE, fileName -> Zip4jSuite.password).build())
                                                  .comment("password: " + new String(Zip4jSuite.password)).build();
        ZipIt.add(Zip4jSuite.deflateSolidPkwareZip, Zip4jSuite.contentSrcDir, settings);

        assertThat(Files.exists(Zip4jSuite.deflateSolidPkwareZip)).isTrue();
        assertThat(Files.isRegularFile(Zip4jSuite.deflateSolidPkwareZip)).isTrue();
        assertThatDirectory(Zip4jSuite.deflateSolidPkwareZip.getParent()).exists().hasSubDirectories(0).hasFiles(1);
    }

    private static void createDeflateSolidAesZip() throws IOException {
        ZipFileSettings settings = ZipFileSettings.builder()
                                                  .entrySettings(
                                                          ZipEntrySettings.builder()
                                                                          .compression(Compression.DEFLATE, CompressionLevel.NORMAL)
                                                                          .encryption(Encryption.AES_256, String::toCharArray).build())
                                                  .comment("password: fileName").build();
        ZipIt.add(Zip4jSuite.deflateSolidAesZip, Zip4jSuite.contentSrcDir, settings);

        assertThat(Files.exists(Zip4jSuite.deflateSolidAesZip)).isTrue();
        assertThat(Files.isRegularFile(Zip4jSuite.deflateSolidAesZip)).isTrue();
        assertThatDirectory(Zip4jSuite.deflateSolidAesZip.getParent()).exists().hasSubDirectories(0).hasFiles(1);
    }

}
