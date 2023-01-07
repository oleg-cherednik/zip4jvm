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
import ru.olegcherednik.zip4jvm.model.CompressionMethod;
import ru.olegcherednik.zip4jvm.model.block.BlockModel;
import ru.olegcherednik.zip4jvm.model.block.EncryptedCentralDirectoryBlock;
import ru.olegcherednik.zip4jvm.model.settings.ZipInfoSettings;
import ru.olegcherednik.zip4jvm.view.centraldirectory.CentralDirectoryView;
import ru.olegcherednik.zip4jvm.view.centraldirectory.EncryptedCentralDirectoryView;
import ru.olegcherednik.zip4jvm.view.crypto.DecryptionHeaderView;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

/**
 * @author Oleg Cherednik
 * @since 25.12.2022
 */
public final class EncryptedCentralDirectoryDecompose extends CentralDirectoryDecompose {

    private static final String DECRYPTION_HEADER = "decryption_header";

    private final EncryptedCentralDirectoryBlock block;

    public EncryptedCentralDirectoryDecompose(BlockModel blockModel, ZipInfoSettings settings) {
        super(blockModel, settings);
        block = (EncryptedCentralDirectoryBlock)blockModel.getCentralDirectoryBlock();
    }

    @Override
    public Path decompose(Path dir) throws IOException {
        dir = super.decompose(dir);
        decryptionHeader(dir);
        encryptedCentralDirectory(dir);
        compressedCentralDirectory(dir);
        return dir;
    }

    @Override
    protected void centralDirectory(Path dir) throws IOException {
        Utils.print(dir.resolve(CENTRAL_DIRECTORY + ".txt"), out -> centralDirectoryView().printTextInfo(out));
        Utils.copyByteArray(dir.resolve(CENTRAL_DIRECTORY + ".data"),
                            Optional.ofNullable(block.getDecompressedCentralDirectory())
                                    .orElse(block.getDecompressedCentralDirectory()));
    }

    @Override
    protected CentralDirectoryView centralDirectoryView() {
        return new EncryptedCentralDirectoryView(centralDirectory,
                                                 extensibleDataSector,
                                                 block.getEncryptedCentralDirectoryBlock(),
                                                 settings.getOffs(),
                                                 settings.getColumnWidth(),
                                                 zipModel.getTotalDisks());
    }

    @Override
    protected FileHeaderDecompose fileHeaderDecompose() {
        return new EncryptedFileHeaderDecompose(zipModel, settings, centralDirectory, block);
    }

    private void decryptionHeader(Path dir) throws IOException {
        Utils.print(dir.resolve(DECRYPTION_HEADER + ".txt"), out -> decryptionHeaderView().printTextInfo(out));
        Utils.copyLarge(zipModel, dir.resolve(DECRYPTION_HEADER + ".data"), block.getDecryptionHeaderBlock());
    }

    private void encryptedCentralDirectory(Path dir) throws IOException {
        String fileName = CENTRAL_DIRECTORY;
        Compression compression = Compression.parseCompressionMethod(extensibleDataSector.getCompressionMethod());

        if (compression != Compression.STORE)
            fileName += '_' + compression.getTitle();

        fileName += '_' + extensibleDataSector.getEncryptionAlgorithm().getTitle().toLowerCase();
        Utils.copyLarge(zipModel, dir.resolve(fileName + ".data"), block.getEncryptedCentralDirectoryBlock());
    }

    private void compressedCentralDirectory(Path dir) throws IOException {
        if (block.getDecryptedCentralDirectory() != null) {
            Compression compression = Compression.parseCompressionMethod(extensibleDataSector.getCompressionMethod());
            String fileName = (CENTRAL_DIRECTORY + '_' + compression.getTitle()).toLowerCase();
            Utils.copyByteArray(dir.resolve(fileName + ".data"), block.getDecryptedCentralDirectory());
        }
    }

    private DecryptionHeaderView decryptionHeaderView() {
        return new DecryptionHeaderView(centralDirectory.getDecryptionHeader(),
                                        block.getDecryptionHeaderBlock(),
                                        null,
                                        settings.getOffs(),
                                        settings.getColumnWidth(),
                                        zipModel.getTotalDisks());
    }
}
