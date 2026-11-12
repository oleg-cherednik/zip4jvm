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
import ru.olegcherednik.zip4jvm.model.settings.UnzipSettings;
import ru.olegcherednik.zip4jvm.model.settings.ZipSettings;

import com.github.luben.zstd.Zstd;
import com.github.luben.zstd.ZstdCompressCtx;
import io.airlift.compress.zstd.OutByteArrayWithOffs;
import io.airlift.compress.zstd.ZstdCompressor;
import io.airlift.compress.zstd.ZstdInputStream;
import io.airlift.compress.zstd.dep.InByteArrayWithOffs;
import org.apache.commons.io.IOUtils;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.olegcherednik.zip4jvm.TestData.fileEmpty;
import static ru.olegcherednik.zip4jvm.TestData.fileNameBentley;
import static ru.olegcherednik.zip4jvm.TestData.fileNameEmpty;
import static ru.olegcherednik.zip4jvm.TestData.fileNameOlegCherednik;
import static ru.olegcherednik.zip4jvm.TestData.fileNameZipSrc;
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

    public void shouldCreateSingleZipWithHugeFilesWhenZstdCompressionNormalLevel() {
        Path file = Zip4jvmSuite.getResourcePath("/zstd/snow-mountains-5120x3413-26362.jpg");
        Path zip = getTestRoot().resolve(fileNameZipSrc);
        ZipIt.zip(zip).settings(ZipSettings.of(CompressionEnum.ZSTD)).add(file);

        assertThatZipFile(zip)
                .isSolid()
                .root().regularFile("snow-mountains-5120x3413-26362.jpg")
                .matches(regularFileAssert -> {
                    regularFileAssert.hasSize(9114746);
                    regularFileAssert.isImage();
                });
    }

    public void shouldCreateSingleZipWithLetterGroupsWhenZstdCompression() throws IOException {
        Path file = Zip4jvmSuite.getResourcePath("/zstd/test_rle_literals.bin");

        Path zip = getZip();
        ZipIt.zip(zip).settings(ZipSettings.of(CompressionEnum.ZSTD)).add(file);

        Path dstDir = getTestRoot().resolve("unzip");
        UnzipIt.zip(zip).dstDir(dstDir).extract();

        assertThatDirectory(dstDir)
                .hasRegularFiles(1)
                .regularFile("test_rle_literals.bin")
                .hasSize(192)
                .isContentEqualTo(file);
    }

    public void shouldDecompressOut1Zstd() throws IOException {
        Path dstDir = getTestRoot();
        Path zstd = Zip4jvmSuite.getResourcePath("/zstd/bentley-continental.zstd");
        ByteArrayOutputStream out = new ByteArrayOutputStream((int) Files.size(zstd) * 2);

        try (InputStream in = new ZstdInputStream(Files.newInputStream(zstd))) {
            IOUtils.copyLarge(in, out);
        }

        Files.write(dstDir.resolve(fileNameBentley), out.toByteArray());
        assertThatDirectory(dstDir).regularFile(fileNameBentley).matches(fileBentleyAssert);
    }

    // output buffer without extra space: the last sequences are close to its end, so safe (not long-at-a-time) copy is used
    public void shouldDecompressZstdWhenOutputBufferHasExactSize() throws IOException {
        Path dstDir = getTestRoot();
        Path zstd = Zip4jvmSuite.getResourcePath("/zstd/bentley-continental.zstd");
        ByteArrayOutputStream out = new ByteArrayOutputStream((int) Files.size(zstd) * 2);

        try (InputStream in = new ZstdInputStream(Files.newInputStream(zstd))) {
            IOUtils.copyLarge(in, out);
        }

        Files.write(dstDir.resolve(fileNameBentley), out.toByteArray());
        assertThatDirectory(dstDir).regularFile(fileNameBentley).matches(fileBentleyAssert);
    }

    // repetitive data compressed by the reference zstd: the last match runs up to the end of the data, so with an
    // exact size output buffer it ends close to the buffer end and the safe (byte by byte) tail of the match copy is used
    public void shouldDecompressZstdWhenLastMatchEndsAtOutputBufferEnd() throws IOException {
        StringBuilder buf = new StringBuilder();

        for (int i = 0; buf.length() < 10_000; i++)
            buf.append("zip4jvm zstd sequence ").append(i % 7).append(' ');

        byte[] expected = buf.toString().getBytes(StandardCharsets.UTF_8);
        byte[] input = Zstd.compress(expected);

        ByteArrayOutputStream out = new ByteArrayOutputStream(input.length * 2);

        try (InputStream in = new ZstdInputStream(new ByteArrayInputStream(input))) {
            IOUtils.copyLarge(in, out);
        }

        assertThat(out.toByteArray()).isEqualTo(expected);
    }

    // repetitive data compressed by this compressor and decompressed by the reference zstd: the last match runs up to
    // the end of the input, so the byte-at-a-time tail of the match length counting is used
    public void shouldCompressZstdWhenLastMatchEndsAtInputEnd() throws IOException {
        StringBuilder buf = new StringBuilder();

        for (int i = 0; buf.length() < 10_000; i++)
            buf.append("zip4jvm zstd compressor ").append(i % 5).append(' ');

        byte[] expected = buf.toString().getBytes(StandardCharsets.UTF_8);

        ZstdCompressor compressor = new ZstdCompressor();
        byte[] output = new byte[compressor.maxCompressedLength(expected.length)];
        OutByteArrayWithOffs out = new OutByteArrayWithOffs(output);
        compressor.compress(new InByteArrayWithOffs(expected), out);
        assertThat(Zstd.decompress(Arrays.copyOf(output, out.getOffs()), expected.length)).isEqualTo(expected);
    }

    // all-identical input leaves a short run of equal literals; EncodeLiterals emits RLE_LITERALS_BLOCK for them
    public void shouldCompressZstdWhenRleLiteralsBlock() throws IOException {
        byte[] expected = new byte[10_000];
        Arrays.fill(expected, (byte) 'a');

        ZstdCompressor compressor = new ZstdCompressor();
        byte[] output = new byte[compressor.maxCompressedLength(expected.length)];
        OutByteArrayWithOffs out = new OutByteArrayWithOffs(output);
        compressor.compress(new InByteArrayWithOffs(expected), out);
        assertThat(Zstd.decompress(Arrays.copyOf(output, out.getOffs()), expected.length)).isEqualTo(expected);
    }

    // small data compressed by this compressor and decompressed by the reference zstd: a block with less than 256
    // literals encodes them with Huffman as a single stream
    public void shouldCompressZstdWhenLessThan256Literals() throws IOException {
        for (int size : new int[] { 50, 100, 200, 255, 256, 300, 1_000, 5_000 }) {
            for (int distinct : new int[] { 4, 90 }) {
                byte[] expected = new byte[size];
                Random random = new Random(distinct * 31L + size);

                for (int i = 0; i < size; i++)
                    expected[i] = (byte) (' ' + random.nextInt(distinct) % (1 + random.nextInt(distinct)));

                ZstdCompressor compressor = new ZstdCompressor();
                byte[] output = new byte[compressor.maxCompressedLength(expected.length)];
                OutByteArrayWithOffs out = new OutByteArrayWithOffs(output);
                compressor.compress(new InByteArrayWithOffs(expected), out);

                assertThat(Zstd.decompress(Arrays.copyOf(output, out.getOffs()), expected.length))
                        .as("size=" + size + " distinct=" + distinct)
                        .isEqualTo(expected);
            }
        }
    }

    // frame with Content_Checksum compressed by the reference zstd: the decompressor verifies it with XxHash64;
    // 10_015 = 312 * 32 (body) + 3 * 8 + 4 + 3 (tail), so every step of the hash is used
    public void shouldDecompressZstdWhenFrameHasContentChecksum() throws IOException {
        byte[] expected = new byte[10_015];

        for (int i = 0; i < expected.length; i++)
            expected[i] = (byte) ("zip4jvm zstd checksum ".charAt(i % 22) + i / 1000);

        byte[] input;

        try (ZstdCompressCtx ctx = new ZstdCompressCtx()) {
            ctx.setChecksum(true);
            input = ctx.compress(expected);
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream(input.length * 2);

        try (InputStream in = new ZstdInputStream(new ByteArrayInputStream(input))) {
            IOUtils.copyLarge(in, out);
        }

        assertThat(out.toByteArray()).isEqualTo(expected);
    }

    // several frames one after another (with and without Content_Checksum, including empty ones) compressed by the
    // reference zstd: the decompressor starts a new frame after the last block of the previous one and skips empty blocks
    public void shouldDecompressZstdWhenMultipleFrames() throws IOException {
        byte[] data1 = new byte[300_000];
        Random random = new Random(1);

        for (int i = 0; i < data1.length; i++)
            data1[i] = (byte) (' ' + random.nextInt(90) % (1 + random.nextInt(90)));

        byte[] data2 = "zip4jvm zstd frame zip4jvm zstd frame".getBytes(StandardCharsets.UTF_8);
        byte[] empty = new byte[0];

        for (boolean checksum : new boolean[] { false, true }) {
            ByteArrayOutputStream input = new ByteArrayOutputStream();
            input.write(compressByReferenceZstd(data1, 3, checksum));
            input.write(compressByReferenceZstd(empty, 3, checksum));
            input.write(compressByReferenceZstd(data2, 3, checksum));
            input.write(compressByReferenceZstd(empty, 3, checksum));

            ByteArrayOutputStream expected = new ByteArrayOutputStream();
            expected.write(data1);
            expected.write(data2);

            assertThat(decompress(input.toByteArray())).as("checksum=" + checksum)
                                                       .isEqualTo(expected.toByteArray());
        }
    }

    // data with only a few distinct bytes compressed by the reference zstd: Huffman weights are compressed by FSE into
    // a bitstream shorter than 8 bytes
    public void shouldDecompressZstdWhenFewDistinctBytes() throws IOException {
        for (int distinct = 2; distinct <= 8; distinct++) {
            for (int size : new int[] { 1_000, 300_000 }) {
                byte[] expected = new byte[size];
                Random random = new Random(distinct * 31L + size);

                for (int i = 0; i < size; i++)
                    expected[i] = (byte) ('a' + random.nextInt(distinct));

                for (int level : new int[] { 1, 3, 19 }) {
                    byte[] input = compressByReferenceZstd(expected, level, true);
                    assertThat(decompress(input)).as("distinct=" + distinct + " size=" + size + " level=" + level)
                                                 .isEqualTo(expected);
                }
            }
        }
    }

    private static byte[] compressByReferenceZstd(byte[] data, int level, boolean checksum) {
        try (ZstdCompressCtx ctx = new ZstdCompressCtx()) {
            ctx.setLevel(level);
            ctx.setChecksum(checksum);
            return ctx.compress(data);
        }
    }

    private static byte[] decompress(byte[] input) throws IOException {
        try (InputStream in = new ZstdInputStream(new ByteArrayInputStream(input))) {
            return IOUtils.toByteArray(in);
        }
    }

    public void shouldDecompressOut2Zstd() throws IOException {
        Path dstDir = getTestRoot();
        Path zstd = Zip4jvmSuite.getResourcePath("/zstd/oleg-cherednik.zstd");
        ByteArrayOutputStream out = new ByteArrayOutputStream((int) Files.size(zstd) * 2);

        try (InputStream in = new ZstdInputStream(Files.newInputStream(zstd))) {
            IOUtils.copyLarge(in, out);
        }

        Files.write(dstDir.resolve(fileNameOlegCherednik), out.toByteArray());
    }

    public void shouldUnzipSingleZipWhenZstdCompression() {
        Path dstDir = getTestRoot();
        Path zip = Zip4jvmSuite.getResourcePath("/zip/zstd.zip");
        UnzipIt.zip(zip).dstDir(dstDir).extract();
        assertThatDirectory(dstDir)
                .hasRegularFiles(1)
                .regularFile(fileNameBentley).matches(fileBentleyAssert);
    }

    public void shouldUnzipSingleBigZipWhenZstdCompression() {
        Path dstDir = getTestRoot();
        Path zip = Zip4jvmSuite.getResourcePath("/zip/zstd_big.zip");
        UnzipIt.zip(zip).settings(UnzipSettings.builder().asyncOff().build()).dstDir(dstDir).extract();
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
