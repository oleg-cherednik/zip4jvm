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

import ru.olegcherednik.zip4jvm.model.ExternalFileAttributes;

import java.io.PrintStream;

import static ru.olegcherednik.zip4jvm.utils.ValidationUtils.requireNotNull;

/**
 * @author Oleg Cherednik
 * @since 15.10.2019
 */
public final class ExternalFileAttributesView extends BaseView {

    private final ExternalFileAttributes externalFileAttributes;

    public ExternalFileAttributesView(ExternalFileAttributes externalFileAttributes, int offs, int columnWidth) {
        super(offs, columnWidth);
        this.externalFileAttributes = externalFileAttributes;

        requireNotNull(externalFileAttributes, "ExternalFileAttributesView.externalFileAttributes");
    }

    @Override
    public boolean printTextInfo(PrintStream out) {
        byte[] data = externalFileAttributes.getData();
        int val = data[3] << 24 | data[2] << 16 | data[1] << 8 | data[0];

        printLine(out, "external file attributes:", String.format("0x%08X", val));
        printLine(out, String.format("  WINDOWS   (0x%02X):", val & 0xFF), externalFileAttributes.getDetailsWin());
        printLine(out,
                  String.format("  POSIX (0x%06X):", val >> 8 & 0xFFFFFF),
                  externalFileAttributes.getDetailsPosix());

        return true;
    }
}
