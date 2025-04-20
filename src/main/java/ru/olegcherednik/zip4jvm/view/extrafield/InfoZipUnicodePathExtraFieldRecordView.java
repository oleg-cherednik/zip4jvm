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

import ru.olegcherednik.zip4jvm.model.extrafield.records.InfoZipUnicodePathExtraFieldRecord;
import ru.olegcherednik.zip4jvm.view.BaseView;
import ru.olegcherednik.zip4jvm.view.ByteArrayHexView;

import java.io.PrintStream;

/**
 * @author Oleg Cherednik
 * @since 16.04.2025
 */
final class InfoZipUnicodePathExtraFieldRecordView extends ExtraFieldRecordView<InfoZipUnicodePathExtraFieldRecord> {

    public static Builder<InfoZipUnicodePathExtraFieldRecord, InfoZipUnicodePathExtraFieldRecordView> builder() {
        return new Builder<>(InfoZipUnicodePathExtraFieldRecordView::new);
    }

    @SuppressWarnings("PMD.UseDiamondOperator")
    private InfoZipUnicodePathExtraFieldRecordView(
            Builder<InfoZipUnicodePathExtraFieldRecord, InfoZipUnicodePathExtraFieldRecordView> builder) {
        super(builder, new PrintConsumer<InfoZipUnicodePathExtraFieldRecord, BaseView>() {
            @Override
            public void print(InfoZipUnicodePathExtraFieldRecord record, BaseView view, PrintStream out) {
                InfoZipUnicodePathExtraFieldRecord.Payload payload = record.getPayload();

                if (payload instanceof InfoZipUnicodePathExtraFieldRecord.VersionOnePayload)
                    print((InfoZipUnicodePathExtraFieldRecord.VersionOnePayload) record.getPayload(), view, out);
                else if (payload instanceof InfoZipUnicodePathExtraFieldRecord.UnknownPayload)
                    print((InfoZipUnicodePathExtraFieldRecord.UnknownPayload) record.getPayload(), view, out);

                // TODO add final else
            }

            private void print(InfoZipUnicodePathExtraFieldRecord.VersionOnePayload payload,
                               BaseView view,
                               PrintStream out) {
                view.printLine(out, "  version:", String.valueOf(payload.getVersion()));

                view.printLine(out, "  NameCRC32:", String.format("0x%08X", payload.getCrc32()));
                view.printLine(out, "  UnicodeName:", payload.getName());
            }

            private void print(InfoZipUnicodePathExtraFieldRecord.UnknownPayload payload,
                               BaseView view,
                               PrintStream out) {
                view.printLine(out, "  version:", String.format("%d (unknown)", payload.getVersion()));
                new ByteArrayHexView(payload.getData(), view.getOffs(), view.getColumnWidth()).printTextInfo(out);
            }
        });
    }
}

