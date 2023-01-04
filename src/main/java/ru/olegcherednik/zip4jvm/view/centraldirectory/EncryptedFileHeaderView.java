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
package ru.olegcherednik.zip4jvm.view.centraldirectory;

import lombok.Builder;
import ru.olegcherednik.zip4jvm.model.CentralDirectory;
import ru.olegcherednik.zip4jvm.model.block.Block;
import ru.olegcherednik.zip4jvm.model.block.CentralDirectoryBlock;

import java.io.PrintStream;
import java.nio.charset.Charset;

/**
 * @author Oleg Cherednik
 * @since 03.01.2023
 */
public class EncryptedFileHeaderView extends FileHeaderView {

    public EncryptedFileHeaderView(CentralDirectory.FileHeader fileHeader,
                                   CentralDirectoryBlock.FileHeaderBlock block,
                                   long pos,
                                   Charset charset,
                                   int offs,
                                   int columnWidth,
                                   long totalDisks) {
        super(fileHeader, block, pos, charset, offs, columnWidth, totalDisks);
    }

    @Override
    protected void printLocationTitle(PrintStream out, Block block) {
        printLine(out, "- location in central directory:", String.format("%1$d (0x%1$08X) bytes", block.getRelativeOffs()));
    }

}
