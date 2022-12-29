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

import ru.olegcherednik.zip4jvm.model.block.BlockModel;
import ru.olegcherednik.zip4jvm.model.settings.ZipInfoSettings;
import ru.olegcherednik.zip4jvm.view.entry.ZipEntriesView;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author Oleg Cherednik
 * @since 06.12.2019
 */
public final class ZipEntriesDecompose implements Decompose {

    private final BlockModel blockModel;
    private final ZipInfoSettings settings;

    public ZipEntriesDecompose(BlockModel blockModel, ZipInfoSettings settings) {
        this.blockModel = blockModel;
        this.settings = settings;
    }

    @Override
    public boolean printTextInfo(PrintStream out, boolean emptyLine) {
        if (blockModel.isEmpty())
            return false;

        emptyLine |= zipEntriesView().print(out, emptyLine);
        return localFileHeaderDecompose().printTextInfo(out, emptyLine);
    }

    @Override
    public Path decompose(Path dir) throws IOException {
        if (blockModel.isEmpty())
            return dir;

        dir = Files.createDirectories(dir.resolve("entries"));
        localFileHeaderDecompose().decompose(dir);

        return dir;
    }

    private ZipEntriesView zipEntriesView() {
        long totalEntries = blockModel.getFileNameZipEntryBlock().size();
        long totalDisks = blockModel.getZipModel().getTotalDisks();
        return new ZipEntriesView(totalEntries, settings.getOffs(), settings.getColumnWidth(), totalDisks);
    }

    private LocalFileHeaderDecompose localFileHeaderDecompose() {
        return new LocalFileHeaderDecompose(blockModel, settings);
    }

}
