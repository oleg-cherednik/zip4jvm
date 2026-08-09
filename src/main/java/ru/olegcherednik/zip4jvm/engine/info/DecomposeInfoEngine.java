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

import ru.olegcherednik.zip4jvm.decompose.CentralDirectoryDecompose;
import ru.olegcherednik.zip4jvm.decompose.Decompose;
import ru.olegcherednik.zip4jvm.decompose.EncryptedCentralDirectoryDecompose;
import ru.olegcherednik.zip4jvm.decompose.EndCentralDirectoryDecompose;
import ru.olegcherednik.zip4jvm.decompose.Zip64Decompose;
import ru.olegcherednik.zip4jvm.decompose.ZipEntriesDecompose;
import ru.olegcherednik.zip4jvm.model.Zip64;
import ru.olegcherednik.zip4jvm.model.block.BlockModel;
import ru.olegcherednik.zip4jvm.model.settings.ZipInfoSettings;
import ru.olegcherednik.zip4jvm.utils.quitely.Quietly;

import lombok.RequiredArgsConstructor;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author Oleg Cherednik
 * @since 22.03.2026
 */
@RequiredArgsConstructor
final class DecomposeInfoEngine implements Decompose {

    private final ZipInfoSettings settings;
    private final BlockModel blockModel;

    // ---------- Decompose ----------

    @Override
    public Path decompose(Path dir) {
        Quietly.doRuntime(() -> {
            Files.createDirectories(dir);

            createEndCentralDirectoryDecompose(blockModel).decompose(dir);
            createZip64Decompose(blockModel).decompose(dir);
            getCentralDirectoryDecompose(blockModel).decompose(dir);
            createZipEntriesDecompose(blockModel).decompose(dir);
        });

        return dir;
    }

    // ----------

    private EndCentralDirectoryDecompose createEndCentralDirectoryDecompose(BlockModel blockModel) {
        return new EndCentralDirectoryDecompose(blockModel, settings);
    }

    private Decompose createZip64Decompose(BlockModel blockModel) {
        return blockModel.getZip64() == Zip64.NULL ? NULL : new Zip64Decompose(blockModel, settings);
    }

    private CentralDirectoryDecompose getCentralDirectoryDecompose(BlockModel blockModel) {
        if (blockModel.getZipModel().isCentralDirectoryEncrypted())
            return new EncryptedCentralDirectoryDecompose(blockModel, settings);
        return new CentralDirectoryDecompose(blockModel, settings);
    }

    private ZipEntriesDecompose createZipEntriesDecompose(BlockModel blockModel) {
        return new ZipEntriesDecompose(blockModel, settings);
    }

}
