package ru.olegcherednik.zip4jvm;

import org.apache.commons.io.FileUtils;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import ru.olegcherednik.zip4jvm.data.DefalteZipData;
import ru.olegcherednik.zip4jvm.data.StoreZipData;
import ru.olegcherednik.zip4jvm.exception.Zip4jvmException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static ru.olegcherednik.zip4jvm.assertj.Zip4jvmAssertions.assertThatDirectory;

/**
 * @author Oleg Cherednik
 * @since 23.03.2019
 */
@SuppressWarnings("FieldNamingConvention")
public class Zip4jvmSuite {

    //    public static final Path dirRoot = createTempDirectory("zip4jvm");
    public static final Path dirRoot = Paths.get("d:/zip4jvm/foo");
    public static final Path dirSrc = dirRoot.resolve("src");

    public static final Path dirBikes = dirSrc.resolve("bikes");
    public static final Path dirCars = dirSrc.resolve("cars");
    public static final Path emptyDir = dirSrc.resolve("empty_dir");

    public static final List<Path> contentSrcDir = collect(dirSrc, "bikes", "cars", "empty_dir", "empty_file.txt",
            "mcdonnell-douglas-f15-eagle.jpg", "Oleg Cherednik.txt", "saint-petersburg.jpg", "sig-sauer-pistol.jpg");

    /** Password for encrypted zip */
    public static final char[] password = "1".toCharArray();
    /** Clear resources */
    public static final boolean clear = false;

    public static final long SIZE_1MB = 1024 * 1024;

    private static final long time = System.currentTimeMillis();

    private static Path createTempDirectory(String prefix) {
        try {
            return Files.createTempDirectory(prefix);
        } catch(IOException e) {
            throw new Zip4jvmException(e);
        }
    }

    private static List<Path> collect(Path dir, String... fileNames) {
        List<Path> paths = Arrays.stream(fileNames)
                                 .map(dir::resolve)
                                 .collect(Collectors.toList());
        return Collections.unmodifiableList(paths);
    }

    @BeforeSuite
    public void beforeSuite() throws IOException {
        removeDir(dirRoot);

        copyTestData();
        StoreZipData.createStoreZip();
        DefalteZipData.createDeflateZip();
    }

    @AfterSuite(enabled = clear)
    public void afterSuite() throws IOException {
        removeDir(dirRoot);
    }

    private static void copyTestData() throws IOException {
        Files.createDirectories(emptyDir);

        Path dataDir = Paths.get("src/test/resources/data").toAbsolutePath();

        Files.walk(dataDir).forEach(path -> {
            try {
                if (Files.isDirectory(path))
                    Files.createDirectories(dirSrc.resolve(dataDir.relativize(path)));
                else if (Files.isRegularFile(path))
                    Files.copy(path, dirSrc.resolve(dataDir.relativize(path)));
            } catch(IOException e) {
                e.printStackTrace();
            }
        });

        assertThatDirectory(dirSrc).matches(TestDataAssert.dirAssert);
    }

    public static void removeDir(Path path) throws IOException {
        if (Files.exists(path))
            FileUtils.deleteQuietly(path.toFile());
    }

    public static Path copy(Path rootDir, Path srcFile) throws IOException {
        Path zipFile = generateZipFileName(rootDir);
        Files.copy(srcFile, zipFile);
        return zipFile;
    }

    public static Path generateZipFileName(Path rootDir) {
        return rootDir.resolve("src_" + System.currentTimeMillis() + ".zip");
    }

    public static Path generateSubDirName(Class<?> cls) {
        return dirRoot.resolve(cls.getSimpleName());
    }

    public static Path generateSubDirNameWithTime(Class<?> cls) {
        String baseDir = Zip4jvmSuite.class.getPackage().getName();
        String relativePath = cls.getName().substring(baseDir.length() + 1).replaceAll("\\.", "/");
        return dirRoot.resolve(relativePath).resolve(Paths.get(String.valueOf(time)));
    }

    public static Path subDirNameAsMethodNameWithTme(Path rootDir) {
        return rootDir.resolve(TestDataAssert.getMethodName()).resolve(Paths.get(String.valueOf(time)));
    }

    public static Path subDirNameAsMethodName(Path rootDir) {
        return rootDir.resolve(TestDataAssert.getMethodName());
    }

    public static Path subDirNameAsRelativePathToRoot(Path rootDir, Path zipFile) {
        Path path;

        if (zipFile.toAbsolutePath().toString().contains("resources"))
            path = Paths.get("src/test/resources/winrar").toAbsolutePath().relativize(zipFile);
        else
            path = dirRoot.relativize(zipFile);

        String dirName = path.toString().replaceAll("\\\\", "_");

        return rootDir.resolve(dirName);
    }

}
