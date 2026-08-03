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
package ru.olegcherednik.zip4jvm.view;

import ru.olegcherednik.zip4jvm.view.out.Out;

import static ru.olegcherednik.zip4jvm.utils.ValidationUtils.requireNotBlank;

/**
 * @author Oleg Cherednik
 * @since 10.12.2019
 */
public final class SizeView extends BaseView {

    private final String name;
    private final long size;
    private final boolean centralDirectoryEncrypted;

    public SizeView(String name, long size, int offs, int columnWidth) {
        this(name, size, offs, columnWidth, false);
    }

    public SizeView(String name, long size, int offs, int columnWidth, boolean centralDirectoryEncrypted) {
        super(offs, columnWidth);
        this.name = requireNotBlank(name, "SizeView.name");
        this.size = size;
        this.centralDirectoryEncrypted = centralDirectoryEncrypted;
    }

    @Override
    public void printTextInfo(Out out) {
        if (centralDirectoryEncrypted)
            printLine(out, name, "----");
        else
            printSize(out, name, size);
    }
}
