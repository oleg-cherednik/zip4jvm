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
import ru.olegcherednik.zip4jvm.model.extrafield.records.NtfsTimestampExtraFieldRecord;
import ru.olegcherednik.zip4jvm.utils.ZipUtils;
import ru.olegcherednik.zip4jvm.view.ByteArrayHexView;
import ru.olegcherednik.zip4jvm.view.PrintStreamDecorator;

import lombok.Builder;

/**
 * @author Oleg Cherednik
 * @since 26.10.2019
 */
final class NtfsTimestampExtraFieldRecordView extends ExtraFieldRecordView<NtfsTimestampExtraFieldRecord> {

    @Builder
    NtfsTimestampExtraFieldRecordView(int offs, int columnWidth, long totalDisks,
                                      NtfsTimestampExtraFieldRecord record, Block block) {
        super(offs, columnWidth, totalDisks, record, block);
    }

    // ---------- ExtraFieldRecordView ----------

    @Override
    public void printRecord(PrintStreamDecorator out) {
        printTags(out);
    }

    // ----------

    private void printTags(PrintStreamDecorator out) {
        printLine(out, "  - total tags:", String.valueOf(record.getTags().size()));

        for (NtfsTimestampExtraFieldRecord.Tag tag : record.getTags()) {
            printOneTag(tag, out);
            printUnknownTag(tag, out);
            // TODO tag could be unknown for view
        }
    }

    private void printOneTag(NtfsTimestampExtraFieldRecord.Tag tag, PrintStreamDecorator out) {
        if (!(tag instanceof NtfsTimestampExtraFieldRecord.OneTag))
            return;

        NtfsTimestampExtraFieldRecord.OneTag oneTag = (NtfsTimestampExtraFieldRecord.OneTag) tag;

        printLine(out,
                  String.format("  (0x%04X) Tag1:", oneTag.getSignature()),
                  String.format("%d bytes", oneTag.getSize()));
        printLine(out, "    Creation Date:", ZipUtils.utcDateTime(oneTag.getCreationTime()));
        printLine(out, "    Last Modified Date:", ZipUtils.utcDateTime(oneTag.getLastModificationTime()));
        printLine(out, "    Last Accessed Date:", ZipUtils.utcDateTime(oneTag.getLastAccessTime()));
    }

    private void printUnknownTag(NtfsTimestampExtraFieldRecord.Tag tag, PrintStreamDecorator out) {
        if (!(tag instanceof NtfsTimestampExtraFieldRecord.UnknownTag))
            return;

        NtfsTimestampExtraFieldRecord.UnknownTag unknownTag = (NtfsTimestampExtraFieldRecord.UnknownTag) tag;

        printLine(out,
                  String.format("  (0x%04X) Unknown Tag:", tag.getSignature()),
                  String.format("%d bytes", tag.getSize()));
        new ByteArrayHexView(unknownTag.getData(), offs, columnWidth).printTextInfo(out);
    }

}
