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
package ru.olegcherednik.zip4jvm.view.out;

import ru.olegcherednik.zip4jvm.utils.apache.StringUtils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.PrintStream;
import java.util.Locale;
import java.util.Objects;

/**
 * @author Oleg Cherednik
 * @since 17.03.2026
 */

@RequiredArgsConstructor
public class PrintStreamOut implements Out {

    private static final Locale LOCALE = Locale.US;

    private final PrintStream out;
    @Getter
    private boolean empty = true;
    private boolean doPrintEmptyLine;

    // ---------- Out ----------

    @Override
    public void println() {
        out.println();
    }

    @Override
    public void println(String str, char underscore) {
        printEmptyLine();
        out.println(str);
        out.println(repeat(str.length(), underscore));
        empty = false;
    }

    @Override
    public void printLine(int offs, String format, Object one, Object two) {
        printEmptyLine();
        out.format(LOCALE, format, repeat(offs, ' ') + Objects.toString(one, ""), Objects.toString(two, ""));
        out.println();
        empty = false;
    }

    @Override
    public void printLine(int offs, Object one) {
        printEmptyLine();
        out.println(repeat(offs, ' ') + Objects.toString(one, ""));
        empty = false;
    }

    @Override
    public void addEmptyLine() {
        doPrintEmptyLine = true;
    }

    // ---------- Closeable ----------

    @Override
    public void close() {
        out.close();
    }

    // ---------- Object ----------

    @Override
    public String toString() {
        return Objects.toString(out);
    }

    // ----------

    private void printEmptyLine() {
        if (!empty && doPrintEmptyLine)
            out.println();

        doPrintEmptyLine = false;
    }

    // ---------- static ----------

    public static String repeat(int offs, char ch) {
        return StringUtils.repeat(ch, offs);
    }
}
