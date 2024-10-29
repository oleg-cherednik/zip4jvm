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
import ru.olegcherednik.zip4jvm.exception.Zip4jvmException;
import ru.olegcherednik.zip4jvm.io.out.data.SolidZipDataOutput;
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
@SuppressWarnings("FieldNamingConvention")
public class SolidZipDataOutputTest {

    private static final Path rootDir = Zip4jvmSuite.generateSubDirNameWithTime(SolidZipDataOutputTest.class);

    @BeforeClass
    public static void createDir() throws IOException {
        Files.createDirectories(rootDir);
    }

    @AfterClass(enabled = Zip4jvmSuite.clear)
    public static void removeDir() throws IOException {
        Zip4jvmSuite.removeDir(rootDir);
    }

    public void shouldWriteStreamWhenUsingDataOutput() throws IOException {
        Path zip = Zip4jvmSuite.subDirNameAsMethodName(rootDir).resolve(fileNameDataSrc);
        ZipModel zipModel = new ZipModel(SrcZip.of(zip));

        try (SolidZipDataOutput out = new SolidZipDataOutput(zipModel)) {
            assertThat(out.getRelativeOffs()).isEqualTo(0);

            out.writeWord(0x0201);
            assertThat(out.getRelativeOffs()).isEqualTo(2);

            out.writeDword(0x06050403);
            assertThat(out.getRelativeOffs()).isEqualTo(6);

            out.writeQword(0x0E0D0C0B0A090807L);
            assertThat(out.getRelativeOffs()).isEqualTo(14);
            assertThat(out.toString()).isEqualTo("offs: 14 (0xe)");

            out.writeBytes("oleg".getBytes(Charsets.UTF_8));
            assertThat(out.getRelativeOffs()).isEqualTo(18);

            out.writeBytes((byte) 0x11);
            assertThat(out.getRelativeOffs()).isEqualTo(19);

            out.writeBytes(new byte[] { 0x12, 0x13, 0x14 });
            assertThat(out.getRelativeOffs()).isEqualTo(22);
        }

        byte[] buf = FileUtils.readFileToByteArray(zip.toFile());

        assertThat(Arrays.copyOfRange(buf, 0, 2)).isEqualTo(new byte[] { 0x1, 0x2 });
        assertThat(Arrays.copyOfRange(buf, 2, 6)).isEqualTo(new byte[] { 0x3, 0x4, 0x5, 0x6 });
        assertThat(Arrays.copyOfRange(buf, 6, 14)).isEqualTo(new byte[] { 0x7, 0x8, 0x9, 0xA, 0xB, 0xC, 0xD, 0xE });
        assertThat(new String(Arrays.copyOfRange(buf, 14, 18), Charsets.UTF_8)).isEqualTo("oleg");
        assertThat(Arrays.copyOfRange(buf, 18, 19)).isEqualTo(new byte[] { 0x11 });
        assertThat(Arrays.copyOfRange(buf, 19, 22)).isEqualTo(new byte[] { 0x12, 0x13, 0x14 });
    }

    public void shouldThrowExceptionWhenGetUnknownMark() throws IOException {
        Path zip = Zip4jvmSuite.subDirNameAsMethodName(rootDir).resolve(fileNameDataSrc);
        ZipModel zipModel = new ZipModel(SrcZip.of(zip));

        assertThatThrownBy(() -> {
            try (SolidZipDataOutput out = new SolidZipDataOutput(zipModel)) {
                out.writeWord(0x0201);
                out.writeDword(0x06050403);

                out.mark("mark");

                out.writeQword(0x0E0D0C0B0A090807L);
                out.getMark("<unknwon>");
            }
        }).isExactlyInstanceOf(Zip4jvmException.class);
    }
}
