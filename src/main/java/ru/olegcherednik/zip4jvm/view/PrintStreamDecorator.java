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

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.io.Closeable;
import java.io.PrintStream;
import java.util.Locale;
import java.util.Optional;

/**
 * @author Oleg Cherednik
 * @since 11.09.2025
 */
@RequiredArgsConstructor
public class PrintStreamDecorator implements Closeable {

    private static final Locale LOCALE = Locale.US;

    private final PrintStream out;

    public void format(String format, Object one, Object two) {
        out.format(LOCALE, format, one, two);
    }

    public void println() {
        out.println();
    }

    public void print(char ch) {
        out.print(ch);
    }

    public void println(Object obj) {
        out.println(obj);
    }

    public void println(String str, char underscore) {
        out.println(str);

        for (int i = 0; i < str.length(); i++)
            out.print(underscore);

        out.println();
    }

    public void printLine(String format, Object one, Object two, int offs) {
        one = Optional.ofNullable(one).orElse("");
        two = Optional.ofNullable(two).orElse("");

        if (offs > 0)
            one = StringUtils.repeat(" ", offs) + one;

        format(format, one, two);
        out.println();
    }

    public void printLine(Object one, int offs) {
        one = Optional.ofNullable(one).orElse("");

        if (offs > 0)
            one = StringUtils.repeat(" ", offs) + one;

        out.println(one);
    }

    // ---------- Closeable ----------

    @Override
    public void close() {
        out.close();
    }
}
