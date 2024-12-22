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
package ru.olegcherednik.zip4jvm.engine.info;

import ru.olegcherednik.zip4jvm.ZipFile;
import ru.olegcherednik.zip4jvm.decompose.CentralDirectoryDecompose;
import ru.olegcherednik.zip4jvm.decompose.Decompose;
import ru.olegcherednik.zip4jvm.decompose.EncryptedCentralDirectoryDecompose;
import ru.olegcherednik.zip4jvm.decompose.EndCentralDirectoryDecompose;
import ru.olegcherednik.zip4jvm.decompose.Zip64Decompose;
import ru.olegcherednik.zip4jvm.decompose.ZipEntriesDecompose;
import ru.olegcherednik.zip4jvm.exception.EntryNotFoundException;
import ru.olegcherednik.zip4jvm.io.readers.ZipModelReader;
import ru.olegcherednik.zip4jvm.io.readers.block.BlockZipModelReader;
import ru.olegcherednik.zip4jvm.model.CentralDirectory;
import ru.olegcherednik.zip4jvm.model.block.BlockModel;
import ru.olegcherednik.zip4jvm.model.settings.ZipInfoSettings;
import ru.olegcherednik.zip4jvm.model.src.SrcZip;

import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author Oleg Cherednik
 * @since 15.11.2019
 */
@RequiredArgsConstructor
public final class InfoEngine implements ZipFile.Info {

    private final SrcZip srcZip;
    private final ZipInfoSettings settings;

    @Override
    @SuppressWarnings("NonShortCircuitBooleanExpression")
    public void printTextInfo(PrintStream out) throws IOException {
        BlockModel blockModel = createModel();

        boolean emptyLine = new EndCentralDirectoryDecompose(blockModel, settings).printTextInfo(out, false);
        emptyLine |= new Zip64Decompose(blockModel, settings).printTextInfo(out, emptyLine);
        emptyLine |= new CentralDirectoryDecompose(blockModel, settings).printTextInfo(out, emptyLine);
        new ZipEntriesDecompose(blockModel, settings).printTextInfo(out, emptyLine);
    }

    @Override
    public void decompose(Path dir) throws IOException {
        Files.createDirectories(dir);

        BlockModel blockModel = createModel();

        new EndCentralDirectoryDecompose(blockModel, settings).decompose(dir);
        new Zip64Decompose(blockModel, settings).decompose(dir);
        getCentralDirectoryDecompose(blockModel).decompose(dir);
        new ZipEntriesDecompose(blockModel, settings).decompose(dir);
    }

    private Decompose getCentralDirectoryDecompose(BlockModel blockModel) {
        if (blockModel.getZipModel().isCentralDirectoryEncrypted())
            return new EncryptedCentralDirectoryDecompose(blockModel, settings);
        return new CentralDirectoryDecompose(blockModel, settings);
    }

    @Override
    public CentralDirectory.FileHeader getFileHeader(String entryName) throws IOException {
        ZipModelReader reader = new ZipModelReader(srcZip,
                                                   settings.getCustomizeCharset(),
                                                   settings.getPasswordProvider(),
                                                   false);
        reader.readCentralData();
        return reader.getCentralDirectory().getFileHeaders().stream()
                     .filter(fh -> fh.getFileName().equalsIgnoreCase(entryName))
                     .findFirst().orElseThrow(() -> new EntryNotFoundException(entryName));
    }

    public BlockModel createModel() throws IOException {
        BlockZipModelReader reader = new BlockZipModelReader(srcZip,
                                                             settings.getCustomizeCharset(),
                                                             settings.getPasswordProvider());
        return settings.isReadEntries() ? reader.readWithEntries() : reader.read();
    }

}
