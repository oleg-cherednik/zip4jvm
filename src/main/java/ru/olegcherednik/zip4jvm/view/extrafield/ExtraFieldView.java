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
import ru.olegcherednik.zip4jvm.model.extrafield.records.AndroidAlignmentExtraFieldRecord;
import ru.olegcherednik.zip4jvm.model.extrafield.records.ExtendedTimestampExtraFieldRecord;
import ru.olegcherednik.zip4jvm.model.extrafield.records.InfoZipNewUnixExtraFieldRecord;
import ru.olegcherednik.zip4jvm.model.extrafield.records.InfoZipOldUnixExtraFieldRecord;
import ru.olegcherednik.zip4jvm.model.extrafield.records.InfoZipUnicodeCommentExtraFieldRecord;
import ru.olegcherednik.zip4jvm.model.extrafield.records.InfoZipUnicodePathExtraFieldRecord;
import ru.olegcherednik.zip4jvm.model.extrafield.records.NtfsTimestampExtraFieldRecord;
import ru.olegcherednik.zip4jvm.model.extrafield.records.StrongEncryptionHeaderExtraFieldRecord;
import ru.olegcherednik.zip4jvm.view.BaseView;

import java.io.PrintStream;
import java.util.Optional;
import java.util.Set;

/**
 * @author Oleg Cherednik
 * @since 16.10.2019
 */
public final class ExtraFieldView extends BaseView {

    private final PkwareExtraField extraField;
    private final ExtraFieldBlock block;
    private final GeneralPurposeFlag generalPurposeFlag;

    public ExtraFieldView(int offs,
                          int columnWidth,
                          long totalDisks,
                          PkwareExtraField extraField,
                          ExtraFieldBlock block,
                          GeneralPurposeFlag generalPurposeFlag) {
        super(offs, columnWidth, totalDisks);
        this.extraField = extraField;
        this.block = block;
        this.generalPurposeFlag = generalPurposeFlag;
    }

    // ---------- View ----------

    @Override
    public boolean printTextInfo(PrintStream out) {
        Set<Integer> signatures = block.getSignatures();

        if (signatures.isEmpty())
            return false;

        signatures.stream()
                  .map(extraField::getRecord)
                  .forEach(record -> printRecord(out, record));

        return true;
    }

    // ----------

    public void printLocation(PrintStream out) {
        printValueWithLocation(out, "extra field:", block, extraField.getTotalRecords());
    }

    public void printRecord(PrintStream out, PkwareExtraField.Record record) {
        getView(record).ifPresent(view -> view.printTextInfo(out));
    }

    public Optional<ExtraFieldRecordView<?>> getView(PkwareExtraField.Record record) {
        if (record == null || record.isNull())
            return Optional.empty();

        if (record instanceof NtfsTimestampExtraFieldRecord)
            return Optional.of(createView((NtfsTimestampExtraFieldRecord) record));
        if (record instanceof InfoZipUnicodeCommentExtraFieldRecord)
            return Optional.of(createView((InfoZipUnicodeCommentExtraFieldRecord) record));
        if (record instanceof InfoZipUnicodePathExtraFieldRecord)
            return Optional.of(createView((InfoZipUnicodePathExtraFieldRecord) record));
        if (record instanceof InfoZipOldUnixExtraFieldRecord)
            return Optional.of(createView((InfoZipOldUnixExtraFieldRecord) record));
        if (record instanceof InfoZipNewUnixExtraFieldRecord)
            return Optional.of(createView((InfoZipNewUnixExtraFieldRecord) record));
        if (record instanceof ExtendedTimestampExtraFieldRecord)
            return Optional.of(createView((ExtendedTimestampExtraFieldRecord) record));
        if (record instanceof Zip64.ExtendedInfo)
            return Optional.of(createView((Zip64.ExtendedInfo) record));
        if (record instanceof AesExtraFieldRecord)
            return Optional.of(createView((AesExtraFieldRecord) record));
        if (record instanceof StrongEncryptionHeaderExtraFieldRecord)
            return Optional.of(createView((StrongEncryptionHeaderExtraFieldRecord) record));
        if (record instanceof AndroidAlignmentExtraFieldRecord)
            return Optional.of(createView((AndroidAlignmentExtraFieldRecord) record));

        return Optional.of(createView(record));
    }

