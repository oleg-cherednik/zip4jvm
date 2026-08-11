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
import ru.olegcherednik.zip4jvm.model.extrafield.records.NtfsTimestampExtraFieldRecord;
import ru.olegcherednik.zip4jvm.utils.ZipUtils;
import ru.olegcherednik.zip4jvm.view.ByteArrayHexView;
import ru.olegcherednik.zip4jvm.view.out.Out;

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
    protected void printRecord(Out out) {
        printTags(out);
    }

    // ----------

    private void printTags(Out out) {
        printLine(out, "  - total tags:", String.valueOf(record.getTags().size()));

        for (NtfsTimestampExtraFieldRecord.Tag tag : record.getTags()) {
            printOneTag(tag, out);
            printUnknownTag(tag, out);
            // TODO tag could be unknown for view
        }
    }

    private void printOneTag(NtfsTimestampExtraFieldRecord.Tag tag, Out out) {
        if (!(tag instanceof NtfsTimestampExtraFieldRecord.OneTag))
            return;

        NtfsTimestampExtraFieldRecord.OneTag oneTag = (NtfsTimestampExtraFieldRecord.OneTag) tag;

        printSize(out, String.format("  (0x%04X) Tag1:", oneTag.getSignature()), oneTag.getSize());
        printLine(out, "    Creation Date:", ZipUtils.utcDateTime(oneTag.getCreationTime()));
        printLine(out, "    Last Modified Date:", ZipUtils.utcDateTime(oneTag.getLastModificationTime()));
        printLine(out, "    Last Accessed Date:", ZipUtils.utcDateTime(oneTag.getLastAccessTime()));
    }

    private void printUnknownTag(NtfsTimestampExtraFieldRecord.Tag tag, Out out) {
        if (!(tag instanceof NtfsTimestampExtraFieldRecord.UnknownTag))
            return;

        NtfsTimestampExtraFieldRecord.UnknownTag unknownTag = (NtfsTimestampExtraFieldRecord.UnknownTag) tag;

        printSize(out, String.format("  (0x%04X) Unknown Tag:", tag.getSignature()), tag.getSize());
        new ByteArrayHexView(unknownTag.getData(), offs, columnWidth).printTextInfo(out);
    }

}
