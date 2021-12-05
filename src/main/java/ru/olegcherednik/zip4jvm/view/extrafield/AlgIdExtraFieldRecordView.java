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

import ru.olegcherednik.zip4jvm.crypto.strong.EncryptionAlgorithm;
import ru.olegcherednik.zip4jvm.crypto.strong.Flags;
import ru.olegcherednik.zip4jvm.model.extrafield.AlgIdExtraFieldRecord;
import ru.olegcherednik.zip4jvm.view.ByteArrayHexView;

/**
 * @author Oleg Cherednik
 * @since 16.02.2020
 */
final class AlgIdExtraFieldRecordView extends ExtraFieldRecordView<AlgIdExtraFieldRecord> {

    public static Builder<AlgIdExtraFieldRecord, AlgIdExtraFieldRecordView> builder() {
        return new Builder<>(AlgIdExtraFieldRecordView::new);
    }

    private AlgIdExtraFieldRecordView(Builder<AlgIdExtraFieldRecord, AlgIdExtraFieldRecordView> builder) {
        super(builder, (record, view, out) -> {
            EncryptionAlgorithm encryptionAlgorithm = record.getEncryptionAlgorithm();
            Flags flags = record.getFlags();

            view.printLine(out, "  format:", record.getFormat());
            view.printLine(out, String.format("  encryption algorithm (0x%04X):", encryptionAlgorithm.getCode()), encryptionAlgorithm.getTitle());
            view.printLine(out, "  encryption key bits:", record.getBitLength());
            view.printLine(out, String.format("  flags (0x%02X):", flags.getCode()), flags.getTitle());
            view.printLine(out, "  unknown data length:", String.format("%d bytes", record.getUnknown().length));
            new ByteArrayHexView(record.getUnknown(), view.getOffs() + 4, view.getColumnWidth()).print(out);
        });
    }

}
