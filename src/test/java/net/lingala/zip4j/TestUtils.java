package net.lingala.zip4j;

import lombok.experimental.UtilityClass;
import net.lingala.zip4j.assertj.AbstractZipEntryDirectoryAssert;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.lingala.zip4j.assertj.Zip4jAssertions.assertThat;

/**
 * @author Oleg Cherednik
 * @since 14.03.2019
 */
@UtilityClass
@SuppressWarnings("FieldNamingConvention")
class TestUtils {

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

    final Consumer<AbstractZipEntryDirectoryAssert<?>> rootDirAssert = dir -> {
        dir.exists().hasSubDirectories(3).hasFiles(5);

        TestUtils.carsDirAssert.accept(dir.directory("cars/"));
        TestUtils.starWarsDirAssert.accept(dir.directory("Star Wars/"));
        TestUtils.emptyDirAssert.accept(dir.directory("empty_dir/"));

        dir.file("mcdonnell-douglas-f15-eagle.jpg").exists().isImage().hasSize(624_746);
        dir.file("saint-petersburg.jpg").exists().isImage().hasSize(1_074_836);
        dir.file("sig-sauer-pistol.jpg").exists().isImage().hasSize(431_478);
        dir.file("empty_file.txt").exists().hasEmptyContent().hasSize(0);
        dir.file("Oleg Cherednik.txt").exists().hasContent("Oleg Cherednik\nОлег Чередник").hasSize(41);
    };

    final Consumer<AbstractZipEntryDirectoryAssert<?>> carsDirAssert = dir -> {
        dir.exists().hasSubDirectories(0).hasFiles(3);
        dir.file("bentley-continental.jpg").exists().isImage().hasSize(1_395_362);
        dir.file("ferrari-458-italia.jpg").exists().isImage().hasSize(320_894);
        dir.file("wiesmann-gt-mf5.jpg").exists().isImage().hasSize(729_633);
    };

    final Consumer<AbstractZipEntryDirectoryAssert<?>> starWarsDirAssert = dir -> {
        dir.exists().hasSubDirectories(0).hasFiles(4);
        dir.file("0qQnv2v.jpg").exists().isImage().hasSize(2_204_448);
        dir.file("080fc325efa248454e59b84be24ea829.jpg").exists().isImage().hasSize(277_857);
        dir.file("pE9Hkw6.jpg").exists().isImage().hasSize(1_601_879);
        dir.file("star-wars-wallpapers-29931-7188436.jpg").exists().isImage().hasSize(1_916_776);
    };

    final Consumer<AbstractZipEntryDirectoryAssert<?>> emptyDirAssert = dir -> dir.exists().hasSubDirectories(0).hasFiles(0);

}
