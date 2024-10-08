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
package ru.olegcherednik.zip4jvm.io.readers.block.crypto.strong;

import ru.olegcherednik.zip4jvm.crypto.strong.DecryptionHeader;
import ru.olegcherednik.zip4jvm.crypto.strong.Recipient;
import ru.olegcherednik.zip4jvm.io.in.data.DataInput;
import ru.olegcherednik.zip4jvm.io.readers.crypto.strong.DecryptionHeaderReader;
import ru.olegcherednik.zip4jvm.model.block.crypto.strong.DecryptionHeaderBlock;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * @author Oleg Cherednik
 * @since 24.02.2020
 */
@Getter
@RequiredArgsConstructor
public class BlockDecryptionHeaderReader extends DecryptionHeaderReader {

    private final DecryptionHeaderBlock decryptionHeaderBlock;

    public BlockDecryptionHeaderReader() {
        this(new DecryptionHeaderBlock());
    }

    @Override
    public DecryptionHeader read(DataInput in) {
        return decryptionHeaderBlock.calcSize(in, () -> super.read(in));
    }

    @Override
    protected List<Recipient> readRecipients(int total, int hashSize, DataInput in) {
        return decryptionHeaderBlock.getRecipientsBlock().calcSize(in, () -> super.readRecipients(total, hashSize, in));
    }

}
