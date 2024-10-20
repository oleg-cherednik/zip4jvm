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

import ru.olegcherednik.zip4jvm.model.GeneralPurposeFlag;
import ru.olegcherednik.zip4jvm.model.Zip64;
import ru.olegcherednik.zip4jvm.model.block.ExtraFieldBlock;
import ru.olegcherednik.zip4jvm.model.extrafield.PkwareExtraField;
import ru.olegcherednik.zip4jvm.model.extrafield.records.AesExtraFieldRecord;
import ru.olegcherednik.zip4jvm.model.extrafield.records.AlignmentExtraFieldRecord;
import ru.olegcherednik.zip4jvm.model.extrafield.records.ExtendedTimestampExtraFieldRecord;
import ru.olegcherednik.zip4jvm.model.extrafield.records.InfoZipNewUnixExtraFieldRecord;
import ru.olegcherednik.zip4jvm.model.extrafield.records.InfoZipOldUnixExtraFieldRecord;
import ru.olegcherednik.zip4jvm.model.extrafield.records.NtfsTimestampExtraFieldRecord;
import ru.olegcherednik.zip4jvm.model.extrafield.records.StrongEncryptionHeaderExtraFieldRecord;
import ru.olegcherednik.zip4jvm.view.BaseView;

import java.io.PrintStream;
import java.util.Collection;
import java.util.function.Function;

/**
 * @author Oleg Cherednik
 * @since 16.10.2019
 */
public final class ExtraFieldView extends BaseView {

    private final PkwareExtraField extraField;
    private final ExtraFieldBlock block;
    private final GeneralPurposeFlag generalPurposeFlag;

    @SuppressWarnings("PMD.AvoidFieldNameMatchingMethodName")
    private final Function<PkwareExtraField.Record, ExtraFieldRecordView<?>> createView = record -> {
        if (record instanceof NtfsTimestampExtraFieldRecord)
            return createView((NtfsTimestampExtraFieldRecord) record);
        if (record instanceof InfoZipOldUnixExtraFieldRecord)
            return createView((InfoZipOldUnixExtraFieldRecord) record);
        if (record instanceof InfoZipNewUnixExtraFieldRecord)
            return createView((InfoZipNewUnixExtraFieldRecord) record);
        if (record instanceof ExtendedTimestampExtraFieldRecord)
            return createView((ExtendedTimestampExtraFieldRecord) record);
        if (record instanceof Zip64.ExtendedInfo)
            return createView((Zip64.ExtendedInfo) record);
        if (record instanceof AesExtraFieldRecord)
            return createView((AesExtraFieldRecord) record);
        if (record instanceof StrongEncryptionHeaderExtraFieldRecord)
            return createView((StrongEncryptionHeaderExtraFieldRecord) record);
        if (record instanceof AlignmentExtraFieldRecord)
            return createView((AlignmentExtraFieldRecord) record);
        return createView(record);
    };

    public ExtraFieldView(PkwareExtraField extraField,
                          ExtraFieldBlock block,
                          GeneralPurposeFlag generalPurposeFlag,
                          int offs,
                          int columnWidth,
                          long totalDisks) {
        super(offs, columnWidth, totalDisks);
        this.extraField = extraField;
        this.block = block;
        this.generalPurposeFlag = generalPurposeFlag;
    }

    @Override
    public boolean printTextInfo(PrintStream out) {
        Collection<PkwareExtraField.Record> records = extraField.getRecords();
        records.forEach(record -> printRecord(out, record));
        return !records.isEmpty();
    }

    public void printLocation(PrintStream out) {
        printValueWithLocation(out, "extra field:", block, extraField.getTotalRecords());
    }

    public void printRecord(PrintStream out, PkwareExtraField.Record record) {
        if (record != null && !record.isNull())
            getView(record).printTextInfo(out);
    }

    public ExtraFieldRecordView<?> getView(PkwareExtraField.Record record) {
        // TODO check for record != null && !record.isNull()
        return createView.apply(record);
    }

    private NtfsTimestampExtraFieldRecordView createView(NtfsTimestampExtraFieldRecord record) {
        return NtfsTimestampExtraFieldRecordView.builder()
                                                .record(record)
                                                .block(block.getRecord(record.getSignature()))
                                                .position(offs, columnWidth, totalDisks).build();
    }

    private InfoZipOldUnixExtraFieldRecordView createView(InfoZipOldUnixExtraFieldRecord record) {
        return InfoZipOldUnixExtraFieldRecordView.builder()
                                                 .record(record)
                                                 .block(block.getRecord(record.getSignature()))
                                                 .position(offs, columnWidth, totalDisks).build();
    }

    private InfoZipNewUnixExtraFieldRecordView createView(InfoZipNewUnixExtraFieldRecord record) {
        return InfoZipNewUnixExtraFieldRecordView.builder()
                                                 .record(record)
                                                 .block(block.getRecord(record.getSignature()))
                                                 .position(offs, columnWidth, totalDisks).build();
    }

    private ExtendedTimestampExtraFieldRecordView createView(ExtendedTimestampExtraFieldRecord record) {
        return ExtendedTimestampExtraFieldRecordView.builder()
                                                    .record(record)
                                                    .block(block.getRecord(record.getSignature()))
                                                    .position(offs, columnWidth, totalDisks).build();
    }

    private Zip64ExtendedInfoView createView(Zip64.ExtendedInfo record) {
        return Zip64ExtendedInfoView.builder()
                                    .record(record)
                                    .block(block.getRecord(record.getSignature()))
                                    .position(offs, columnWidth, totalDisks).build();
    }

    private AesExtraFieldRecordView createView(AesExtraFieldRecord record) {
        return AesExtraFieldRecordView.builder()
                                      .record(record)
                                      .generalPurposeFlag(generalPurposeFlag)
                                      .block(block.getRecord(record.getSignature()))
                                      .position(offs, columnWidth, totalDisks).build();
    }

    private StrongEncryptionHeaderExtraFieldRecordView createView(StrongEncryptionHeaderExtraFieldRecord record) {
        return StrongEncryptionHeaderExtraFieldRecordView.builder()
                                                         .record(record)
                                                         .generalPurposeFlag(generalPurposeFlag)
                                                         .block(block.getRecord(record.getSignature()))
                                                         .position(offs, columnWidth, totalDisks).build();
    }

    private AlignmentExtraFieldRecordView createView(AlignmentExtraFieldRecord record) {
        return AlignmentExtraFieldRecordView.builder()
                                            .record(record)
                                            .generalPurposeFlag(generalPurposeFlag)
                                            .block(block.getRecord(record.getSignature()))
                                            .position(offs, columnWidth, totalDisks).build();
    }

    private UnknownExtraFieldRecordView createView(PkwareExtraField.Record record) {
        return UnknownExtraFieldRecordView.builder()
                                          .record(record)
                                          .block(block.getRecord(record.getSignature()))
                                          .position(offs, columnWidth, totalDisks).build();
    }

}
