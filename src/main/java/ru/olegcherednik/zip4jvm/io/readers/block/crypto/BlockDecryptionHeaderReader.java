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

import ru.olegcherednik.zip4jvm.crypto.strong.DecryptionHeader;
import ru.olegcherednik.zip4jvm.io.in.DataInput;
import ru.olegcherednik.zip4jvm.io.readers.DecryptionHeaderReader;
import ru.olegcherednik.zip4jvm.model.block.crypto.DecryptionHeaderBlock;
import ru.olegcherednik.zip4jvm.utils.function.Reader;

import lombok.RequiredArgsConstructor;

/**
 * @author Oleg Cherednik
 * @since 24.02.2020
 */
@RequiredArgsConstructor
public class BlockDecryptionHeaderReader implements Reader<DecryptionHeaderBlock> {

    private final long compressedSize;

    @Override
    public DecryptionHeaderBlock read(DataInput in) {
        // TODO this hotifx. Do investigate about correct implementation (have no time now to do this)
        DecryptionHeader decryptionHeader = new DecryptionHeaderReader().read(in);
        DecryptionHeaderBlock block = new DecryptionHeaderBlock();
        block.setDecryptionHeader(decryptionHeader);
        return block;
    }

}
