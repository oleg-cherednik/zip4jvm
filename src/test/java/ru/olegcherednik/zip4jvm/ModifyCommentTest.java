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
import static ru.olegcherednik.zip4jvm.TestData.fileNameZipSrc;
import static ru.olegcherednik.zip4jvm.TestData.fileOlegCherednik;
import static ru.olegcherednik.zip4jvm.TestData.zipDeflateSolid;
import static ru.olegcherednik.zip4jvm.assertj.Zip4jvmAssertions.assertThatZipFile;

/**
 * @author Oleg Cherednik
 * @since 15.03.2019
 */
@Test
public class ModifyCommentTest extends BaseTest {

    private final Path srcZip = dirRoot.resolve(fileNameZipSrc);

    public void shouldCreateNewZipWithComment() {
        ZipSettings settings = ZipSettings.builder()
                                          .entrySettings(ZipEntrySettings.of(CompressionEnum.DEFLATE))
                                          .comment("Oleg Cherednik - Олег Чередник").build();
        ZipIt.zip(srcZip).settings(settings).add(fileOlegCherednik);
        assertThatZipFile(srcZip).hasComment("Oleg Cherednik - Олег Чередник");
    }

    @Test(dependsOnMethods = "shouldCreateNewZipWithComment")
    public void shouldAddCommentToExistedNoSplitZip() {
        ZipMisc.zip(srcZip).setComment("this is new comment - новый комментарий");
        assertThatZipFile(srcZip).hasComment("this is new comment - новый комментарий");
    }

    @Test(dependsOnMethods = "shouldAddCommentToExistedNoSplitZip")
    public void shouldClearCommentForExistedZip() {
        ZipMisc.zip(srcZip).setComment(null);
        assertThatZipFile(srcZip).hasCommentSize(0);
    }

    @Test(dependsOnMethods = "shouldClearCommentForExistedZip")
    public void shouldAddCommentToEncryptedZip() {
        assertThatZipFile(srcZip, Zip4jvmSuite.password).hasCommentSize(0);

        ZipMisc.zip(srcZip).setComment("this is new comment");
        assertThatZipFile(srcZip, Zip4jvmSuite.password).hasComment("this is new comment");
    }

    public void shouldSetCommentWithMaxLength() {
        Path srcZip = Zip4jvmSuite.subDirNameAsMethodName(dirRoot).resolve("src.zip");
        Zip4jvmSuite.createDir(srcZip.getParent());
        Zip4jvmSuite.copyFile(zipDeflateSolid, srcZip);

        ZipMisc.zip(srcZip).setComment(StringUtils.repeat("_", ZipModel.MAX_COMMENT_SIZE));
        assertThatZipFile(srcZip).hasCommentSize(ZipModel.MAX_COMMENT_SIZE);
    }

    public void shouldThrowExceptionWhenCommentIsOverMaxLength() {
        Path srcZip = Zip4jvmSuite.subDirNameAsMethodName(dirRoot).resolve("src.zip");
        Zip4jvmSuite.createDir(srcZip.getParent());
        Zip4jvmSuite.copyFile(zipDeflateSolid, srcZip);

        assertThatThrownBy(() -> ZipMisc.zip(srcZip).setComment(StringUtils.repeat("_", ZipModel.MAX_COMMENT_SIZE + 1)))
                .isInstanceOf(IllegalArgumentException.class);
    }

}
