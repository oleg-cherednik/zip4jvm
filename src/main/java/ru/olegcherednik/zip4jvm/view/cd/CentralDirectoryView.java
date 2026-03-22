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
package ru.olegcherednik.zip4jvm.view.cd;

import ru.olegcherednik.zip4jvm.model.CentralDirectory;
import ru.olegcherednik.zip4jvm.model.block.Block;
import ru.olegcherednik.zip4jvm.model.block.BlockModel;
import ru.olegcherednik.zip4jvm.model.settings.ZipInfoSettings;
import ru.olegcherednik.zip4jvm.view.BaseView;
import ru.olegcherednik.zip4jvm.view.out.Out;

import static ru.olegcherednik.zip4jvm.utils.ValidationUtils.requireNotNull;

/**
 * @author Oleg Cherednik
 * @since 14.10.2019
 */
public class CentralDirectoryView extends BaseView {

    private final CentralDirectory centralDirectory;
    private final Block block;

    public CentralDirectoryView(CentralDirectory centralDirectory,
                                Block block,
                                int offs,
                                int columnWidth,
                                long totalDisks) {
        super(offs, columnWidth, totalDisks);
        this.centralDirectory = requireNotNull(centralDirectory, "CentralDirectoryView.centralDirectory");
        this.block = requireNotNull(block, "CentralDirectoryView.block");
    }

    public CentralDirectoryView(BlockModel blockModel, ZipInfoSettings settings) {
        this(blockModel.getCentralDirectory(),
             blockModel.getCentralDirectoryBlock(),
             settings.getOffs(),
             settings.getColumnWidth(),
             blockModel.getZipModel().getTotalDisks());
    }

    // ---------- View ----------

    @Override
    public void printTextInfo(Out out) {
        super.printTextInfo(out);
        printTitle(out, CentralDirectory.FileHeader.SIGNATURE, "Central directory", block);
        printLine(out, "total entries:", centralDirectory.getFileHeaders().size());
    }

}
