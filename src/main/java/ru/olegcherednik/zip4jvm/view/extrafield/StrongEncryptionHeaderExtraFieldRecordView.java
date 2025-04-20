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
import ru.olegcherednik.zip4jvm.crypto.strong.Flag;
import ru.olegcherednik.zip4jvm.model.block.Block;
import ru.olegcherednik.zip4jvm.model.extrafield.records.StrongEncryptionHeaderExtraFieldRecord;
import ru.olegcherednik.zip4jvm.view.ByteArrayHexView;

import lombok.Builder;

import java.io.PrintStream;

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
    public void printRecord(PrintStream out) {
        printFormat(out);
        printEncryptionAlgorithm(out);
        printEncryptionKeyBits(out);
        printFlags(out);
        printEncryptionVariableData(out);
    }

    // ----------

    private void printFormat(PrintStream out) {
        printLine(out, "  format:", record.getFormat());
    }

    private void printEncryptionAlgorithm(PrintStream out) {
        EncryptionAlgorithm encryptionAlgorithm = record.getEncryptionAlgorithm();
        printLine(out,
                  String.format("  encryption algorithm (0x%04X):", encryptionAlgorithm.getCode()),
                  encryptionAlgorithm.getTitle());
    }

    private void printEncryptionKeyBits(PrintStream out) {
        printLine(out, "  encryption key bits:", record.getBitLength());
    }

    private void printFlags(PrintStream out) {
        Flag flag = record.getFlag();
        printLine(out, String.format("  flags (0x%02X):", flag.getCode()), flag.getTitle());
    }

    private void printEncryptionVariableData(PrintStream out) {
        printLine(out, "  encryption variable data:", String.format("%d bytes", record.getUnknown().length));
        new ByteArrayHexView(record.getUnknown(), offs + 4, columnWidth).printTextInfo(out);
    }

}
