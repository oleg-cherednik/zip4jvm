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
package ru.olegcherednik.zip4jvm.view.zip64;

import ru.olegcherednik.zip4jvm.model.Zip64;
import ru.olegcherednik.zip4jvm.model.block.Block;
import ru.olegcherednik.zip4jvm.view.BaseView;

import java.io.PrintStream;

import static ru.olegcherednik.zip4jvm.utils.ValidationUtils.requireNotNull;

/**
 * @author Oleg Cherednik
 * @since 29.12.2022
 */
public class EndCentralDirectoryLocatorView extends BaseView {

    private final Zip64.EndCentralDirectoryLocator locator;
    private final Block block;

    public EndCentralDirectoryLocatorView(Zip64.EndCentralDirectoryLocator locator,
                                          Block block,
                                          int offs,
                                          int columnWidth,
                                          long totalDisks) {
        super(offs, columnWidth, totalDisks);
        this.locator = requireNotNull(locator, "EndCentralDirectoryLocatorView.locator");
        this.block = requireNotNull(block, "EndCentralDirectoryLocatorView.block");
    }

    @Override
    public boolean printTextInfo(PrintStream out) {
        printTitle(out, Zip64.EndCentralDirectoryLocator.SIGNATURE, "ZIP64 End of Central directory locator", block);
        printLine(out, String.format("part number of new-end-of-central-dir (%04X):", locator.getMainDiskNo()), locator.getMainDiskNo() + 1);
        printLine(out, "relative offset of new-end-of-central-dir:",
                  String.format("%1$d (0x%1$08X) bytes", locator.getEndCentralDirectoryRelativeOffs()));
        printLine(out, "total number of parts in archive:", locator.getTotalDisks());
        return true;
    }

}
