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

import ru.olegcherednik.zip4jvm.model.DataDescriptor;
import ru.olegcherednik.zip4jvm.model.LocalFileHeader;
import ru.olegcherednik.zip4jvm.model.ZipModel;
import ru.olegcherednik.zip4jvm.model.block.Block;
import ru.olegcherednik.zip4jvm.model.block.BlockModel;
import ru.olegcherednik.zip4jvm.model.block.ZipEntryBlock;
import ru.olegcherednik.zip4jvm.model.extrafield.AlignmentExtraField;
import ru.olegcherednik.zip4jvm.model.extrafield.ExtraField;
import ru.olegcherednik.zip4jvm.model.extrafield.PkwareExtraField;
import ru.olegcherednik.zip4jvm.model.settings.ZipInfoSettings;
import ru.olegcherednik.zip4jvm.view.View;
import ru.olegcherednik.zip4jvm.view.crypto.EncryptionHeaderView;
import ru.olegcherednik.zip4jvm.view.entry.DataDescriptorView;
import ru.olegcherednik.zip4jvm.view.entry.LocalFileHeaderView;
import ru.olegcherednik.zip4jvm.view.extrafield.PkwareExtraFieldView;
import ru.olegcherednik.zip4jvm.view.out.Out;

/**
 * @author Oleg Cherednik
 * @since 22.03.2026
 */
public final class LocalFileHeaderInCentralDirectoryView implements View {

    private final BlockModel blockModel;
    private final ZipModel zipModel;
    private final ZipInfoSettings settings;

    public LocalFileHeaderInCentralDirectoryView(BlockModel blockModel, ZipInfoSettings settings) {
        this.blockModel = blockModel;
        zipModel = blockModel.getZipModel();
        this.settings = settings;
    }

    // ---------- View ----------

    @Override
    public void printTextInfo(Out out) {
        long pos = 0;

        for (ZipEntryBlock zipEntryBlock : blockModel.getFileNameZipEntryBlock().values()) {
            String fileName = zipEntryBlock.getFileName();

            out.printEmptyLine();
            localFileHeaderView(zipEntryBlock.getLocalFileHeader(), fileName, pos).printTextInfo(out);
            extraFieldView(zipEntryBlock, settings.getOffs()).printTextInfo(out);
            encryptionHeaderView(zipEntryBlock, pos).printTextInfo(out);
            dataDescriptor(zipEntryBlock, pos).printTextInfo(out);
            pos++;
        }
    }

    // ----------

    private View encryptionHeaderView(ZipEntryBlock zipEntryBlock, long pos) {
        if (zipEntryBlock.getEncryptionHeaderBlock() == null)
            return NULL;

        return new EncryptionHeaderView(zipEntryBlock.getDecryptionHeader(),
                                        zipEntryBlock.getEncryptionHeaderBlock(),
                                        pos,
                                        settings.getOffs(),
                                        settings.getColumnWidth(),
                                        zipModel.getTotalDisks());
    }

    private View dataDescriptor(ZipEntryBlock zipEntryBlock, long pos) {
        if (zipEntryBlock.getDataDescriptor() == null)
            return NULL;
        return dataDescriptorView(zipEntryBlock.getDataDescriptor(), zipEntryBlock.getDataDescriptorBlock(), pos);
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

    private View extraFieldView(ZipEntryBlock zipEntryBlock, int offs) {
        ExtraField extraField = zipEntryBlock.getLocalFileHeader().getExtraField();

        if (extraField instanceof AlignmentExtraField)
            return NULL;

        return pkwareExtraFieldView(zipEntryBlock, (PkwareExtraField) extraField, offs);
    }

    private PkwareExtraFieldView pkwareExtraFieldView(ZipEntryBlock zipEntryBlock,
                                                      PkwareExtraField extraField,
                                                      int offs) {
        return new PkwareExtraFieldView(zipModel,
                                        extraField,
                                        zipEntryBlock.getLocalFileHeaderBlock().getExtraFieldBlock(),
                                        zipEntryBlock.getLocalFileHeader().getGeneralPurposeFlag(),
                                        offs,
                                        settings.getColumnWidth());
    }

}
