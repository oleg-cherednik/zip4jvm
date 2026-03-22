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
package ru.olegcherednik.zip4jvm.decompose;

import ru.olegcherednik.zip4jvm.model.CentralDirectory;
import ru.olegcherednik.zip4jvm.model.Zip64;
import ru.olegcherednik.zip4jvm.model.ZipModel;
import ru.olegcherednik.zip4jvm.model.block.BaseCentralDirectoryBlock;
import ru.olegcherednik.zip4jvm.model.block.BlockModel;
import ru.olegcherednik.zip4jvm.model.settings.ZipInfoSettings;
import ru.olegcherednik.zip4jvm.utils.PathUtils;
import ru.olegcherednik.zip4jvm.view.View;
import ru.olegcherednik.zip4jvm.view.cd.CentralDirectoryView;
import ru.olegcherednik.zip4jvm.view.cd.DigitalSignatureView;
import ru.olegcherednik.zip4jvm.view.cd.FileHeaderInCentralDirectoryView;
import ru.olegcherednik.zip4jvm.view.out.Out;

import java.nio.file.Path;

/**
 * @author Oleg Cherednik
 * @since 06.12.2019
 */
public class CentralDirectoryDecompose implements Decompose, View {

    protected static final String CENTRAL_DIRECTORY = "central_directory";

    protected final ZipModel zipModel;
    protected final ZipInfoSettings settings;
    @SuppressWarnings("PMD.AvoidFieldNameMatchingMethodName")
    protected final CentralDirectory centralDirectory;
    protected final Zip64.ExtensibleDataSector extensibleDataSector;
    private final BaseCentralDirectoryBlock block;

    public CentralDirectoryDecompose(BlockModel blockModel, ZipInfoSettings settings) {
        zipModel = blockModel.getZipModel();
        this.settings = settings;
        centralDirectory = blockModel.getCentralDirectory();
        extensibleDataSector = blockModel.getZip64().getExtensibleDataSector();
        block = blockModel.getCentralDirectoryBlock();
    }

    @Override
    public void printTextInfo(Out out) {
        centralDirectoryView().printTextInfo(out);
        fileHeaderView().printTextInfo(out);
        digitalSignatureView().printTextInfo(out);
    }

    @Override
    public Path decompose(Path dir) {
        dir = PathUtils.createDirectories(dir.resolve(CENTRAL_DIRECTORY));

        centralDirectory(dir);
        fileHeaderDecompose().decompose(dir);
        digitalSignature(dir);

        return dir;
    }

    protected void centralDirectory(Path dir) {
        Utils.print(dir.resolve(CENTRAL_DIRECTORY + EXT_TXT), out -> centralDirectoryView().printTextInfo(out));
        Utils.copyLarge(zipModel, dir.resolve(CENTRAL_DIRECTORY + EXT_DATA), block);
    }

    private void digitalSignature(Path dir) {
        if (centralDirectory.getDigitalSignature() == null)
            return;

        Utils.print(dir.resolve("digital_signature" + EXT_TXT), out -> digitalSignatureView().printTextInfo(out));
        // TODO write digital signature data file
    }

    protected CentralDirectoryView centralDirectoryView() {
        return new CentralDirectoryView(centralDirectory,
                                        block,
                                        settings.getOffs(),
                                        settings.getColumnWidth(),
                                        zipModel.getTotalDisks());
    }

    protected FileHeaderDecompose fileHeaderDecompose() {
        return new FileHeaderDecompose(zipModel, settings, centralDirectory, block);
    }

    protected FileHeaderInCentralDirectoryView fileHeaderView() {
        return new FileHeaderInCentralDirectoryView(zipModel, settings, centralDirectory, block);
    }

    private View digitalSignatureView() {
        CentralDirectory.DigitalSignature digitalSignature = centralDirectory.getDigitalSignature();

        if (digitalSignature == null)
            return View.NULL;

        int offs = settings.getOffs();
        int columnWidth = settings.getColumnWidth();
        long totalDisks = zipModel.getTotalDisks();
        return new DigitalSignatureView(digitalSignature, block.getDigitalSignature(), offs, columnWidth, totalDisks);
    }

}
