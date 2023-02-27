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
package ru.olegcherednik.zip4jvm.model.entry;

import org.testng.annotations.Test;
import ru.olegcherednik.zip4jvm.model.settings.ZipEntrySettings;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.olegcherednik.zip4jvm.TestData.dirBikes;
import static ru.olegcherednik.zip4jvm.TestData.dirNameBikes;
import static ru.olegcherednik.zip4jvm.TestData.fileBentley;
import static ru.olegcherednik.zip4jvm.TestData.fileNameBentley;
import static ru.olegcherednik.zip4jvm.TestData.zipDirNameBikes;

/**
 * @author Oleg Cherednik
 * @since 01.10.2019
 */
@Test
public class ZipEntryTest {

    public void shouldRetrieveFileNameWhenToString() throws IOException {
        ZipEntry file = ZipEntryBuilder.regularFile(fileBentley, fileNameBentley, ZipEntrySettings.DEFAULT);
        ZipEntry dir = ZipEntryBuilder.emptyDirectory(dirBikes, dirNameBikes, ZipEntrySettings.DEFAULT);

        assertThat(file.toString()).isEqualTo(fileNameBentley);
        assertThat(dir.toString()).isEqualTo(zipDirNameBikes);
    }
}
