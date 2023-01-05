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
package ru.olegcherednik.zip4jvm.view;

import org.apache.commons.lang3.ArrayUtils;

import java.io.PrintStream;

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
    public boolean printTextInfo(PrintStream out) {
        int i = 0;

        while (i < data.length) {
            StringBuilder one = new StringBuilder();

            do {
                if (one.length() > 0)
                    one.append(' ');
                one.append(String.format("%02X", data[i++]));
            } while (i < data.length && one.length() + 3 < columnWidth - offs);

            printLine(out, one);
        }

        return data.length > 0;
    }

}
