package ru.olegcherednik.zip4jvm;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.io.IOUtils;
import ru.olegcherednik.zip4jvm.assertj.AbstractDirectoryAssert;
import ru.olegcherednik.zip4jvm.assertj.AbstractZipEntryDirectoryAssert;
import ru.olegcherednik.zip4jvm.exception.Zip4jvmException;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.function.Consumer;

import static ru.olegcherednik.zip4jvm.TestData.fileNameBentley;
import static ru.olegcherednik.zip4jvm.TestData.fileNameDucati;
import static ru.olegcherednik.zip4jvm.TestData.fileNameEmpty;
import static ru.olegcherednik.zip4jvm.TestData.fileNameFerrari;
import static ru.olegcherednik.zip4jvm.TestData.fileNameHonda;
import static ru.olegcherednik.zip4jvm.TestData.fileNameKawasaki;
import static ru.olegcherednik.zip4jvm.TestData.fileNameMcdonnelDouglas;
import static ru.olegcherednik.zip4jvm.TestData.fileNameSaintPetersburg;
import static ru.olegcherednik.zip4jvm.TestData.fileNameSigSauer;
import static ru.olegcherednik.zip4jvm.TestData.fileNameSuzuki;
import static ru.olegcherednik.zip4jvm.TestData.fileNameWiesmann;
import static ru.olegcherednik.zip4jvm.TestData.zipDirNameBikes;
import static ru.olegcherednik.zip4jvm.TestData.zipDirNameCars;
import static ru.olegcherednik.zip4jvm.TestData.zipDirNameEmpty;

/**
 * @author Oleg Cherednik
 * @since 14.03.2019
 */
@SuppressWarnings("FieldNamingConvention")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TestDataAssert {

    public static final Consumer<AbstractDirectoryAssert<?>> dirAssert = dir -> {
        dir.exists().hasSubDirectories(3).hasFiles(5);

        TestDataAssert.dirBikesAssert.accept(dir.directory(zipDirNameBikes));
        TestDataAssert.dirCarsAssert.accept(dir.directory(zipDirNameCars));
        TestDataAssert.emptyDirAssert.accept(dir.directory(zipDirNameEmpty));

        dir.file(fileNameEmpty).exists().hasEmptyContent().hasSize(0);
        dir.file(fileNameMcdonnelDouglas).exists().isImage().hasSize(624_746);
//        dir.file("Oleg Cherednik.txt").exists().hasContent("Oleg Cherednik\nОлег Чередник").hasSize(41);
        dir.file(fileNameSaintPetersburg).exists().isImage().hasSize(1_074_836);
        dir.file(fileNameSigSauer).exists().isImage().hasSize(431_478);
    };

    public static final Consumer<AbstractDirectoryAssert<?>> dirBikesAssert = dir -> {
        dir.exists().hasSubDirectories(0).hasFiles(4);
        dir.file(fileNameDucati).exists().isImage().hasSize(293_823);
        dir.file(fileNameHonda).exists().isImage().hasSize(154_591);
        dir.file(fileNameKawasaki).exists().isImage().hasSize(167_026);
        dir.file(fileNameSuzuki).exists().isImage().hasSize(287_349);
    };

    public static final Consumer<AbstractDirectoryAssert<?>> dirCarsAssert = dir -> {
        dir.exists().hasSubDirectories(0).hasFiles(3);
        dir.file(fileNameBentley).exists().isImage().hasSize(1_395_362);
        dir.file(fileNameFerrari).exists().isImage().hasSize(320_894);
        dir.file(fileNameWiesmann).exists().isImage().hasSize(729_633);
    };

    public static final Consumer<AbstractDirectoryAssert<?>> emptyDirAssert = dir -> dir.exists().hasSubDirectories(0).hasFiles(0);

    public static final Consumer<AbstractZipEntryDirectoryAssert<?>> zipRootDirAssert = dir -> {
        dir.exists().hasSubDirectories(3).hasFiles(5);

        TestDataAssert.zipBikesDirAssert.accept(dir.directory(zipDirNameBikes));
        TestDataAssert.zipCarsDirAssert.accept(dir.directory(zipDirNameCars));
        TestDataAssert.zipEmptyDirAssert.accept(dir.directory(zipDirNameEmpty));

        dir.file("mcdonnell-douglas-f15-eagle.jpg").exists().hasSize(624_746);
        dir.file("saint-petersburg.jpg").exists().hasSize(1_074_836);
        dir.file("sig-sauer-pistol.jpg").exists().hasSize(431_478);
        dir.file("empty_file.txt").exists().hasEmptyContent().hasSize(0);
//        dir.file("Oleg Cherednik.txt").exists().hasContent("Oleg Cherednik\nОлег Чередник").hasSize(41);
    };

    public static final Consumer<AbstractZipEntryDirectoryAssert<?>> zipBikesDirAssert = dir -> {
        dir.exists().hasSubDirectories(0).hasFiles(4);
        dir.file(fileNameDucati).exists().hasSize(293_823);
        dir.file(fileNameHonda).exists().hasSize(154_591);
        dir.file(fileNameKawasaki).exists().hasSize(167_026);
        dir.file(fileNameSuzuki).exists().hasSize(287_349);
    };

    public static final Consumer<AbstractZipEntryDirectoryAssert<?>> zipCarsDirAssert = dir -> {
        dir.exists().hasSubDirectories(0).hasFiles(3);
        dir.file("bentley-continental.jpg").exists().hasSize(1_395_362);
        dir.file("ferrari-458-italia.jpg").exists().hasSize(320_894);
        dir.file("wiesmann-gt-mf5.jpg").exists().hasSize(729_633);
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

        throw new Zip4jvmException("Cannot detect method name");
    }

}
