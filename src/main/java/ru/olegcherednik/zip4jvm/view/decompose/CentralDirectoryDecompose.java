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
package ru.olegcherednik.zip4jvm.view.decompose;

import ru.olegcherednik.zip4jvm.model.CentralDirectory;
import ru.olegcherednik.zip4jvm.model.Zip64;
import ru.olegcherednik.zip4jvm.model.ZipModel;
import ru.olegcherednik.zip4jvm.model.block.BaseCentralDirectoryBlock;
import ru.olegcherednik.zip4jvm.model.block.BlockModel;
import ru.olegcherednik.zip4jvm.model.settings.ZipInfoSettings;
import ru.olegcherednik.zip4jvm.view.View;
import ru.olegcherednik.zip4jvm.view.centraldirectory.CentralDirectoryView;
import ru.olegcherednik.zip4jvm.view.centraldirectory.DigitalSignatureView;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author Oleg Cherednik
 * @since 06.12.2019
 */
public class CentralDirectoryDecompose implements Decompose {

    protected static final String CENTRAL_DIRECTORY = "central_directory";

    protected final ZipModel zipModel;
    protected final ZipInfoSettings settings;
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
    public boolean printTextInfo(PrintStream out, boolean emptyLine) {
        emptyLine |= centralDirectoryView().print(out, emptyLine);
        emptyLine |= fileHeaderDecompose().printTextInfo(out, emptyLine);
        return digitalSignatureView().print(out, emptyLine);
    }

    @Override
    public Path decompose(Path dir) throws IOException {
        dir = Files.createDirectories(dir.resolve(CENTRAL_DIRECTORY));

        centralDirectory(dir);
        fileHeaderDecompose().decompose(dir);
        digitalSignature(dir);

        return dir;
    }

    protected void centralDirectory(Path dir) throws IOException {
        Utils.print(dir.resolve(CENTRAL_DIRECTORY + ".txt"), out -> centralDirectoryView().print(out));
        Utils.copyLarge(zipModel, dir.resolve(CENTRAL_DIRECTORY + ".data"), block);
    }

    private void digitalSignature(Path dir) throws FileNotFoundException {
        if (centralDirectory.getDigitalSignature() == null)
            return;

        Utils.print(dir.resolve("digital_signature.txt"), out -> digitalSignatureView().print(out));
        // TODO write digital signature data file
    }

    protected CentralDirectoryView centralDirectoryView() {
        return new CentralDirectoryView(centralDirectory,
                                        block,
                                        settings.getOffs(),
                                        settings.getColumnWidth(),
                                        zipModel.getTotalDisks());
    }

    private FileHeaderDecompose fileHeaderDecompose() {
        return new FileHeaderDecompose(zipModel, settings, centralDirectory, block);
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
