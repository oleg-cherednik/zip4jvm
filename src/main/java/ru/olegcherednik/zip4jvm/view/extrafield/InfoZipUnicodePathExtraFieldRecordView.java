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

import ru.olegcherednik.zip4jvm.model.block.Block;
import ru.olegcherednik.zip4jvm.model.charset.Charsets;
import ru.olegcherednik.zip4jvm.model.extrafield.records.InfoZipUnicodePathExtraFieldRecord;
import ru.olegcherednik.zip4jvm.view.ByteArrayHexView;
import ru.olegcherednik.zip4jvm.view.StringHexView;
import ru.olegcherednik.zip4jvm.view.out.Out;

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
    public void printRecord(Out out) {
        if (record.getPayload() instanceof InfoZipUnicodePathExtraFieldRecord.VersionOnePayload)
            printVersionOnePayload((InfoZipUnicodePathExtraFieldRecord.VersionOnePayload) record.getPayload(), out);
        else if (record.getPayload() instanceof InfoZipUnicodePathExtraFieldRecord.UnknownPayload)
            printUnknownPayload((InfoZipUnicodePathExtraFieldRecord.UnknownPayload) record.getPayload(), out);
    }

    // ----------

    private void printVersionOnePayload(InfoZipUnicodePathExtraFieldRecord.VersionOnePayload payload, Out out) {
        printLine(out, "  version:", payload.getVersion());
        printCrc32(out, "  NameCRC32:", payload.getCrc32());
        new StringHexView(payload.getName(), Charsets.UTF_8, offs, columnWidth).printTextInfo(out);
    }

    private void printUnknownPayload(InfoZipUnicodePathExtraFieldRecord.UnknownPayload payload, Out out) {
        printLine(out, "  version:", String.format("%d (unknown)", payload.getVersion()));
        new ByteArrayHexView(payload.getData(), offs, columnWidth).printTextInfo(out);
    }

}

