package ru.olegcherednik.zip4jvm.snippets;

import org.testng.annotations.Test;
import ru.olegcherednik.zip4jvm.Zip4jvmSuite;
import ru.olegcherednik.zip4jvm.ZipFile;
import ru.olegcherednik.zip4jvm.ZipIt;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;

import static ru.olegcherednik.zip4jvm.TestData.dirCars;
import static ru.olegcherednik.zip4jvm.TestData.fileBentley;
import static ru.olegcherednik.zip4jvm.TestData.fileDucati;
import static ru.olegcherednik.zip4jvm.TestData.fileHonda;
import static ru.olegcherednik.zip4jvm.TestData.fileSaintPetersburg;

/**
 * @author Oleg Cherednik
 * @since 04.10.2019
 */
@Test
@SuppressWarnings({ "FieldNamingConvention", "NewClassNamingConvention" })
public class ZipItSnippet {

    private static final Path rootDir = Zip4jvmSuite.generateSubDirNameWithTime(ZipItSnippet.class);

    public void createOrOpenExistedZipArchiveAndAddRegularFile() throws IOException {
        Path zip = Zip4jvmSuite.subDirNameAsMethodName(rootDir).resolve("filename.zip");
        ZipIt.zip(zip).add(fileBentley);
    }

    public void createOrOpenExistedZipArchiveAndAddDirectory() throws IOException {
        Path zip = Zip4jvmSuite.subDirNameAsMethodName(rootDir).resolve("filename.zip");
        ZipIt.zip(zip).add(dirCars);
    }

    public void createOrOpenExistedZipArchiveAndAddSomeRegularFilesAndDirectories() throws IOException {
        Path zip = Zip4jvmSuite.subDirNameAsMethodName(rootDir).resolve("filename.zip");
        Collection<Path> paths = Arrays.asList(
                Paths.get("/bikes/ducati-panigale-1199.jpg"),
                Paths.get("/bikes/honda-cbr600rr.jpg"),
                Paths.get("/cars"),
                Paths.get("/saint-petersburg.jpg"));
        ZipIt.zip(zip).add(paths);
    }

    public void createOrOpenExistedZipArchiveAndAddSomeRegularFilesAndDirectoriesUsingStream() throws IOException {
        Path zip = Zip4jvmSuite.subDirNameAsMethodName(rootDir).resolve("filename.zip");

        try (ZipFile.Writer zipFile = ZipIt.zip(zip).stream()) {
            zipFile.add(fileDucati);
            zipFile.add(fileHonda);
            zipFile.add(dirCars);
            zipFile.add(fileSaintPetersburg);
        }
    }

}
