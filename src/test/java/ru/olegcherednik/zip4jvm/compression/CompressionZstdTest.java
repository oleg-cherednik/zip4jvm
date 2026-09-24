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
package ru.olegcherednik.zip4jvm.compression;

import ru.olegcherednik.zip4jvm.BaseTest;
import ru.olegcherednik.zip4jvm.UnzipIt;
import ru.olegcherednik.zip4jvm.Zip4jvmSuite;
import ru.olegcherednik.zip4jvm.ZipInfo;
import ru.olegcherednik.zip4jvm.ZipIt;
import ru.olegcherednik.zip4jvm.model.CentralDirectory;
import ru.olegcherednik.zip4jvm.model.Compression;
import ru.olegcherednik.zip4jvm.model.settings.CompressionEnum;
import ru.olegcherednik.zip4jvm.model.settings.ZipSettings;

import io.airlift.compress.zstd.ByteArrayWithOffs;
import io.airlift.compress.zstd.ZstdDecompressor;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.olegcherednik.zip4jvm.TestData.fileEmpty;
import static ru.olegcherednik.zip4jvm.TestData.fileNameBentley;
import static ru.olegcherednik.zip4jvm.TestData.fileNameEmpty;
import static ru.olegcherednik.zip4jvm.TestData.fileNameOlegCherednik;
import static ru.olegcherednik.zip4jvm.TestData.fileNameZipSrc;
import static ru.olegcherednik.zip4jvm.TestData.fileOlegCherednik;
import static ru.olegcherednik.zip4jvm.TestData.filesDirBikes;
import static ru.olegcherednik.zip4jvm.TestDataAssert.dirBikesAssert;
import static ru.olegcherednik.zip4jvm.TestDataAssert.fileBentleyAssert;
import static ru.olegcherednik.zip4jvm.TestDataAssert.rootAssert;
import static ru.olegcherednik.zip4jvm.assertj.Zip4jvmAssertions.assertThatDirectory;
import static ru.olegcherednik.zip4jvm.assertj.Zip4jvmAssertions.assertThatZipFile;

/**
 * @author Oleg Cherednik
 * @since 07.11.2021
 */
@Test
public class CompressionZstdTest extends BaseTest {

    public void shouldCreateSingleZipWithFilesWhenZstdCompressionNormalLevel() {
        Path zip = getTestRoot().resolve(fileNameZipSrc);
        ZipIt.zip(zip).settings(ZipSettings.of(CompressionEnum.ZSTD)).add(filesDirBikes);

        assertThatZipFile(zip)
                .isSolid()
                .root().matches(dirBikesAssert);
    }

    public void shouldDecompressOut1Zstd() throws IOException {
        Path dstDir = getTestRoot();
        Path zstd = Zip4jvmSuite.getResourcePath("/zstd/bentley-continental.zstd");

        byte[] input = Files.readAllBytes(zstd);
        byte[] output = new byte[input.length * 3];
        int length = new ZstdDecompressor().decompress(new ByteArrayWithOffs(input), new ByteArrayWithOffs(output));
        Files.write(dstDir.resolve(fileNameBentley), Arrays.copyOf(output, length));

        assertThatDirectory(dstDir).regularFile(fileNameBentley).matches(fileBentleyAssert);
    }

    public void shouldDecompressOut2Zstd() throws IOException {
        Path dstDir = getTestRoot();
        Path zstd = Zip4jvmSuite.getResourcePath("/zstd/oleg-cherednik.zstd");

        byte[] input = Files.readAllBytes(zstd);
        byte[] output = new byte[input.length * 3];
        int length = new ZstdDecompressor().decompress(new ByteArrayWithOffs(input), new ByteArrayWithOffs(output));
        Files.write(dstDir.resolve(fileNameOlegCherednik), Arrays.copyOf(output, length));
    }

//    public void shouldUnzipSingleZipWhenZstdCompression() {
//        Path dstDir = getTestRoot();
//        Path zip = Zip4jvmSuite.getResourcePath("/zip/zstd.zip");
//        UnzipIt.zip(zip).dstDir(dstDir).extract();
//        assertThatDirectory(dstDir)
//                .hasRegularFiles(1)
//                .regularFile(fileNameBentley).matches(fileBentleyAssert);
//    }

    public void shouldUnzipSingleBigZipWhenZstdCompression() {
        Path dstDir = getTestRoot();
        Path zip = Zip4jvmSuite.getResourcePath("/zip/zstd_big.zip");
        UnzipIt.zip(zip).dstDir(dstDir).extract();
        assertThatDirectory(dstDir)
                .hasDirectories(2)
                .withDirectory("data1", rootAssert)
                .withDirectory("data2", rootAssert);
    }

    public void shouldUseCompressStoreWhenFileEmpty() {
        Path zip = getTestRoot().resolve(fileNameZipSrc);
        ZipIt.zip(zip).settings(ZipSettings.of(CompressionEnum.ZSTD)).add(fileEmpty);
        CentralDirectory.FileHeader fileHeader = ZipInfo.zip(zip).getFileHeader(fileNameEmpty);
        assertThat(fileHeader.getCompression()).isSameAs(Compression.STORE);
    }

}
