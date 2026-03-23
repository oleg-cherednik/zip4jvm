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
import ru.olegcherednik.zip4jvm.model.Zip64;
import ru.olegcherednik.zip4jvm.model.ZipModel;
import ru.olegcherednik.zip4jvm.model.block.BaseCentralDirectoryBlock;
import ru.olegcherednik.zip4jvm.model.block.BlockModel;
import ru.olegcherednik.zip4jvm.model.settings.ZipInfoSettings;
import ru.olegcherednik.zip4jvm.view.View;
import ru.olegcherednik.zip4jvm.view.out.Out;

/**
 * @author Oleg Cherednik
 * @since 22.03.2022
 */
public class CentralDirectoryView implements View {

    protected final ZipModel zipModel;
    protected final ZipInfoSettings settings;
    protected final CentralDirectory centralDirectory;
    protected final Zip64.ExtensibleDataSector extensibleDataSector;
    private final BaseCentralDirectoryBlock block;

    public CentralDirectoryView(BlockModel blockModel, ZipInfoSettings settings) {
        zipModel = blockModel.getZipModel();
        this.settings = settings;
        centralDirectory = blockModel.getCentralDirectory();
        extensibleDataSector = blockModel.getZip64().getExtensibleDataSector();
        block = blockModel.getCentralDirectoryBlock();
    }

    // ---------- View ----------

    @Override
    public void printTextInfo(Out out) {
        centralDirectoryInfoView().printTextInfo(out);
        fileHeaderView().printTextInfo(out);
        digitalSignatureView().printTextInfo(out);
    }

    // ----------

    protected CentralDirectoryInfoView centralDirectoryInfoView() {
        return new CentralDirectoryInfoView(centralDirectory,
                                            block,
                                            settings.getOffs(),
                                            settings.getColumnWidth(),
                                            zipModel.getTotalDisks());
    }

    protected FileHeaderInCentralDirectoryView fileHeaderView() {
        return new FileHeaderInCentralDirectoryView(zipModel, settings, centralDirectory, block);
    }

    protected View digitalSignatureView() {
        CentralDirectory.DigitalSignature digitalSignature = centralDirectory.getDigitalSignature();

        if (digitalSignature == null)
            return NULL;

        int offs = settings.getOffs();
        int columnWidth = settings.getColumnWidth();
        long totalDisks = zipModel.getTotalDisks();
        return new DigitalSignatureView(digitalSignature, block.getDigitalSignature(), offs, columnWidth, totalDisks);
    }

}
