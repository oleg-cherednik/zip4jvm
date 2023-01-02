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

import lombok.RequiredArgsConstructor;
import ru.olegcherednik.zip4jvm.crypto.strong.DecryptionHeader;
import ru.olegcherednik.zip4jvm.model.EncryptionMethod;
import ru.olegcherednik.zip4jvm.model.ZipModel;
import ru.olegcherednik.zip4jvm.model.block.crypto.AesEncryptionHeaderBlock;
import ru.olegcherednik.zip4jvm.model.block.crypto.DecryptionHeaderBlock;
import ru.olegcherednik.zip4jvm.model.block.crypto.EncryptionHeaderBlock;
import ru.olegcherednik.zip4jvm.model.block.crypto.PkwareEncryptionHeaderBlock;
import ru.olegcherednik.zip4jvm.model.settings.ZipInfoSettings;
import ru.olegcherednik.zip4jvm.view.crypto.EncryptionHeaderView;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author Oleg Cherednik
 * @since 09.12.2019
 */
@RequiredArgsConstructor
public final class EncryptionHeaderDecompose implements Decompose {

    private final ZipModel zipModel;
    private final ZipInfoSettings settings;
    private final EncryptionMethod encryptionMethod;
    private final DecryptionHeader decryptionHeader;
    private final EncryptionHeaderBlock encryptionHeaderBlock;
    private final long pos;

    @Override
    public boolean printTextInfo(PrintStream out, boolean emptyLine) {
        if (encryptionHeaderBlock != null)
            return encryptionHeaderView().print(out, emptyLine);

        return emptyLine;
    }

    @Override
    public Path decompose(Path dir) throws IOException {
        if (encryptionHeaderBlock == null)
            return dir;

        dir = Files.createDirectories(dir.resolve("encryption"));

        if (encryptionMethod.isStrong()) {
            DecryptionHeaderBlock block = (DecryptionHeaderBlock)encryptionHeaderBlock;
            Utils.print(dir.resolve("decryption_header.txt"), out -> encryptionHeaderView().print(out));
            Utils.copyLarge(zipModel, dir.resolve("decryption_header.data"), block);
        } else if (encryptionMethod.isAes()) {
            // TODO probably same with block reader
            AesEncryptionHeaderBlock block = (AesEncryptionHeaderBlock)encryptionHeaderBlock;
            Utils.print(dir.resolve("aes_encryption_header.txt"), out -> encryptionHeaderView().print(out));

            Utils.copyLarge(zipModel, dir.resolve("aes_salt.data"), block.getSalt());
            Utils.copyLarge(zipModel, dir.resolve("aes_password_checksum.data"), block.getPasswordChecksum());
            Utils.copyLarge(zipModel, dir.resolve("aes_mac.data"), block.getMac());
        } else if (encryptionMethod == EncryptionMethod.PKWARE) {
            PkwareEncryptionHeaderBlock block = (PkwareEncryptionHeaderBlock)encryptionHeaderBlock;
            Utils.print(dir.resolve("pkware_encryption_header.txt"), out -> encryptionHeaderView().print(out));
            Utils.copyLarge(zipModel, dir.resolve("pkware_encryption_header.data"), block);
        } else {
            // TODO print unknown header
            System.out.println("TODO print unknown header");
        }

        return dir;
    }

    private EncryptionHeaderView encryptionHeaderView() {
        int offs = settings.getOffs();
        int columnWidth = settings.getColumnWidth();
        long totalDisks = zipModel.getTotalDisks();
        return new EncryptionHeaderView(decryptionHeader, encryptionHeaderBlock, pos, offs, columnWidth, totalDisks);
    }
}
