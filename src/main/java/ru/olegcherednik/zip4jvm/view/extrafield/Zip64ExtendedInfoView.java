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

import ru.olegcherednik.zip4jvm.model.ExtraField;
import ru.olegcherednik.zip4jvm.model.Zip64;

/**
 * @author Oleg Cherednik
 * @since 26.10.2019
 */
final class Zip64ExtendedInfoView extends ExtraFieldRecordView<Zip64.ExtendedInfo> {

    public static Builder<Zip64.ExtendedInfo, Zip64ExtendedInfoView> builder() {
        return new Builder<>(Zip64ExtendedInfoView::new);
    }

    private Zip64ExtendedInfoView(Builder<Zip64.ExtendedInfo, Zip64ExtendedInfoView> builder) {
        super(builder, (record, view, out) -> {
            if (record.getUncompressedSize() != ExtraField.NO_DATA)
                view.printLine(out, "  original compressed size:", String.format("%d bytes", record.getUncompressedSize()));
            if (record.getCompressedSize() != ExtraField.NO_DATA)
                view.printLine(out, "  original uncompressed size:", String.format("%d bytes", record.getCompressedSize()));
            if (record.getLocalFileHeaderRelativeOffs() != ExtraField.NO_DATA)
                view.printLine(out, "  original relative offset of local header:",
                        String.format("%1$d (0x%1$08X) bytes", record.getLocalFileHeaderRelativeOffs()));
            if (record.getDiskNo() != ExtraField.NO_DATA)
                view.printLine(out, String.format("  original part number of this part (%04X):", record.getDiskNo()), record.getDiskNo());
        });
    }
}
