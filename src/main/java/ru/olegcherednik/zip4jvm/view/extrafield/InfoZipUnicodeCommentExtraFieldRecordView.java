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
import ru.olegcherednik.zip4jvm.model.extrafield.records.InfoZipUnicodeCommentExtraFieldRecord;
import ru.olegcherednik.zip4jvm.view.ByteArrayHexView;
import ru.olegcherednik.zip4jvm.view.StringHexView;
import ru.olegcherednik.zip4jvm.view.out.Out;

import lombok.Builder;

/**
 * @author Oleg Cherednik
 * @since 20.04.2025
 */
final class InfoZipUnicodeCommentExtraFieldRecordView
        extends ExtraFieldRecordView<InfoZipUnicodeCommentExtraFieldRecord> {

    @Builder
    InfoZipUnicodeCommentExtraFieldRecordView(int offs, int columnWidth, long totalDisks,
                                              InfoZipUnicodeCommentExtraFieldRecord record, Block block) {
        super(offs, columnWidth, totalDisks, record, block);
    }

    // ---------- ExtraFieldRecordView ----------

    @Override
    protected void printRecord(Out out) {
        if (record.getPayload() instanceof InfoZipUnicodeCommentExtraFieldRecord.VersionOnePayload)
            printVersionOnePayload((InfoZipUnicodeCommentExtraFieldRecord.VersionOnePayload) record.getPayload(), out);
        else if (record.getPayload() instanceof InfoZipUnicodeCommentExtraFieldRecord.UnknownPayload)
            printUnknownPayload((InfoZipUnicodeCommentExtraFieldRecord.UnknownPayload) record.getPayload(), out);
    }

    // ----------

    private void printVersionOnePayload(InfoZipUnicodeCommentExtraFieldRecord.VersionOnePayload payload, Out out) {
        printLine(out, "  version:", payload.getVersion());
        printCrc32(out, "  ComCRC32:", payload.getCrc32());
        new StringHexView(payload.getComment(), Charsets.UTF_8, offs, columnWidth).printTextInfo(out);
    }

    private void printUnknownPayload(InfoZipUnicodeCommentExtraFieldRecord.UnknownPayload payload, Out out) {
        printLine(out, "  version:", String.format("%d (unknown)", payload.getVersion()));
        new ByteArrayHexView(payload.getData(), offs, columnWidth).printTextInfo(out);
    }
}

