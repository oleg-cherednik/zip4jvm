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

import ru.olegcherednik.zip4jvm.ZipFile;
import ru.olegcherednik.zip4jvm.exception.EntryNotFoundException;
import ru.olegcherednik.zip4jvm.io.readers.ZipModelReader;
import ru.olegcherednik.zip4jvm.io.readers.block.BlockZipModelReader;
import ru.olegcherednik.zip4jvm.model.CentralDirectory;
import ru.olegcherednik.zip4jvm.model.block.BlockModel;
import ru.olegcherednik.zip4jvm.model.settings.ZipInfoSettings;
import ru.olegcherednik.zip4jvm.model.src.SrcZip;
import ru.olegcherednik.zip4jvm.utils.quitely.Quietly;
import ru.olegcherednik.zip4jvm.view.out.Out;

import lombok.RequiredArgsConstructor;

import java.nio.file.Path;

/**
 * @author Oleg Cherednik
 * @since 15.11.2019
 */
@RequiredArgsConstructor
public final class InfoEngine implements ZipFile.Info {

    private final SrcZip srcZip;
    private final ZipInfoSettings settings;

    // ---------- ZipFile.Info ----------

    @Override
    public void printTextInfo(Out out) {
        new ViewInfoEngine(settings, createModel()).printTextInfo(out);
    }

    @Override
    public void decompose(Path dir) {
        new DecomposeInfoEngine(settings, createModel()).decompose(dir);
    }

    @Override
    public CentralDirectory.FileHeader getFileHeader(String entryName) {
        ZipModelReader reader = new ZipModelReader(srcZip,
                                                   settings.getCharsetProvider(),
                                                   settings.getPasswordProvider());
        reader.readCentralData();
        return reader.getCentralDirectory().getFileHeaders().stream()
                     .filter(fh -> fh.getFileName().equalsIgnoreCase(entryName))
                     .findFirst().orElseThrow(() -> new EntryNotFoundException(entryName));
    }

    // ----------

    public BlockModel createModel() {
        return Quietly.doRuntime(() -> {
            BlockZipModelReader reader = new BlockZipModelReader(srcZip,
                                                                 settings.getCharsetProvider(),
                                                                 settings.getPasswordProvider());
            return settings.isReadEntries() ? reader.readWithEntries() : reader.read();
        });
    }

}
