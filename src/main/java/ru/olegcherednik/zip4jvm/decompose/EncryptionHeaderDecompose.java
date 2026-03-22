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
import ru.olegcherednik.zip4jvm.model.block.crypto.AesEncryptionHeaderBlock;
import ru.olegcherednik.zip4jvm.model.block.crypto.EncryptionHeaderBlock;
import ru.olegcherednik.zip4jvm.model.block.crypto.PkwareEncryptionHeaderBlock;
import ru.olegcherednik.zip4jvm.model.block.crypto.strong.DecryptionHeaderBlock;
import ru.olegcherednik.zip4jvm.model.settings.ZipInfoSettings;
import ru.olegcherednik.zip4jvm.utils.PathUtils;
import ru.olegcherednik.zip4jvm.view.crypto.EncryptionHeaderView;

import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;

/**
 * @author Oleg Cherednik
 * @since 09.12.2019
 */
@Slf4j
public final class EncryptionHeaderDecompose implements Decompose {

    private final ZipModel zipModel;
    private final Encryption encryption;
    private final EncryptionHeaderBlock encryptionHeaderBlock;
    private final EncryptionHeaderView view;

    public EncryptionHeaderDecompose(ZipModel zipModel,
                                     ZipInfoSettings settings,
                                     Encryption encryption,
                                     DecryptionHeader decryptionHeader,
                                     EncryptionHeaderBlock encryptionHeaderBlock,
                                     long pos) {
        this.zipModel = zipModel;
        this.encryption = encryption;
        this.encryptionHeaderBlock = encryptionHeaderBlock;
        view = new EncryptionHeaderView(decryptionHeader,
                                        encryptionHeaderBlock,
                                        pos,
                                        settings.getOffs(),
                                        settings.getColumnWidth(),
                                        zipModel.getTotalDisks());
    }

    private void strong(Path dir) {
        DecryptionHeaderBlock block = (DecryptionHeaderBlock) encryptionHeaderBlock;
        Utils.print(dir.resolve("decryption_header" + EXT_TXT), view::printTextInfo);
        Utils.copyLarge(zipModel, dir.resolve("decryption_header" + EXT_DATA), block);
    }

    private void aes(Path dir) {
        // TODO probably same with block reader
        AesEncryptionHeaderBlock block = (AesEncryptionHeaderBlock) encryptionHeaderBlock;
        Utils.print(dir.resolve("aes_encryption_header" + EXT_TXT), view::printTextInfo);
        Utils.copyLarge(zipModel, dir.resolve("aes_salt" + EXT_DATA), block.getSalt());
        Utils.copyLarge(zipModel, dir.resolve("aes_password_checksum" + EXT_DATA), block.getPasswordChecksum());
        Utils.copyLarge(zipModel, dir.resolve("aes_mac" + EXT_DATA), block.getMac());
    }

    private void pkware(Path dir) {
        PkwareEncryptionHeaderBlock block = (PkwareEncryptionHeaderBlock) encryptionHeaderBlock;
        Utils.print(dir.resolve("pkware_encryption_header" + EXT_TXT), view::printTextInfo);
        Utils.copyLarge(zipModel, dir.resolve("pkware_encryption_header" + EXT_DATA), block);
    }

    // ---------- Decompose ----------

    @Override
    public Path decompose(Path dir) {
        if (encryptionHeaderBlock == null)
            return dir;

        dir = PathUtils.createDirectories(dir.resolve("encryption"));

        if (encryption.isStrong()) strong(dir);
        else if (encryption.isAes()) aes(dir);
        else if (encryption == Encryption.PKWARE) pkware(dir);
            // TODO print unknown header
        else log.warn("TODO print unknown header");

        return dir;
    }

}
