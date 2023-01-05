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

import ru.olegcherednik.zip4jvm.model.extrafield.records.ExtendedTimestampExtraFieldRecord;
import ru.olegcherednik.zip4jvm.utils.ZipUtils;

/**
 * @author Oleg Cherednik
 * @since 26.10.2019
 */
final class ExtendedTimestampExtraFieldRecordView extends ExtraFieldRecordView<ExtendedTimestampExtraFieldRecord> {

    public static Builder<ExtendedTimestampExtraFieldRecord, ExtendedTimestampExtraFieldRecordView> builder() {
        return new Builder<>(ExtendedTimestampExtraFieldRecordView::new);
    }

    private ExtendedTimestampExtraFieldRecordView(Builder<ExtendedTimestampExtraFieldRecord, ExtendedTimestampExtraFieldRecordView> builder) {
        super(builder, (record, view, out) -> {
            if (record.getFlag().isLastModificationTime() && record.getLastModificationTime() >= 0)
                view.printLine(out, "  Last Modified Date:", ZipUtils.utcDateTime(record.getLastModificationTime()));
            if (record.getFlag().isLastAccessTime() && record.getLastAccessTime() >= 0)
                view.printLine(out, "  Last Accessed Date:", ZipUtils.utcDateTime(record.getLastAccessTime()));
            if (record.getFlag().isCreationTime() && record.getCreationTime() >= 0)
                view.printLine(out, "  Creation Date:", ZipUtils.utcDateTime(record.getCreationTime()));
        });
    }

}
