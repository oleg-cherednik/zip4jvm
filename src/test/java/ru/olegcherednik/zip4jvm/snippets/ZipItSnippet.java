package ru.olegcherednik.zip4jvm.snippets;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.olegcherednik.zip4jvm.Zip4jvmSuite;
import ru.olegcherednik.zip4jvm.ZipFile;
import ru.olegcherednik.zip4jvm.ZipIt;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static ru.olegcherednik.zip4jvm.TestData.dirCars;
import static ru.olegcherednik.zip4jvm.TestData.fileBentley;
import static ru.olegcherednik.zip4jvm.TestData.fileDucati;
import static ru.olegcherednik.zip4jvm.TestData.fileHonda;
import static ru.olegcherednik.zip4jvm.TestData.fileKawasaki;
import static ru.olegcherednik.zip4jvm.TestData.fileSaintPetersburg;

/**
 * @author Oleg Cherednik
 * @since 04.10.2019
 */
@Test
@SuppressWarnings({ "FieldNamingConvention", "NewClassNamingConvention" })
public class ZipItSnippet {

    private static final Path rootDir = Zip4jvmSuite.generateSubDirNameWithTime(ZipItSnippet.class);

    @BeforeClass
    public static void createDir() throws IOException {
        Files.createDirectories(rootDir);
    }

    @AfterClass(enabled = Zip4jvmSuite.clear)
    public static void removeDir() throws IOException {
        Zip4jvmSuite.removeDir(rootDir);
    }

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
        Collection<Path> paths = Arrays.asList(fileDucati, fileHonda, dirCars, fileSaintPetersburg);
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

    public void createOrOpenExistedZipArchiveAndAddInputStreamContentAsRegularFile() throws IOException {
        ZipFile.Entry entry = ZipFile.Entry.builder()
                                           .inputStreamSupplier(() -> new FileInputStream(fileBentley.toFile()))
                                           .fileName("my_cars/bentley-continental.jpg")
                                           .lastModifiedTime(System.currentTimeMillis()).build();

        Path zip = Zip4jvmSuite.subDirNameAsMethodName(rootDir).resolve("filename.zip");
        ZipIt.zip(zip).addEntry(entry);
    }

    public void createOrOpenExistedZipArchiveAndAddInputStreamsContentAsRegularFiles() throws IOException {
        ZipFile.Entry entryBentley = ZipFile.Entry.builder()
                                                  .inputStreamSupplier(() -> new FileInputStream(fileBentley.toFile()))
                                                  .fileName("my_cars/bentley-continental.jpg")
                                                  .lastModifiedTime(System.currentTimeMillis()).build();

        ZipFile.Entry entryKawasaki = ZipFile.Entry.builder()
                                                   .inputStreamSupplier(() -> new FileInputStream(fileKawasaki.toFile()))
                                                   .fileName("my_bikes/kawasaki.jpg")
                                                   .lastModifiedTime(System.currentTimeMillis()).build();

        List<ZipFile.Entry> entries = Arrays.asList(entryBentley, entryKawasaki);

        Path zip = Zip4jvmSuite.subDirNameAsMethodName(rootDir).resolve("filename.zip");
        ZipIt.zip(zip).addEntry(entries);
    }

}
