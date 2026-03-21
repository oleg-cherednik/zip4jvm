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

import ru.olegcherednik.zip4jvm.view.out.Out;

import org.apache.commons.lang3.StringUtils;

import java.nio.charset.Charset;

import static ru.olegcherednik.zip4jvm.utils.ValidationUtils.requireNotNull;

/**
 * @author Oleg Cherednik
 * @since 13.10.2019
 */
public final class StringHexView extends BaseView {

    private final String str;
    private final Charset charset;
    private final boolean showCharset;

    public StringHexView(String str, Charset charset, int offs, int columnWidth) {
        this(str, charset, true, offs, columnWidth);
    }

    public StringHexView(String str, Charset charset, boolean showCharset, int offs, int columnWidth) {
        super(offs, columnWidth);
        this.str = StringUtils.isEmpty(str) ? null : str;
        this.charset = requireNotNull(charset, "StringHexView.charset");
        this.showCharset = showCharset;
    }

    @Override
    public void printTextInfoNew(Out out) {
        if (str != null) {
            printCharsetName(out);
            printLines(out);
        }
    }

    private void printCharsetName(Out out) {
        if (showCharset)
            printLine(out, "", charset.name());
    }

    @SuppressWarnings({ "PMD.AvoidInstantiatingObjectsInLoops", "PMD.CognitiveComplexity" })
    private void printLines(Out out) {
        int i = 0;

        while (i < str.length()) {
            StringBuilder one = new StringBuilder();
            StringBuilder two = new StringBuilder();

            while (i < str.length() && one.length() + 3 < columnWidth - offs) {
                char ch = str.charAt(i);
                byte[] data = String.valueOf(ch).getBytes(charset);

                if (data.length * 2 + 2 + one.length() > columnWidth - offs)
                    break;

                for (byte d : data) {
                    if (one.length() > 0)
                        one.append(' ');
                    one.append(String.format("%02X", d));
                }

                two.append(Character.isISOControl(ch) ? '.' : ch);
                i++;
            }

            printLine(out, one, two);
        }
    }

}
