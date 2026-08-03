/*
 * Copyright 2019 Oleg Cherednik (oleg.cherednik@gmail.com)
 *
 * Licensed under The Apache Software License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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
package ru.olegcherednik.zip4jvm.engine.info;

import ru.olegcherednik.zip4jvm.model.Zip64;
import ru.olegcherednik.zip4jvm.model.block.BlockModel;
import ru.olegcherednik.zip4jvm.model.settings.ZipInfoSettings;
import ru.olegcherednik.zip4jvm.view.EndCentralDirectoryView;
import ru.olegcherednik.zip4jvm.view.View;
import ru.olegcherednik.zip4jvm.view.cd.CentralDirectoryView;
import ru.olegcherednik.zip4jvm.view.cd.Zip64View;
import ru.olegcherednik.zip4jvm.view.cd.ZipEntriesInCentralDirctoryView;
import ru.olegcherednik.zip4jvm.view.out.Out;

import lombok.RequiredArgsConstructor;

/**
 * @author Oleg Cherednik
 * @since 15.11.2019
 */
@RequiredArgsConstructor
public final class ViewInfoEngine implements View {

    private final ZipInfoSettings settings;
    private final BlockModel blockModel;

    // ---------- View ----------

    @Override
    public void printTextInfo(Out out) {
        createEndCentralDirectoryView(blockModel).printTextInfoWithEmptyLine(out);
        createZip64View(blockModel).printTextInfoWithEmptyLine(out);
        createCentralDirectoryView(blockModel).printTextInfoWithEmptyLine(out);
        createZipEntriesInCentralDirctoryView(blockModel).printTextInfoWithEmptyLine(out);
    }

    // ----------

    private EndCentralDirectoryView createEndCentralDirectoryView(BlockModel blockModel) {
        return new EndCentralDirectoryView(blockModel, settings);
    }

    private View createZip64View(BlockModel blockModel) {
        return blockModel.getZip64() == Zip64.NULL ? NULL : new Zip64View(blockModel, settings);
    }

    private CentralDirectoryView createCentralDirectoryView(BlockModel blockModel) {
        return new CentralDirectoryView(blockModel, settings);
    }

    private ZipEntriesInCentralDirctoryView createZipEntriesInCentralDirctoryView(BlockModel blockModel) {
        return new ZipEntriesInCentralDirctoryView(blockModel, settings);
    }

}