    private NtfsTimestampExtraFieldRecordView createView(NtfsTimestampExtraFieldRecord record) {
        return NtfsTimestampExtraFieldRecordView.builder()
                                                .offs(offs)
                                                .columnWidth(columnWidth)
                                                .totalDisks(totalDisks)
                                                .record(record)
                                                .block(block.getRecord(record.getSignature())).build();
    }

    private InfoZipUnicodeCommentExtraFieldRecordView createView(InfoZipUnicodeCommentExtraFieldRecord record) {
        return InfoZipUnicodeCommentExtraFieldRecordView.builder()
                                                        .offs(offs)
                                                        .columnWidth(columnWidth)
                                                        .totalDisks(totalDisks)
                                                        .record(record)
                                                        .block(block.getRecord(record.getSignature())).build();
    }

    private InfoZipUnicodePathExtraFieldRecordView createView(InfoZipUnicodePathExtraFieldRecord record) {
        return InfoZipUnicodePathExtraFieldRecordView.builder()
                                                     .offs(offs)
                                                     .columnWidth(columnWidth)
                                                     .totalDisks(totalDisks)
                                                     .record(record)
                                                     .block(block.getRecord(record.getSignature())).build();
    }

    private InfoZipOldUnixExtraFieldRecordView createView(InfoZipOldUnixExtraFieldRecord record) {
        return InfoZipOldUnixExtraFieldRecordView.builder()
                                                 .offs(offs)
                                                 .columnWidth(columnWidth)
                                                 .totalDisks(totalDisks)
                                                 .record(record)
                                                 .block(block.getRecord(record.getSignature())).build();
    }

    private InfoZipNewUnixExtraFieldRecordView createView(InfoZipNewUnixExtraFieldRecord record) {
        return InfoZipNewUnixExtraFieldRecordView.builder()
                                                 .offs(offs)
                                                 .columnWidth(columnWidth)
                                                 .totalDisks(totalDisks)
                                                 .record(record)
                                                 .block(block.getRecord(record.getSignature())).build();
    }

    private ExtendedTimestampExtraFieldRecordView createView(ExtendedTimestampExtraFieldRecord record) {
        return ExtendedTimestampExtraFieldRecordView.builder()
                                                    .offs(offs)
                                                    .columnWidth(columnWidth)
                                                    .totalDisks(totalDisks)
                                                    .record(record)
                                                    .block(block.getRecord(record.getSignature())).build();
    }

    private Zip64ExtendedInfoView createView(Zip64.ExtendedInfo record) {
        return Zip64ExtendedInfoView.builder()
                                    .offs(offs)
                                    .columnWidth(columnWidth)
                                    .totalDisks(totalDisks)
                                    .record(record)
                                    .block(block.getRecord(record.getSignature())).build();
    }

    private AesExtraFieldRecordView createView(AesExtraFieldRecord record) {
        return AesExtraFieldRecordView.builder()
                                      .offs(offs)
                                      .columnWidth(columnWidth)
                                      .totalDisks(totalDisks)
                                      .record(record)
                                      .block(block.getRecord(record.getSignature()))
                                      .generalPurposeFlag(generalPurposeFlag).build();
    }

    private StrongEncryptionHeaderExtraFieldRecordView createView(StrongEncryptionHeaderExtraFieldRecord record) {
        return StrongEncryptionHeaderExtraFieldRecordView.builder()
                                                         .offs(offs)
                                                         .columnWidth(columnWidth)
                                                         .totalDisks(totalDisks)
                                                         .record(record)
                                                         .block(block.getRecord(record.getSignature())).build();
    }

    private AndroidAlignmentExtraFieldRecordView createView(AndroidAlignmentExtraFieldRecord record) {
        return AndroidAlignmentExtraFieldRecordView.builder()
                                                   .offs(offs)
                                                   .columnWidth(columnWidth)
                                                   .totalDisks(totalDisks)
                                                   .record(record)
                                                   .block(block.getRecord(record.getSignature())).build();
    }

    private UnknownExtraFieldRecordView createView(PkwareExtraField.Record record) {
        return UnknownExtraFieldRecordView.builder()
                                          .offs(offs)
                                          .columnWidth(columnWidth)
                                          .totalDisks(totalDisks)
                                          .record(record)
                                          .block(block.getRecord(record.getSignature())).build();
    }

}
