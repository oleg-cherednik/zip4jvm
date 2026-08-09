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

import ru.olegcherednik.zip4jvm.model.block.BlockModel;
import ru.olegcherednik.zip4jvm.model.settings.ZipInfoSettings;
import ru.olegcherednik.zip4jvm.utils.PathUtils;

import lombok.RequiredArgsConstructor;

import java.nio.file.Path;

/**
 * @author Oleg Cherednik
 * @since 06.12.2019
 */
@RequiredArgsConstructor
public final class ZipEntriesDecompose implements Decompose {

    private final BlockModel blockModel;
    private final ZipInfoSettings settings;

    // ---------- Decompose ----------

    @Override
    public Path decompose(Path dir) {
        if (!blockModel.isEmpty()) {
            dir = PathUtils.createDirectories(dir.resolve("entries"));
            new LocalFileHeaderDecompose(blockModel, settings).decompose(dir);
        }

        return dir;
    }

}
