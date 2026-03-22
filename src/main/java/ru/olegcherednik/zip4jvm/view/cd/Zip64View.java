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

import ru.olegcherednik.zip4jvm.model.Zip64;
import ru.olegcherednik.zip4jvm.model.ZipModel;
import ru.olegcherednik.zip4jvm.model.block.BlockModel;
import ru.olegcherednik.zip4jvm.model.block.Zip64Block;
import ru.olegcherednik.zip4jvm.model.settings.ZipInfoSettings;
import ru.olegcherednik.zip4jvm.view.View;
import ru.olegcherednik.zip4jvm.view.out.Out;
import ru.olegcherednik.zip4jvm.view.zip64.EndCentralDirectoryLocatorView;
import ru.olegcherednik.zip4jvm.view.zip64.EndCentralDirectoryView;
import ru.olegcherednik.zip4jvm.view.zip64.ExtensibleDataSectorView;

import static ru.olegcherednik.zip4jvm.utils.ValidationUtils.requireNotNull;

/**
 * @author Oleg Cherednik
 * @since 22.03.2026
 */
public final class Zip64View implements View {

    private final ZipModel zipModel;
    private final ZipInfoSettings settings;
    private final Zip64 zip64;
    private final Zip64Block block;

    public Zip64View(BlockModel blockModel, ZipInfoSettings settings) {
        zipModel = blockModel.getZipModel();
        this.settings = settings;
        zip64 = requireNotNull(blockModel.getZip64(), "Zip64View.zip64");
        block = blockModel.getZip64Block();
    }

    // ---------- View ----------

    @Override
    public void printTextInfo(Out out) {
        createEndCentralDirectoryLocatorView().printTextInfo(out);
        createEndCentralDirectoryView().printTextInfo(out);
        createExtensibleDataSectorView().printTextInfo(out);
    }

    // ----------

    public EndCentralDirectoryLocatorView createEndCentralDirectoryLocatorView() {
        return new EndCentralDirectoryLocatorView(zip64.getEndCentralDirectoryLocator(),
                                                  block.getEndCentralDirectoryLocatorBlock(),
                                                  settings.getOffs(),
                                                  settings.getColumnWidth(),
                                                  zipModel.getTotalDisks());
    }

    public EndCentralDirectoryView createEndCentralDirectoryView() {
        return new EndCentralDirectoryView(zip64.getEndCentralDirectory(),
                                           block.getEndCentralDirectoryBlock(),
                                           settings.getOffs(),
                                           settings.getColumnWidth(),
                                           zipModel.getTotalDisks());
    }

    public ExtensibleDataSectorView createExtensibleDataSectorView() {
        return new ExtensibleDataSectorView(zip64.getExtensibleDataSector(),
                                            block.getExtensibleDataSectorBlock(),
                                            settings.getOffs(),
                                            settings.getColumnWidth());
    }

}
