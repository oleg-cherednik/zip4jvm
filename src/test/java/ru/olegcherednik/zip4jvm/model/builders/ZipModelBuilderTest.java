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
package ru.olegcherednik.zip4jvm.model.builders;

import org.testng.annotations.Test;
import ru.olegcherednik.zip4jvm.exception.Zip4jvmException;
import ru.olegcherednik.zip4jvm.model.CentralDirectory;
import ru.olegcherednik.zip4jvm.model.Charsets;
import ru.olegcherednik.zip4jvm.model.CustomizeCharset;
import ru.olegcherednik.zip4jvm.model.EndCentralDirectory;
import ru.olegcherednik.zip4jvm.model.Zip64;
import ru.olegcherednik.zip4jvm.model.ZipModel;
import ru.olegcherednik.zip4jvm.model.settings.ZipSettings;
import ru.olegcherednik.zip4jvm.model.src.SrcZip;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static ru.olegcherednik.zip4jvm.TestData.zipStoreSolid;

/**
 * @author Oleg Cherednik
 * @since 27.09.2019
 */
@Test
public class ZipModelBuilderTest {

    public void shouldThrowExceptionWhenCreateModelForExistedFile() {
        assertThatThrownBy(() -> ZipModelBuilder.build(zipStoreSolid, ZipSettings.DEFAULT)).isExactlyInstanceOf(Zip4jvmException.class);
    }

    public void shouldCreateEmptyZipModelWhenCentralDirectoryNull() {
        SrcZip srcZip = mock(SrcZip.class);
        EndCentralDirectory endCentralDirectory = mock(EndCentralDirectory.class);
        Zip64 zip64 = Zip64.NULL;
        CentralDirectory centralDirectory = null;
        CustomizeCharset customizeCharset = Charsets.UNMODIFIED;

        ZipModelBuilder builder = new ZipModelBuilder(srcZip,
                                                      endCentralDirectory,
                                                      zip64,
                                                      centralDirectory,
                                                      customizeCharset);

        ZipModel zipModel = builder.build();
        assertThat(zipModel.isEmpty()).isTrue();
    }

}
