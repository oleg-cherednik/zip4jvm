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
import ru.olegcherednik.zip4jvm.model.extrafield.records.InfoZipOldUnixExtraFieldRecord;
import ru.olegcherednik.zip4jvm.utils.ZipUtils;

import lombok.Builder;

import java.io.PrintStream;

import static ru.olegcherednik.zip4jvm.model.extrafield.PkwareExtraField.NO_DATA;

/**
 * @author Oleg Cherednik
 * @since 26.10.2019
 */
final class InfoZipOldUnixExtraFieldRecordView extends ExtraFieldRecordView<InfoZipOldUnixExtraFieldRecord> {

    @Builder
    InfoZipOldUnixExtraFieldRecordView(int offs, int columnWidth, long totalDisks,
                                       InfoZipOldUnixExtraFieldRecord record, Block block) {
        super(offs, columnWidth, totalDisks, record, block);
    }

    // ---------- ExtraFieldRecordView ----------

    @Override
    public void printRecord(PrintStream out) {
        printLastModifiedDate(out);
        printLastAccessedDate(out);
        printUserIdentifier(out);
        printGroupIdentifier(out);
    }

    // ----------

    private void printLastModifiedDate(PrintStream out) {
        printLine(out, "  Last Modified Date:", ZipUtils.utcDateTime(record.getLastModificationTime()));
    }

    private void printLastAccessedDate(PrintStream out) {
        printLine(out, "  Last Accessed Date:", ZipUtils.utcDateTime(record.getLastAccessTime()));
    }

    private void printUserIdentifier(PrintStream out) {
        if (record.getUid() != NO_DATA)
            printLine(out, "  User identifier (UID):", String.valueOf(record.getUid()));
    }

    private void printGroupIdentifier(PrintStream out) {
        if (record.getGid() != NO_DATA)
            printLine(out, "  Group Identifier (GID):", String.valueOf(record.getGid()));
    }

}
