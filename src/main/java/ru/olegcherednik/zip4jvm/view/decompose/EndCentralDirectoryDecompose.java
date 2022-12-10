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

import ru.olegcherednik.zip4jvm.model.EndCentralDirectory;
import ru.olegcherednik.zip4jvm.model.ZipModel;
import ru.olegcherednik.zip4jvm.model.block.Block;
import ru.olegcherednik.zip4jvm.model.block.BlockModel;
import ru.olegcherednik.zip4jvm.model.settings.ZipInfoSettings;
import ru.olegcherednik.zip4jvm.view.EndCentralDirectoryView;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.file.Path;

/**
 * @author Oleg Cherednik
 * @since 06.12.2019
 */
public final class EndCentralDirectoryDecompose implements Decompose {

    private final ZipModel zipModel;
    private final ZipInfoSettings settings;
    private final EndCentralDirectory endCentralDirectory;
    private final Block block;

    public EndCentralDirectoryDecompose(BlockModel blockModel, ZipInfoSettings settings) {
        zipModel = blockModel.getZipModel();
        this.settings = settings;
        endCentralDirectory = blockModel.getEndCentralDirectory();
        block = blockModel.getEndCentralDirectoryBlock();
    }

    @Override
    public boolean printTextInfo(PrintStream out, boolean emptyLine) {
        return createView().print(out, emptyLine);
    }

    @Override
    public void decompose(Path dir) throws IOException {
        Utils.print(dir.resolve("end_central_directory.txt"), out -> createView().print(out));
        Utils.copyLarge(zipModel, dir.resolve("end_central_directory.data"), block);
    }

    private EndCentralDirectoryView createView() {
        return new EndCentralDirectoryView(endCentralDirectory,
                                           block,
                                           settings.getCharset(),
                                           settings.getOffs(),
                                           settings.getColumnWidth(),
                                           zipModel.getTotalDisks());
    }

}
