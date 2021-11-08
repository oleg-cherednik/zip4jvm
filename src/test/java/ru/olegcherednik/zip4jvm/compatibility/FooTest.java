package ru.olegcherednik.zip4jvm.compatibility;

import org.testng.annotations.Test;
import ru.olegcherednik.zip4jvm.TestDataAssert;
import ru.olegcherednik.zip4jvm.UnzipIt;
import ru.olegcherednik.zip4jvm.Zip4jvmSuite;
import ru.olegcherednik.zip4jvm.ZipIt;
import ru.olegcherednik.zip4jvm.assertj.IDirectoryAssert;
import ru.olegcherednik.zip4jvm.model.Compression;
import ru.olegcherednik.zip4jvm.model.CompressionLevel;
import ru.olegcherednik.zip4jvm.model.settings.ZipEntrySettings;
import ru.olegcherednik.zip4jvm.model.settings.ZipSettings;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Consumer;

import static ru.olegcherednik.zip4jvm.TestData.fileDucati;
import static ru.olegcherednik.zip4jvm.TestData.fileNameDucati;
import static ru.olegcherednik.zip4jvm.assertj.Zip4jvmAssertions.assertThatDirectory;
import static ru.olegcherednik.zip4jvm.assertj.Zip4jvmAssertions.assertThatZipFile;

/**
 * @author Oleg Cherednik
 * @since 08.11.2021
 */
@Test
public class FooTest {

    public static final Path zip = Paths.get("src/test/resources/seven-zip/ducati-panigale-1199.zip").toAbsolutePath();

    private static final Path rootDir = Zip4jvmSuite.generateSubDirNameWithTime(FooTest.class);

    public void shouldUnzipWhenZstdSolid() throws IOException {
        Path destDir = rootDir;
        UnzipIt.zip(zip).destDir(destDir).extract();
        assertThatDirectory(destDir).matches(foo);
    }

    public void shouldCreateSingleZipWithFilesWhenZstdCompressionNormalLevel() throws IOException {
        ZipEntrySettings entrySettings = ZipEntrySettings.builder()
                                                         .compression(Compression.ZSTD, CompressionLevel.NORMAL)
                                                         .build();
        ZipSettings settings = ZipSettings.builder().entrySettingsProvider(fileName -> entrySettings).build();

        Path zip = rootDir.resolve("src.zip");

        ZipIt.zip(zip).settings(settings).add(fileDucati);
        assertThatDirectory(zip.getParent()).exists().hasDirectories(0).hasFiles(1);
        assertThatZipFile(zip).root().matches(foo);
    }

    private static final Consumer<IDirectoryAssert<?>> foo = dir -> {
        dir.exists().hasDirectories(0).hasFiles(2);
        TestDataAssert.fileDucatiAssert.accept(dir.file(fileNameDucati));
    };

}
