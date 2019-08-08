package com.cop.zip4j.data;

import com.cop.zip4j.TestUtils;
import com.cop.zip4j.Zip4jSuite;
import com.cop.zip4j.ZipIt;
import com.cop.zip4j.model.Compression;
import com.cop.zip4j.model.ZipParameters;
import lombok.experimental.UtilityClass;

import java.io.IOException;
import java.nio.file.Files;

import static com.cop.zip4j.assertj.Zip4jAssertions.assertThatDirectory;
import static com.cop.zip4j.assertj.Zip4jAssertions.assertThatZipFile;
import static org.assertj.core.api.Assertions.assertThat;

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
        ZipParameters parameters = ZipParameters.builder()
                                                .compressionMethod(Compression.STORE)
                                                .build();
        ZipIt zip = ZipIt.builder().zipFile(Zip4jSuite.storeSolidZip).build();
        zip.add(Zip4jSuite.srcDir, parameters);

        assertThat(Files.exists(Zip4jSuite.storeSolidZip)).isTrue();
        assertThat(Files.isRegularFile(Zip4jSuite.storeSolidZip)).isTrue();
        assertThatDirectory(Zip4jSuite.storeSolidZip.getParent()).exists().hasSubDirectories(0).hasFiles(1);
        assertThatZipFile(Zip4jSuite.storeSolidZip).exists().rootEntry().matches(TestUtils.zipRootDirAssert);
    }

    private static void createStoreSplitZip() throws IOException {
        ZipParameters parameters = ZipParameters.builder()
                                                .compressionMethod(Compression.STORE)
                                                .defaultFolderPath(Zip4jSuite.srcDir)
                                                .splitLength(1024 * 1024)
                                                .build();

        ZipIt zip = ZipIt.builder().zipFile(Zip4jSuite.storeSplitZip).build();
        zip.add(Zip4jSuite.srcDir, parameters);

        assertThat(Files.exists(Zip4jSuite.storeSplitZip)).isTrue();
        assertThat(Files.isRegularFile(Zip4jSuite.storeSplitZip)).isTrue();
        assertThatDirectory(Zip4jSuite.storeSplitZip.getParent()).exists().hasSubDirectories(0).hasFiles(11);
//        assertThatZipFile(Zip4jSuite.storeSplitZip).exists().rootEntry().matches(TestUtils.zipRootDirAssert);
    }

}
