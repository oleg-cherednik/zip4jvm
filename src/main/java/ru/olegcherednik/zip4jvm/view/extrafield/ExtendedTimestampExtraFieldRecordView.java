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
import ru.olegcherednik.zip4jvm.model.extrafield.records.ExtendedTimestampExtraFieldRecord;
import ru.olegcherednik.zip4jvm.utils.ZipUtils;
import ru.olegcherednik.zip4jvm.view.PrintStreamDecorator;

import lombok.Builder;

/**
 * @author Oleg Cherednik
 * @since 26.10.2019
 */
final class ExtendedTimestampExtraFieldRecordView extends ExtraFieldRecordView<ExtendedTimestampExtraFieldRecord> {

    @Builder
    ExtendedTimestampExtraFieldRecordView(int offs, int columnWidth, long totalDisks,
                                          ExtendedTimestampExtraFieldRecord record, Block block) {
        super(offs, columnWidth, totalDisks, record, block);
    }

    // ---------- ExtraFieldRecordView ----------

    @Override
    public void printRecord(PrintStreamDecorator out) {
        printLastModifiedDate(out);
        printLastAccessedDate(out);
        printCreationDate(out);
    }

    // ----------

    private void printLastModifiedDate(PrintStreamDecorator out) {
        if (record.getFlag().isLastModificationTime() && record.getLastModificationTime() >= 0)
            printLine(out, "  Last Modified Date:", ZipUtils.utcDateTime(record.getLastModificationTime()));
    }

    private void printLastAccessedDate(PrintStreamDecorator out) {
        if (record.getFlag().isLastAccessTime() && record.getLastAccessTime() >= 0)
            printLine(out, "  Last Accessed Date:", ZipUtils.utcDateTime(record.getLastAccessTime()));
    }

    private void printCreationDate(PrintStreamDecorator out) {
        if (record.getFlag().isCreationTime() && record.getCreationTime() >= 0)
            printLine(out, "  Creation Date:", ZipUtils.utcDateTime(record.getCreationTime()));
    }

}
