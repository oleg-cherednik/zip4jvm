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

import ru.olegcherednik.zip4jvm.model.extrafield.records.InfoZipNewUnixExtraFieldRecord;
import ru.olegcherednik.zip4jvm.view.BaseView;
import ru.olegcherednik.zip4jvm.view.ByteArrayHexView;

import org.apache.commons.lang3.StringUtils;

import java.io.PrintStream;

/**
 * @author Oleg Cherednik
 * @since 26.10.2019
 */
final class InfoZipNewUnixExtraFieldRecordView extends ExtraFieldRecordView<InfoZipNewUnixExtraFieldRecord> {

    public static Builder<InfoZipNewUnixExtraFieldRecord, InfoZipNewUnixExtraFieldRecordView> builder() {
        return new Builder<>(InfoZipNewUnixExtraFieldRecordView::new);
    }

    @SuppressWarnings("PMD.UseDiamondOperator")
    private InfoZipNewUnixExtraFieldRecordView(
            Builder<InfoZipNewUnixExtraFieldRecord, InfoZipNewUnixExtraFieldRecordView> builder) {
        super(builder, new PrintConsumer<InfoZipNewUnixExtraFieldRecord, BaseView>() {
            @Override
            public void print(InfoZipNewUnixExtraFieldRecord record, BaseView view, PrintStream out) {
                InfoZipNewUnixExtraFieldRecord.Payload payload = record.getPayload();

                if (payload instanceof InfoZipNewUnixExtraFieldRecord.VersionOnePayload)
                    print((InfoZipNewUnixExtraFieldRecord.VersionOnePayload) record.getPayload(), view, out);
                else if (payload instanceof InfoZipNewUnixExtraFieldRecord.VersionUnknownPayload)
                    print((InfoZipNewUnixExtraFieldRecord.VersionUnknownPayload) record.getPayload(), view, out);

                // TODO add final else
            }

            private void print(InfoZipNewUnixExtraFieldRecord.VersionOnePayload payload,
                               BaseView view,
                               PrintStream out) {
                view.printLine(out, "  version:", String.valueOf(payload.getVersion()));

                if (StringUtils.isNotBlank(payload.getUid()))
                    view.printLine(out, "  User identifier (UID):", payload.getUid());
                if (StringUtils.isNotBlank(payload.getGid()))
                    view.printLine(out, "  Group Identifier (GID):", payload.getGid());
            }

            private void print(InfoZipNewUnixExtraFieldRecord.VersionUnknownPayload payload,
                               BaseView view,
                               PrintStream out) {
                view.printLine(out, "  version:", String.format("%d (unknown)", payload.getVersion()));
                new ByteArrayHexView(payload.getData(), view.getOffs(), view.getColumnWidth()).printTextInfo(out);
            }
        });
    }
}

