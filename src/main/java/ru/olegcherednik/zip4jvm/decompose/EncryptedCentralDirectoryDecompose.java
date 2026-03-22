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
package ru.olegcherednik.zip4jvm.decompose;

import ru.olegcherednik.zip4jvm.model.Compression;
import ru.olegcherednik.zip4jvm.model.Zip64;
import ru.olegcherednik.zip4jvm.model.block.BlockModel;
import ru.olegcherednik.zip4jvm.model.block.crypto.EncryptedCentralDirectoryBlock;
import ru.olegcherednik.zip4jvm.model.settings.ZipInfoSettings;
import ru.olegcherednik.zip4jvm.utils.PathUtils;
import ru.olegcherednik.zip4jvm.view.cd.EncryptedCentralDirectoryView;
import ru.olegcherednik.zip4jvm.view.crypto.strong.DecryptionHeaderView;

import java.nio.file.Path;
import java.util.Locale;
import java.util.function.Supplier;

/**
 * @author Oleg Cherednik
 * @since 25.12.2022
 */
public final class EncryptedCentralDirectoryDecompose extends CentralDirectoryDecompose {

    private static final String DECRYPTION_HEADER = "decryption_header";

    private final Zip64.ExtensibleDataSector extensibleDataSector;
    private final EncryptedCentralDirectoryBlock block;
    private final Supplier<DecryptionHeaderView> createDecryptionHeaderView;

    public EncryptedCentralDirectoryDecompose(BlockModel blockModel, ZipInfoSettings settings) {
        super(blockModel,
              settings,
              () -> new EncryptedCentralDirectoryView(blockModel, settings),
              () -> new EncryptedFileHeaderDecompose(blockModel, settings));
        extensibleDataSector = blockModel.getZip64().getExtensibleDataSector();
        block = (EncryptedCentralDirectoryBlock) blockModel.getCentralDirectoryBlock();
        createDecryptionHeaderView = () -> new DecryptionHeaderView(blockModel, settings);
    }

    // ---------- Decompose ----------

    @Override
    public Path decompose(Path dir) {
        dir = super.decompose(dir);
        decryptionHeader(dir);
        encryptedCentralDirectory(dir);
        compressedCentralDirectory(dir);
        return dir;
    }

    // ----------

    private void decryptionHeader(Path dir) {
        Utils.print(dir.resolve(DECRYPTION_HEADER + EXT_TXT),
                    out -> createDecryptionHeaderView.get().printTextInfo(out));
        Utils.copyLarge(zipModel, dir.resolve(DECRYPTION_HEADER + EXT_DATA), block.getDecryptionHeaderBlock());
    }

    private void encryptedCentralDirectory(Path dir) {
        String fileName = CENTRAL_DIRECTORY;
        Compression compression = extensibleDataSector.getCompression();

        if (compression != Compression.STORE)
            fileName += '_' + compression.getFileMarker();

        fileName += '_' + extensibleDataSector.getEncryptionAlgorithm().getTitle().toLowerCase(Locale.ENGLISH);
        Utils.copyLarge(zipModel, dir.resolve(fileName + EXT_DATA), block.getEcdBlock());
    }

    private void compressedCentralDirectory(Path dir) {
        if (block.getDecryptedCentralDirectory() != null) {
            String fileMarker = extensibleDataSector.getCompression().getFileMarker();
            String fileName = (CENTRAL_DIRECTORY + '_' + fileMarker).toLowerCase(Locale.ENGLISH);
            PathUtils.copyByteArray(dir.resolve(fileName + EXT_DATA), block.getDecryptedCentralDirectory());
        }
    }

}
