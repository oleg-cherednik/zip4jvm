package ru.olegcherednik.zip4jvm;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.io.IOUtils;
import ru.olegcherednik.zip4jvm.assertj.AbstractDirectoryAssert;
import ru.olegcherednik.zip4jvm.assertj.AbstractZipEntryDirectoryAssert;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.function.Consumer;

/**
 * @author Oleg Cherednik
 * @since 14.03.2019
 */
@SuppressWarnings("FieldNamingConvention")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TestUtils {

    public static final Consumer<AbstractDirectoryAssert<?>> dirAssert = dir -> {
        dir.exists().hasSubDirectories(3).hasFiles(5);

        TestUtils.carsDirAssert.accept(dir.directory("cars/"));
        TestUtils.starWarsDirAssert.accept(dir.directory("Star Wars/"));
        TestUtils.emptyDirAssert.accept(dir.directory("empty_dir/"));

        dir.file("mcdonnell-douglas-f15-eagle.jpg").exists().isImage().hasSize(624_746);
        dir.file("saint-petersburg.jpg").exists().isImage().hasSize(1_074_836);
        dir.file("sig-sauer-pistol.jpg").exists().isImage().hasSize(431_478);
        dir.file("empty_file.txt").exists().hasEmptyContent().hasSize(0);
//        dir.file("Oleg Cherednik.txt").exists().hasContent("Oleg Cherednik\nОлег Чередник").hasSize(41);
    };

    public static final Consumer<AbstractDirectoryAssert<?>> carsDirAssert = dir -> {
        dir.exists().hasSubDirectories(0).hasFiles(3);
        dir.file("bentley-continental.jpg").exists().isImage().hasSize(1_395_362);
        dir.file("ferrari-458-italia.jpg").exists().isImage().hasSize(320_894);
        dir.file("wiesmann-gt-mf5.jpg").exists().isImage().hasSize(729_633);
    };

    public static final Consumer<AbstractDirectoryAssert<?>> starWarsDirAssert = dir -> {
        dir.exists().hasSubDirectories(0).hasFiles(4);
        dir.file("one.jpg").exists().isImage().hasSize(2_204_448);
        dir.file("two.jpg").exists().isImage().hasSize(277_857);
        dir.file("three.jpg").exists().isImage().hasSize(1_601_879);
        dir.file("four.jpg").exists().isImage().hasSize(1_916_776);
    };

    public static final Consumer<AbstractDirectoryAssert<?>> emptyDirAssert = dir -> dir.exists().hasSubDirectories(0).hasFiles(0);

    public static final Consumer<AbstractZipEntryDirectoryAssert<?>> zipRootDirAssert = dir -> {
        dir.exists().hasSubDirectories(3).hasFiles(5);

        TestUtils.zipCarsDirAssert.accept(dir.directory("cars/"));
        TestUtils.zipStarWarsDirAssert.accept(dir.directory("Star Wars/"));
        TestUtils.zipEmptyDirAssert.accept(dir.directory("empty_dir/"));

        dir.file("mcdonnell-douglas-f15-eagle.jpg").exists().isImage().hasSize(624_746);
        dir.file("saint-petersburg.jpg").exists().isImage().hasSize(1_074_836);
        dir.file("sig-sauer-pistol.jpg").exists().isImage().hasSize(431_478);
        dir.file("empty_file.txt").exists().hasEmptyContent().hasSize(0);
//        dir.file("Oleg Cherednik.txt").exists().hasContent("Oleg Cherednik\nОлег Чередник").hasSize(41);
    };

    public static final Consumer<AbstractZipEntryDirectoryAssert<?>> zipCarsDirAssert = dir -> {
        dir.exists().hasSubDirectories(0).hasFiles(3);
        dir.file("bentley-continental.jpg").exists().isImage().hasSize(1_395_362);
        dir.file("ferrari-458-italia.jpg").exists().isImage().hasSize(320_894);
        dir.file("wiesmann-gt-mf5.jpg").exists().isImage().hasSize(729_633);
    };

    public static final Consumer<AbstractZipEntryDirectoryAssert<?>> zipStarWarsDirAssert = dir -> {
        dir.exists().hasSubDirectories(0).hasFiles(4);
        dir.file("one.jpg").exists().isImage().hasSize(2_204_448);
        dir.file("two.jpg").exists().isImage().hasSize(277_857);
        dir.file("three.jpg").exists().isImage().hasSize(1_601_879);
        dir.file("four.jpg").exists().isImage().hasSize(1_916_776);
    };

    public static final Consumer<AbstractZipEntryDirectoryAssert<?>> zipEmptyDirAssert = dir -> dir.exists().hasSubDirectories(0).hasFiles(0);

    public static void copyLarge(InputStream in, Path dst) throws IOException {
        try (OutputStream out = new FileOutputStream(dst.toFile())) {
            IOUtils.copyLarge(in, out);
        } finally {
            in.close();
        }
    }

    public static String getMethodName() {
        for (StackTraceElement element : Thread.currentThread().getStackTrace()) {
            String className = element.getClassName();

            if (className.startsWith(ZipIt.class.getPackage().getName()) && className.endsWith("Test"))
                return element.getMethodName();
        }

        throw new RuntimeException("Cannot detect ");
    }

}
