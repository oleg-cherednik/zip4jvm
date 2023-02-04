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

import org.apache.commons.lang3.StringUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.olegcherednik.zip4jvm.model.Compression;
import ru.olegcherednik.zip4jvm.model.CompressionLevel;
import ru.olegcherednik.zip4jvm.model.ZipModel;
import ru.olegcherednik.zip4jvm.model.settings.ZipEntrySettings;
import ru.olegcherednik.zip4jvm.model.settings.ZipSettings;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static ru.olegcherednik.zip4jvm.TestData.fileOlegCherednik;
import static ru.olegcherednik.zip4jvm.TestData.zipDeflateSolid;
import static ru.olegcherednik.zip4jvm.assertj.Zip4jvmAssertions.assertThatZipFile;

/**
 * @author Oleg Cherednik
 * @since 15.03.2019
 */
@Test
@SuppressWarnings({ "FieldNamingConvention", "NewMethodNamingConvention" })
public class ModifyCommentTest {

    private static final Path rootDir = Zip4jvmSuite.generateSubDirNameWithTime(ModifyCommentTest.class);
    private static final Path zip = rootDir.resolve("src.zip");

    @BeforeClass
    public static void createDir() throws IOException {
        Files.createDirectories(rootDir);
    }

    @AfterClass(enabled = Zip4jvmSuite.clear)
    public static void removeDir() throws IOException {
        Zip4jvmSuite.removeDir(rootDir);
    }

    public void shouldCreateNewZipWithComment() throws IOException {
        ZipSettings settings = ZipSettings.builder()
                                          .entrySettingsProvider(fileName ->
                                                                         ZipEntrySettings.builder()
                                                                                         .compression(Compression.DEFLATE, CompressionLevel.NORMAL)
                                                                                         .build())
                                          .comment("Oleg Cherednik - Олег Чередник").build();
        ZipIt.zip(zip).settings(settings).add(fileOlegCherednik);
        assertThatZipFile(zip).exists().hasComment("Oleg Cherednik - Олег Чередник");
    }

    @Test(dependsOnMethods = "shouldCreateNewZipWithComment")
    public void shouldAddCommentToExistedNoSplitZip() throws IOException {
        ZipMisc.zip(zip).setComment("this is new comment - новый комментарий");
        assertThatZipFile(zip).exists().hasComment("this is new comment - новый комментарий");
    }

    @Test(dependsOnMethods = "shouldAddCommentToExistedNoSplitZip")
    public void shouldClearCommentForExistedZip() throws IOException {
        ZipMisc.zip(zip).setComment(null);
        assertThatZipFile(zip).exists().hasCommentSize(0);
    }

    @Test(dependsOnMethods = "shouldClearCommentForExistedZip")
    public void shouldAddCommentToEncryptedZip() throws IOException {
        assertThatZipFile(zip, Zip4jvmSuite.password).hasCommentSize(0);

        ZipMisc.zip(zip).setComment("this is new comment");
        assertThatZipFile(zip, Zip4jvmSuite.password).hasComment("this is new comment");
    }

    public void shouldSetCommentWithMaxLength() throws IOException {
        Path zip = Zip4jvmSuite.subDirNameAsMethodName(rootDir).resolve("src.zip");
        Files.createDirectories(zip.getParent());
        Files.copy(zipDeflateSolid, zip);

        ZipMisc.zip(zip).setComment(StringUtils.repeat("_", ZipModel.MAX_COMMENT_SIZE));
        assertThatZipFile(zip).hasCommentSize(ZipModel.MAX_COMMENT_SIZE);
    }

    public void shouldThrowExceptionWhenCommentIsOverMaxLength() throws IOException {
        Path zip = Zip4jvmSuite.subDirNameAsMethodName(rootDir).resolve("src.zip");
        Files.createDirectories(zip.getParent());
        Files.copy(zipDeflateSolid, zip);

        assertThatThrownBy(() -> ZipMisc.zip(zip).setComment(StringUtils.repeat("_", ZipModel.MAX_COMMENT_SIZE + 1)))
                .isInstanceOf(IllegalArgumentException.class);
    }

}
