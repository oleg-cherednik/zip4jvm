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

import ru.olegcherednik.zip4jvm.model.Zip64;
import ru.olegcherednik.zip4jvm.model.ZipModel;
import ru.olegcherednik.zip4jvm.model.block.BlockModel;
import ru.olegcherednik.zip4jvm.model.block.Zip64Block;
import ru.olegcherednik.zip4jvm.model.settings.ZipInfoSettings;
import ru.olegcherednik.zip4jvm.view.zip64.EndCentralDirectoryLocatorView;
import ru.olegcherednik.zip4jvm.view.zip64.EndCentralDirectoryView;
import ru.olegcherednik.zip4jvm.view.zip64.ExtensibleDataSectorView;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author Oleg Cherednik
 * @since 06.12.2019
 */
public final class Zip64Decompose implements Decompose {

    private final ZipModel zipModel;
    private final ZipInfoSettings settings;
    private final Zip64 zip64;
    private final Zip64Block block;

    public Zip64Decompose(BlockModel blockModel, ZipInfoSettings settings) {
        zipModel = blockModel.getZipModel();
        this.settings = settings;
        zip64 = blockModel.getZip64();
        block = blockModel.getZip64Block();
    }

    @Override
    public boolean printTextInfo(PrintStream out, boolean emptyLine) {
        if (zip64 == Zip64.NULL)
            return false;

        emptyLine |= endCentralDirectorLocatorView().printTextInfo(out, emptyLine);
        emptyLine |= endCentralDirectoryView().printTextInfo(out, emptyLine);
        emptyLine |= extensibleDataSectorView().printTextInfo(out, emptyLine);

        return emptyLine;
    }

    @Override
    public Path decompose(Path dir) throws IOException {
        if (zip64 == Zip64.NULL)
            return dir;

        dir = Files.createDirectories(dir.resolve("zip64"));

        endOfCentralDirectoryLocator(dir);
        endOfCentralDirectory(dir);
        extensibleDataSector(dir);

        return dir;
    }

    private void endOfCentralDirectoryLocator(Path dir) throws IOException {
        Utils.print(dir.resolve("zip64_end_central_directory_locator.txt"), out -> endCentralDirectorLocatorView().printTextInfo(out));
        Utils.copyLarge(zipModel, dir.resolve("zip64_end_central_directory_locator.data"), block.getEndCentralDirectoryLocatorBlock());
    }

    private void endOfCentralDirectory(Path dir) throws IOException {
        Utils.print(dir.resolve("zip64_end_central_directory.txt"), out -> endCentralDirectoryView().printTextInfo(out));
        Utils.copyLarge(zipModel, dir.resolve("zip64_end_central_directory.data"), block.getEndCentralDirectoryBlock());
    }

    private void extensibleDataSector(Path dir) throws IOException {
        if (zip64.isCentralDirectoryEncrypted()) {
            Utils.print(dir.resolve("zip64_extensible_data_sector.txt"), out -> extensibleDataSectorView().printTextInfo(out));
            Utils.copyLarge(zipModel, dir.resolve("zip64_extensible_data_sector.data"), block.getExtensibleDataSectorBlock());
        }
    }

    private EndCentralDirectoryLocatorView endCentralDirectorLocatorView() {
        return new EndCentralDirectoryLocatorView(zip64.getEndCentralDirectoryLocator(),
                                                  block.getEndCentralDirectoryLocatorBlock(),
                                                  settings.getOffs(),
                                                  settings.getColumnWidth(),
                                                  zipModel.getTotalDisks());
    }

    private EndCentralDirectoryView endCentralDirectoryView() {
        return new EndCentralDirectoryView(zip64.getEndCentralDirectory(),
                                           block.getEndCentralDirectoryBlock(),
                                           settings.getOffs(),
                                           settings.getColumnWidth(),
                                           zipModel.getTotalDisks());
    }

    private ExtensibleDataSectorView extensibleDataSectorView() {
        return new ExtensibleDataSectorView(zip64.getExtensibleDataSector(),
                                            block.getExtensibleDataSectorBlock(),
                                            settings.getOffs(),
                                            settings.getColumnWidth());
    }

}
