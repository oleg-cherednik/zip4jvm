package com.cop.zip4j;

import com.cop.zip4j.data.DefalteZipData;
import com.cop.zip4j.data.StoreZipData;
import org.apache.commons.io.FileUtils;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.cop.zip4j.assertj.Zip4jAssertions.assertThatDirectory;

/**
 * @author Oleg Cherednik
 * @since 23.03.2019
 */
@SuppressWarnings("FieldNamingConvention")
public class Zip4jSuite {

    public static final Path rootDir = Paths.get("d:/zip4j/foo");//Files.createTempDirectory("zip4j");
    public static final Path srcDir = rootDir.resolve("src");

    public static final Path carsDir = srcDir.resolve("cars");
    public static final Path starWarsDir = srcDir.resolve("Star Wars");
    public static final Path emptyDir = srcDir.resolve("empty_dir");

    // store
    public static final Path storeSolidZip = rootDir.resolve("store/solid/src.zip");
    public static final Path storeSplitZip = rootDir.resolve("store/split/src.zip");

    // deflate
    public static final Path deflateSolidZip = rootDir.resolve("deflate/solid/off/src.zip");
    public static final Path deflateSolidPkwareZip = rootDir.resolve("deflate/solid/pkware/src.zip");
    public static final Path deflateSplitZip = rootDir.resolve("deflate/split/щаа.src.zip");

    public static final Path winRarPkwareZip = Paths.get("src/test/resources/pkware.zip").toAbsolutePath();
    public static final Path winRarAesZip = Paths.get("src/test/resources/aes.zip").toAbsolutePath();
    public static final Path storeAesZip = Paths.get("src/test/resources/storeAes.zip").toAbsolutePath();
    public static final Path wirRarStoreZip = Paths.get("src/test/resources/store.zip").toAbsolutePath();

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
        Files.createDirectories(srcDir.resolve("empty_dir"));

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

        assertThatDirectory(srcDir).matches(TestUtils.dirAssert);
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
        return rootDir.resolve(cls.getSimpleName()).resolve(Paths.get(String.valueOf(time)));
    }

    public static Path subDirNameAsMethodNameWithTme(Path rootDir) {
        return rootDir.resolve(TestUtils.getMethodName()).resolve(Paths.get(String.valueOf(time)));
    }

    public static Path subDirNameAsMethodName(Path rootDir) {
        return rootDir.resolve(TestUtils.getMethodName());
    }

}
