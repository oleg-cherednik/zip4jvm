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
import ru.olegcherednik.zip4jvm.model.Version;
import ru.olegcherednik.zip4jvm.model.Zip64;
import ru.olegcherednik.zip4jvm.model.block.Block;

import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.Optional;

import static ru.olegcherednik.zip4jvm.utils.ValidationUtils.requireNotNull;

/**
 * @author Oleg Cherednik
 * @since 10.12.2022
 */
public final class ExtensibleDataSectorView extends BaseView {

//    printLine(out, "extensible data sector:", String.format("%d bytes", dir.getExtensibleDataSector().length));
//            new ByteArrayHexView(dir.getExtensibleDataSector(), offs, columnWidth).print(out);

    private final Zip64.ExtensibleDataSector extensibleDataSector;
    private final Block block;

    public ExtensibleDataSectorView(Zip64.ExtensibleDataSector extensibleDataSector, Block block, int offs, int columnWidth) {
        super(offs, columnWidth);
        this.extensibleDataSector = requireNotNull(extensibleDataSector, "ExtensibleDataSectorView.extensibleDataSector");
        this.block = requireNotNull(block, "ExtensibleDataSectorView.block");
    }

    @Override
    public boolean print(PrintStream out) {
        if (extensibleDataSector == null || extensibleDataSector == Zip64.ExtensibleDataSector.NULL)
            return false;

        printTitle(out, EndCentralDirectory.SIGNATURE, "End of Central directory record", block);
//        printLine(out, String.format("part number of this part (%04X):", dir.getTotalDisks()), dir.getTotalDisks() + 1);
//        printLine(out, String.format("part number of start of central dir (%04X):", dir.getMainDiskNo()), dir.getMainDiskNo() + 1);
//        printLine(out, "number of entries in central dir in this part:", dir.getDiskEntries());
//        printLine(out, "total number of entries in central dir:", dir.getTotalEntries());
//        printLine(out, "size of central dir:", String.format("%1$d (0x%1$08X) bytes", dir.getCentralDirectorySize()));
//        printCentralDirectoryOffs(out);
//        printComment(out);

        return true;
    }

//    private void printCentralDirectoryOffs(PrintStream out) {
//        long centralDirectoryOffs = Math.min(Zip64.LIMIT_DWORD, dir.getCentralDirectoryRelativeOffs());
//        printLine(out, "relative offset of central dir:", String.format("%1$d (0x%1$08X) bytes", centralDirectoryOffs));
//
//        if (centralDirectoryOffs == Zip64.LIMIT_DWORD)
//            printLine(out, "  (see real value in ZIP64 record)");
//    }
//
//    private void printComment(PrintStream out) {
//        String comment = Optional.ofNullable(dir.getComment()).orElse("");
//        printLine(out, "zipfile comment length:", String.format("%d bytes", comment.getBytes(charset).length));
//        new StringHexView(comment, charset, offs, columnWidth).print(out);
//    }

}
