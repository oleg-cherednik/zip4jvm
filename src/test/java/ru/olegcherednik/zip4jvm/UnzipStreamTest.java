package ru.olegcherednik.zip4jvm;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.olegcherednik.zip4jvm.assertj.Zip4jAssertions;

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
    public void shouldUnzipEntryToStreamWhenNoSplit() throws IOException {
        Path imgFile = rootDir.resolve("bentley-continental.jpg");
        ZipFile.Reader zipFile = ZipFile.read(Zip4jSuite.deflateSolidZip);
        TestUtils.copyLarge(zipFile.extract("cars/bentley-continental.jpg").getInputStream(), imgFile);
        Zip4jAssertions.assertThatFile(imgFile).exists().isImage().hasSize(1_395_362);
    }

    @Test
    public void shouldUnzipEntryToStreamWhenSplit() throws IOException {
        Path imgFile = rootDir.resolve("ferrari-458-italia.jpg");
        ZipFile.Reader zipFile = ZipFile.read(Zip4jSuite.deflateSolidZip);
        TestUtils.copyLarge(zipFile.extract("cars/ferrari-458-italia.jpg").getInputStream(), imgFile);
        Zip4jAssertions.assertThatFile(imgFile).exists().isImage().hasSize(320_894);
    }

    @Test
    public void shouldUnzipEntryToStreamWhenPkwareNoSplit() throws IOException {
        Path imgFile = rootDir.resolve("bentley-continental.jpg");
        ZipFile.Reader zipFile = ZipFile.read(Zip4jSuite.deflateSolidPkwareZip, fileName -> Zip4jSuite.password);
        TestUtils.copyLarge(zipFile.extract("cars/bentley-continental.jpg").getInputStream(), imgFile);
        Zip4jAssertions.assertThatFile(imgFile).exists().isImage().hasSize(1_395_362);
    }
}
