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
package ru.olegcherednik.zip4jvm.io.in;

import ru.olegcherednik.zip4jvm.Zip4jvmSuite;
import ru.olegcherednik.zip4jvm.io.in.file.random.SplitRandomAccessDataInput;
import ru.olegcherednik.zip4jvm.model.Charsets;
import ru.olegcherednik.zip4jvm.model.src.SrcZip;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static ru.olegcherednik.zip4jvm.TestData.fileNameDataSrc;

/**
 * @author Oleg Cherednik
 * @since 28.09.2019
 */
@Test
@SuppressWarnings("FieldNamingConvention")
public class ZipInputStreamTest {

    private static final Path rootDir = Zip4jvmSuite.generateSubDirNameWithTime(ZipInputStreamTest.class);

    @BeforeClass
    public static void createDir() throws IOException {
        Files.createDirectories(rootDir);
    }

    @AfterClass(enabled = Zip4jvmSuite.clear)
    public static void removeDir() throws IOException {
        Zip4jvmSuite.removeDir(rootDir);
    }

    public void shouldReadStreamWhenUsingDataInput() throws IOException {
        Path file = Zip4jvmSuite.subDirNameAsMethodName(rootDir).resolve(fileNameDataSrc);
        FileUtils.writeByteArrayToFile(file.toFile(), new byte[] { 0x1, 0x2 }, true);
        FileUtils.writeByteArrayToFile(file.toFile(), new byte[] { 0x3, 0x4, 0x5, 0x6 }, true);
        FileUtils.writeByteArrayToFile(file.toFile(), new byte[] { 0x7, 0x8, 0x9, 0xA, 0xB, 0xC, 0xD, 0xE }, true);
        FileUtils.writeByteArrayToFile(file.toFile(), new byte[] { 0xF, 0x10 }, true);
        FileUtils.writeStringToFile(file.toFile(), "oleg", Charsets.UTF_8, true);
        FileUtils.writeByteArrayToFile(file.toFile(), new byte[] { 0x11 }, true);
        FileUtils.writeByteArrayToFile(file.toFile(), new byte[] { 0x12, 0x13, 0x14 }, true);

        try (SplitRandomAccessDataInput in = new SplitRandomAccessDataInput(SrcZip.of(file))) {
            assertThat(in.getAbsOffs()).isEqualTo(0);

            assertThat(in.readWord()).isEqualTo(0x201);
            assertThat(in.getAbsOffs()).isEqualTo(2);

            assertThat(in.readDword()).isEqualTo(0x06050403);
            assertThat(in.getAbsOffs()).isEqualTo(6);

            assertThat(in.readQword()).isEqualTo(0x0E0D0C0B0A090807L);
            assertThat(in.getAbsOffs()).isEqualTo(14);
            assertThat(in.toString()).isEqualTo("offs: 14 (0xe)");

            assertThat(in.skip(2)).isEqualTo(2);
            assertThat(in.getAbsOffs()).isEqualTo(16);

            assertThat(in.readString(4, Charsets.UTF_8)).isEqualTo("oleg");
            assertThat(in.getAbsOffs()).isEqualTo(20);

            assertThat(in.readByte()).isEqualTo(0x11);
            assertThat(in.getAbsOffs()).isEqualTo(21);

            assertThat(in.readBytes(3)).isEqualTo(new byte[] { 0x12, 0x13, 0x14 });
            assertThat(in.getAbsOffs()).isEqualTo(24);
        }
    }

    public void shouldIgnoreSkipWhenZeroBytes() throws IOException {
        Path file = Zip4jvmSuite.subDirNameAsMethodName(rootDir).resolve(fileNameDataSrc);
        FileUtils.writeByteArrayToFile(file.toFile(), new byte[] { 0x1, 0x2 }, true);

        try (SplitRandomAccessDataInput in = new SplitRandomAccessDataInput(SrcZip.of(file))) {
            assertThat(in.getAbsOffs()).isEqualTo(0);

            assertThatCode(() -> in.skip(0)).doesNotThrowAnyException();
            assertThat(in.getAbsOffs()).isEqualTo(0);
        }
    }

    public void shouldThrowIllegalArgumentExceptionWhenSkipNegative() throws IOException {
        Path file = Zip4jvmSuite.subDirNameAsMethodName(rootDir).resolve(fileNameDataSrc);
        FileUtils.writeByteArrayToFile(file.toFile(), new byte[] { 0x1, 0x2 }, true);

        try (SplitRandomAccessDataInput in = new SplitRandomAccessDataInput(SrcZip.of(file))) {
            assertThat(in.getAbsOffs()).isEqualTo(0);
            assertThatThrownBy(() -> in.skip(-1)).isExactlyInstanceOf(IllegalArgumentException.class)
                                                 .hasMessage("Parameter should be zero or positive: 'skip.bytes'");
            assertThat(in.getAbsOffs()).isEqualTo(0);
        }
    }

    // TODO should be fixed
    @Test
    @Ignore
    public void shouldRetrieveAllBytesWhenReadTooManyBytes() throws IOException {
        Path file = Zip4jvmSuite.subDirNameAsMethodName(rootDir).resolve(fileNameDataSrc);
        FileUtils.writeByteArrayToFile(file.toFile(), new byte[] { 0x1, 0x2 }, true);

        try (SplitRandomAccessDataInput in = new SplitRandomAccessDataInput(SrcZip.of(file))) {
            assertThat(in.readBytes(3)).isEqualTo(new byte[] { 0x1, 0x2 });
            assertThat(in.getAbsOffs()).isEqualTo(2);
        }
    }

    @SuppressWarnings("PMD.CloseResource")
    public void shouldRetrieveNegativeOffsWhenCannotGetFilePointer() throws IOException {
        Path file = Zip4jvmSuite.subDirNameAsMethodName(rootDir).resolve(fileNameDataSrc);
        FileUtils.writeByteArrayToFile(file.toFile(), new byte[] { 0x1, 0x2 }, true);

        SplitRandomAccessDataInput in = new SplitRandomAccessDataInput(SrcZip.of(file));
        assertThat(in.getAbsOffs()).isEqualTo(0);

        in.close();
        assertThatCode(in::getAbsOffs).doesNotThrowAnyException();
        assertThat(in.getAbsOffs()).isEqualTo(IOUtils.EOF);
    }

}
