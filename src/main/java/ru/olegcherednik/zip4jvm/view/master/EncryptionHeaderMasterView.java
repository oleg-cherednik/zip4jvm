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
package ru.olegcherednik.zip4jvm.view.master;

import ru.olegcherednik.zip4jvm.crypto.strong.DecryptionHeader;
import ru.olegcherednik.zip4jvm.model.ZipModel;
import ru.olegcherednik.zip4jvm.model.block.crypto.EncryptionHeaderBlock;
import ru.olegcherednik.zip4jvm.model.settings.ZipInfoSettings;
import ru.olegcherednik.zip4jvm.view.View;
import ru.olegcherednik.zip4jvm.view.crypto.EncryptionHeaderView;
import ru.olegcherednik.zip4jvm.view.out.Out;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Oleg Cherednik
 * @since 22.03.2026
 */
@Slf4j
@RequiredArgsConstructor
public final class EncryptionHeaderMasterView implements View {

    private final ZipModel zipModel;
    private final ZipInfoSettings settings;
    private final DecryptionHeader decryptionHeader;
    private final EncryptionHeaderBlock encryptionHeaderBlock;
    private final long pos;

    public EncryptionHeaderView encryptionHeaderView() {
        int offs = settings.getOffs();
        int columnWidth = settings.getColumnWidth();
        long totalDisks = zipModel.getTotalDisks();
        return new EncryptionHeaderView(decryptionHeader, encryptionHeaderBlock, pos, offs, columnWidth, totalDisks);
    }

    // ---------- View ----------

    @Override
    public void printTextInfo(Out out) {
        if (encryptionHeaderBlock != null)
            encryptionHeaderView().printTextInfo(out);
    }

}
