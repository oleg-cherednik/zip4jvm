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

import ru.olegcherednik.zip4jvm.crypto.pkware.PkwareHeader;
import ru.olegcherednik.zip4jvm.io.in.DataInput;
import ru.olegcherednik.zip4jvm.io.in.file.random.BaseRandomAccessDataInput;
import ru.olegcherednik.zip4jvm.model.block.crypto.PkwareEncryptionHeaderBlock;
import ru.olegcherednik.zip4jvm.utils.function.Reader;

/**
 * @author Oleg Cherednik
 * @since 24.10.2019
 */
public class BlockPkwareHeaderReader implements Reader<PkwareEncryptionHeaderBlock> {

    @Override
    public PkwareEncryptionHeaderBlock read(DataInput in) {
        PkwareEncryptionHeaderBlock encryptionHeader = new PkwareEncryptionHeaderBlock();
        encryptionHeader.calcSize((BaseRandomAccessDataInput) in, () -> in.readBytes(PkwareHeader.SIZE));
        return encryptionHeader;
    }

}
