package net.lingala.zip4j.examples;

import net.lingala.zip4j.ZipIt;
import net.lingala.zip4j.core.ZipFileUnzip;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.CompressionLevel;
import net.lingala.zip4j.model.CompressionMethod;
import net.lingala.zip4j.model.UnzipParameters;
import net.lingala.zip4j.model.ZipParameters;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author Oleg Cherednik
 * @since 14.03.2019
 */
@Test
public class ZipItDirectoryTest {

    private Path root;
    private Path srcDir;
    private Path destDir;
    private Path resDir;

    @BeforeMethod
    public void createDirectory() throws IOException {
        root = Paths.get("d:/zip4j");//Files.createTempDirectory("zip4j");
        srcDir = root.resolve("src");
        destDir = root.resolve("dest");
        resDir = destDir.resolve("res");

        Files.createDirectories(srcDir);
        Files.createDirectories(destDir);
        Files.createDirectories(resDir);
    }

    //    @BeforeMethod(dependsOnMethods = "createDirectory")
//    public void copyTestData() throws IOException {
//        Path dataDir = Paths.get("src/test/resources/data").toAbsolutePath();
//
//        Files.walk(dataDir).forEach(path -> {
//            try {
//                if (Files.isDirectory(path))
//                    Files.createDirectories(srcDir.resolve(dataDir.relativize(path)));
//                else if (Files.isRegularFile(path))
//                    Files.copy(path, srcDir.resolve(dataDir.relativize(path)));
//            } catch(IOException e) {
//                e.printStackTrace();
//            }
//        });
//    }

//    @AfterMethod
//    public void removeDirectory() throws IOException {
//        FileUtils.deleteQuietly(root.toFile());
//        FileUtils.deleteQuietly(destDir.toFile());
//    }

    public void shouldZipDirectoryWithSplitArchive() throws ZipException, IOException {
        Path zipFile = destDir.resolve("src.zip");

        zipIt(zipFile, 1024 * 1024);
        TestUtils.checkDestinationDir(10, destDir);

        unzipId(zipFile);
        TestUtils.checkResultDir(resDir);
    }

    private void zipIt(Path zipFile, long splitLength) throws ZipException, IOException {
        ZipParameters parameters = ZipParameters.builder()
                                                .compressionMethod(CompressionMethod.DEFLATE)
                                                .compressionLevel(CompressionLevel.NORMAL)
                                                .splitLength(splitLength).build();
        ZipIt zip = ZipIt.builder().zipFile(zipFile).build();
        zip.add(srcDir, parameters);
    }

    private void unzipId(Path zipFile) throws ZipException {
        new ZipFileUnzip(zipFile).extract(resDir, new UnzipParameters());
    }


}
