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
import ru.olegcherednik.zip4jvm.model.block.Block;
import ru.olegcherednik.zip4jvm.model.extrafield.records.AesExtraFieldRecord;
import ru.olegcherednik.zip4jvm.view.CompressionView;
import ru.olegcherednik.zip4jvm.view.out.Out;

import lombok.Builder;

/**
 * @author Oleg Cherednik
 * @since 26.10.2019
 */
final class AesExtraFieldRecordView extends ExtraFieldRecordView<AesExtraFieldRecord> {

    private final GeneralPurposeFlag generalPurposeFlag;

    @Builder
    AesExtraFieldRecordView(int offs, int columnWidth, long totalDisks,
                            AesExtraFieldRecord record, Block block,
                            GeneralPurposeFlag generalPurposeFlag) {
        super(offs, columnWidth, totalDisks, record, block);
        this.generalPurposeFlag = generalPurposeFlag;
    }

    // ---------- ExtraFieldRecordView ----------

    @Override
    public void printRecord(Out out) {
        printEncryptionTagVersion(out);
        printEncryptionKeyBits(out);
        printCompressionMethod(out);
    }

    // ----------

    private void printEncryptionTagVersion(Out out) {
        printLine(out,
                  "  Encryption Tag Version:",
                  String.format("%s-%d", record.getVendor(), record.getVersion().getCode()));
    }

    private void printEncryptionKeyBits(Out out) {
        printLine(out, "  Encryption Key Bits:", record.getStrength().getSize());
    }

    private void printCompressionMethod(Out out) {
        new CompressionView(record.getCompression(), generalPurposeFlag, offs + 2, columnWidth)
                .printTextInfo(out);
    }

}
