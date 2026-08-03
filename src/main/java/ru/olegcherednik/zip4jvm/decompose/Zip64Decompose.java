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
package ru.olegcherednik.zip4jvm.decompose;

import ru.olegcherednik.zip4jvm.model.Zip64;
import ru.olegcherednik.zip4jvm.model.ZipModel;
import ru.olegcherednik.zip4jvm.model.block.BlockModel;
import ru.olegcherednik.zip4jvm.model.block.Zip64Block;
import ru.olegcherednik.zip4jvm.model.settings.ZipInfoSettings;
import ru.olegcherednik.zip4jvm.utils.PathUtils;
import ru.olegcherednik.zip4jvm.utils.ValidationUtils;
import ru.olegcherednik.zip4jvm.view.cd.Zip64View;

import java.nio.file.Path;

/**
 * @author Oleg Cherednik
 * @since 06.12.2019
 */
public final class Zip64Decompose implements Decompose {

    private final ZipModel zipModel;
    private final Zip64 zip64;
    private final Zip64Block block;
    private final Zip64View view;

    public Zip64Decompose(BlockModel blockModel, ZipInfoSettings settings) {
        zipModel = blockModel.getZipModel();
        zip64 = ValidationUtils.requireNotNull(blockModel.getZip64(), "Zip64Decompose.zip64");
        block = blockModel.getZip64Block();
        view = new Zip64View(blockModel, settings);
    }

    // ---------- Decompose ----------

    @Override
    public Path decompose(Path dir) {
        dir = PathUtils.createDirectories(dir.resolve("zip64"));

        endOfCentralDirectoryLocatorDecompose(dir);
        endOfCentralDirectoryDecompose(dir);
        extensibleDataSectorDecompose(dir);

        return dir;
    }

    // ----------

    private void endOfCentralDirectoryLocatorDecompose(Path dir) {
        Utils.print(dir.resolve("zip64_end_central_directory_locator" + EXT_TXT),
                    out -> view.createEndCentralDirectoryLocatorView().printTextInfo(out));
        Utils.copyLarge(zipModel,
                        dir.resolve("zip64_end_central_directory_locator" + EXT_DATA),
                        block.getEndCentralDirectoryLocatorBlock());
    }

    private void endOfCentralDirectoryDecompose(Path dir) {
        Utils.print(dir.resolve("zip64_end_central_directory" + EXT_TXT),
                    out -> view.createEndCentralDirectoryView().printTextInfo(out));
        Utils.copyLarge(zipModel,
                        dir.resolve("zip64_end_central_directory" + EXT_DATA),
                        block.getEndCentralDirectoryBlock());
    }

    private void extensibleDataSectorDecompose(Path dir) {
        if (zip64.isCentralDirectoryEncrypted()) {
            Utils.print(dir.resolve("zip64_extensible_data_sector" + EXT_TXT),
                        out -> view.createExtensibleDataSectorView().printTextInfo(out));
            Utils.copyLarge(zipModel,
                            dir.resolve("zip64_extensible_data_sector" + EXT_DATA),
                            block.getExtensibleDataSectorBlock());
        }
    }

}
