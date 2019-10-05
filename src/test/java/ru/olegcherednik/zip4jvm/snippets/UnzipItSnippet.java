package ru.olegcherednik.zip4jvm.snippets;

import org.apache.commons.io.FileUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.olegcherednik.zip4jvm.UnzipIt;
import ru.olegcherednik.zip4jvm.Zip4jvmSuite;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static ru.olegcherednik.zip4jvm.TestData.dirNameBikes;
import static ru.olegcherednik.zip4jvm.TestData.dirNameCars;
import static ru.olegcherednik.zip4jvm.TestData.fileNameDucati;
import static ru.olegcherednik.zip4jvm.TestData.fileNameSaintPetersburg;
import static ru.olegcherednik.zip4jvm.TestData.zipDeflateSolid;

/**
 * @author Oleg Cherednik
 * @since 05.10.2019
 */
@Test
@SuppressWarnings({ "FieldNamingConvention", "NewClassNamingConvention" })
public class UnzipItSnippet {

    private static final Path rootDir = Zip4jvmSuite.generateSubDirNameWithTime(UnzipItSnippet.class);
    private static final Path zip = rootDir.resolve("filename.zip");

    @BeforeClass
    public static void createDir() throws IOException {
        Files.createDirectories(rootDir);
        FileUtils.copyFile(zipDeflateSolid.toFile(), zip.toFile());
    }

    @AfterClass(enabled = Zip4jvmSuite.clear)
    public static void removeDir() throws IOException {
        Zip4jvmSuite.removeDir(rootDir);
    }

    public void extractAllEntriesIntoGivenDirectory() throws IOException {
        Path destDir = Zip4jvmSuite.subDirNameAsMethodName(rootDir).resolve("filename_content");
        UnzipIt.zip(zip).destDir(destDir).extract();
    }

    public void extractRegularFileIntoGivenDirectory() throws IOException {
        Path destDir = Zip4jvmSuite.subDirNameAsMethodName(rootDir).resolve("filename_content");
        UnzipIt.zip(zip).destDir(destDir).extract("cars/bentley-continental.jpg");
    }

    public void extractDirectoryIntoGivenDirectory() throws IOException {
        Path destDir = Zip4jvmSuite.subDirNameAsMethodName(rootDir).resolve("filename_content");
        UnzipIt.zip(zip).destDir(destDir).extract("cars");
    }

    public void extractSomeEntriesIntoGivenDirectory() throws IOException {
        List<String> fileNames = Arrays.asList(dirNameCars, dirNameBikes + '/' + fileNameDucati, fileNameSaintPetersburg);
        Path destDir = Zip4jvmSuite.subDirNameAsMethodName(rootDir).resolve("filename_content");
        UnzipIt.zip(zip).destDir(destDir).extract(fileNames);
    }

}
