/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
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

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.olegcherednik.zip4jvm.assertj.IDirectoryAssert;
import ru.olegcherednik.zip4jvm.assertj.IRegularFileAssert;
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

    public static final Consumer<IDirectoryAssert<?>> rootAssert = dir -> {
        dir.exists().hasEntries(8).hasDirectories(3).hasRegularFiles(5);

        TestDataAssert.dirBikesAssert.accept(dir.directory(zipDirNameBikes));
        TestDataAssert.dirCarsAssert.accept(dir.directory(zipDirNameCars));
        TestDataAssert.dirEmptyAssert.accept(dir.directory(zipDirNameEmpty));

        TestDataAssert.fileMcdonnelDouglasAssert.accept(dir.regularFile(fileNameMcdonnelDouglas));
        TestDataAssert.fileSaintPetersburgAssert.accept(dir.regularFile(fileNameSaintPetersburg));
        TestDataAssert.fileSigSauerAssert.accept(dir.regularFile(fileNameSigSauer));
        TestDataAssert.fileEmptyAssert.accept(dir.regularFile(fileNameEmpty));
//        TestDataAssert.zipFileOlegCherednikAssert.accept(dir.file(fileNameOlegCherednik));
    };

    public static final Consumer<IRegularFileAssert<?>> fileMcdonnelDouglasAssert = file -> file.exists().isImage().hasSize(624_746);
    public static final Consumer<IRegularFileAssert<?>> fileSaintPetersburgAssert = file -> file.exists().isImage().hasSize(1_074_836);
    public static final Consumer<IRegularFileAssert<?>> fileSigSauerAssert = file -> file.exists().isImage().hasSize(431_478);
    public static final Consumer<IRegularFileAssert<?>> fileEmptyAssert = file -> file.exists().hasSize(0);
    public static final Consumer<IRegularFileAssert<?>> zipFileOlegCherednikAssert = file -> file.exists().hasSize(1_395_362);

    public static final Consumer<IDirectoryAssert<?>> dirBikesAssert = dir -> {
        dir.exists().hasEntries(4).hasRegularFiles(4);
        TestDataAssert.fileDucatiAssert.accept(dir.regularFile(fileNameDucati));
        TestDataAssert.fileHondaAssert.accept(dir.regularFile(fileNameHonda));
        TestDataAssert.fileKawasakiAssert.accept(dir.regularFile(fileNameKawasaki));
        TestDataAssert.fileSuzukiAssert.accept(dir.regularFile(fileNameSuzuki));
    };

    public static final Consumer<IRegularFileAssert<?>> fileDucatiAssert = file -> file.exists().isImage().hasSize(293_823);
    public static final Consumer<IRegularFileAssert<?>> fileHondaAssert = file -> file.exists().isImage().hasSize(154_591);
    public static final Consumer<IRegularFileAssert<?>> fileKawasakiAssert = file -> file.exists().isImage().hasSize(167_026);
    public static final Consumer<IRegularFileAssert<?>> fileSuzukiAssert = file -> file.exists().isImage().hasSize(287_349);

    public static final Consumer<IDirectoryAssert<?>> dirCarsAssert = dir -> {
        dir.exists().hasEntries(3).hasRegularFiles(3);
        TestDataAssert.fileBentleyAssert.accept(dir.regularFile(fileNameBentley));
        TestDataAssert.fileFerrariAssert.accept(dir.regularFile(fileNameFerrari));
        TestDataAssert.fileWiesmannAssert.accept(dir.regularFile(fileNameWiesmann));
    };

    public static final Consumer<IRegularFileAssert<?>> fileBentleyAssert = file -> file.exists().isImage().hasSize(1_395_362);
    public static final Consumer<IRegularFileAssert<?>> fileFerrariAssert = file -> file.exists().isImage().hasSize(320_894);
    public static final Consumer<IRegularFileAssert<?>> fileWiesmannAssert = file -> file.exists().isImage().hasSize(729_633);

    public static final Consumer<IDirectoryAssert<?>> dirEmptyAssert = dir -> dir.exists().hasEntries(0);

    public static void copyLarge(InputStream in, Path dst) throws IOException {
        ZipUtils.copyLarge(in, new FileOutputStream(dst.toFile()));
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
