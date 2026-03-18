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

import ru.olegcherednik.zip4jvm.model.Zip64;
import ru.olegcherednik.zip4jvm.model.block.Block;
import ru.olegcherednik.zip4jvm.model.extrafield.PkwareExtraField;
import ru.olegcherednik.zip4jvm.view.out.Out;

import lombok.Builder;

/**
 * @author Oleg Cherednik
 * @since 26.10.2019
 */
final class Zip64ExtendedInfoView extends ExtraFieldRecordView<Zip64.ExtendedInfo> {

    @Builder
    Zip64ExtendedInfoView(int offs, int columnWidth, long totalDisks,
                          Zip64.ExtendedInfo record, Block block) {
        super(offs, columnWidth, totalDisks, record, block);
    }

    // ---------- ExtraFieldRecordView ----------

    @Override
    public void printRecord(Out out) {
        printCompressedSize(out);
        printUncompressedSize(out);
        printLocalFileHeaderRelativeOffs(out);
        printDiskNo(out);
    }

    // ----------

    private void printCompressedSize(Out out) {
        if (record.getCompressedSize() != PkwareExtraField.NO_DATA)
            printLength(out, "  original compressed size:", record.getCompressedSize());
    }

    private void printUncompressedSize(Out out) {
        if (record.getUncompressedSize() != PkwareExtraField.NO_DATA)
            printLength(out, "  original uncompressed size:", record.getUncompressedSize());
    }

    private void printLocalFileHeaderRelativeOffs(Out out) {
        if (record.getLocalFileHeaderRelativeOffs() != PkwareExtraField.NO_DATA)
            printLine(out, "  original relative offset of local header:",
                      strOffs(record.getLocalFileHeaderRelativeOffs()));
    }

    private void printDiskNo(Out out) {
        if (record.getDiskNo() != PkwareExtraField.NO_DATA)
            printLine(out,
                      String.format("  original part number of this part (%04X):", record.getDiskNo()),
                      record.getDiskNo());
    }

}
