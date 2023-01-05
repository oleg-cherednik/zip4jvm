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
package ru.olegcherednik.zip4jvm.model;

import org.apache.commons.lang3.ArrayUtils;
import org.testng.annotations.Test;
import ru.olegcherednik.zip4jvm.model.extrafield.PkwareExtraField;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Oleg Cherednik
 * @since 28.09.2019
 */
@Test
public class LocalFileHeaderBlockTest {

    public void shouldUseSettersGettersCorrectly() throws IOException {
        GeneralPurposeFlag generalPurposeFlag = new GeneralPurposeFlag();
        PkwareExtraField extraField = PkwareExtraField.builder().addRecord(Zip64.ExtendedInfo.builder().uncompressedSize(4).build()).build();

        Version versionToExtract = Version.of(Version.FileSystem.Z_SYSTEM, 15);

        assertThat(extraField).isNotSameAs(PkwareExtraField.NULL);

        LocalFileHeader localFileHeader = new LocalFileHeader();
        localFileHeader.setVersionToExtract(versionToExtract);
        localFileHeader.setGeneralPurposeFlag(generalPurposeFlag);
        localFileHeader.setCompressionMethod(CompressionMethod.AES);
        localFileHeader.setLastModifiedTime(3);
        localFileHeader.setCrc32(4);
        localFileHeader.setCompressedSize(5);
        localFileHeader.setUncompressedSize(6);
        localFileHeader.setFileName("fileName");
        localFileHeader.setExtraField(extraField);

        assertThat(localFileHeader.getVersionToExtract()).isSameAs(versionToExtract);
        assertThat(localFileHeader.getGeneralPurposeFlag()).isSameAs(generalPurposeFlag);
        assertThat(localFileHeader.getCompressionMethod()).isSameAs(CompressionMethod.AES);
        assertThat(localFileHeader.getLastModifiedTime()).isEqualTo(3);
        assertThat(localFileHeader.getCrc32()).isEqualTo(4);
        assertThat(localFileHeader.getCompressedSize()).isEqualTo(5);
        assertThat(localFileHeader.getUncompressedSize()).isEqualTo(6);
        assertThat(((PkwareExtraField)localFileHeader.getExtraField()).getExtendedInfo()).isNotNull();
        assertThat(localFileHeader.getFileName()).isEqualTo("fileName");
    }

    public void shouldRetrieveFileNameWhenToString() {
        LocalFileHeader localFileHeader = new LocalFileHeader();
        assertThat(localFileHeader.toString()).isNull();

        localFileHeader.setFileName("zip4jvm");
        assertThat(localFileHeader.toString()).isEqualTo("zip4jvm");
    }

    public void shouldRetrieveNotNullFileName() {
        LocalFileHeader localFileHeader = new LocalFileHeader();
        assertThat(localFileHeader.getFileName()).isNull();
        assertThat(localFileHeader.getFileName(Charsets.UTF_8)).isSameAs(ArrayUtils.EMPTY_BYTE_ARRAY);

        localFileHeader.setFileName("zip4jvm");
        assertThat(localFileHeader.getFileName(Charsets.UTF_8)).isEqualTo("zip4jvm".getBytes(Charsets.UTF_8));
    }

}
