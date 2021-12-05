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

import ru.olegcherednik.zip4jvm.model.extrafield.InfoZipOldUnixExtraFieldRecord;
import ru.olegcherednik.zip4jvm.utils.ZipUtils;

import static ru.olegcherednik.zip4jvm.model.ExtraField.NO_DATA;

/**
 * @author Oleg Cherednik
 * @since 26.10.2019
 */
final class InfoZipOldUnixExtraFieldRecordView extends ExtraFieldRecordView<InfoZipOldUnixExtraFieldRecord> {

    public static Builder<InfoZipOldUnixExtraFieldRecord, InfoZipOldUnixExtraFieldRecordView> builder() {
        return new Builder<>(InfoZipOldUnixExtraFieldRecordView::new);
    }

    private InfoZipOldUnixExtraFieldRecordView(Builder<InfoZipOldUnixExtraFieldRecord, InfoZipOldUnixExtraFieldRecordView> builder) {
        super(builder, (record, view, out) -> {
            view.printLine(out, "  Last Modified Date:", ZipUtils.utcDateTime(record.getLastModificationTime()));
            view.printLine(out, "  Last Accessed Date:", ZipUtils.utcDateTime(record.getLastAccessTime()));

            if (record.getUid() != NO_DATA)
                view.printLine(out, "  User identifier (UID):", String.valueOf(record.getUid()));
            if (record.getGid() != NO_DATA)
                view.printLine(out, "  Group Identifier (GID):", String.valueOf(record.getGid()));
        });
    }

}
