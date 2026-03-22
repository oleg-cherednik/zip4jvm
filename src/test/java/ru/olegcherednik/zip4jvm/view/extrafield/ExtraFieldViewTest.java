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
package ru.olegcherednik.zip4jvm.view.extrafield;

import ru.olegcherednik.zip4jvm.io.out.DataOutput;
import ru.olegcherednik.zip4jvm.model.GeneralPurposeFlag;
import ru.olegcherednik.zip4jvm.model.Zip64;
import ru.olegcherednik.zip4jvm.model.block.ExtraFieldBlock;
import ru.olegcherednik.zip4jvm.model.extrafield.PkwareExtraField;
import ru.olegcherednik.zip4jvm.model.extrafield.records.AesExtraFieldRecord;
import ru.olegcherednik.zip4jvm.model.extrafield.records.AndroidAlignmentExtraFieldRecord;
import ru.olegcherednik.zip4jvm.model.extrafield.records.ExtendedTimestampExtraFieldRecord;
import ru.olegcherednik.zip4jvm.model.extrafield.records.InfoZipNewUnixExtraFieldRecord;
import ru.olegcherednik.zip4jvm.model.extrafield.records.InfoZipOldUnixExtraFieldRecord;
import ru.olegcherednik.zip4jvm.model.extrafield.records.InfoZipUnicodeCommentExtraFieldRecord;
import ru.olegcherednik.zip4jvm.model.extrafield.records.InfoZipUnicodePathExtraFieldRecord;
import ru.olegcherednik.zip4jvm.model.extrafield.records.NtfsTimestampExtraFieldRecord;
import ru.olegcherednik.zip4jvm.model.extrafield.records.StrongEncryptionHeaderExtraFieldRecord;
import ru.olegcherednik.zip4jvm.view.out.Out;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * @author Oleg Cherednik
 * @since 22.04.2025
 */
@Test
public class ExtraFieldViewTest {

    private ExtraFieldBlock block;
    private ExtraFieldView view;

    @BeforeMethod
    public void createDir() {
        PkwareExtraField extraField = mock(PkwareExtraField.class);
        GeneralPurposeFlag generalPurposeFlag = mock(GeneralPurposeFlag.class);
        block = mock(ExtraFieldBlock.class);
        view = new ExtraFieldView(1, 2, 3, extraField, block, generalPurposeFlag);
    }

    public void shouldRetrieveEmptyOptionalWhenNullExtraField() {
        PkwareExtraField.Record record = mock(PkwareExtraField.Record.class);
        when(record.isNull()).thenReturn(true);

        assertThat(view.getView(null)).isEmpty();
        assertThat(view.getView(record)).isEmpty();
    }

    public void shouldRetrieveNtfsTimestampViewWhenNtfsTimestamp() {
        NtfsTimestampExtraFieldRecord record = NtfsTimestampExtraFieldRecord.builder().build();
        Optional<ExtraFieldRecordView<?>> recordView = view.getView(record);
        assertThat(recordView).isPresent();
        assertThat(recordView.get()).isInstanceOf(NtfsTimestampExtraFieldRecordView.class);
    }

    public void shouldRetrieveInfoZipUnicodeCommentViewWhenInfoZipUnicodeComment() {
        InfoZipUnicodeCommentExtraFieldRecord record = InfoZipUnicodeCommentExtraFieldRecord.builder().build();
        Optional<ExtraFieldRecordView<?>> recordView = view.getView(record);
        assertThat(recordView).isPresent();
        assertThat(recordView.get()).isInstanceOf(InfoZipUnicodeCommentExtraFieldRecordView.class);
    }

    public void shouldRetrieveInfoZipUnicodePathViewWhenInfoZipUnicodePath() {
        InfoZipUnicodePathExtraFieldRecord record = InfoZipUnicodePathExtraFieldRecord.builder().build();
        Optional<ExtraFieldRecordView<?>> recordView = view.getView(record);
        assertThat(recordView).isPresent();
        assertThat(recordView.get()).isInstanceOf(InfoZipUnicodePathExtraFieldRecordView.class);
    }

