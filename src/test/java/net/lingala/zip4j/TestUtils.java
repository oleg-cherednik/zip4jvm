package net.lingala.zip4j;

import lombok.experimental.UtilityClass;
import net.lingala.zip4j.assertj.AbstractZipEntryDirectoryAssert;

import java.util.function.Consumer;

/**
 * @author Oleg Cherednik
 * @since 14.03.2019
 */
@UtilityClass
@SuppressWarnings("FieldNamingConvention")
class TestUtils {

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
