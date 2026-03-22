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
import ru.olegcherednik.zip4jvm.model.ZipModel;
import ru.olegcherednik.zip4jvm.model.block.BaseCentralDirectoryBlock;
import ru.olegcherednik.zip4jvm.model.block.BlockModel;
import ru.olegcherednik.zip4jvm.model.settings.ZipInfoSettings;
import ru.olegcherednik.zip4jvm.utils.PathUtils;
import ru.olegcherednik.zip4jvm.view.cd.CentralDirectoryView;
import ru.olegcherednik.zip4jvm.view.cd.DigitalSignatureView;

import java.nio.file.Path;
import java.util.function.Supplier;

/**
 * @author Oleg Cherednik
 * @since 06.12.2019
 */
public class CentralDirectoryDecompose implements Decompose {

    protected static final String CENTRAL_DIRECTORY = "central_directory";

    protected final ZipModel zipModel;
    private final CentralDirectory.DigitalSignature digitalSignature;
    private final BaseCentralDirectoryBlock block;
    private final Supplier<CentralDirectoryView> createCentralDirectoryView;
    private final Supplier<FileHeaderDecompose> createFileHeaderDecompose;
    private final Supplier<DigitalSignatureView> createDigitalSignatureView;

    public CentralDirectoryDecompose(BlockModel blockModel, ZipInfoSettings settings) {
        this(blockModel,
             settings,
             () -> new CentralDirectoryView(blockModel, settings),
             () -> new FileHeaderDecompose(blockModel, settings)
        );
    }

    public CentralDirectoryDecompose(BlockModel blockModel,
                                     ZipInfoSettings settings,
                                     Supplier<CentralDirectoryView> createCentralDirectoryView,
                                     Supplier<FileHeaderDecompose> createFileHeaderDecompose) {
        zipModel = blockModel.getZipModel();
        digitalSignature = blockModel.getCentralDirectory().getDigitalSignature();
        block = blockModel.getCentralDirectoryBlock();
        this.createCentralDirectoryView = createCentralDirectoryView;
        this.createFileHeaderDecompose = createFileHeaderDecompose;
        createDigitalSignatureView = () -> new DigitalSignatureView(blockModel, settings);
    }

    // ---------- Decompose ----------

    @Override
    public Path decompose(Path dir) {
        dir = PathUtils.createDirectories(dir.resolve(CENTRAL_DIRECTORY));

        centralDirectory(dir);
        fileHeader(dir);
        digitalSignature(dir);

        return dir;
    }

    // ----------

    private void centralDirectory(Path dir) {
        Utils.print(dir.resolve(CENTRAL_DIRECTORY + EXT_TXT),
                    out -> createCentralDirectoryView.get().printTextInfo(out));
        Utils.copyLarge(zipModel, dir.resolve(CENTRAL_DIRECTORY + EXT_DATA), block);
    }

    private void fileHeader(Path dir) {
        createFileHeaderDecompose.get().decompose(dir);
    }

    private void digitalSignature(Path dir) {
        if (digitalSignature == null)
            return;

        Utils.print(dir.resolve("digital_signature" + EXT_TXT),
                    out -> createDigitalSignatureView.get().printTextInfo(out));
        // TODO write digital signature data file
    }

}
