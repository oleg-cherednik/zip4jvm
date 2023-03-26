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
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import ru.olegcherednik.zip4jvm.exception.Zip4jvmException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

/**
 * @author Oleg Cherednik
 * @since 20.09.2019
 */
@SuppressWarnings({ "FieldNamingConvention", "StaticCollection" })
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TestData {

    public static final Path dirRoot = createTempDirectory("zip4jvm");
//    public static final Path dirRoot = Paths.get("/Users/o.cherednik/Documents/zip4jvm/foo");
//    public static final Path dirRoot = Paths.get("d:/zip4jvm/foo");

    public static final String dirNameSrcData = "data";

    public static final Path dirSrc = dirRoot.resolve("src");
    public static final Path dirSrcData = dirSrc.resolve(dirNameSrcData);
    public static final Path dirSrcSymlink = dirSrc.resolve("symlink");

    public static final String dirNameBikes = "bikes";
    public static final String dirNameCars = "cars";
    public static final String dirNameEmpty = "empty_dir";

    public static final String zipDirNameBikes = dirNameBikes + '/';
    public static final String zipDirNameCars = dirNameCars + '/';
    public static final String zipDirNameEmpty = dirNameEmpty + '/';

    public static final String fileNameDucati = "ducati-panigale-1199.jpg";
    public static final String fileNameHonda = "honda-cbr600rr.jpg";
    public static final String fileNameKawasaki = "kawasaki-ninja-300.jpg";
    public static final String fileNameSuzuki = "suzuki-gsxr750.jpg";

    public static final String fileNameBentley = "bentley-continental.jpg";
    public static final String fileNameFerrari = "ferrari-458-italia.jpg";
    public static final String fileNameWiesmann = "wiesmann-gt-mf5.jpg";

    public static final String fileNameEmpty = "empty_file.txt";
    public static final String fileNameMcdonnelDouglas = "mcdonnell-douglas-f15-eagle.jpg";
    public static final String fileNameOlegCherednik = "Oleg Cherednik.txt";
    public static final String fileNameSaintPetersburg = "saint-petersburg.jpg";
    public static final String fileNameSigSauer = "sig-sauer-pistol.jpg";

    public static final String symlinkRelFileNameDucati = getRelativeSymlinkName(fileNameDucati);
    public static final String symlinkRelFileNameHonda = getRelativeSymlinkName(fileNameHonda);
    public static final String symlinkRelDirNameData = getRelativeSymlinkName(dirNameSrcData);
    public static final String symlinkRelDirNameCars = getRelativeSymlinkName(dirNameCars);
    public static final String symlinkTrnFileNameHonda = getTransitiveSymlinkName(fileNameHonda);
    public static final String symlinkTrnDirNameData = getTransitiveSymlinkName(dirNameSrcData);

    public static final String symlinkAbsFileNameDucati = getAbsoluteSymlinkName(fileNameDucati);
    public static final String symlinkAbsFileNameHonda = getAbsoluteSymlinkName(fileNameHonda);
    public static final String symlinkAbsDirNameData = getAbsoluteSymlinkName(dirNameSrcData);

    public static final String zipSymlinkRelDirNameData = symlinkRelDirNameData + '/';
    public static final String zipSymlinkRelDirNameCars = symlinkRelDirNameCars + '/';
    public static final String zipSymlinkAbsDirNameData = symlinkAbsDirNameData + '/';
    public static final String zipSymlinkTrnDirNameData = symlinkTrnDirNameData + '/';

    public static final Path dirBikes = dirSrcData.resolve(dirNameBikes);
    public static final Path dirCars = dirSrcData.resolve(dirNameCars);
    public static final Path dirEmpty = dirSrcData.resolve(dirNameEmpty);

    public static final Path fileDucati = dirBikes.resolve(fileNameDucati);
    public static final Path fileHonda = dirBikes.resolve(fileNameHonda);
    public static final Path fileKawasaki = dirBikes.resolve(fileNameKawasaki);
    public static final Path fileSuzuki = dirBikes.resolve(fileNameSuzuki);

    public static final Path fileBentley = dirCars.resolve(fileNameBentley);
    public static final Path fileFerrari = dirCars.resolve(fileNameFerrari);
    public static final Path fileWiesmann = dirCars.resolve(fileNameWiesmann);

    public static final Path fileEmpty = dirSrcData.resolve(fileNameEmpty);
    public static final Path fileMcdonnelDouglas = dirSrcData.resolve(fileNameMcdonnelDouglas);
    public static final Path fileOlegCherednik = dirSrcData.resolve(fileNameOlegCherednik);
    public static final Path fileSaintPetersburg = dirSrcData.resolve(fileNameSaintPetersburg);
    public static final Path fileSigSauer = dirSrcData.resolve(fileNameSigSauer);

    public static final Path symlinkRelFileDucati = dirSrcSymlink.resolve(symlinkRelFileNameDucati);
    public static final Path symlinkRelFileHonda = dirSrcSymlink.resolve(symlinkRelFileNameHonda);
    public static final Path symlinkRelDirData = dirSrcSymlink.resolve(symlinkRelDirNameData);
    public static final Path symlinkRelDirCars = dirSrcSymlink.resolve(symlinkRelDirNameCars);
    public static final Path symlinkTrnFileHonda = dirSrcSymlink.resolve(symlinkTrnFileNameHonda);
    public static final Path getSymlinkTrnDirData = dirSrcSymlink.resolve(symlinkTrnDirNameData);

    public static final Path symlinkAbsFileDucati = dirSrcSymlink.resolve(symlinkAbsFileNameDucati);
    public static final Path symlinkAbsFileHonda = dirSrcSymlink.resolve(symlinkAbsFileNameHonda);
    public static final Path symlinkAbsDirData = dirSrcSymlink.resolve(symlinkAbsDirNameData);

    public static final List<Path> filesDirBikes = Arrays.asList(fileDucati, fileHonda, fileKawasaki, fileSuzuki);
    public static final List<Path> filesDirCars = Arrays.asList(fileBentley, fileFerrari, fileWiesmann);
    public static final List<Path> filesDirSrc = Arrays.asList(fileEmpty,
                                                               fileMcdonnelDouglas,
                                                               fileOlegCherednik,
                                                               fileSaintPetersburg,
                                                               fileSigSauer);

    public static final List<Path> contentDirSrc = Arrays.asList(dirBikes,
                                                                 dirCars,
                                                                 dirEmpty,
                                                                 fileEmpty,
                                                                 fileMcdonnelDouglas,
                                                                 fileOlegCherednik,
                                                                 fileSaintPetersburg,
                                                                 fileSigSauer);

    // store
    public static final Path zipStoreSolid = dirRoot.resolve("store/solid/off/src.zip");
    public static final Path zipStoreSolidPkware = dirRoot.resolve("store/solid/pkware/src.zip");
    public static final Path zipStoreSolidAes = dirRoot.resolve("store/solid/aes/src.zip");
    public static final Path zipStoreSplit = dirRoot.resolve("store/split/off/src.zip");
    public static final Path zipStoreSplitPkware = dirRoot.resolve("store/split/pkware/src.zip");
    public static final Path zipStoreSplitAes = dirRoot.resolve("store/split/aes/src.zip");

    // deflate
    public static final Path zipDeflateSolid = dirRoot.resolve("deflate/solid/off/src.zip");
    public static final Path zipDeflateSplit = dirRoot.resolve("deflate/split/off/src.zip");
    public static final Path zipDeflateSolidPkware = dirRoot.resolve("deflate/solid/pkware/src.zip");
    public static final Path zipDeflateSolidAes = dirRoot.resolve("deflate/solid/aes/src.zip");

    // winrar
    public static final Path winRarStoreSolidZip = Paths.get("src/test/resources/winrar/store_solid_off.zip").toAbsolutePath();
    public static final Path winRarStoreSolidPkwareZip = Paths.get("src/test/resources/winrar/store_solid_pkware.zip").toAbsolutePath();
    public static final Path winRarStoreSolidAesZip = Paths.get("src/test/resources/winrar/store_solid_aes.zip").toAbsolutePath();
    public static final Path winRarDeflateSolidZip = Paths.get("src/test/resources/winrar/deflate_solid_off.zip").toAbsolutePath();
    public static final Path winRarDeflateSolidPkwareZip = Paths.get("src/test/resources/winrar/deflate_solid_pkware.zip").toAbsolutePath();
    public static final Path winRarDeflateSolidAesZip = Paths.get("src/test/resources/winrar/deflate_solid_aes.zip").toAbsolutePath();

    // 7-Zip
    public static final Path sevenZipStoreSplitZip = Paths.get("src/test/resources/seven-zip/store_split_off/src.zip.001").toAbsolutePath();
    public static final Path sevenZipStoreSolidPkwareZip = Paths.get("src/test/resources/seven-zip/lzma_solid_aes.zip").toAbsolutePath();
    public static final Path sevenZipLzmaSolidZip = Paths.get("src/test/resources/seven-zip/lzma_solid_off.zip").toAbsolutePath();
    public static final Path sevenZipLzmaSolidAesZip = Paths.get("src/test/resources/seven-zip/lzma_solid_aes.zip").toAbsolutePath();
    public static final Path sevenZipZstdSolidZip = Paths.get("src/test/resources/seven-zip/zstd_solid_off.zip").toAbsolutePath();
    public static final Path sevenZipZstdSolidAesZip = Paths.get("src/test/resources/seven-zip/zstd_solid_aes.zip").toAbsolutePath();

    // secure-zip
    public static final Path secureZipStoreSolidAesZip = Paths.get("src/test/resources/secure-zip/store_solid_aes.zip").toAbsolutePath();
    public static final Path secureZipStoreSplitZip = Paths.get("src/test/resources/secure-zip/store_split_off/src.zip").toAbsolutePath();
    public static final Path secureZipLzmaSolidZip = Paths.get("src/test/resources/secure-zip/lzma_solid_off.zip").toAbsolutePath();
    public static final Path secureZipBzip2SolidZip = Paths.get("src/test/resources/secure-zip/bzip2_solid_off.zip").toAbsolutePath();
    public static final Path secureZipBzip2SolidPkwareZip = Paths.get("src/test/resources/secure-zip/bzip2_solid_pkware.zip").toAbsolutePath();
    public static final Path secureZipEnhancedDeflateSolidZip = Paths.get("src/test/resources/secure-zip/ed_solid_off.zip").toAbsolutePath();

    // strong
    public static final Path secureZipStoreSolidAes256StrongZip =
            Paths.get("src/test/resources/secure-zip/strong/store_solid_aes256_strong.zip").toAbsolutePath();
    public static final Path secureZipStoreSolidAes192StrongZip =
            Paths.get("src/test/resources/secure-zip/strong/store_solid_aes192_strong.zip").toAbsolutePath();
    public static final Path secureZipStoreSolidAes128StrongZip =
            Paths.get("src/test/resources/secure-zip/strong/store_solid_aes128_strong.zip").toAbsolutePath();
    public static final Path secureZipDeflateSolidAes256StrongZip =
            Paths.get("src/test/resources/secure-zip/strong/deflate_solid_aes256_strong.zip").toAbsolutePath();
    public static final Path secureZipBzip2SolidAes256StrongZip =
            Paths.get("src/test/resources/secure-zip/strong/bzip2_solid_aes256_strong.zip").toAbsolutePath();
    public static final Path secureZipDeflate64SolidAes256StrongZip =
            Paths.get("src/test/resources/secure-zip/strong/deflate64_solid_aes256_strong.zip").toAbsolutePath();
    public static final Path secureZipLzmaSolidAes256StrongZip =
            Paths.get("src/test/resources/secure-zip/strong/lzma_solid_aes256_strong.zip").toAbsolutePath();
    public static final Path secureZipBzip2SplitAes256StrongZip =
            Paths.get("src/test/resources/secure-zip/strong/bzip2_split_aes_strong/split_bzip2_aes_strong.zip").toAbsolutePath();

    // encrypted central directory
    public static final Path secureZipStoreSolidAes256StrongEcdZip =
            Paths.get("src/test/resources/secure-zip/strong/ecd/store_solid_aes256_strong_ecd.zip").toAbsolutePath();
    public static final Path secureZipDeflateSolidAes256StrongEcdZip =
            Paths.get("src/test/resources/secure-zip/strong/ecd/deflate_solid_aes256_strong_ecd.zip").toAbsolutePath();
    public static final Path secureZipDeflate64SolidAes256StrongEcdZip =
            Paths.get("src/test/resources/secure-zip/strong/ecd/deflate64_solid_aes256_strong_ecd.zip").toAbsolutePath();
    public static final Path secureZipBzip2SolidAes256StrongEcdZip =
            Paths.get("src/test/resources/secure-zip/strong/ecd/bzip2_solid_aes256_strong_ecd.zip").toAbsolutePath();
    public static final Path secureZipLzmaSolidAes256StrongEcdZip =
            Paths.get("src/test/resources/secure-zip/strong/ecd/lzma_solid_aes256_strong_ecd.zip").toAbsolutePath();
    public static final Path secureZipBzip2SplitAes256StrongEcdZip =
            Paths.get("src/test/resources/secure-zip/strong/ecd/bzip2_split_aes_strong_ecd/split_bzip2_aes_strong_ecd.zip").toAbsolutePath();

    // apk
    public static final Path appApk = Paths.get("src/test/resources/apk/app.apk").toAbsolutePath();

    private static String getAbsoluteSymlinkName(String fileName) {
        return getSymlinkName(fileName, "abs");
    }

    private static String getRelativeSymlinkName(String fileName) {
        return getSymlinkName(fileName, "rel");
    }

    private static String getTransitiveSymlinkName(String fileName) {
        return getSymlinkName(fileName, "trn");
    }

    private static String getSymlinkName(String fileName, String prefix) {
        String ext = FilenameUtils.getExtension(fileName);
        String name = FilenameUtils.getBaseName(fileName) + '-' + prefix + "-symlink";
        return StringUtils.isEmpty(ext) ? name : name + '.' + ext;
    }

    private static Path createTempDirectory(String prefix) {
        try {
            return Files.createTempDirectory(prefix);
        } catch(IOException e) {
            throw new Zip4jvmException(e);
        }
    }

}
