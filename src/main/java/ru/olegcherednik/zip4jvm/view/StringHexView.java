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

import org.apache.commons.lang3.StringUtils;

import java.io.PrintStream;
import java.nio.charset.Charset;

import static ru.olegcherednik.zip4jvm.utils.ValidationUtils.requireNotNull;

/**
 * @author Oleg Cherednik
 * @since 13.10.2019
 */
public final class StringHexView extends BaseView {

    private final String str;
    private final Charset charset;

    public StringHexView(String str, Charset charset, int offs, int columnWidth) {
        super(offs, columnWidth);
        this.str = StringUtils.isEmpty(str) ? null : str;
        this.charset = requireNotNull(charset, "StringHexView.charset");
    }

    @Override
    public boolean print(PrintStream out) {
        if (str == null)
            return false;

        printCharsetName(out);
        printLines(out);
        return true;
    }

    private void printCharsetName(PrintStream out) {
        printLine(out, "", charset.name());
    }

    private void printLines(PrintStream out) {
        int i = 0;

        while (i < str.length()) {
            StringBuilder one = new StringBuilder();
            StringBuilder two = new StringBuilder();

            while (i < str.length() && one.length() + 3 < columnWidth - offs) {
                char ch = str.charAt(i);
                byte[] data = String.valueOf(ch).getBytes(charset);

                for (int j = 0; j < data.length; j++) {
                    if (one.length() > 0)
                        one.append(' ');
                    one.append(String.format("%02X", data[j]));
                }

                two.append(Character.isISOControl(ch) ? '.' : ch);
                i++;
            }

            printLine(out, one, two);
        }
    }

}
