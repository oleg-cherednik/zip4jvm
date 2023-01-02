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

import ru.olegcherednik.zip4jvm.model.ExtraField;
import ru.olegcherednik.zip4jvm.model.GeneralPurposeFlag;
import ru.olegcherednik.zip4jvm.model.Zip64;
import ru.olegcherednik.zip4jvm.model.block.Block;
import ru.olegcherednik.zip4jvm.model.block.ExtraFieldBlock;
import ru.olegcherednik.zip4jvm.model.extrafield.AesExtraFieldRecord;
import ru.olegcherednik.zip4jvm.model.extrafield.StrongEncryptionHeaderExtraFieldRecord;
import ru.olegcherednik.zip4jvm.model.extrafield.ExtendedTimestampExtraFieldRecord;
import ru.olegcherednik.zip4jvm.model.extrafield.InfoZipNewUnixExtraFieldRecord;
import ru.olegcherednik.zip4jvm.model.extrafield.InfoZipOldUnixExtraFieldRecord;
import ru.olegcherednik.zip4jvm.model.extrafield.NtfsTimestampExtraFieldRecord;
import ru.olegcherednik.zip4jvm.view.BaseView;

import java.io.PrintStream;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Function;

/**
 * @author Oleg Cherednik
 * @since 16.10.2019
 */
public final class ExtraFieldView extends BaseView {

    private final ExtraField extraField;
    private final ExtraFieldBlock block;
    private final GeneralPurposeFlag generalPurposeFlag;

    private final Function<ExtraField.Record, ExtraFieldRecordView<?>> createView = record -> {
        if (record instanceof NtfsTimestampExtraFieldRecord)
            return createView((NtfsTimestampExtraFieldRecord)record);
        if (record instanceof InfoZipOldUnixExtraFieldRecord)
            return createView((InfoZipOldUnixExtraFieldRecord)record);
        if (record instanceof InfoZipNewUnixExtraFieldRecord)
            return createView((InfoZipNewUnixExtraFieldRecord)record);
        if (record instanceof ExtendedTimestampExtraFieldRecord)
            return createView((ExtendedTimestampExtraFieldRecord)record);
        if (record instanceof Zip64.ExtendedInfo)
            return createView((Zip64.ExtendedInfo)record);
        if (record instanceof AesExtraFieldRecord)
            return createView((AesExtraFieldRecord)record);
        if (record instanceof StrongEncryptionHeaderExtraFieldRecord)
            return createView((StrongEncryptionHeaderExtraFieldRecord)record);
        return createView(record);
    };

    public static Builder builder() {
        return new Builder();
    }

    private ExtraFieldView(Builder builder) {
        super(builder.offs, builder.columnWidth, builder.totalDisks);
        extraField = builder.extraField;
        block = builder.block;
        generalPurposeFlag = builder.generalPurposeFlag;
    }

    @Override
    public boolean print(PrintStream out) {
        Collection<ExtraField.Record> records = extraField.getRecords();
        records.forEach(record -> printRecord(out, record));
        return !records.isEmpty();
    }

    public void printLocation(PrintStream out) {
        printValueWithLocation(out, "extra field:", block, extraField.getTotalRecords());
    }

    public void printRecord(PrintStream out, ExtraField.Record record) {
        if (record != null && !record.isNull())
            getView(record).print(out);
    }

    public ExtraFieldRecordView<?> getView(ExtraField.Record record) {
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

    private UnknownExtraFieldRecordView createView(ExtraField.Record record) {
        Block block = this.block.getRecord(record.getSignature());
        return UnknownExtraFieldRecordView.builder()
                                          .record(record)
                                          .block(block)
                                          .data(block.getData())
                                          .position(offs, columnWidth, totalDisks).build();
    }

    public static final class Builder {

        private ExtraField extraField;
        private ExtraFieldBlock block;
        private GeneralPurposeFlag generalPurposeFlag;
        private int offs;
        private int columnWidth;
        private long totalDisks;

        public ExtraFieldView build() {
            Objects.requireNonNull(extraField, "'extraField' must not be null");
            Objects.requireNonNull(block, "'block' must not be null");
            return new ExtraFieldView(this);
        }

        public Builder extraField(ExtraField extraField) {
            this.extraField = extraField == null || extraField == ExtraField.NULL || extraField.getTotalRecords() == 0 ? null : extraField;
            return this;
        }

        public Builder block(ExtraFieldBlock block) {
            this.block = block;
            return this;
        }

        public Builder generalPurposeFlag(GeneralPurposeFlag generalPurposeFlag) {
            this.generalPurposeFlag = generalPurposeFlag;
            return this;
        }

        public Builder position(int offs, int columnWidth, long totalDisks) {
            this.offs = offs;
            this.columnWidth = columnWidth;
            this.totalDisks = totalDisks;
            return this;
        }
    }

}