    public void shouldRetrieveInfoZipOldUnixViewWhenInfoZipOldUnix() {
        InfoZipOldUnixExtraFieldRecord record = InfoZipOldUnixExtraFieldRecord.builder().build();
        Optional<ExtraFieldRecordView<?>> recordView = view.getView(record);
        assertThat(recordView).isPresent();
        assertThat(recordView.get()).isInstanceOf(InfoZipOldUnixExtraFieldRecordView.class);
    }

    public void shouldRetrieveInfoZipNewUnixViewWhenInfoZipNewUnix() {
        InfoZipNewUnixExtraFieldRecord record = InfoZipNewUnixExtraFieldRecord.builder().build();
        Optional<ExtraFieldRecordView<?>> recordView = view.getView(record);
        assertThat(recordView).isPresent();
        assertThat(recordView.get()).isInstanceOf(InfoZipNewUnixExtraFieldRecordView.class);
    }

    public void shouldRetrieveExtendedTimestampViewWhenExtendedTimestamp() {
        ExtendedTimestampExtraFieldRecord record = ExtendedTimestampExtraFieldRecord.builder().build();
        Optional<ExtraFieldRecordView<?>> recordView = view.getView(record);
        assertThat(recordView).isPresent();
        assertThat(recordView.get()).isInstanceOf(ExtendedTimestampExtraFieldRecordView.class);
    }

    public void shouldRetrieveZip64ExtendedInfoViewWhenZip64ExtendedInfo() {
        Zip64.ExtendedInfo record = Zip64.ExtendedInfo.builder()
                                                      .compressedSize(1)
                                                      .uncompressedSize(2)
                                                      .build();
        Optional<ExtraFieldRecordView<?>> recordView = view.getView(record);
        assertThat(recordView).isPresent();
        assertThat(recordView.get()).isInstanceOf(Zip64ExtendedInfoView.class);
    }

    public void shouldRetrieveAesViewWhenAes() {
        AesExtraFieldRecord record = AesExtraFieldRecord.builder().build();
        Optional<ExtraFieldRecordView<?>> recordView = view.getView(record);
        assertThat(recordView).isPresent();
        assertThat(recordView.get()).isInstanceOf(AesExtraFieldRecordView.class);
    }

    public void shouldRetrieveStrongEncryptionHeaderViewWhenStrongEncryptionHeader() {
        StrongEncryptionHeaderExtraFieldRecord record = StrongEncryptionHeaderExtraFieldRecord.builder().build();
        Optional<ExtraFieldRecordView<?>> recordView = view.getView(record);
        assertThat(recordView).isPresent();
        assertThat(recordView.get()).isInstanceOf(StrongEncryptionHeaderExtraFieldRecordView.class);
    }

    public void shouldRetrieveAndroidAlignmentViewWhenAndroidAlignment() {
        AndroidAlignmentExtraFieldRecord record = AndroidAlignmentExtraFieldRecord.builder().build();
        Optional<ExtraFieldRecordView<?>> recordView = view.getView(record);
        assertThat(recordView).isPresent();
        assertThat(recordView.get()).isInstanceOf(AndroidAlignmentExtraFieldRecordView.class);
    }

    public void shouldRetrieveUnknownViewWhenNotImplemented() {
        PkwareExtraField.Record record = new PkwareExtraField.Record() {

            @Override
            public void write(DataOutput out) {
                // empty
            }

            @Override
            public int getSignature() {
                return 0;
            }

            @Override
            public int getBlockSize() {
                return 0;
            }

            @Override
            public boolean isNull() {
                return false;
            }

            @Override
            public String getTitle() {
                return "";
            }
        };

        Optional<ExtraFieldRecordView<?>> recordView = view.getView(record);
        assertThat(recordView).isPresent();
        assertThat(recordView.get()).isInstanceOf(UnknownExtraFieldRecordView.class);
    }

    @SuppressWarnings("PMD.CloseResource")
    public void shouldRetrieveFalseWhenPrintTextInfoForEmptySignatures() {
        when(block.getSignatures()).thenReturn(Collections.emptySet());

        Out out = mock(Out.class);
        view.printTextInfo(out);
        verifyNoInteractions(out);
    }

}
