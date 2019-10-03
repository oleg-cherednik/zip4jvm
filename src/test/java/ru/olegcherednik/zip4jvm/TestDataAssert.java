package ru.olegcherednik.zip4jvm;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.olegcherednik.zip4jvm.assertj.AbstractDirectoryAssert;
import ru.olegcherednik.zip4jvm.assertj.AbstractZipEntryDirectoryAssert;
import ru.olegcherednik.zip4jvm.assertj.AbstractZipEntryFileAssert;
import ru.olegcherednik.zip4jvm.exception.Zip4jvmException;
import ru.olegcherednik.zip4jvm.utils.ZipUtils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
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

    public static final Consumer<AbstractDirectoryAssert<?>> dirSrcAssert = dir -> {
        dir.exists().hasDirectories(3).hasFiles(5);

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
        dir.exists().hasDirectories(0).hasFiles(4);
        dir.file(fileNameDucati).exists().isImage().hasSize(293_823);
        dir.file(fileNameHonda).exists().isImage().hasSize(154_591);
        dir.file(fileNameKawasaki).exists().isImage().hasSize(167_026);
        dir.file(fileNameSuzuki).exists().isImage().hasSize(287_349);
    };

    public static final Consumer<AbstractDirectoryAssert<?>> dirCarsAssert = dir -> {
        dir.exists().hasDirectories(0).hasFiles(3);
        dir.file(fileNameBentley).exists().isImage().hasSize(1_395_362);
        dir.file(fileNameFerrari).exists().isImage().hasSize(320_894);
        dir.file(fileNameWiesmann).exists().isImage().hasSize(729_633);
    };

    public static final Consumer<AbstractDirectoryAssert<?>> emptyDirAssert = dir -> dir.exists().hasDirectories(0).hasFiles(0);

    public static final Consumer<AbstractZipEntryDirectoryAssert<?>> zipDirRootAssert = dir -> {
        dir.exists().hasDirectories(3).hasFiles(5);

        TestDataAssert.zipDirBikesAssert.accept(dir.directory(zipDirNameBikes));
        TestDataAssert.zipDirCarsAssert.accept(dir.directory(zipDirNameCars));
        TestDataAssert.zipDirEmptyAssert.accept(dir.directory(zipDirNameEmpty));

        TestDataAssert.zipFileMcdonnelDouglasAssert.accept(dir.file(fileNameMcdonnelDouglas));
        TestDataAssert.zipFileSaintPetersburgAssert.accept(dir.file(fileNameSaintPetersburg));
        TestDataAssert.zipFileSigSauerAssert.accept(dir.file(fileNameSigSauer));
        TestDataAssert.zipFileEmptyAssert.accept(dir.file(fileNameEmpty));
//        TestDataAssert.zipFileOlegCherednikAssert.accept(dir.file(fileNameOlegCherednik));
    };

    public static final Consumer<AbstractZipEntryFileAssert<?>> zipFileMcdonnelDouglasAssert = file -> file.exists().hasSize(624_746);
    public static final Consumer<AbstractZipEntryFileAssert<?>> zipFileSaintPetersburgAssert = file -> file.exists().hasSize(1_074_836);
    public static final Consumer<AbstractZipEntryFileAssert<?>> zipFileSigSauerAssert = file -> file.exists().hasSize(431_478);
    public static final Consumer<AbstractZipEntryFileAssert<?>> zipFileEmptyAssert = file -> file.exists().hasSize(0);
    public static final Consumer<AbstractZipEntryFileAssert<?>> zipFileOlegCherednikAssert = file -> file.exists().hasSize(1_395_362);

    public static final Consumer<AbstractZipEntryDirectoryAssert<?>> zipDirBikesAssert = dir -> {
        dir.exists().hasDirectories(0).hasFiles(4);
        TestDataAssert.zipFileDucatiAssert.accept(dir.file(fileNameDucati));
        TestDataAssert.zipFileHondaAssert.accept(dir.file(fileNameHonda));
        TestDataAssert.zipFileKawasakiAssert.accept(dir.file(fileNameKawasaki));
        TestDataAssert.zipFileSuzukiAssert.accept(dir.file(fileNameSuzuki));
    };

    public static final Consumer<AbstractZipEntryFileAssert<?>> zipFileDucatiAssert = file -> file.exists().hasSize(293_823);
    public static final Consumer<AbstractZipEntryFileAssert<?>> zipFileHondaAssert = file -> file.exists().hasSize(154_591);
    public static final Consumer<AbstractZipEntryFileAssert<?>> zipFileKawasakiAssert = file -> file.exists().hasSize(167_026);
    public static final Consumer<AbstractZipEntryFileAssert<?>> zipFileSuzukiAssert = file -> file.exists().hasSize(287_349);

    public static final Consumer<AbstractZipEntryDirectoryAssert<?>> zipDirCarsAssert = dir -> {
        dir.exists().hasDirectories(0).hasFiles(3);
        TestDataAssert.zipFileBentleyAssert.accept(dir.file(fileNameBentley));
        TestDataAssert.zipFileFerrariAssert.accept(dir.file(fileNameFerrari));
        TestDataAssert.zipFileWiesmannAssert.accept(dir.file(fileNameWiesmann));
    };

    public static final Consumer<AbstractZipEntryFileAssert<?>> zipFileBentleyAssert = file -> file.exists().hasSize(1_395_362);
    public static final Consumer<AbstractZipEntryFileAssert<?>> zipFileFerrariAssert = file -> file.exists().hasSize(320_894);
    public static final Consumer<AbstractZipEntryFileAssert<?>> zipFileWiesmannAssert = file -> file.exists().hasSize(729_633);

    public static final Consumer<AbstractZipEntryDirectoryAssert<?>> zipDirEmptyAssert = dir -> dir.exists().hasDirectories(0).hasFiles(0);

    public static void copyLarge(InputStream in, Path dst) throws IOException {
        ZipUtils.copyLarge(in, new FileOutputStream(dst.toFile()));
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
