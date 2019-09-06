package ru.olegcherednik.zip4jvm;

import org.apache.commons.io.FileUtils;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import ru.olegcherednik.zip4jvm.assertj.Zip4jAssertions;
import ru.olegcherednik.zip4jvm.data.DefalteZipData;
import ru.olegcherednik.zip4jvm.data.StoreZipData;
import ru.olegcherednik.zip4jvm.exception.Zip4jException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Oleg Cherednik
 * @since 23.03.2019
 */
@SuppressWarnings("FieldNamingConvention")
public class Zip4jSuite {

    //    public static final Path rootDir = createTempDirectory("zip4j");
    public static final Path rootDir = Paths.get("d:/zip4j/foo");
    public static final Path srcDir = rootDir.resolve("src");

    public static final Path carsDir = srcDir.resolve("cars");
    public static final Path starWarsDir = srcDir.resolve("Star Wars");
    public static final Path emptyDir = srcDir.resolve("empty_dir");

    public static final List<Path> contentSrcDir = collect(srcDir, "cars", "Star Wars", "empty_dir", "empty_file.txt",
            "mcdonnell-douglas-f15-eagle.jpg", "Oleg Cherednik.txt", "saint-petersburg.jpg", "sig-sauer-pistol.jpg");

    public static final List<Path> filesSrcDir = collect(srcDir, "empty_file.txt", "mcdonnell-douglas-f15-eagle.jpg", "Oleg Cherednik.txt",
            "saint-petersburg.jpg", "sig-sauer-pistol.jpg");
    public static final List<Path> filesCarsDir = collect(carsDir, "bentley-continental.jpg", "ferrari-458-italia.jpg", "wiesmann-gt-mf5.jpg");
    public static final List<Path> filesStarWarsDir = collect(starWarsDir, "one.jpg", "two.jpg", "three.jpg", "four.jpg");

    private static Path createTempDirectory(String prefix) {
        try {
            return Files.createTempDirectory(prefix);
        } catch(IOException e) {
            throw new Zip4jException(e);
        }
    }

    private static List<Path> collect(Path dir, String... fileNames) {
        List<Path> paths = Stream.of(fileNames)
                                 .map(dir::resolve)
                                 .collect(Collectors.toList());
        return Collections.unmodifiableList(paths);
    }

    // store
    public static final Path storeSolidZip = rootDir.resolve("store/solid/off/src.zip");
    public static final Path storeSplitZip = rootDir.resolve("store/split/off/src.zip");

    // deflate
    public static final Path deflateSolidZip = rootDir.resolve("deflate/solid/off/src.zip");
    public static final Path deflateSplitZip = rootDir.resolve("deflate/split/off/src.zip");
    public static final Path deflateSolidPkwareZip = rootDir.resolve("deflate/solid/pkware/src.zip");
    public static final Path deflateSolidAesZip = rootDir.resolve("deflate/solid/aes/src.zip");

    // winrar
    public static final Path winRarStoreSolidZip = Paths.get("src/test/resources/winrar/store_solid_off.zip").toAbsolutePath();
    public static final Path winRarStoreSolidPkwareZip = Paths.get("src/test/resources/winrar/store_solid_pkware.zip").toAbsolutePath();
    public static final Path winRarStoreSolidAesZip = Paths.get("src/test/resources/winrar/store_solid_aes.zip").toAbsolutePath();
    public static final Path winRarDeflateSolidZip = Paths.get("src/test/resources/winrar/deflate_solid_off.zip").toAbsolutePath();
    public static final Path winRarDeflateSolidPkwareZip = Paths.get("src/test/resources/winrar/deflate_solid_pkware.zip").toAbsolutePath();
    public static final Path winRarDeflateSolidAesZip = Paths.get("src/test/resources/winrar/deflate_solid_aes.zip").toAbsolutePath();

    /** Password for encrypted zip */
    public static final char[] password = "1".toCharArray();
    /** Clear resources */
    public static final boolean clear = false;

    private static final long time = System.currentTimeMillis();

    @BeforeSuite
    public void beforeSuite() throws IOException {
        removeDir(rootDir);

        copyTestData();
        StoreZipData.createStoreZip();
        DefalteZipData.createDeflateZip();
    }

    @AfterSuite(enabled = clear)
    public void afterSuite() throws IOException {
        removeDir(rootDir);
    }

    private static void copyTestData() throws IOException {
        Files.createDirectories(emptyDir);

        Path dataDir = Paths.get("src/test/resources/data").toAbsolutePath();

        Files.walk(dataDir).forEach(path -> {
            try {
                if (Files.isDirectory(path))
                    Files.createDirectories(srcDir.resolve(dataDir.relativize(path)));
                else if (Files.isRegularFile(path))
                    Files.copy(path, srcDir.resolve(dataDir.relativize(path)));
            } catch(IOException e) {
                e.printStackTrace();
            }
        });

        Zip4jAssertions.assertThatDirectory(srcDir).matches(TestUtils.dirAssert);
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
        return rootDir.resolve(cls.getSimpleName());
    }

    public static Path generateSubDirNameWithTime(Class<?> cls) {
        String baseDir = Zip4jSuite.class.getPackage().getName();
        String relativePath = cls.getName().substring(baseDir.length() + 1).replaceAll("\\.", "/");
        return rootDir.resolve(relativePath).resolve(Paths.get(String.valueOf(time)));
    }

    public static Path subDirNameAsMethodNameWithTme(Path rootDir) {
        return rootDir.resolve(TestUtils.getMethodName()).resolve(Paths.get(String.valueOf(time)));
    }

    public static Path subDirNameAsMethodName(Path rootDir) {
        return rootDir.resolve(TestUtils.getMethodName());
    }

    public static Path subDirNameAsRelativePathToRoot(Path rootDir, Path zipFile) {
        Path path;

        if (zipFile.toAbsolutePath().toString().contains("resources"))
            path = Paths.get("src/test/resources/winrar").toAbsolutePath().relativize(zipFile);
        else
            path = Zip4jSuite.rootDir.relativize(zipFile);

        String dirName = path.toString().replaceAll("\\\\", "_");

        return rootDir.resolve(dirName);
    }

}
