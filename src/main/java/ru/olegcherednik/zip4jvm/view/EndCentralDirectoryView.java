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
package ru.olegcherednik.zip4jvm.view;

import ru.olegcherednik.zip4jvm.model.EndCentralDirectory;
import ru.olegcherednik.zip4jvm.model.Zip64;
import ru.olegcherednik.zip4jvm.model.block.Block;
import ru.olegcherednik.zip4jvm.view.out.Out;

import java.nio.charset.Charset;
import java.util.Optional;

import static ru.olegcherednik.zip4jvm.utils.ValidationUtils.requireNotNull;

/**
 * @author Oleg Cherednik
 * @since 13.10.2019
 */
public final class EndCentralDirectoryView extends BaseView {

    private final EndCentralDirectory ecd;
    private final Block block;
    private final Charset charset;
    private final boolean centralDirectoryEncrypted;

    public EndCentralDirectoryView(EndCentralDirectory ecd,
                                   Block block,
                                   Charset charset,
                                   int offs,
                                   int columnWidth,
                                   long totalDisks,
                                   boolean centralDirectoryEncrypted) {
        super(offs, columnWidth, totalDisks);
        this.ecd = requireNotNull(ecd, "EndCentralDirectoryView.dir");
        this.block = requireNotNull(block, "EndCentralDirectoryView.block");
        this.charset = requireNotNull(charset, "EndCentralDirectoryView.charset");
        this.centralDirectoryEncrypted = centralDirectoryEncrypted;
    }

    @Override
    public void printTextInfoNew(Out out) {
        printTitle(out, EndCentralDirectory.SIGNATURE, "End of Central directory record", block);
        printLine(out, String.format("part number of this part (%04X):", ecd.getTotalDisks()), ecd.getTotalDisks() + 1);
        printLine(out, String.format("part number of start of central dir (%04X):", ecd.getMainDiskNo()),
                  ecd.getMainDiskNo() + 1);
        printLine(out, "number of entries in central dir in this part:", ecd.getDiskEntries());
        printTotalEntries(out);
        printSize(out, "size of central dir:", ecd.getCentralDirectorySize());
        printCentralDirectoryOffs(out);
        printComment(out);
    }

    private void printTotalEntries(Out out) {
        Object total = centralDirectoryEncrypted ? "----" : ecd.getTotalEntries();
        printLine(out, "total number of entries in central dir:", total);
    }

    private void printCentralDirectoryOffs(Out out) {
        long centralDirectoryOffs = Math.min(Zip64.LIMIT_DWORD, ecd.getCentralDirectoryRelativeOffs());
        printOffs(out, "relative offset of central dir:", centralDirectoryOffs);

        if (centralDirectoryOffs == Zip64.LIMIT_DWORD)
            printLine(out, "  (see real value in ZIP64 record)");
    }

    private void printComment(Out out) {
        String comment = Optional.ofNullable(ecd.getComment()).orElse("");
        printSize(out, "zipfile comment length:", comment.getBytes(charset).length);
        new StringHexView(comment, charset, offs, columnWidth).printTextInfo(out);
    }
}

