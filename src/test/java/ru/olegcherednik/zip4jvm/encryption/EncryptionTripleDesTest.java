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
import ru.olegcherednik.zip4jvm.ZipIt;
import ru.olegcherednik.zip4jvm.exception.IncorrectZipEntryPasswordException;
import ru.olegcherednik.zip4jvm.model.settings.CompressionEnum;
import ru.olegcherednik.zip4jvm.model.settings.EncryptionEnum;
import ru.olegcherednik.zip4jvm.model.settings.ZipSettings;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.nio.file.Path;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static ru.olegcherednik.zip4jvm.TestData.contentDirSrc;
import static ru.olegcherednik.zip4jvm.TestData.secureZipStoreSolid3des168StrongZip;
import static ru.olegcherednik.zip4jvm.TestDataAssert.dirBikesAssert;
import static ru.olegcherednik.zip4jvm.TestDataAssert.rootAssert;
import static ru.olegcherednik.zip4jvm.Zip4jvmSuite.password;
import static ru.olegcherednik.zip4jvm.assertj.Zip4jvmAssertions.assertThatDirectory;
import static ru.olegcherednik.zip4jvm.assertj.Zip4jvmAssertions.assertThatZipFile;


/**
 * @author Oleg Cherednik
 * @since 21.07.2026
 */
@Test
public class EncryptionTripleDesTest {

    private static final Path DIR_ROOT = Zip4jvmSuite.generateSubDirNameWithTime();

    @BeforeClass
    public static void createDir() {
        Zip4jvmSuite.createDir(DIR_ROOT);
    }

    @AfterClass(enabled = Zip4jvmSuite.clear)
    public static void removeDir() {
        Zip4jvmSuite.removeDir(DIR_ROOT);
    }

    //    public void shouldCreateNewZipWithFolderAnd3des192Encryption() {
    //        ZipSettings settings = ZipSettings.builder()
    //                                          .entrySettings(CompressionEnum.STORE,
    //                                                         EncryptionEnum.TRIPLE_DES_192, password)
    //                                          .build();
    //
    //        Path zip = Zip4jvmSuite.subDirNameAsMethodName(DIR_ROOT).resolve("src.zip");
    //
    //        ZipIt.zip(zip).settings(settings).add(contentDirSrc);
    //        assertThatZipFile(zip).parent().hasDirectories(0).hasRegularFiles(1);
    //        assertThatZipFile(zip, password).exists().root().matches(rootAssert);
    //    }

    public void shouldCreateNewZipWithFolderAnd3des168Encryption() {
        ZipSettings settings = ZipSettings.builder()
                                          .entrySettings(CompressionEnum.STORE, EncryptionEnum.TRIPLE_DES_168, password)
                                          .build();

        Path zip = Zip4jvmSuite.subDirNameAsMethodName(DIR_ROOT).resolve("src.zip");

        ZipIt.zip(zip).settings(settings).add(contentDirSrc);
        assertThatZipFile(zip).parent().hasDirectories(0).hasRegularFiles(1);
        assertThatZipFile(zip, password).exists().root().matches(rootAssert);
    }

    public void shouldUnzipWhenStoreSolid3Des168() {
        Path dstDir = Zip4jvmSuite.subDirNameAsMethodName(DIR_ROOT);
        UnzipIt.zip(secureZipStoreSolid3des168StrongZip).dstDir(dstDir).password(password).extract();
        assertThatDirectory(dstDir).matches(dirBikesAssert);
    }

    public void shouldThrowExceptionWhenUnzip3desEncryptedZipWithIncorrectPassword() {
        Path dstDir = Zip4jvmSuite.subDirNameAsMethodName(DIR_ROOT);
        char[] password = UUID.randomUUID().toString().toCharArray();

        assertThatThrownBy(() -> UnzipIt.zip(secureZipStoreSolid3des168StrongZip)
                                        .dstDir(dstDir).password(password).extract())
                .isExactlyInstanceOf(IncorrectZipEntryPasswordException.class);
    }

}
