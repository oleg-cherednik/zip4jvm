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

import ru.olegcherednik.zip4jvm.model.block.crypto.AesEncryptionHeaderBlock;
import ru.olegcherednik.zip4jvm.view.BaseView;
import ru.olegcherednik.zip4jvm.view.ByteArrayHexView;

import java.io.PrintStream;

import static ru.olegcherednik.zip4jvm.utils.ValidationUtils.requireNotNull;

/**
 * @author Oleg Cherednik
 * @since 09.11.2019
 */
final class AesEncryptionHeaderView extends BaseView {

    private final AesEncryptionHeaderBlock block;
    private final long pos;

    AesEncryptionHeaderView(AesEncryptionHeaderBlock block, long pos, int offs, int columnWidth, long totalDisks) {
        super(offs, columnWidth, totalDisks);
        this.block = requireNotNull(block, "BlockAesEncryptionHeaderView.centralDirectory");
        this.pos = pos;
    }

    @Override
    public boolean printTextInfo(PrintStream out) {
        printSubTitle(out, pos, "(AES) encryption header");
        printSalt(out);
        printPasswordChecksum(out);
        printMac(out);
        return true;
    }

    private void printSalt(PrintStream out) {
        printValueWithLocation(out, "salt:", block.getSalt());
        new ByteArrayHexView(block.getSalt().getData(), offs, columnWidth).printTextInfo(out);
    }

    private void printPasswordChecksum(PrintStream out) {
        printValueWithLocation(out, "password checksum:", block.getPasswordChecksum());
        new ByteArrayHexView(block.getPasswordChecksum().getData(), offs, columnWidth).printTextInfo(out);
    }

    private void printMac(PrintStream out) {
        printValueWithLocation(out, "mac:", block.getMac());
        new ByteArrayHexView(block.getMac().getData(), offs, columnWidth).printTextInfo(out);
    }
}
