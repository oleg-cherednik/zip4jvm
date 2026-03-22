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

import ru.olegcherednik.zip4jvm.model.DataDescriptor;
import ru.olegcherednik.zip4jvm.model.Encryption;
import ru.olegcherednik.zip4jvm.model.LocalFileHeader;
import ru.olegcherednik.zip4jvm.model.ZipModel;
import ru.olegcherednik.zip4jvm.model.block.Block;
import ru.olegcherednik.zip4jvm.model.block.BlockModel;
import ru.olegcherednik.zip4jvm.model.block.ZipEntryBlock;
import ru.olegcherednik.zip4jvm.model.block.crypto.AesEncryptionHeaderBlock;
import ru.olegcherednik.zip4jvm.model.block.crypto.EncryptionHeaderBlock;
import ru.olegcherednik.zip4jvm.model.block.crypto.PkwareEncryptionHeaderBlock;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;
import ru.olegcherednik.zip4jvm.model.extrafield.AlignmentExtraField;
import ru.olegcherednik.zip4jvm.model.extrafield.ExtraField;
import ru.olegcherednik.zip4jvm.model.extrafield.PkwareExtraField;
import ru.olegcherednik.zip4jvm.model.settings.ZipInfoSettings;
import ru.olegcherednik.zip4jvm.view.entry.DataDescriptorView;
import ru.olegcherednik.zip4jvm.view.entry.LocalFileHeaderView;

import java.nio.file.Path;

/**
 * @author Oleg Cherednik
 * @since 09.12.2019
 */
public final class LocalFileHeaderDecompose implements Decompose {

    private static final String LOCAL_FILE_HEADER = "local_file_header";
    private static final String DATA_DESCRIPTOR = "data_descriptor";

    private final BlockModel blockModel;
    private final ZipModel zipModel;
    private final ZipInfoSettings settings;

    public LocalFileHeaderDecompose(BlockModel blockModel, ZipInfoSettings settings) {
        this.blockModel = blockModel;
        zipModel = blockModel.getZipModel();
        this.settings = settings;
    }

    // ---------- Decompose ----------

    @Override
    public Path decompose(Path dir) {
        long pos = 0;

        for (ZipEntryBlock zipEntryBlock : blockModel.getFileNameZipEntryBlock().values()) {
            String fileName = zipEntryBlock.getFileName();
            Encryption encryption = zipModel.getZipEntryByFileName(fileName).getEncryption();

            Path subDir = Utils.createSubDir(dir, zipModel.getZipEntryByFileName(fileName), pos);

            localFileHeader(subDir, zipEntryBlock.getLocalFileHeader(), fileName, pos);
            extraFieldDecompose(zipEntryBlock, 0).decompose(subDir);
            encryptionHeaderDecompose(encryption, zipEntryBlock, pos).decompose(subDir);
            dataDescriptor(subDir, zipEntryBlock.getDataDescriptor(), zipEntryBlock.getDataDescriptorBlock(), pos);
            copyPayload(subDir,
                        zipModel.getZipEntryByFileName(fileName),
                        zipEntryBlock.getLocalFileHeaderBlock(),
                        zipEntryBlock.getEncryptionHeaderBlock());

            pos++;
        }

        return dir;
    }

    // ----------

    private void localFileHeader(Path dir, LocalFileHeader localFileHeader, String fileName, long pos) {
        ZipEntryBlock.LocalFileHeaderBlock block = blockModel.getZipEntryBlock(fileName).getLocalFileHeaderBlock();
        Utils.print(dir.resolve(LOCAL_FILE_HEADER + EXT_TXT),
                    out -> localFileHeaderView(localFileHeader, fileName, pos).printTextInfo(out));
        Utils.copyLarge(zipModel, dir.resolve(LOCAL_FILE_HEADER + EXT_DATA), block.getContent());
    }

