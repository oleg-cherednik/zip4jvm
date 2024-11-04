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
package ru.olegcherednik.zip4jvm.io.out.file;

import ru.olegcherednik.zip4jvm.Zip4jvmSuite;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Oleg Cherednik
 * @since 09.04.2023
 */
@Test
public class OffsOutputStreamTest {

    @SuppressWarnings("FieldNamingConvention")
    private static final Path rootDir =
            Zip4jvmSuite.generateSubDirNameWithTime(OffsOutputStreamTest.class);

    @BeforeClass
    public static void createDir() throws IOException {
        Files.createDirectories(rootDir);
    }

    @AfterClass(enabled = Zip4jvmSuite.clear)
    public static void removeDir() throws IOException {
        Zip4jvmSuite.removeDir(rootDir);
    }

    public void shouldSupportFlush() throws IOException {
        Path file = Zip4jvmSuite.subDirNameAsMethodName(rootDir).resolve("foo.txt");

        try (OffsOutputStream out = OffsOutputStream.create(file)) {
            assertThat(Files.readAllBytes(file)).isEmpty();

            out.write(new byte[] { 0x0, 0x1, 0x2 }, 0, 3);
            assertThat(Files.readAllBytes(file)).isEmpty();

            out.flush();
            assertThat(Files.readAllBytes(file)).isEqualTo(new byte[] { 0x0, 0x1, 0x2 });

            out.write(new byte[] { 0x3, 0x4 }, 0, 2);
            assertThat(Files.readAllBytes(file)).isEqualTo(new byte[] { 0x0, 0x1, 0x2 });
        }

        assertThat(Files.readAllBytes(file)).isEqualTo(new byte[] { 0x0, 0x1, 0x2, 0x3, 0x4 });
    }

}
