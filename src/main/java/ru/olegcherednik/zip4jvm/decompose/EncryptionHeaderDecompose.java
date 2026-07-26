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
package ru.olegcherednik.zip4jvm.decompose;

import ru.olegcherednik.zip4jvm.crypto.strong.DecryptionHeader;
import ru.olegcherednik.zip4jvm.model.Encryption;
import ru.olegcherednik.zip4jvm.model.ZipModel;
import ru.olegcherednik.zip4jvm.model.block.crypto.DecryptionHeaderBlock;
import ru.olegcherednik.zip4jvm.model.block.crypto.EncryptionHeaderBlock;
import ru.olegcherednik.zip4jvm.model.block.crypto.PkwareEncryptionHeaderBlock;
import ru.olegcherednik.zip4jvm.model.block.crypto.WinZipAesEncryptionHeaderBlock;
import ru.olegcherednik.zip4jvm.model.settings.ZipInfoSettings;
import ru.olegcherednik.zip4jvm.utils.PathUtils;
import ru.olegcherednik.zip4jvm.view.crypto.EncryptionHeaderView;

import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;

import static ru.olegcherednik.zip4jvm.utils.ValidationUtils.requireNotNull;

/**
 * @author Oleg Cherednik
 * @since 09.12.2019
 */
@Slf4j
public final class EncryptionHeaderDecompose implements Decompose {

    private final ZipModel zipModel;
    private final Encryption encryption;
    private final EncryptionHeaderBlock block;
    private final EncryptionHeaderView view;

    public EncryptionHeaderDecompose(ZipModel zipModel,
                                     ZipInfoSettings settings,
                                     Encryption encryption,
                                     DecryptionHeader decryptionHeader,
                                     EncryptionHeaderBlock block,
                                     long pos) {
        this.zipModel = zipModel;
        this.encryption = encryption;
        this.block = requireNotNull(block, "EncryptionHeaderDecompose.block");
        view = new EncryptionHeaderView(decryptionHeader,
                                        block,
                                        pos,
                                        settings.getOffs(),
                                        settings.getColumnWidth(),
                                        zipModel.getTotalDisks());
    }

    // ---------- Decompose ----------

    @Override
    public Path decompose(Path dir) {
        dir = PathUtils.createDirectories(dir.resolve("encryption"));

        if (encryption.isStrong()) strongDecompose(dir);
        else if (encryption.isAes()) aesDecompose(dir);
        else if (encryption == Encryption.PKWARE) pkwareDecompose(dir);
        else {
            // TODO print unknown header
            log.warn("TODO print unknown header");
        }

        return dir;
    }

    // ----------

    private void strongDecompose(Path dir) {
        DecryptionHeaderBlock decryptionBlock = (DecryptionHeaderBlock) block;
        Utils.print(dir.resolve("decryption_header" + EXT_TXT), view::printTextInfo);
        Utils.copyLarge(zipModel, dir.resolve("decryption_header" + EXT_DATA), decryptionBlock);
    }

    private void aesDecompose(Path dir) {
        // TODO probably same with block reader
        WinZipAesEncryptionHeaderBlock aesBlock = (WinZipAesEncryptionHeaderBlock) block;
        Utils.print(dir.resolve("aes_encryption_header" + EXT_TXT), view::printTextInfo);
        Utils.copyLarge(zipModel, dir.resolve("aes_salt" + EXT_DATA), aesBlock.getSalt());
        Utils.copyLarge(zipModel, dir.resolve("aes_password_checksum" + EXT_DATA), aesBlock.getPasswordChecksum());
        Utils.copyLarge(zipModel, dir.resolve("aes_mac" + EXT_DATA), aesBlock.getMac());
    }

    private void pkwareDecompose(Path dir) {
        PkwareEncryptionHeaderBlock pkwareBlock = (PkwareEncryptionHeaderBlock) block;
        Utils.print(dir.resolve("pkware_encryption_header" + EXT_TXT), view::printTextInfo);
        Utils.copyLarge(zipModel, dir.resolve("pkware_encryption_header" + EXT_DATA), pkwareBlock);
    }

}
