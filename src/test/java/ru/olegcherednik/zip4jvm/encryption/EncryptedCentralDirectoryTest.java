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
package ru.olegcherednik.zip4jvm.encryption;

import ru.olegcherednik.zip4jvm.UnzipIt;
import ru.olegcherednik.zip4jvm.Zip4jvmSuite;
import ru.olegcherednik.zip4jvm.exception.EncryptionNotSupportedException;
import ru.olegcherednik.zip4jvm.exception.IncorrectCentralDirectoryPasswordException;
import ru.olegcherednik.zip4jvm.model.password.PasswordProvider;
import ru.olegcherednik.zip4jvm.model.settings.UnzipSettings;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static ru.olegcherednik.zip4jvm.Zip4jvmSuite.password;

/**
 * @author Oleg Cherednik
 * @since 19.09.2024
 */
@Test
public class EncryptedCentralDirectoryTest {

    private static final Path ROOT_DIR = Zip4jvmSuite.generateSubDirNameWithTime(EncryptedCentralDirectoryTest.class);

    @BeforeClass
    public static void createDir() throws IOException {
        Files.createDirectories(ROOT_DIR);
    }

    @AfterClass(enabled = Zip4jvmSuite.clear)
    public static void removeDir() throws IOException {
        Zip4jvmSuite.removeDir(ROOT_DIR);
    }

    public void shouldUnzipWhenStoreSolidAes() throws IOException {
        Path zip = Zip4jvmSuite.getResourcePath("/encrypted-central-directory/store_aes256bit.zip");
        // Path zip = Zip4jvmSuite.getResourcePath("/encrypted-central-directory/deflate_aes256bit.zip");
        // Path zip = Zip4jvmSuite.getResourcePath("/encrypted-central-directory/lzma_aes256bit.zip");
        // Path zip = Zip4jvmSuite.getResourcePath("/encrypted-central-directory/bzip2_aes256bit.zip");

        // Path zip = Zip4jvmSuite.getResourcePath("/encrypted-central-directory/3des168bit.zip");
        Path dstDir = Zip4jvmSuite.subDirNameAsMethodName(ROOT_DIR);

        UnzipSettings settings = UnzipSettings.builder().password(password).build();
        UnzipIt.zip(zip).dstDir(dstDir).settings(settings).extract();
        //        ZipInfo.zip(zip).password(password).decompose(dstDir.resolve("-----"));
        //        assertThatDirectory(dstDir).matches(rootAssert);
    }

    public void shouldThrowEncryptionNotSupportedExceptionWhenReadEncryptedCentralDirectory() throws IOException {
        Path zip = Zip4jvmSuite.getResourcePath("/encrypted-central-directory/3des168bit.zip");
        Path dstDir = Zip4jvmSuite.subDirNameAsMethodName(ROOT_DIR);

        UnzipSettings settings = UnzipSettings.builder().password(password).build();

        assertThatThrownBy(() -> UnzipIt.zip(zip).dstDir(dstDir).settings(settings).extract())
                .isExactlyInstanceOf(EncryptionNotSupportedException.class)
                .hasMessageContaining("central directory");
    }

    @Test(enabled = false)
    public void shouldThrowIncorrectCentralDirectoryPasswordExceptionWhenNotCorrectPasswordForCentralDirectory()
            throws IOException {
        Path zip = Zip4jvmSuite.getResourcePath("/encrypted-central-directory/aes128bit.zip");
        Path dstDir = Zip4jvmSuite.subDirNameAsMethodName(ROOT_DIR);

        UnzipSettings settings = UnzipSettings.builder()
                                              .passwordProvider(new PasswordProvider() {
                                                  @Override
                                                  public char[] getFilePassword(String fileName) {
                                                      return password;
                                                  }

                                                  @Override
                                                  public char[] getCentralDirectoryPassword() {
                                                      return "unknown".toCharArray();
                                                  }
                                              }).build();

        assertThatThrownBy(() -> UnzipIt.zip(zip).dstDir(dstDir).settings(settings).extract())
                .isExactlyInstanceOf(IncorrectCentralDirectoryPasswordException.class)
                .hasMessageContaining("central directory");
    }

}
