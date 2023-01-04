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
package ru.olegcherednik.zip4jvm.model.block;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import ru.olegcherednik.zip4jvm.decompose.Utils;
import ru.olegcherednik.zip4jvm.io.in.data.DataInputLocation;
import ru.olegcherednik.zip4jvm.model.ZipModel;
import ru.olegcherednik.zip4jvm.model.block.crypto.DecryptionHeaderBlock;
import ru.olegcherednik.zip4jvm.utils.function.LocalSupplier;

import java.io.IOException;
import java.nio.file.Path;

/**
 * @author Oleg Cherednik
 * @since 25.12.2022
 */
@Getter
public class EncryptedCentralDirectoryBlock extends BaseCentralDirectoryBlock {

    private final CentralDirectoryBlock centralDirectoryBlock;
    private final DecryptionHeaderBlock decryptionHeaderBlock = new DecryptionHeaderBlock();
    private final Block encryptedCentralDirectoryBlock = new Block();
    @Setter
    private byte[] decryptedCentralDirectory;
    @Setter
    private byte[] decompressedCentralDirectory;

    public EncryptedCentralDirectoryBlock(CentralDirectoryBlock centralDirectoryBlock) {
        this.centralDirectoryBlock = centralDirectoryBlock;
    }

    @Override
    public <T> T calcSize(DataInputLocation dataInputLocation, LocalSupplier<T> task) {
        return super.calcSize(dataInputLocation, task);
    }

    @Deprecated
    public void calcSize(DataInputLocation in) {
        super.calcSize(in);
    }

    @Override
    public void addFileHeader(String fileName, CentralDirectoryBlock.FileHeaderBlock block) {
        centralDirectoryBlock.addFileHeader(fileName, block);
    }

    @Override
    public void setDigitalSignature(Block block) {
        centralDirectoryBlock.setDigitalSignature(block);
    }

    @Override
    public Block getDigitalSignature() {
        return centralDirectoryBlock.getDigitalSignature();
    }

    @Override
    public CentralDirectoryBlock.FileHeaderBlock getFileHeader(String fileName) {
        return centralDirectoryBlock.getFileHeader(fileName);
    }

    @Override
    public EncryptedFileHeaderBlock createFileHeaderBlock() {
        return new EncryptedFileHeaderBlock(decompressedCentralDirectory);
    }

    @Getter
    public static class EncryptedFileHeaderBlock extends CentralDirectoryBlock.FileHeaderBlock {

        private final byte[] buf;

        public EncryptedFileHeaderBlock(byte[] buf) {
            super(new EncryptedExtraFieldBlock(buf));
            this.buf = buf;
        }

        @Override
        public void copyLarge(ZipModel zipModel, Path out) throws IOException {
            Utils.copyByteArray(out, buf, this);
        }
    }

}
