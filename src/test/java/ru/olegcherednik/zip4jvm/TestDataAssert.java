/*
 * Copyright 2019 Oleg Cherednik (oleg.cherednik@gmail.com)
 *
 * Licensed under The Apache Software License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package ru.olegcherednik.zip4jvm;

import ru.olegcherednik.zip4jvm.assertj.IDirectoryAssert;
import ru.olegcherednik.zip4jvm.assertj.IRegularFileAssert;
import ru.olegcherednik.zip4jvm.exception.Zip4jvmException;
import ru.olegcherednik.zip4jvm.utils.PathUtils;
import ru.olegcherednik.zip4jvm.utils.ZipUtils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.function.Consumer;

import static ru.olegcherednik.zip4jvm.TestData.dirNameBikes;
import static ru.olegcherednik.zip4jvm.TestData.dirNameCars;
import static ru.olegcherednik.zip4jvm.TestData.dirNameEmpty;
import static ru.olegcherednik.zip4jvm.TestData.fileBentley;
import static ru.olegcherednik.zip4jvm.TestData.fileDucati;
import static ru.olegcherednik.zip4jvm.TestData.fileFerrari;
import static ru.olegcherednik.zip4jvm.TestData.fileHonda;
import static ru.olegcherednik.zip4jvm.TestData.fileKawasaki;
import static ru.olegcherednik.zip4jvm.TestData.fileMcdonnelDouglas;
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
import static ru.olegcherednik.zip4jvm.TestData.fileSaintPetersburg;
import static ru.olegcherednik.zip4jvm.TestData.fileSigSauer;
import static ru.olegcherednik.zip4jvm.TestData.fileSuzuki;
import static ru.olegcherednik.zip4jvm.TestData.fileWiesmann;

/**
 * @author Oleg Cherednik
 * @since 14.03.2019
 */
@SuppressWarnings("FieldNamingConvention")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TestDataAssert {

    public static final Consumer<IRegularFileAssert<?>> fileDucatiAssert =
            file -> file.hasSize(293_823).isImage().isContentEqualTo(fileDucati);
    public static final Consumer<IRegularFileAssert<?>> fileHondaAssert =
            file -> file.hasSize(154_591).isImage().isContentEqualTo(fileHonda);
    public static final Consumer<IRegularFileAssert<?>> fileKawasakiAssert =
            file -> file.hasSize(167_026).isImage().isContentEqualTo(fileKawasaki);
    public static final Consumer<IRegularFileAssert<?>> fileSuzukiAssert =
            file -> file.hasSize(287_349).isImage().isContentEqualTo(fileSuzuki);

    public static final Consumer<IDirectoryAssert<?>> dirBikesAssert = dir -> {
        dir.exists().hasOnlyRegularFiles(4);
        fileDucatiAssert.accept(dir.regularFile(fileNameDucati));
        fileHondaAssert.accept(dir.regularFile(fileNameHonda));
        fileKawasakiAssert.accept(dir.regularFile(fileNameKawasaki));
        fileSuzukiAssert.accept(dir.regularFile(fileNameSuzuki));
    };

    public static final long fileBentleySize = 1_395_362;

    public static final Consumer<IRegularFileAssert<?>> fileBentleyAssert =
            file -> file.hasSize(fileBentleySize).isImage().isContentEqualTo(fileBentley);
    public static final Consumer<IRegularFileAssert<?>> fileFerrariAssert =
            file -> file.hasSize(320_894).isImage().isContentEqualTo(fileFerrari);
    public static final Consumer<IRegularFileAssert<?>> fileWiesmannAssert =
            file -> file.hasSize(729_633).isImage().isContentEqualTo(fileWiesmann);

    public static final Consumer<IDirectoryAssert<?>> dirCarsAssert = dir -> {
        dir.exists().hasOnlyRegularFiles(3);
        fileBentleyAssert.accept(dir.regularFile(fileNameBentley));
        fileFerrariAssert.accept(dir.regularFile(fileNameFerrari));
        fileWiesmannAssert.accept(dir.regularFile(fileNameWiesmann));
    };

    public static final Consumer<IDirectoryAssert<?>> dirEmptyAssert = dir -> dir.exists().hasEntries(0);

    public static final Consumer<IRegularFileAssert<?>> fileMcDonnellDouglasAssert =
            file -> file.hasSize(624_746).isImage().isContentEqualTo(fileMcdonnelDouglas);
    public static final Consumer<IRegularFileAssert<?>> fileSaintPetersburgAssert =
            file -> file.hasSize(1_074_836).isImage().isContentEqualTo(fileSaintPetersburg);
    public static final Consumer<IRegularFileAssert<?>> fileSigSauerAssert =
            file -> file.hasSize(431_478).isImage().isContentEqualTo(fileSigSauer);
    public static final Consumer<IRegularFileAssert<?>> fileEmptyAssert = file -> file.hasSize(0);
    // public static final Consumer<IRegularFileAssert<?>> zipFileOlegCherednikAssert =
    //         file -> file.hasSize(1_395_362);

    public static final Consumer<IDirectoryAssert<?>> rootAssert = dir -> {
        dir.hasOnlyDirectoriesRegularFiles(3, 5);

        dirBikesAssert.accept(dir.directory(dirNameBikes));
        dirCarsAssert.accept(dir.directory(dirNameCars));
        dirEmptyAssert.accept(dir.directory(dirNameEmpty));

        fileMcDonnellDouglasAssert.accept(dir.regularFile(fileNameMcdonnelDouglas));
        fileSaintPetersburgAssert.accept(dir.regularFile(fileNameSaintPetersburg));
        fileSigSauerAssert.accept(dir.regularFile(fileNameSigSauer));
        fileEmptyAssert.accept(dir.regularFile(fileNameEmpty));
        // zipFileOlegCherednikAssert.accept(dir.file(fileNameOlegCherednik));
    };

    public static void copyLarge(InputStream in, Path dst) {
        ZipUtils.copyLarge(in, PathUtils.newOutputStream(dst.toFile().toPath()));
    }

    public static String getMethodName() {
        boolean get = false;

        for (StackTraceElement element : Thread.currentThread().getStackTrace()) {
            String className = element.getClassName();

            if (get)
                return element.getMethodName();
            if (Zip4jvmSuite.class.getName().equals(className))
                get = true;
        }

        throw new Zip4jvmException("Cannot detect method name");
    }

}
