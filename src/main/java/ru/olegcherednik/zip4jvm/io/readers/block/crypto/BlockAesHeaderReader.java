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
package ru.olegcherednik.zip4jvm.io.readers.block.crypto;

import ru.olegcherednik.zip4jvm.crypto.aes.AesEngine;
import ru.olegcherednik.zip4jvm.crypto.aes.AesStrength;
import ru.olegcherednik.zip4jvm.io.in.data.DataInput;
import ru.olegcherednik.zip4jvm.model.block.crypto.AesEncryptionHeaderBlock;
import ru.olegcherednik.zip4jvm.utils.function.Reader;
import ru.olegcherednik.zip4jvm.utils.quitely.Quietly;

import lombok.RequiredArgsConstructor;

import static ru.olegcherednik.zip4jvm.crypto.aes.AesEngine.MAC_SIZE;
import static ru.olegcherednik.zip4jvm.crypto.aes.AesEngine.PASSWORD_CHECKSUM_SIZE;

/**
 * @author Oleg Cherednik
 * @since 23.10.2019
 */
@RequiredArgsConstructor
public class BlockAesHeaderReader implements Reader<AesEncryptionHeaderBlock> {

    private final AesStrength strength;
    private final long compressedSize;

    @Override
    public AesEncryptionHeaderBlock read(DataInput in) {
        return Quietly.doQuietly(() -> {
            AesEncryptionHeaderBlock block = new AesEncryptionHeaderBlock();
            block.getSalt().calcSize(in, () -> in.readBytes(strength.getSaltSize()));
            block.getPasswordChecksum().calcSize(in, () -> in.readBytes(PASSWORD_CHECKSUM_SIZE));
            in.skip(AesEngine.getDataCompressedSize(compressedSize, strength));
            block.getMac().calcSize(in, () -> in.readBytes(MAC_SIZE));
            return block;
        });
    }

}
