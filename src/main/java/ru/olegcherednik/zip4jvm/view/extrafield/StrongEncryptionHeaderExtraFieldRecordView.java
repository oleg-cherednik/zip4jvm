/*
 * Copyright 2019 Oleg Cherednik (oleg.cherednik@gmail.com)
 *
 * Licensed under The Apache Software License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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
import ru.olegcherednik.zip4jvm.crypto.strong.Flag;
import ru.olegcherednik.zip4jvm.model.block.Block;
import ru.olegcherednik.zip4jvm.model.extrafield.records.StrongEncryptionHeaderExtraFieldRecord;
import ru.olegcherednik.zip4jvm.view.ByteArrayHexView;
import ru.olegcherednik.zip4jvm.view.out.Out;

import lombok.Builder;

/**
 * @author Oleg Cherednik
 * @since 16.02.2020
 */
final class StrongEncryptionHeaderExtraFieldRecordView
        extends ExtraFieldRecordView<StrongEncryptionHeaderExtraFieldRecord> {

    @Builder
    StrongEncryptionHeaderExtraFieldRecordView(int offs, int columnWidth, long totalDisks,
                                               StrongEncryptionHeaderExtraFieldRecord record, Block block) {
        super(offs, columnWidth, totalDisks, record, block);
    }

    // ---------- ExtraFieldRecordView ----------

    @Override
    protected void printRecord(Out out) {
        printFormat(out);
        printEncryptionAlgorithm(out);
        printEncryptionKeyBits(out);
        printFlags(out);
        printEncryptionVariableData(out);
    }

    // ----------

    private void printFormat(Out out) {
        printLine(out, "  format:", record.getFormat());
    }

    private void printEncryptionAlgorithm(Out out) {
        EncryptionAlgorithm encryptionAlgorithm = record.getEncryptionAlgorithm();
        printLine(out,
                  String.format("  encryption algorithm (0x%04X):", encryptionAlgorithm.getCode()),
                  encryptionAlgorithm.getTitle());
    }

    private void printEncryptionKeyBits(Out out) {
        printLine(out, "  encryption key bits:", record.getBitLength());
    }

    private void printFlags(Out out) {
        Flag flag = record.getFlag();
        printLine(out, String.format("  flags (0x%02X):", flag.getCode()), flag.getTitle());
    }

    private void printEncryptionVariableData(Out out) {
        printSize(out, "  encryption variable data:", record.getUnknown().length);
        new ByteArrayHexView(record.getUnknown(), offs + 4, columnWidth).printTextInfo(out);
    }

}
