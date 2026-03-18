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
package ru.olegcherednik.zip4jvm.view.crypto.strong;

import ru.olegcherednik.zip4jvm.crypto.strong.DecryptionHeader;
import ru.olegcherednik.zip4jvm.crypto.strong.EncryptionAlgorithm;
import ru.olegcherednik.zip4jvm.crypto.strong.Flag;
import ru.olegcherednik.zip4jvm.crypto.strong.HashAlgorithm;
import ru.olegcherednik.zip4jvm.crypto.strong.Recipient;
import ru.olegcherednik.zip4jvm.model.block.crypto.strong.DecryptionHeaderBlock;
import ru.olegcherednik.zip4jvm.view.BaseView;
import ru.olegcherednik.zip4jvm.view.ByteArrayHexView;
import ru.olegcherednik.zip4jvm.view.SizeView;
import ru.olegcherednik.zip4jvm.view.crypto.RecipientView;
import ru.olegcherednik.zip4jvm.view.out.Out;

import org.apache.commons.lang3.ArrayUtils;

import java.util.Optional;

import static ru.olegcherednik.zip4jvm.utils.ValidationUtils.requireNotNull;

/**
 * @author Oleg Cherednik
 * @since 24.02.2020
 */
public class DecryptionHeaderView extends BaseView {

    private final DecryptionHeader decryptionHeader;
    private final DecryptionHeaderBlock block;
    private final Long pos;

    public DecryptionHeaderView(DecryptionHeader decryptionHeader,
                                DecryptionHeaderBlock block,
                                Long pos,
                                int offs,
                                int columnWidth,
                                long totalDisks) {
        super(offs, columnWidth, totalDisks);
        this.decryptionHeader = decryptionHeader;
        this.block = requireNotNull(block, "BlockDecryptionHeaderView.localFileHeader");
        this.pos = pos;
    }

    @Override
    public boolean printTextInfo(Out out) {
        if (pos == null)
            printTitle(out, "(Strong) encryption header", block);
        else
            printSubTitle(out, pos, "(Strong) encryption header", block);

        printIv(out);
        printVersion(out);
        printEncryptionAlgorithm(out);
        printBitLength(out);
        printFlags(out);
        printEncryptedRandomData(out);
        printHashAlgorithm(out);
        printPasswordValidationData(out);
        printRecipients(out);
        return true;
    }

    private void printIv(Out out) {
        new SizeView("iv:", decryptionHeader.getIv().length, offs, columnWidth).printTextInfo(out);
        new ByteArrayHexView(decryptionHeader.getIv(), offs, columnWidth).printTextInfo(out);
    }

    private void printVersion(Out out) {
        printLine(out, "version:", decryptionHeader.getVersion());
    }

    private void printEncryptionAlgorithm(Out out) {
        int code = decryptionHeader.getEncryptionAlgorithmCode();
        EncryptionAlgorithm encryptionAlgorithm = decryptionHeader.getEncryptionAlgorithm();
        printLine(out, String.format("encryption algorithm (0x%04X):", code), encryptionAlgorithm.getTitle());
    }

    private void printBitLength(Out out) {
        printLine(out, "encryption key bits:", decryptionHeader.getBitLength());
    }

    private void printFlags(Out out) {
        Flag flags = decryptionHeader.getFlags();
        printLine(out, String.format("flags (0x%02X):", flags.getCode()), flags.getTitle());
    }

    private void printEncryptedRandomData(Out out) {
        byte[] encryptedRandomData = Optional.ofNullable(decryptionHeader.getEncryptedRandomData())
                                             .orElse(ArrayUtils.EMPTY_BYTE_ARRAY);
        printLength(out, "length of encrypted random data:", encryptedRandomData.length);
        new ByteArrayHexView(encryptedRandomData, offs, columnWidth).printTextInfo(out);
    }

    private void printHashAlgorithm(Out out) {
        int code = decryptionHeader.getHashAlgorithmCode();
        HashAlgorithm hashAlgorithm = decryptionHeader.getHashAlgorithm();
        printLine(out, String.format("hash algorithm (0x%04X):", code), hashAlgorithm.getTitle());
    }

    private void printPasswordValidationData(Out out) {
        byte[] passwordValidationData = Optional.ofNullable(decryptionHeader.getPasswordValidationData()).orElse(
                ArrayUtils.EMPTY_BYTE_ARRAY);
        printLength(out, "password validation data:", passwordValidationData.length);
        new ByteArrayHexView(passwordValidationData, offs, columnWidth).printTextInfo(out);
    }

    private void printRecipients(Out out) {
        if (decryptionHeader.getRecipients().isEmpty())
            return;

        printValueWithLocation(out, "recipients:", block, decryptionHeader.getRecipients().size());

        int i = 0;

        for (Recipient recipient : decryptionHeader.getRecipients()) {
            RecipientView.builder()
                         .num(i)
                         .recipient(recipient)
                         .block(block.getRecipientsBlock().getRecipient(i))
                         .position(offs + 2, columnWidth, totalDisks).build().printTextInfo(out);
            i++;
        }
    }
}
