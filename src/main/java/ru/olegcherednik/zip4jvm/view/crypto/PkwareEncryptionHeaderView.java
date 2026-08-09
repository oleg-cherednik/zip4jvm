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
package ru.olegcherednik.zip4jvm.view.crypto;

import ru.olegcherednik.zip4jvm.model.block.crypto.PkwareEncryptionHeaderBlock;
import ru.olegcherednik.zip4jvm.view.BaseView;
import ru.olegcherednik.zip4jvm.view.ByteArrayHexView;
import ru.olegcherednik.zip4jvm.view.out.Out;

import static ru.olegcherednik.zip4jvm.utils.ValidationUtils.requireNotNull;

/**
 * @author Oleg Cherednik
 * @since 09.11.2019
 */
final class PkwareEncryptionHeaderView extends BaseView {

    private final PkwareEncryptionHeaderBlock block;
    private final long pos;

    PkwareEncryptionHeaderView(PkwareEncryptionHeaderBlock block,
                               long pos,
                               int offs,
                               int columnWidth,
                               long totalDisks) {
        super(offs, columnWidth, totalDisks);
        this.block = requireNotNull(block, "PkwareEncryptionHeaderView.centralDirectory");
        this.pos = pos;
    }

    @Override
    public void printTextInfo(Out out) {
        printSubTitle(out, pos, "(PKWARE) encryption header", block);
        new ByteArrayHexView(block.getData(), offs, columnWidth).printTextInfo(out);
    }
}
