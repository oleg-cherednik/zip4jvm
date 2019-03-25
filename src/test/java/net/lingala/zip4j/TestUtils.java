package net.lingala.zip4j;

import lombok.experimental.UtilityClass;
import org.apache.commons.io.FilenameUtils;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Oleg Cherednik
 * @since 14.03.2019
 */
@UtilityClass
class TestUtils {

    void checkDestinationDir(int totalParts, Path destDir) throws IOException {
        checkDirectory(destDir, 0, totalParts);
    }

    void checkResultDir(Path resDir) throws IOException {
        checkDirectory(resDir, 3, 5);

        checkCarsDirectory(resDir.resolve("cars"));
        checkStarWarsDirectory(resDir.resolve("Star Wars"));
        checkEmptyDirectory(resDir.resolve("empty_dir"));

        checkImage(resDir.resolve("mcdonnell-douglas-f15-eagle.jpg"), 624_746);
        checkImage(resDir.resolve("saint-petersburg.jpg"), 1_074_836);
        checkImage(resDir.resolve("sig-sauer-pistol.jpg"), 431_478);
        checkTextFile(resDir.resolve("empty_file.txt"), 0, null);
        checkTextFile(resDir.resolve("Oleg Cherednik.txt"), 41, "Oleg Cherednik\nОлег Чередник");
    }

    void checkCarsDirectory(Path path) throws IOException {
        checkDirectory(path, 0, 3);
        checkImage(path.resolve("bentley-continental.jpg"), 1_395_362);
        checkImage(path.resolve("ferrari-458-italia.jpg"), 320_894);
        checkImage(path.resolve("wiesmann-gt-mf5.jpg"), 729_633);
    }

    void checkStarWarsDirectory(Path path) throws IOException {
        checkDirectory(path, 0, 4);
        checkImage(path.resolve("0qQnv2v.jpg"), 2_204_448);
        checkImage(path.resolve("080fc325efa248454e59b84be24ea829.jpg"), 277_857);
        checkImage(path.resolve("pE9Hkw6.jpg"), 1_601_879);
        checkImage(path.resolve("star-wars-wallpapers-29931-7188436.jpg"), 1_916_776);
    }

    void checkEmptyDirectory(Path path) throws IOException {
        checkDirectory(path, 0, 0);
    }

    void checkImage(Path path, long size) throws IOException {
        assertThat(Files.exists(path)).isTrue();
        assertThat(Files.isRegularFile(path)).isTrue();
        assertThat(Files.size(path)).isEqualTo(size);
        assertThat(isImage(path)).isTrue();
    }

    private void checkTextFile(Path path, long size, String content) throws IOException {
        assertThat(Files.exists(path)).isTrue();
        assertThat(Files.isRegularFile(path)).isTrue();
        assertThat(Files.size(path)).isEqualTo(size);

        if (size != 0)
            assertThat(getTextFileContent(path)).isEqualTo(content);
    }

    private boolean isImage(Path path) {
        try {
            return ImageIO.read(path.toFile()) != null;
        } catch(Exception e) {
            return false;
        }
    }

    private String getTextFileContent(Path path) throws IOException {
        try (Stream<String> stream = Files.lines(path, StandardCharsets.UTF_8)) {
            return stream.collect(Collectors.joining("\n"));
        }
    }

    void checkDirectory(Path path, int foldersAmount, int regularFilesAmount) throws IOException {
        assertThat(Files.exists(path)).isTrue();
        assertThat(Files.isDirectory(path)).isTrue();
        assertThat(getFoldersAmount(path)).isEqualTo(foldersAmount);
        assertThat(getRegularFilesAmount(path)).isEqualTo(regularFilesAmount);
    }

    private long getRegularFilesAmount(Path dir) throws IOException {
        return Files.list(dir).filter(path -> Files.isRegularFile(path)).count();
    }

    private long getFoldersAmount(Path dir) throws IOException {
        return Files.list(dir).filter(path -> Files.isDirectory(path)).count();
    }

    // -------

    void checkCarsDirectory(ZipFile zip, String dir) {
        checkDirectory(zip, dir, 0, 3);
        checkImage(zip, dir + "bentley-continental.jpg", 1_395_362);
        checkImage(zip, dir + "ferrari-458-italia.jpg", 320_894);
        checkImage(zip, dir + "wiesmann-gt-mf5.jpg", 729_633);
    }

//    void checkDirectory(ZipFile zip, String dir, int foldersAmount, int regularFilesAmount) {
//        assertThat(getFoldersAmount(zip, dir)).isEqualTo(foldersAmount);
//        assertThat(getRegularFilesAmount(zip, dir)).isEqualTo(regularFilesAmount);
//    }

    void checkDirectory(ZipFile zipFile, String dir, int foldersAmount, int regularFilesAmount) {
        if (!"/".equals(dir)) {
            ZipEntry entry = zipFile.getEntry(dir);

            assertThat(entry).isNotNull();
            assertThat(entry.isDirectory()).isTrue();
        }

        assertThat(getFoldersAmount(zipFile, dir)).isEqualTo(foldersAmount);
        assertThat(getRegularFilesAmount(zipFile, dir)).isEqualTo(regularFilesAmount);
    }

    private long getRegularFilesAmount(ZipFile zip, String dir) {
        Map<String, Set<String>> map = walk(zip);
        long count = 0;

        for (String entryName : map.getOrDefault(dir, Collections.emptySet()))
            if (!isDirectory(entryName))
                count++;

        return count;
    }

    private long getFoldersAmount(ZipFile zip, String dir) {
        Map<String, Set<String>> map = walk(zip);
        long count = 0;

        for (String entryName : map.getOrDefault(dir, Collections.emptySet()))
            if (isDirectory(entryName))
                count++;

        return count;
    }

    private static Map<String, Set<String>> walk(ZipFile zip) {
        Map<String, Set<String>> map = new HashMap<>();

        Enumeration<? extends ZipEntry> entries = zip.entries();

        while (entries.hasMoreElements())
            add(entries.nextElement().getName(), map);

        return map;
    }

    private static void add(String entryName, Map<String, Set<String>> map) {
        int offs = 0;
        String parent = "/";

        while (parent != null) {
            map.computeIfAbsent(parent, val -> new HashSet<>());

            int pos = entryName.indexOf('/', offs);

            if (pos >= 0) {
                String part = entryName.substring(offs, pos + 1);
                String path = entryName.substring(0, pos + 1);

                map.computeIfAbsent(path, val -> new HashSet<>());
                map.get(parent).add(part);

                offs = pos + 1;
                parent = path;
            } else {
                if (offs < entryName.length())
                    map.get(parent).add(entryName.substring(offs));
                parent = null;
            }
        }
    }

    private boolean isDirectory(String entryName) {
        return FilenameUtils.getExtension(entryName).isEmpty();
    }

    void checkImage(ZipFile zip, String entryName, long size) {
        ZipEntry entry = zip.getEntry(entryName);

        assertThat(entry).isNotNull();
        assertThat(entry.isDirectory()).isFalse();
        assertThat(entry.getSize()).isEqualTo(size);
        assertThat(isImage(zip, entry)).isTrue();
    }

    private boolean isImage(ZipFile zipFile, ZipEntry entry) {
        try (InputStream in = zipFile.getInputStream(entry)) {
            return ImageIO.read(in) != null;
        } catch(Exception e) {
            return false;
        }
    }

}
