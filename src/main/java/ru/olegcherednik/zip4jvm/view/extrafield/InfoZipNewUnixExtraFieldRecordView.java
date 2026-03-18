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
import ru.olegcherednik.zip4jvm.model.extrafield.records.InfoZipNewUnixExtraFieldRecord;
import ru.olegcherednik.zip4jvm.view.ByteArrayHexView;
import ru.olegcherednik.zip4jvm.view.out.Out;

import lombok.Builder;
import org.apache.commons.lang3.StringUtils;

/**
 * @author Oleg Cherednik
 * @since 26.10.2019
 */
final class InfoZipNewUnixExtraFieldRecordView extends ExtraFieldRecordView<InfoZipNewUnixExtraFieldRecord> {

    @Builder
    InfoZipNewUnixExtraFieldRecordView(int offs, int columnWidth, long totalDisks,
                                       InfoZipNewUnixExtraFieldRecord record, Block block) {
        super(offs, columnWidth, totalDisks, record, block);
    }

    // ---------- ExtraFieldRecordView ----------

    @Override
    public void printRecord(Out out) {
        printVersionOnePayload(out);
        printUnknownPayload(out);
    }

    // ----------

    private void printVersionOnePayload(Out out) {
        if (!(record.getPayload() instanceof InfoZipNewUnixExtraFieldRecord.VersionOnePayload))
            return;

        InfoZipNewUnixExtraFieldRecord.VersionOnePayload payload = record.getPayload();
        printLine(out, "  version:", String.valueOf(payload.getVersion()));

        if (StringUtils.isNotBlank(payload.getUid()))
            printLine(out, "  User identifier (UID):", payload.getUid());
        if (StringUtils.isNotBlank(payload.getGid()))
            printLine(out, "  Group Identifier (GID):", payload.getGid());
    }

    private void printUnknownPayload(Out out) {
        if (!(record.getPayload() instanceof InfoZipNewUnixExtraFieldRecord.UnknownPayload))
            return;

        InfoZipNewUnixExtraFieldRecord.UnknownPayload payload = record.getPayload();
        printLine(out, "  version:", String.format("%d (unknown)", payload.getVersion()));
        new ByteArrayHexView(payload.getData(), offs, columnWidth).printTextInfo(out);
    }

}

