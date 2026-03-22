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

import ru.olegcherednik.zip4jvm.model.block.BlockModel;
import ru.olegcherednik.zip4jvm.model.settings.ZipInfoSettings;
import ru.olegcherednik.zip4jvm.view.View;
import ru.olegcherednik.zip4jvm.view.entry.ZipEntriesView;
import ru.olegcherednik.zip4jvm.view.out.Out;

import lombok.RequiredArgsConstructor;

/**
 * @author Oleg Cherednik
 * @since 22.03.2026
 */
@RequiredArgsConstructor
public final class ZipEntriesInCentralDirctoryView implements View {

    private final BlockModel blockModel;
    private final ZipInfoSettings settings;

    // ---------- View ----------

    @Override
    public void printTextInfo(Out out) {
        if (!blockModel.isEmpty()) {
            zipEntriesView().printTextInfo(out);
            localFileHeaderView().printTextInfo(out);
        }
    }

    // ----------

    private ZipEntriesView zipEntriesView() {
        long totalEntries = blockModel.getFileNameZipEntryBlock().size();
        long totalDisks = blockModel.getZipModel().getTotalDisks();
        return new ZipEntriesView(totalEntries, settings.getOffs(), settings.getColumnWidth(), totalDisks);
    }

    private LocalFileHeaderInCentralDirectoryView localFileHeaderView() {
        return new LocalFileHeaderInCentralDirectoryView(blockModel, settings);
    }

}
