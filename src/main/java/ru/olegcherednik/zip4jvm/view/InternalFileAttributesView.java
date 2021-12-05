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

import ru.olegcherednik.zip4jvm.model.InternalFileAttributes;

import java.io.PrintStream;
import java.util.Objects;

/**
 * @author Oleg Cherednik
 * @since 15.10.2019
 */
public final class InternalFileAttributesView extends BaseView {

    private final InternalFileAttributes internalFileAttributes;

    public InternalFileAttributesView(InternalFileAttributes internalFileAttributes, int offs, int columnWidth) {
        super(offs, columnWidth);
        this.internalFileAttributes = internalFileAttributes;

        Objects.requireNonNull(internalFileAttributes, "'internalFileAttributes' must not be null");
    }

    @Override
    public boolean print(PrintStream out) {
        byte[] data = internalFileAttributes.getData();

        printLine(out, "internal file attributes:", String.format("0x%04X", data[1] << 8 | data[0]));
        printLine(out, "  apparent file type: ", internalFileAttributes.getApparentFileType().getTitle());

        return true;
    }

}
