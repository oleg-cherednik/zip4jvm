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
import ru.olegcherednik.zip4jvm.model.EncryptionMethod;
import ru.olegcherednik.zip4jvm.model.GeneralPurposeFlag;
import ru.olegcherednik.zip4jvm.model.LocalFileHeader;
import ru.olegcherednik.zip4jvm.model.ZipModel;
import ru.olegcherednik.zip4jvm.model.block.Block;
import ru.olegcherednik.zip4jvm.model.block.BlockModel;
import ru.olegcherednik.zip4jvm.model.block.ExtraFieldBlock;
import ru.olegcherednik.zip4jvm.model.block.ZipEntryBlock;
import ru.olegcherednik.zip4jvm.model.block.crypto.AesEncryptionHeaderBlock;
import ru.olegcherednik.zip4jvm.model.block.crypto.EncryptionHeaderBlock;
import ru.olegcherednik.zip4jvm.model.block.crypto.PkwareEncryptionHeaderBlock;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;
import ru.olegcherednik.zip4jvm.model.extrafield.AlignmentExtraField;
import ru.olegcherednik.zip4jvm.model.extrafield.ExtraField;
import ru.olegcherednik.zip4jvm.model.extrafield.IExtraField;
import ru.olegcherednik.zip4jvm.model.settings.ZipInfoSettings;
import ru.olegcherednik.zip4jvm.view.entry.DataDescriptorView;
import ru.olegcherednik.zip4jvm.view.entry.LocalFileHeaderView;

