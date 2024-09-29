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
package ru.olegcherednik.zip4jvm.view.crypto;

import ru.olegcherednik.zip4jvm.crypto.strong.DecryptionHeader;
import ru.olegcherednik.zip4jvm.model.block.crypto.AesEncryptionHeaderBlock;
import ru.olegcherednik.zip4jvm.model.block.crypto.strong.DecryptionHeaderBlock;
import ru.olegcherednik.zip4jvm.model.block.crypto.EncryptionHeaderBlock;
import ru.olegcherednik.zip4jvm.model.block.crypto.PkwareEncryptionHeaderBlock;
import ru.olegcherednik.zip4jvm.view.BaseView;
import ru.olegcherednik.zip4jvm.view.crypto.strong.DecryptionHeaderView;

import java.io.PrintStream;

import static ru.olegcherednik.zip4jvm.utils.ValidationUtils.requireNotNull;

/**
 * @author Oleg Cherednik
 * @since 24.10.2019
 */
public final class EncryptionHeaderView extends BaseView {

    private final DecryptionHeader decryptionHeader;
    private final EncryptionHeaderBlock block;
    private final long pos;

    public EncryptionHeaderView(DecryptionHeader decryptionHeader, EncryptionHeaderBlock block, long pos, int offs, int columnWidth,
                                long totalDisks) {
        super(offs, columnWidth, totalDisks);
        this.decryptionHeader = decryptionHeader;
        this.block = requireNotNull(block, "EncryptionHeaderView.centralDirectory");
        this.pos = pos;
    }

    @Override
    public boolean printTextInfo(PrintStream out) {
        if (block instanceof AesEncryptionHeaderBlock)
            new AesEncryptionHeaderView((AesEncryptionHeaderBlock)block, pos, offs, columnWidth, totalDisks).printTextInfo(out);
        else if (block instanceof PkwareEncryptionHeaderBlock)
            new PkwareEncryptionHeaderView((PkwareEncryptionHeaderBlock)block, pos, offs, columnWidth, totalDisks).printTextInfo(out);
        else if (block instanceof DecryptionHeaderBlock)
            new DecryptionHeaderView(decryptionHeader, (DecryptionHeaderBlock)block, pos, offs, columnWidth, totalDisks).printTextInfo(out);

        return true;
    }

}