    private void copyPayload(Path dir,
                             ZipEntry zipEntry,
                             ZipEntryBlock.LocalFileHeaderBlock diagLocalFileHeader,
                             EncryptionHeaderBlock encryptionHeaderBlock) {
        if (zipEntry.getCompressedSize() == 0 || !settings.isCopyPayload())
            return;

        Block content = diagLocalFileHeader.getContent();
        long size = zipEntry.getCompressedSize();
        // TODO here we should use SrcZip methods
        long absOffs = content.getDiskOffs() + content.getSize();

        Encryption encryption = zipEntry.getEncryption();

        if (encryption.isAes()) {
            AesEncryptionHeaderBlock block = (AesEncryptionHeaderBlock) encryptionHeaderBlock;

            absOffs += block.getSalt().getSize();
            absOffs += block.getPasswordChecksum().getSize();

            size -= block.getSalt().getSize();
            size -= block.getPasswordChecksum().getSize();
            size -= block.getMac().getSize();
        } else if (encryption == Encryption.PKWARE) {
            PkwareEncryptionHeaderBlock block = (PkwareEncryptionHeaderBlock) encryptionHeaderBlock;
            absOffs += block.getSize();
            size -= block.getSize();
        }

        Utils.copyLarge(blockModel.getZipModel(), dir.resolve("payload" + EXT_DATA), absOffs, absOffs, size);
    }

    private Decompose encryptionHeaderDecompose(Encryption encryption, ZipEntryBlock zipEntryBlock, long pos) {
        if (zipEntryBlock.getEncryptionHeaderBlock() == null)
            return NULL;
        return new EncryptionHeaderDecompose(zipModel,
                                             settings,
                                             encryption,
                                             zipEntryBlock.getDecryptionHeader(),
                                             zipEntryBlock.getEncryptionHeaderBlock(),
                                             pos);
    }

    private void dataDescriptor(Path dir, DataDescriptor dataDescriptor, Block block, long pos) {
        if (dataDescriptor != null) {
            Utils.print(dir.resolve(DATA_DESCRIPTOR + EXT_TXT),
                        out -> dataDescriptorView(dataDescriptor, block, pos).printTextInfo(out));
            Utils.copyLarge(zipModel, dir.resolve(DATA_DESCRIPTOR + EXT_DATA), block);
        }
    }

    private LocalFileHeaderView localFileHeaderView(LocalFileHeader localFileHeader, String fileName, long pos) {
        return LocalFileHeaderView.builder()
                                  .localFileHeader(localFileHeader)
                                  .diagLocalFileHeader(blockModel.getZipEntryBlock(fileName).getLocalFileHeaderBlock())
                                  .pos(pos)
                                  .charset(settings.getCharset())
                                  .centralDirectoryEncrypted(zipModel.isCentralDirectoryEncrypted())
                                  .position(settings.getOffs(), settings.getColumnWidth(), zipModel.getTotalDisks())
                                  .build();
    }

    private DataDescriptorView dataDescriptorView(DataDescriptor dataDescriptor, Block block, long pos) {
        return new DataDescriptorView(dataDescriptor,
                                      block,
                                      pos,
                                      settings.getOffs(),
                                      settings.getColumnWidth(),
                                      zipModel.getTotalDisks());
    }

    private Decompose extraFieldDecompose(ZipEntryBlock zipEntryBlock, int offs) {
        ExtraField extraField = zipEntryBlock.getLocalFileHeader().getExtraField();

        if (extraField instanceof AlignmentExtraField)
            return NULL;

        return pkwareExtraFieldDecompose(zipEntryBlock, (PkwareExtraField) extraField, offs);
    }

    private PkwareExtraFieldDecompose pkwareExtraFieldDecompose(ZipEntryBlock zipEntryBlock,
                                                                PkwareExtraField extraField,
                                                                int offs) {
        return new PkwareExtraFieldDecompose(zipModel,
                                             extraField,
                                             zipEntryBlock.getLocalFileHeaderBlock().getExtraFieldBlock(),
                                             zipEntryBlock.getLocalFileHeader().getGeneralPurposeFlag(),
                                             offs,
                                             settings.getColumnWidth());
    }

}
