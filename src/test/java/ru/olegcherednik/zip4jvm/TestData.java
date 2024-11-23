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

import ru.olegcherednik.zip4jvm.exception.Zip4jvmException;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

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
    // public static final Path dirRoot = Paths.get("/Users/o.cherednik/Documents/zip4jvm/foo");
    // public static final Path dirRoot = Paths.get("d:/zip4jvm/foo");

    public static final String dirNameSrcData = "data";

    public static final Path dirSrc = dirRoot.resolve("src");
    public static final Path dirSrcData = dirSrc.resolve(dirNameSrcData);
    public static final Path dirSrcSymlink = dirSrc.resolve("symlink");

    public static final String dirNameBikes = "bikes";
    public static final String dirNameCars = "cars";
    public static final String dirNameEmpty = "empty_dir";

    public static final String fileNameZipSrc = "src.zip";
    public static final String fileNameDataSrc = "src.data";
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
    private static final Path dirWinRar = Paths.get("src/test/resources/winrar").toAbsolutePath();
    public static final Path winRarStoreSolidZip = dirWinRar.resolve("store_solid_off.zip");
    public static final Path winRarStoreSolidPkwareZip = dirWinRar.resolve("store_solid_pkware.zip");
    public static final Path winRarStoreSolidAesZip = dirWinRar.resolve("store_solid_aes.zip");
    public static final Path winRarDeflateSolidZip = dirWinRar.resolve("deflate_solid_off.zip");
    public static final Path winRarDeflateSolidPkwareZip = dirWinRar.resolve("deflate_solid_pkware.zip");
    public static final Path winRarDeflateSolidAesZip = dirWinRar.resolve("deflate_solid_aes.zip");

    // 7-Zip
    private static final Path dirSevenZip = Paths.get("src/test/resources/seven-zip").toAbsolutePath();
    public static final Path sevenZipStoreSplitZip = dirSevenZip.resolve("store_split_off/src.zip.001");
    public static final Path sevenZipStoreSolidPkwareZip = dirSevenZip.resolve("lzma_solid_aes.zip");
    public static final Path sevenZipLzmaSolidZip = dirSevenZip.resolve("lzma_solid_off.zip");
    public static final Path sevenZipLzmaSolidAesZip = dirSevenZip.resolve("lzma_solid_aes.zip");
    public static final Path sevenZipZstdSolidZip = dirSevenZip.resolve("zstd_solid_off.zip");
    public static final Path sevenZipZstdSolidAesZip = dirSevenZip.resolve("zstd_solid_aes.zip");

    // secure-zip
    private static final Path dirSecureZip = Paths.get("src/test/resources/secure-zip").toAbsolutePath();
    public static final Path secureZipStoreSolidAesZip = dirSecureZip.resolve("store_solid_aes.zip");
    public static final Path secureZipStoreSplitZip = dirSecureZip.resolve("store_split_off/src.zip");
    public static final Path secureZipLzmaSolidZip = dirSecureZip.resolve("lzma_solid_off.zip");
    public static final Path secureZipBzip2SolidZip = dirSecureZip.resolve("bzip2_solid_off.zip");
    public static final Path secureZipBzip2SolidPkwareZip = dirSecureZip.resolve("bzip2_solid_pkware.zip");
    public static final Path secureZipEnhancedDeflateSolidZip = dirSecureZip.resolve("ed_solid_off.zip");

    // strong
    private static final Path dirSecureZipStrong = dirSecureZip.resolve("strong");
    public static final Path secureZipStoreSolidAes256StrongZip =
            dirSecureZipStrong.resolve("store_solid_aes256_strong.zip");
    public static final Path secureZipStoreSolidAes192StrongZip =
            dirSecureZipStrong.resolve("store_solid_aes192_strong.zip");
    public static final Path secureZipStoreSolidAes128StrongZip =
            dirSecureZipStrong.resolve("store_solid_aes128_strong.zip");
    public static final Path secureZipDeflateSolidAes256StrongZip =
            dirSecureZipStrong.resolve("deflate_solid_aes256_strong.zip");
    public static final Path secureZipBzip2SolidAes256StrongZip =
            dirSecureZipStrong.resolve("bzip2_solid_aes256_strong.zip");
    public static final Path secureZipDeflate64SolidAes256StrongZip =
            dirSecureZipStrong.resolve("deflate64_solid_aes256_strong.zip");
    public static final Path secureZipLzmaSolidAes256StrongZip =
            dirSecureZipStrong.resolve("lzma_solid_aes256_strong.zip");
    public static final Path secureZipBzip2SplitAes256StrongZip =
            dirSecureZipStrong.resolve("bzip2_split_aes_strong/split_bzip2_aes_strong.zip");

    // encrypted central directory
    private static final Path dirSecureZipEcd = dirSecureZipStrong.resolve("ecd");
    public static final Path secureZipStoreSolidAes256StrongEcdZip =
            dirSecureZipEcd.resolve("store_solid_aes256_strong_ecd.zip");
    public static final Path secureZipDeflateSolidAes256StrongEcdZip =
            dirSecureZipEcd.resolve("deflate_solid_aes256_strong_ecd.zip");
    public static final Path secureZipDeflate64SolidAes256StrongEcdZip =
            dirSecureZipEcd.resolve("deflate64_solid_aes256_strong_ecd.zip");
    public static final Path secureZipBzip2SolidAes256StrongEcdZip =
            dirSecureZipEcd.resolve("bzip2_solid_aes256_strong_ecd.zip");
    public static final Path secureZipLzmaSolidAes256StrongEcdZip =
            dirSecureZipEcd.resolve("lzma_solid_aes256_strong_ecd.zip");
    public static final Path secureZipBzip2SplitAes256StrongEcdZip =
            dirSecureZipEcd.resolve("bzip2_split_aes_strong_ecd/split_bzip2_aes_strong_ecd.zip");

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


    @SuppressWarnings({ "unused", "PMD.UnusedPrivateMethod" })
    private static Path createTempDirectory(String prefix) {
        try {
            return Files.createTempDirectory(prefix);
        } catch (IOException e) {
            throw new Zip4jvmException(e);
        }
    }

}
