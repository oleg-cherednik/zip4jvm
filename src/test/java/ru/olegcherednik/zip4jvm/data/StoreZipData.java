package ru.olegcherednik.zip4jvm.data;

import lombok.experimental.UtilityClass;
import ru.olegcherednik.zip4jvm.TestUtils;
import ru.olegcherednik.zip4jvm.Zip4jSuite;
import ru.olegcherednik.zip4jvm.ZipIt;
import ru.olegcherednik.zip4jvm.model.Compression;
import ru.olegcherednik.zip4jvm.model.CompressionLevel;
import ru.olegcherednik.zip4jvm.model.ZipEntrySettings;
import ru.olegcherednik.zip4jvm.model.ZipFileSettings;

import java.io.IOException;
import java.nio.file.Files;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.olegcherednik.zip4jvm.assertj.Zip4jAssertions.assertThatDirectory;
import static ru.olegcherednik.zip4jvm.assertj.Zip4jAssertions.assertThatZipFile;

/**
 * @author Oleg Cherednik
 * @since 06.08.2019
 */
@UtilityClass
public class StoreZipData {

    public static void createStoreZip() throws IOException {
        createStoreSolidZip();
        createStoreSplitZip();
    }

    private static void createStoreSolidZip() throws IOException {
        ZipFileSettings settings = ZipFileSettings.builder()
                                                  .entrySettings(
                                                          ZipEntrySettings.builder()
                                                                          .compression(Compression.STORE, CompressionLevel.NORMAL).build())
                                                  .build();
        ZipIt.add(Zip4jSuite.storeSolidZip, Zip4jSuite.contentSrcDir, settings);

        assertThat(Files.exists(Zip4jSuite.storeSolidZip)).isTrue();
        assertThat(Files.isRegularFile(Zip4jSuite.storeSolidZip)).isTrue();
        assertThatDirectory(Zip4jSuite.storeSolidZip.getParent()).exists().hasSubDirectories(0).hasFiles(1);
        assertThatZipFile(Zip4jSuite.storeSolidZip).exists().rootEntry().matches(TestUtils.zipRootDirAssert);
    }

    private static void createStoreSplitZip() throws IOException {
        ZipFileSettings settings = ZipFileSettings.builder()
                                                  .entrySettings(
                                                          ZipEntrySettings.builder()
                                                                          .compression(Compression.STORE, CompressionLevel.NORMAL).build())
                                                  .splitSize(1024 * 1024).build();
        ZipIt.add(Zip4jSuite.storeSplitZip, Zip4jSuite.contentSrcDir, settings);

        assertThat(Files.exists(Zip4jSuite.storeSplitZip)).isTrue();
        assertThat(Files.isRegularFile(Zip4jSuite.storeSplitZip)).isTrue();
        assertThatDirectory(Zip4jSuite.storeSplitZip.getParent()).exists().hasSubDirectories(0).hasFiles(11);
    }

}
