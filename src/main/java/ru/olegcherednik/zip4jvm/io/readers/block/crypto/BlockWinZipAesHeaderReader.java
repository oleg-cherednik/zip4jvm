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
package ru.olegcherednik.zip4jvm.io.readers.block.crypto;

import ru.olegcherednik.zip4jvm.crypto.aes.AesStrength;
import ru.olegcherednik.zip4jvm.crypto.aes.factory.WinZipAesDecoderFactory;
import ru.olegcherednik.zip4jvm.io.in.DataInput;
import ru.olegcherednik.zip4jvm.io.in.file.random.BaseRandomAccessDataInput;
import ru.olegcherednik.zip4jvm.model.block.crypto.WinZipAesEncryptionHeaderBlock;
import ru.olegcherednik.zip4jvm.utils.function.Reader;

import lombok.RequiredArgsConstructor;

import static ru.olegcherednik.zip4jvm.crypto.aes.factory.BaseWinZipAesFactory.MAC_SIZE;
import static ru.olegcherednik.zip4jvm.crypto.aes.factory.BaseWinZipAesFactory.PASSWORD_CHECKSUM_SIZE;

/**
 * @author Oleg Cherednik
 * @since 23.10.2019
 */
@RequiredArgsConstructor
public class BlockWinZipAesHeaderReader implements Reader<WinZipAesEncryptionHeaderBlock> {

    private final AesStrength strength;
    private final long compressedSize;

    @Override
    public WinZipAesEncryptionHeaderBlock read(DataInput in) {
        WinZipAesEncryptionHeaderBlock block = new WinZipAesEncryptionHeaderBlock();
        block.getSalt().calcSize((BaseRandomAccessDataInput) in, () -> in.readBytes(strength.getSaltSize()));
        block.getPasswordChecksum().calcSize((BaseRandomAccessDataInput) in,
                                             () -> in.readBytes(PASSWORD_CHECKSUM_SIZE));
        in.skip(WinZipAesDecoderFactory.getDataCompressedSize(strength, compressedSize));
        block.getMac().calcSize((BaseRandomAccessDataInput) in, () -> in.readBytes(MAC_SIZE));
        return block;
    }

}
