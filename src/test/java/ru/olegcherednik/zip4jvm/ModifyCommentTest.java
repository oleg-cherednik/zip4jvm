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
import ru.olegcherednik.zip4jvm.model.settings.EncryptionEnum;
import ru.olegcherednik.zip4jvm.model.settings.ZipEntrySettings;
import ru.olegcherednik.zip4jvm.model.settings.ZipSettings;

import org.apache.commons.lang3.StringUtils;
import org.testng.annotations.Test;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static ru.olegcherednik.zip4jvm.TestData.fileNameZipSrc;
import static ru.olegcherednik.zip4jvm.TestData.fileOlegCherednik;
import static ru.olegcherednik.zip4jvm.TestData.zipDeflateSolid;
import static ru.olegcherednik.zip4jvm.Zip4jvmSuite.password;
import static ru.olegcherednik.zip4jvm.assertj.Zip4jvmAssertions.assertThatZipFile;

/**
 * @author Oleg Cherednik
 * @since 15.03.2019
 */
@Test
public class ModifyCommentTest extends BaseTest {

    public void shouldCreateZipWithCommentUpdateAndClearItForNotEncryptedZip() {
        ZipSettings settings = ZipSettings.builder()
                                          .entrySettings(ZipEntrySettings.of(CompressionEnum.DEFLATE))
                                          .comment("Oleg Cherednik - Олег Чередник").build();

        Path zip = getZip();

        ZipIt.zip(zip).settings(settings).add(fileOlegCherednik);
        assertThatZipFile(zip).hasComment("Oleg Cherednik - Олег Чередник");

        ZipMisc.zip(zip).setComment("this is new comment - новый комментарий");
        assertThatZipFile(zip).hasComment("this is new comment - новый комментарий");

        ZipMisc.zip(zip).setComment(null);
        assertThatZipFile(zip).hasCommentSize(0);
    }

    public void shouldCreateZipWithCommentUpdateAndClearItForEncryptedZip() {
        ZipSettings settings = ZipSettings.builder()
                                          .entrySettings(ZipEntrySettings.of(CompressionEnum.DEFLATE,
                                                                             EncryptionEnum.AES_256,
                                                                             password))
                                          .comment("Oleg Cherednik - Олег Чередник").build();

        Path zip = getZip();

        ZipIt.zip(zip).settings(settings).add(fileOlegCherednik);
        assertThatZipFile(zip).hasComment("Oleg Cherednik - Олег Чередник");

        ZipMisc.zip(zip).setComment("this is new comment - новый комментарий");
        assertThatZipFile(zip).hasComment("this is new comment - новый комментарий");

        ZipMisc.zip(zip).setComment(null);
        assertThatZipFile(zip).hasCommentSize(0);
    }

    public void shouldSetCommentWithMaxLength() {
        Path zip = getZip();
        Zip4jvmSuite.createDir(zip.getParent());
        Zip4jvmSuite.copyFile(zipDeflateSolid, zip);

        ZipMisc.zip(zip).setComment(StringUtils.repeat("_", ZipModel.MAX_COMMENT_SIZE));
        assertThatZipFile(zip).hasCommentSize(ZipModel.MAX_COMMENT_SIZE);
    }

    public void shouldThrowExceptionWhenCommentIsOverMaxLength() {
        Path zip = getZip();
        Zip4jvmSuite.createDir(zip.getParent());
        Zip4jvmSuite.copyFile(zipDeflateSolid, zip);

        assertThatThrownBy(() -> ZipMisc.zip(zip).setComment(StringUtils.repeat("_", ZipModel.MAX_COMMENT_SIZE + 1)))
                .isInstanceOf(IllegalArgumentException.class);
    }

}
