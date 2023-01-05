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

import ru.olegcherednik.zip4jvm.model.extrafield.records.NtfsTimestampExtraFieldRecord;
import ru.olegcherednik.zip4jvm.utils.ZipUtils;
import ru.olegcherednik.zip4jvm.view.ByteArrayHexView;
import ru.olegcherednik.zip4jvm.view.BaseView;

import java.io.PrintStream;

/**
 * @author Oleg Cherednik
 * @since 26.10.2019
 */
final class NtfsTimestampExtraFieldRecordView extends ExtraFieldRecordView<NtfsTimestampExtraFieldRecord> {

    public static Builder<NtfsTimestampExtraFieldRecord, NtfsTimestampExtraFieldRecordView> builder() {
        return new Builder<>(NtfsTimestampExtraFieldRecordView::new);
    }

    private NtfsTimestampExtraFieldRecordView(Builder<NtfsTimestampExtraFieldRecord, NtfsTimestampExtraFieldRecordView> builder) {
        super(builder, new PrintConsumer<NtfsTimestampExtraFieldRecord, BaseView>() {
            @Override
            public void print(NtfsTimestampExtraFieldRecord record, BaseView view, PrintStream out) {
                view.printLine(out, "  - total tags:", String.valueOf(record.getTags().size()));

                for (NtfsTimestampExtraFieldRecord.Tag tag : record.getTags()) {
                    if (tag instanceof NtfsTimestampExtraFieldRecord.OneTag)
                        print((NtfsTimestampExtraFieldRecord.OneTag)tag, view, out);
                    else if (tag instanceof NtfsTimestampExtraFieldRecord.UnknownTag)
                        print((NtfsTimestampExtraFieldRecord.UnknownTag)tag, view, out);
                    // TODO tag could be unknown for view
                }
            }

            private void print(NtfsTimestampExtraFieldRecord.OneTag tag, BaseView view, PrintStream out) {
                view.printLine(out, String.format("  (0x%04X) Tag1:", tag.getSignature()), String.format("%d bytes", tag.getSize()));
                view.printLine(out, "    Creation Date:", ZipUtils.utcDateTime(tag.getCreationTime()));
                view.printLine(out, "    Last Modified Date:", ZipUtils.utcDateTime(tag.getLastModificationTime()));
                view.printLine(out, "    Last Accessed Date:", ZipUtils.utcDateTime(tag.getLastAccessTime()));
            }

            private void print(NtfsTimestampExtraFieldRecord.UnknownTag tag, BaseView view, PrintStream out) {
                view.printLine(out, String.format("  (0x%04X) Unknown Tag:", tag.getSignature()), String.format("%d bytes", tag.getSize()));
                new ByteArrayHexView(tag.getData(), view.getOffs(), view.getColumnWidth()).printTextInfo(out);
            }
        });
    }

}
