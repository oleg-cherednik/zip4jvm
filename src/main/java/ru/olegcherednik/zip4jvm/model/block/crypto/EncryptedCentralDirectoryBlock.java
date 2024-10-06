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
package ru.olegcherednik.zip4jvm.model.block.crypto;

import lombok.Getter;
import lombok.Setter;
import ru.olegcherednik.zip4jvm.decompose.Utils;
import ru.olegcherednik.zip4jvm.model.ZipModel;
import ru.olegcherednik.zip4jvm.model.block.Block;
import ru.olegcherednik.zip4jvm.model.block.CentralDirectoryBlock;
import ru.olegcherednik.zip4jvm.model.block.EncryptedExtraFieldBlock;
import ru.olegcherednik.zip4jvm.model.block.crypto.strong.DecryptionHeaderBlock;

import java.io.IOException;
import java.nio.file.Path;

/**
 * @author Oleg Cherednik
 * @since 25.12.2022
 */
@Getter
public class EncryptedCentralDirectoryBlock extends CentralDirectoryBlock {

    private final DecryptionHeaderBlock decryptionHeaderBlock = new DecryptionHeaderBlock();
    private final Block encryptedCentralDirectoryBlock = new Block();
    @Setter
    private byte[] decryptedCentralDirectory;
    @Setter
    private byte[] decompressedCentralDirectory;

    @Override
    public EncryptedFileHeaderBlock createFileHeaderBlock() {
        return new EncryptedFileHeaderBlock(decompressedCentralDirectory);
    }

    public static class EncryptedFileHeaderBlock extends CentralDirectoryBlock.FileHeaderBlock {

        public EncryptedFileHeaderBlock(byte[] buf) {
            super(new EncryptedExtraFieldBlock(buf));
        }

        @Override
        public void copyLarge(ZipModel zipModel, Path out) throws IOException {
            Utils.copyByteArray(out, ((EncryptedExtraFieldBlock) extraFieldBlock).getBuf(), this);
        }
    }

}