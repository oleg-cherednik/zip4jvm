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

import ru.olegcherednik.zip4jvm.model.ZipModel;
import ru.olegcherednik.zip4jvm.model.settings.CompressionEnum;
import ru.olegcherednik.zip4jvm.model.settings.ZipEntrySettings;
import ru.olegcherednik.zip4jvm.model.settings.ZipSettings;

import org.apache.commons.lang3.StringUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

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
public class ModifyCommentTest {

    private static final Path DIR_ROOT = Zip4jvmSuite.generateSubDirNameWithTime();
    private static final Path SRC_ZIP = DIR_ROOT.resolve("src.zip");

    @BeforeClass
    public static void createDir() {
        Zip4jvmSuite.createDir(DIR_ROOT);
    }

    @AfterClass(enabled = Zip4jvmSuite.clear)
    public static void removeDir() {
        Zip4jvmSuite.removeDir(DIR_ROOT);
    }

    public void shouldCreateNewZipWithComment() {
        ZipEntrySettings entrySettings = ZipEntrySettings.of(CompressionEnum.DEFLATE);

        ZipSettings settings = ZipSettings.builder()
                                          .entrySettings(entrySettings)
                                          .comment("Oleg Cherednik - Олег Чередник").build();
        ZipIt.zip(SRC_ZIP).settings(settings).add(fileOlegCherednik);
        assertThatZipFile(SRC_ZIP).exists().hasComment("Oleg Cherednik - Олег Чередник");
    }

    @Test(dependsOnMethods = "shouldCreateNewZipWithComment")
    public void shouldAddCommentToExistedNoSplitZip() {
        ZipMisc.zip(SRC_ZIP).setComment("this is new comment - новый комментарий");
        assertThatZipFile(SRC_ZIP).exists().hasComment("this is new comment - новый комментарий");
    }

    @Test(dependsOnMethods = "shouldAddCommentToExistedNoSplitZip")
    public void shouldClearCommentForExistedZip() {
        ZipMisc.zip(SRC_ZIP).setComment(null);
        assertThatZipFile(SRC_ZIP).exists().hasCommentSize(0);
    }

    @Test(dependsOnMethods = "shouldClearCommentForExistedZip")
    public void shouldAddCommentToEncryptedZip() {
        assertThatZipFile(SRC_ZIP, Zip4jvmSuite.password).hasCommentSize(0);

        ZipMisc.zip(SRC_ZIP).setComment("this is new comment");
        assertThatZipFile(SRC_ZIP, Zip4jvmSuite.password).hasComment("this is new comment");
    }

    public void shouldSetCommentWithMaxLength() {
        Path srcZip = Zip4jvmSuite.subDirNameAsMethodName(DIR_ROOT).resolve("src.zip");
        Zip4jvmSuite.createDir(srcZip.getParent());
        Zip4jvmSuite.copyFile(zipDeflateSolid, srcZip);

        ZipMisc.zip(srcZip).setComment(StringUtils.repeat("_", ZipModel.MAX_COMMENT_SIZE));
        assertThatZipFile(srcZip).hasCommentSize(ZipModel.MAX_COMMENT_SIZE);
    }

    public void shouldThrowExceptionWhenCommentIsOverMaxLength() {
        Path srcZip = Zip4jvmSuite.subDirNameAsMethodName(DIR_ROOT).resolve("src.zip");
        Zip4jvmSuite.createDir(srcZip.getParent());
        Zip4jvmSuite.copyFile(zipDeflateSolid, srcZip);

        assertThatThrownBy(() -> ZipMisc.zip(srcZip).setComment(StringUtils.repeat("_", ZipModel.MAX_COMMENT_SIZE + 1)))
                .isInstanceOf(IllegalArgumentException.class);
    }

}
