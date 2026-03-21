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
import ru.olegcherednik.zip4jvm.view.out.Out;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * @author Oleg Cherednik
 * @since 16.10.2019
 */
public final class ExtraFieldView extends BaseView {

    private final PkwareExtraField extraField;
    private final ExtraFieldBlock block;
    private final GeneralPurposeFlag generalPurposeFlag;
    private final Map<Class<?>, Function<PkwareExtraField.Record, ExtraFieldRecordView<?>>> viewMap;

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
        viewMap = createViewMap();
    }

    private Map<Class<?>, Function<PkwareExtraField.Record, ExtraFieldRecordView<?>>> createViewMap() {
        Map<Class<?>, Function<PkwareExtraField.Record, ExtraFieldRecordView<?>>> map = new HashMap<>();

        map.put(NtfsTimestampExtraFieldRecord.class, this::ntfsTimestampView);
        map.put(InfoZipUnicodeCommentExtraFieldRecord.class, this::infoZipUnicodeCommentView);
        map.put(InfoZipUnicodePathExtraFieldRecord.class, this::infoZipUnicodePathView);
        map.put(InfoZipOldUnixExtraFieldRecord.class, this::infoZipOldUnixView);
        map.put(InfoZipNewUnixExtraFieldRecord.class, this::infoZipNewUnixView);
        map.put(ExtendedTimestampExtraFieldRecord.class, this::extendedTimestampView);
        map.put(Zip64.ExtendedInfo.class, this::zip64ExtendedInfoView);
        map.put(AesExtraFieldRecord.class, this::aesView);
        map.put(StrongEncryptionHeaderExtraFieldRecord.class, this::strongEncryptionHeaderView);
        map.put(AndroidAlignmentExtraFieldRecord.class, this::androidAlignmentView);

        return Collections.unmodifiableMap(map);
    }

    // ---------- View ----------

    @Override
    public void printTextInfo(Out out) {
        block.getSignatures().stream()
             .map(extraField::getRecord)
             .forEach(record -> printRecord(out, record));
    }

    // ----------

    public void printLocation(Out out) {
        printValueWithLocation(out, "extra field:", block, extraField.getTotalRecords());
    }

    public void printRecord(Out out, PkwareExtraField.Record record) {
        getView(record).ifPresent(view -> view.printTextInfo(out));
    }

    public Optional<ExtraFieldRecordView<?>> getView(PkwareExtraField.Record record) {
        if (record == null || record.isNull())
            return Optional.empty();

        Function<PkwareExtraField.Record, ExtraFieldRecordView<?>> func = viewMap.get(record.getClass());
        return func == null ? Optional.of(createView(record)) : Optional.of(func.apply(record));
    }

    private NtfsTimestampExtraFieldRecordView ntfsTimestampView(PkwareExtraField.Record record) {
        return NtfsTimestampExtraFieldRecordView.builder()
                                                .offs(offs)
                                                .columnWidth(columnWidth)
                                                .totalDisks(totalDisks)
                                                .record((NtfsTimestampExtraFieldRecord) record)
                                                .block(block.getRecord(record.getSignature())).build();
    }

    private InfoZipUnicodeCommentExtraFieldRecordView infoZipUnicodeCommentView(PkwareExtraField.Record record) {
        return InfoZipUnicodeCommentExtraFieldRecordView.builder()
                                                        .offs(offs)
                                                        .columnWidth(columnWidth)
                                                        .totalDisks(totalDisks)
                                                        .record((InfoZipUnicodeCommentExtraFieldRecord) record)
                                                        .block(block.getRecord(record.getSignature())).build();
    }

    private InfoZipUnicodePathExtraFieldRecordView infoZipUnicodePathView(PkwareExtraField.Record record) {
        return InfoZipUnicodePathExtraFieldRecordView.builder()
                                                     .offs(offs)
                                                     .columnWidth(columnWidth)
                                                     .totalDisks(totalDisks)
                                                     .record((InfoZipUnicodePathExtraFieldRecord) record)
                                                     .block(block.getRecord(record.getSignature())).build();
    }

    private InfoZipOldUnixExtraFieldRecordView infoZipOldUnixView(PkwareExtraField.Record record) {
        return InfoZipOldUnixExtraFieldRecordView.builder()
                                                 .offs(offs)
                                                 .columnWidth(columnWidth)
                                                 .totalDisks(totalDisks)
                                                 .record((InfoZipOldUnixExtraFieldRecord) record)
                                                 .block(block.getRecord(record.getSignature())).build();
    }

    private InfoZipNewUnixExtraFieldRecordView infoZipNewUnixView(PkwareExtraField.Record record) {
        return InfoZipNewUnixExtraFieldRecordView.builder()
                                                 .offs(offs)
                                                 .columnWidth(columnWidth)
                                                 .totalDisks(totalDisks)
                                                 .record((InfoZipNewUnixExtraFieldRecord) record)
                                                 .block(block.getRecord(record.getSignature())).build();
    }

    private ExtendedTimestampExtraFieldRecordView extendedTimestampView(PkwareExtraField.Record record) {
        return ExtendedTimestampExtraFieldRecordView.builder()
                                                    .offs(offs)
                                                    .columnWidth(columnWidth)
                                                    .totalDisks(totalDisks)
                                                    .record((ExtendedTimestampExtraFieldRecord) record)
                                                    .block(block.getRecord(record.getSignature())).build();
    }

    private Zip64ExtendedInfoView zip64ExtendedInfoView(PkwareExtraField.Record record) {
        return Zip64ExtendedInfoView.builder()
                                    .offs(offs)
                                    .columnWidth(columnWidth)
                                    .totalDisks(totalDisks)
                                    .record((Zip64.ExtendedInfo) record)
                                    .block(block.getRecord(record.getSignature())).build();
    }

    private AesExtraFieldRecordView aesView(PkwareExtraField.Record record) {
        return AesExtraFieldRecordView.builder()
                                      .offs(offs)
                                      .columnWidth(columnWidth)
                                      .totalDisks(totalDisks)
                                      .record((AesExtraFieldRecord) record)
                                      .block(block.getRecord(record.getSignature()))
                                      .generalPurposeFlag(generalPurposeFlag).build();
    }

    private StrongEncryptionHeaderExtraFieldRecordView strongEncryptionHeaderView(PkwareExtraField.Record record) {
        return StrongEncryptionHeaderExtraFieldRecordView.builder()
                                                         .offs(offs)
                                                         .columnWidth(columnWidth)
                                                         .totalDisks(totalDisks)
                                                         .record((StrongEncryptionHeaderExtraFieldRecord) record)
                                                         .block(block.getRecord(record.getSignature())).build();
    }

    private AndroidAlignmentExtraFieldRecordView androidAlignmentView(PkwareExtraField.Record record) {
        return AndroidAlignmentExtraFieldRecordView.builder()
                                                   .offs(offs)
                                                   .columnWidth(columnWidth)
                                                   .totalDisks(totalDisks)
                                                   .record((AndroidAlignmentExtraFieldRecord) record)
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