import java.io.IOException;
import java.io.PrintStream;
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

    @Override
    public boolean printTextInfo(PrintStream out, boolean emptyLine) {
        long pos = 0;

        for (ZipEntryBlock zipEntryBlock : blockModel.getFileNameZipEntryBlock().values()) {
            String fileName = zipEntryBlock.getFileName();

            EncryptionMethod encryptionMethod = zipModel.getZipEntryByFileName(fileName).getEncryptionMethod();

            emptyLine |= localFileHeaderView(zipEntryBlock.getLocalFileHeader(), fileName, pos).print(out, pos != 0 || emptyLine);
            emptyLine |= extraFieldDecompose(zipEntryBlock, settings.getOffs()).printTextInfo(out, false);
            emptyLine |= encryptionHeader(encryptionMethod, zipEntryBlock, pos).printTextInfo(out, emptyLine);
            emptyLine |= dataDescriptor(zipEntryBlock.getDataDescriptor(), zipEntryBlock.getDataDescriptorBlock(), pos, out, emptyLine);

            pos++;
        }

        return emptyLine;
    }

    @Override
    public Path decompose(Path dir) throws IOException {
        long pos = 0;

        for (ZipEntryBlock zipEntryBlock : blockModel.getFileNameZipEntryBlock().values()) {
            String fileName = zipEntryBlock.getFileName();
            EncryptionMethod encryptionMethod = zipModel.getZipEntryByFileName(fileName).getEncryptionMethod();

            Path subDir = Utils.createSubDir(dir, zipModel.getZipEntryByFileName(fileName), pos);

            localFileHeader(subDir, zipEntryBlock.getLocalFileHeader(), fileName, pos);
            extraFieldDecompose(zipEntryBlock, 0).decompose(subDir);
            encryptionHeader(encryptionMethod, zipEntryBlock, pos).decompose(subDir);
            dataDescriptor(subDir, zipEntryBlock.getDataDescriptor(), zipEntryBlock.getDataDescriptorBlock(), pos);
            copyPayload(subDir,
                        zipModel.getZipEntryByFileName(fileName),
                        zipEntryBlock.getLocalFileHeaderBlock(),
                        zipEntryBlock.getEncryptionHeaderBlock());

            pos++;
        }

        return dir;
    }

    private void localFileHeader(Path dir, LocalFileHeader localFileHeader, String fileName, long pos) throws IOException {
        ZipEntryBlock.LocalFileHeaderBlock block = blockModel.getZipEntryBlock(fileName).getLocalFileHeaderBlock();

        Utils.print(dir.resolve(LOCAL_FILE_HEADER + ".txt"),
                    out -> localFileHeaderView(localFileHeader, fileName, pos).print(out));
        Utils.copyLarge(zipModel, dir.resolve(LOCAL_FILE_HEADER + ".data"), block.getContent());
    }

    private void copyPayload(Path dir, ZipEntry zipEntry, ZipEntryBlock.LocalFileHeaderBlock diagLocalFileHeader,
                             EncryptionHeaderBlock encryptionHeaderBlock) throws IOException {
        if (zipEntry.getCompressedSize() == 0 || !settings.isCopyPayload())
            return;

        Block content = diagLocalFileHeader.getContent();
        ExtraFieldBlock extraFieldBlock = diagLocalFileHeader.getExtraFieldBlock();

        long size = zipEntry.getCompressedSize();
        long offs = content.getRelativeOffs() + content.getSize() + extraFieldBlock.getSize();

        EncryptionMethod encryptionMethod = zipEntry.getEncryptionMethod();

        if (encryptionMethod.isAes()) {
            AesEncryptionHeaderBlock block = (AesEncryptionHeaderBlock)encryptionHeaderBlock;

            offs += block.getSalt().getSize();
            offs += block.getPasswordChecksum().getSize();

            size -= block.getSalt().getSize();
            size -= block.getPasswordChecksum().getSize();
            size -= block.getMac().getSize();
        } else if (encryptionMethod == EncryptionMethod.PKWARE) {
            PkwareEncryptionHeaderBlock block = (PkwareEncryptionHeaderBlock)encryptionHeaderBlock;
            offs += block.getSize();
            size -= block.getSize();
        }

        Utils.copyLarge(blockModel.getZipModel(), dir.resolve("payload.data"), offs, size);
    }

    private EncryptionHeaderDecompose encryptionHeader(EncryptionMethod encryptionMethod,
                                                       ZipEntryBlock zipEntryBlock,
                                                       long pos) {
        return new EncryptionHeaderDecompose(zipModel,
                                             settings,
                                             encryptionMethod,
                                             zipEntryBlock.getDecryptionHeader(),
                                             zipEntryBlock.getEncryptionHeaderBlock(),
                                             pos);
    }

    private boolean dataDescriptor(DataDescriptor dataDescriptor,
                                   Block block,
                                   long pos,
                                   PrintStream out,
                                   boolean emptyLine) {
        if (dataDescriptor != null)
            return dataDescriptorView(dataDescriptor, block, pos).print(out, emptyLine);

        return emptyLine;
    }

    private void dataDescriptor(Path dir, DataDescriptor dataDescriptor, Block block, long pos) throws IOException {
        if (dataDescriptor != null) {
            Utils.print(dir.resolve(DATA_DESCRIPTOR + ".txt"),
                        out -> dataDescriptorView(dataDescriptor, block, pos).print(out));
            Utils.copyLarge(zipModel, dir.resolve(DATA_DESCRIPTOR + ".data"), block);
        }
    }

    private LocalFileHeaderView localFileHeaderView(LocalFileHeader localFileHeader, String fileName, long pos) {
        return LocalFileHeaderView.builder()
                                  .localFileHeader(localFileHeader)
                                  .diagLocalFileHeader(blockModel.getZipEntryBlock(fileName).getLocalFileHeaderBlock())
                                  .pos(pos)
                                  .charset(settings.getCharset())
                                  .centralDirectoryEncrypted(zipModel.isCentralDirectoryEncrypted())
                                  .position(settings.getOffs(), settings.getColumnWidth(), zipModel.getTotalDisks()).build();
    }

    private DataDescriptorView dataDescriptorView(DataDescriptor dataDescriptor, Block block, long pos) {
        return new DataDescriptorView(dataDescriptor, block, pos, settings.getOffs(), settings.getColumnWidth(), zipModel.getTotalDisks());
    }

    private Decompose extraFieldDecompose(ZipEntryBlock zipEntryBlock, int offs) {
        IExtraField extraField = zipEntryBlock.getLocalFileHeader().getExtraField();

        if (extraField instanceof AlignmentExtraField)
            return Decompose.NULL;

        GeneralPurposeFlag generalPurposeFlag = zipEntryBlock.getLocalFileHeader().getGeneralPurposeFlag();
        return new ExtraFieldDecompose(zipModel,
                                       (ExtraField)extraField,
                                       zipEntryBlock.getLocalFileHeaderBlock().getExtraFieldBlock(),
                                       generalPurposeFlag,
                                       offs,
                                       settings.getColumnWidth());
    }

}
