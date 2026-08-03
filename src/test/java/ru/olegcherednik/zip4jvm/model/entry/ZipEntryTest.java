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
package ru.olegcherednik.zip4jvm.model.entry;

import ru.olegcherednik.zip4jvm.model.settings.ZipEntrySettings;

import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.olegcherednik.zip4jvm.TestData.dirBikes;
import static ru.olegcherednik.zip4jvm.TestData.dirNameBikes;
import static ru.olegcherednik.zip4jvm.TestData.fileBentley;
import static ru.olegcherednik.zip4jvm.TestData.fileNameBentley;

/**
 * @author Oleg Cherednik
 * @since 01.10.2019
 */
@Test
public class ZipEntryTest {

    public void shouldRetrieveFileNameWhenToString() {
        ZipEntry file = ZipEntryBuilder.regularFile(fileBentley, fileNameBentley, ZipEntrySettings.DEFAULT);
        ZipEntry dir = ZipEntryBuilder.emptyDirectory(dirBikes, dirNameBikes, ZipEntrySettings.DEFAULT);

        assertThat(file.toString()).isEqualTo(fileNameBentley);
        assertThat(dir.toString()).isEqualTo(dirNameBikes + '/');
    }
}
