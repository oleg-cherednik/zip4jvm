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

import ru.olegcherednik.zip4jvm.model.block.Block;
import ru.olegcherednik.zip4jvm.model.charset.Charsets;
import ru.olegcherednik.zip4jvm.model.extrafield.records.InfoZipUnicodePathExtraFieldRecord;
import ru.olegcherednik.zip4jvm.view.ByteArrayHexView;
import ru.olegcherednik.zip4jvm.view.PrintStreamDecorator;
import ru.olegcherednik.zip4jvm.view.StringHexView;

import lombok.Builder;

/**
 * @author Oleg Cherednik
 * @since 16.04.2025
 */
final class InfoZipUnicodePathExtraFieldRecordView extends ExtraFieldRecordView<InfoZipUnicodePathExtraFieldRecord> {

    @Builder
    InfoZipUnicodePathExtraFieldRecordView(int offs, int columnWidth, long totalDisks,
                                           InfoZipUnicodePathExtraFieldRecord record, Block block) {
        super(offs, columnWidth, totalDisks, record, block);
    }

    // ---------- ExtraFieldRecordView ----------

    @Override
    public void printRecord(PrintStreamDecorator out) {
        printVersionOnePayload(out);
        printUnknownPayload(out);
    }

    // ----------

    private void printVersionOnePayload(PrintStreamDecorator out) {
        if (!(record.getPayload() instanceof InfoZipUnicodePathExtraFieldRecord.VersionOnePayload))
            return;

        InfoZipUnicodePathExtraFieldRecord.VersionOnePayload payload = record.getPayload();
        printLine(out, "  version:", String.valueOf(payload.getVersion()));
        printLine(out, "  NameCRC32:", String.format("0x%08X", payload.getCrc32()));
        new StringHexView(payload.getName(), Charsets.UTF_8, offs, columnWidth).printTextInfo(out);
    }

    private void printUnknownPayload(PrintStreamDecorator out) {
        if (!(record.getPayload() instanceof InfoZipUnicodePathExtraFieldRecord.UnknownPayload))
            return;

        InfoZipUnicodePathExtraFieldRecord.UnknownPayload payload = record.getPayload();
        printLine(out, "  version:", String.format("%d (unknown)", payload.getVersion()));
        new ByteArrayHexView(payload.getData(), offs, columnWidth).printTextInfo(out);
    }
}

