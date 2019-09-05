package ru.olegcherednik.zip4jvm;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.olegcherednik.zip4jvm.assertj.Zip4jAssertions;
import ru.olegcherednik.zip4jvm.exception.Zip4jException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author Oleg Cherednik
 * @since 22.03.2019
 */
@SuppressWarnings("FieldNamingConvention")
public class UnzipStreamTest {

    private static final Path rootDir = Zip4jSuite.rootDir.resolve(UnzipStreamTest.class.getSimpleName());

    @BeforeClass
    public static void createDir() throws IOException {
        Files.createDirectories(rootDir);
    }

    @AfterClass(enabled = Zip4jSuite.clear)
    public static void removeDir() throws IOException {
        Zip4jSuite.removeDir(rootDir);
    }

    @Test
    public void shouldUnzipEntryToStreamWhenNoSplit() throws Zip4jException, IOException {
        Path imgFile = rootDir.resolve("bentley-continental.jpg");
        UnzipIt unzip = UnzipIt.builder().zipFile(Zip4jSuite.deflateSolidZip).build();
        TestUtils.copyLarge(unzip.extract("cars/bentley-continental.jpg"), imgFile);
        Zip4jAssertions.assertThatFile(imgFile).exists().isImage().hasSize(1_395_362);
    }

    @Test
    public void shouldUnzipEntryToStreamWhenSplit() throws Zip4jException, IOException {
        Path imgFile = rootDir.resolve("ferrari-458-italia.jpg");
        UnzipIt unzip = UnzipIt.builder().zipFile(Zip4jSuite.deflateSplitZip).build();
        TestUtils.copyLarge(unzip.extract("cars/ferrari-458-italia.jpg"), imgFile);
        Zip4jAssertions.assertThatFile(imgFile).exists().isImage().hasSize(320_894);
    }
}