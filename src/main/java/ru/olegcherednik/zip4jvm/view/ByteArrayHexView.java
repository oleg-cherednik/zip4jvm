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

import org.apache.commons.lang3.ArrayUtils;

/**
 * @author Oleg Cherednik
 * @since 17.10.2019
 */
public final class ByteArrayHexView extends BaseView {

    private final byte[] data;

    public ByteArrayHexView(byte[] data, int offs, int columnWidth) {
        super(offs, columnWidth);
        this.data = ArrayUtils.isEmpty(data) ? ArrayUtils.EMPTY_BYTE_ARRAY : ArrayUtils.clone(data);
    }

    @Override
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public void printTextInfo(Out out) {
        int i = 0;
        int maxLineLen = columnWidth - offs;

        while (i < data.length) {
            StringBuilder buf = new StringBuilder();

            while (i < data.length && buf.length() + 3 <= maxLineLen) {
                if (buf.length() > 0)
                    buf.append(' ');

                buf.append(String.format("%02X", data[i]));
                i++;
            }

            printLine(out, buf);
        }
    }

}
