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

import ru.olegcherednik.zip4jvm.model.Zip64;
import ru.olegcherednik.zip4jvm.model.ZipModel;
import ru.olegcherednik.zip4jvm.model.block.BlockModel;
import ru.olegcherednik.zip4jvm.model.block.Zip64Block;
import ru.olegcherednik.zip4jvm.model.settings.ZipInfoSettings;
import ru.olegcherednik.zip4jvm.view.Zip64View;

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

        emptyLine |= endCentralDirectorLocatorView().print(out, emptyLine);
        return endCentralDirectoryView().print(out, emptyLine);
    }

    @Override
    public void decompose(Path dir) throws IOException {
        if (zip64 == Zip64.NULL)
            return;

        dir = Files.createDirectories(dir.resolve("zip64"));

        endOfCentralDirectoryLocator(dir);
        endOfCentralDirectory(dir);
    }

    private void endOfCentralDirectoryLocator(Path dir) throws IOException {
        Utils.print(dir.resolve("zip64_end_central_directory_locator.txt"), out -> endCentralDirectorLocatorView().print(out));
        Utils.copyLarge(zipModel, dir.resolve("zip64_end_central_directory_locator.data"), block.getEndCentralDirectoryLocatorBlock());
    }

    private void endOfCentralDirectory(Path dir) throws IOException {
        Utils.print(dir.resolve("zip64_end_central_directory.txt"), out -> endCentralDirectoryView().print(out));
        Utils.copyLarge(zipModel, dir.resolve("zip64_end_central_directory.data"), block.getEndCentralDirectoryBlock());
    }

    private Zip64View.EndCentralDirectoryLocatorView endCentralDirectorLocatorView() {
        Zip64.EndCentralDirectoryLocator locator = zip64.getEndCentralDirectoryLocator();
        int offs = settings.getOffs();
        int columnWidth = settings.getColumnWidth();
        long totalDisks = zipModel.getTotalDisks();
        return new Zip64View.EndCentralDirectoryLocatorView(locator, block.getEndCentralDirectoryLocatorBlock(), offs, columnWidth, totalDisks);
    }

    private Zip64View.EndCentralDirectoryView endCentralDirectoryView() {
        Zip64.EndCentralDirectory dir = zip64.getEndCentralDirectory();
        int offs = settings.getOffs();
        int columnWidth = settings.getColumnWidth();
        long totalDisks = zipModel.getTotalDisks();
        return new Zip64View.EndCentralDirectoryView(dir, block.getEndCentralDirectoryBlock(), offs, columnWidth, totalDisks);
    }

}
