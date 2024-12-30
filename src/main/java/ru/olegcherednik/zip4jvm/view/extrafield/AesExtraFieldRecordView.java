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

import ru.olegcherednik.zip4jvm.model.CompressionMethod;
import ru.olegcherednik.zip4jvm.model.GeneralPurposeFlag;
import ru.olegcherednik.zip4jvm.model.extrafield.records.AesExtraFieldRecord;
import ru.olegcherednik.zip4jvm.view.BaseView;
import ru.olegcherednik.zip4jvm.view.CompressionMethodView;

/**
 * @author Oleg Cherednik
 * @since 26.10.2019
 */
final class AesExtraFieldRecordView extends ExtraFieldRecordView<AesExtraFieldRecord> {

    public static Builder<AesExtraFieldRecord, AesExtraFieldRecordView> builder() {
        return new Builder<>(AesExtraFieldRecordView::new);
    }

    private AesExtraFieldRecordView(Builder<AesExtraFieldRecord, AesExtraFieldRecordView> builder) {
        super(builder, (record, view, out) -> {
            view.printLine(out,
                           "  Encryption Tag Version:",
                           String.format("%s-%d", record.getVendor(), record.getVersion().getNumber()));
            view.printLine(out, "  Encryption Key Bits:", record.getStrength().getSize());
            compressionMethodView(record, view, builder).printTextInfo(out);
        });
    }

    private static CompressionMethodView compressionMethodView(
            AesExtraFieldRecord record,
            BaseView view,
            Builder<AesExtraFieldRecord, AesExtraFieldRecordView> builder) {
        CompressionMethod compressionMethod = record.getCompressionMethod();
        GeneralPurposeFlag generalPurposeFlag = builder.getGeneralPurposeFlag();
        int offs = view.getOffs() + 2;
        int columnWidth = view.getColumnWidth();
        return new CompressionMethodView(compressionMethod, generalPurposeFlag, offs, columnWidth);
    }

}
