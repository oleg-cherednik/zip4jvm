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
package ru.olegcherednik.zip4jvm.io.out;

import ru.olegcherednik.zip4jvm.Zip4jvmSuite;
import ru.olegcherednik.zip4jvm.io.out.file.SplitZipDataOutput;
import ru.olegcherednik.zip4jvm.model.Charsets;
import ru.olegcherednik.zip4jvm.model.ZipModel;
import ru.olegcherednik.zip4jvm.model.src.SrcZip;

import org.apache.commons.io.FileUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static ru.olegcherednik.zip4jvm.TestData.fileNameDataSrc;

/**
 * @author Oleg Cherednik
 * @since 29.09.2019
 */
@Test
public class SplitZipDataOutputTest {

    private static final Path ROOT_DIR = Zip4jvmSuite.generateSubDirNameWithTime(SplitZipDataOutputTest.class);

    @BeforeClass
    public static void createDir() throws IOException {
        Files.createDirectories(ROOT_DIR);
    }

    @AfterClass(enabled = Zip4jvmSuite.clear)
    public static void removeDir() throws IOException {
        Zip4jvmSuite.removeDir(ROOT_DIR);
    }

    public void shouldWriteStreamWhenUsingDataOutput() throws IOException {
        Path zip = Zip4jvmSuite.subDirNameAsMethodName(ROOT_DIR).resolve(fileNameDataSrc);
        ZipModel zipModel = new ZipModel(SrcZip.of(zip));
        zipModel.setSplitSize(10);

        try (SplitZipDataOutput out = new SplitZipDataOutput(zipModel)) {
            assertThat(out.getDiskOffs()).isEqualTo(4);

            out.writeWord(0x0201);
            assertThat(out.getDiskOffs()).isEqualTo(6);

            out.writeDword(0x06050403);
            assertThat(out.getDiskOffs()).isEqualTo(10);

            out.writeQword(0x0E0D0C0B0A090807L);
            assertThat(out.getDiskOffs()).isEqualTo(8);
            assertThat(out.toString()).isEqualTo("offs: 8 (0x8); disk: 1");

            out.writeBytes("oleg".getBytes(Charsets.UTF_8));
            assertThat(out.getDiskOffs()).isEqualTo(2);

            out.writeBytes((byte) 0x11);
            assertThat(out.getDiskOffs()).isEqualTo(3);

            out.writeBytes(new byte[] { 0x12, 0x13, 0x14 });
            assertThat(out.getDiskOffs()).isEqualTo(6);
        }

        zipModel.setTotalDisks(5);

        byte[] buf = FileUtils.readFileToByteArray(zipModel.getDisk(0).toFile());
        assertThat(Arrays.copyOfRange(buf, 0, 4)).isEqualTo(new byte[] { 0x50, 0x4B, 0x7, 0x8 });
        assertThat(Arrays.copyOfRange(buf, 4, 6)).isEqualTo(new byte[] { 0x1, 0x2 });
        assertThat(Arrays.copyOfRange(buf, 6, 10)).isEqualTo(new byte[] { 0x3, 0x4, 0x5, 0x6 });

        buf = FileUtils.readFileToByteArray(zipModel.getDisk(1).toFile());
        assertThat(Arrays.copyOfRange(buf, 0, 8)).isEqualTo(new byte[] { 0x7, 0x8, 0x9, 0xA, 0xB, 0xC, 0xD, 0xE });
        assertThat(new String(Arrays.copyOfRange(buf, 8, 10), Charsets.UTF_8)).isEqualTo("ol");

        buf = FileUtils.readFileToByteArray(zipModel.getDisk(2).toFile());
        assertThat(new String(Arrays.copyOfRange(buf, 0, 2), Charsets.UTF_8)).isEqualTo("eg");
        assertThat(Arrays.copyOfRange(buf, 2, 3)).isEqualTo(new byte[] { 0x11 });
        assertThat(Arrays.copyOfRange(buf, 3, 6)).isEqualTo(new byte[] { 0x12, 0x13, 0x14 });
    }

    public void shouldMoveToNextDiskWhenNotEnoughSpaceToWriteSignature() throws IOException {
        Path zip = Zip4jvmSuite.subDirNameAsMethodName(ROOT_DIR).resolve(fileNameDataSrc);
        ZipModel zipModel = new ZipModel(SrcZip.of(zip));
        zipModel.setSplitSize(10);

        try (SplitZipDataOutput out = new SplitZipDataOutput(zipModel)) {
            assertThat(out.getDiskOffs()).isEqualTo(4);

            out.writeDwordSignature(0x01020304);
            assertThat(out.getDiskOffs()).isEqualTo(8);

            out.writeDwordSignature(0x05060708);
            assertThat(out.getDiskOffs()).isEqualTo(4);
        }

        zipModel.setTotalDisks(5);

        byte[] buf = FileUtils.readFileToByteArray(zipModel.getDisk(0).toFile());
        assertThat(Arrays.copyOfRange(buf, 0, 4)).isEqualTo(new byte[] { 0x50, 0x4B, 0x7, 0x8 });
        assertThat(Arrays.copyOfRange(buf, 4, 8)).isEqualTo(new byte[] { 0x4, 0x3, 0x2, 0x1 });
        assertThat(buf).hasSize(8);

        buf = FileUtils.readFileToByteArray(zipModel.getDisk(1).toFile());
        assertThat(Arrays.copyOfRange(buf, 0, 4)).isEqualTo(new byte[] { 0x8, 0x7, 0x6, 0x5 });
    }

    public void shouldThrowExceptionWhenSplitFileExists() throws IOException {
        Path zip = Zip4jvmSuite.subDirNameAsMethodName(ROOT_DIR).resolve(fileNameDataSrc);
        ZipModel zipModel = new ZipModel(SrcZip.of(zip));
        zipModel.setSplitSize(10);

        zipModel.setTotalDisks(5);
        FileUtils.writeByteArrayToFile(zipModel.getDisk(0).toFile(), new byte[] { 0x1, 0x2 }, true);
        zipModel.setTotalDisks(0);

        assertThatThrownBy(() -> {
            try (SplitZipDataOutput out = new SplitZipDataOutput(zipModel)) {
                assertThat(out.getDiskOffs()).isEqualTo(4);

                out.writeDwordSignature(0x01020304);
                assertThat(out.getDiskOffs()).isEqualTo(8);

                out.writeDwordSignature(0x05060708);
                out.writeDwordSignature(0x05060708);
            }
        }).isExactlyInstanceOf(IOException.class);
    }

}
