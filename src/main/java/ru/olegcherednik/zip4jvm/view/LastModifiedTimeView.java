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

import ru.olegcherednik.zip4jvm.utils.time.DosTimestampConverterUtils;

import java.io.PrintStream;

/**
 * @author Oleg Cherednik
 * @since 15.10.2019
 */
public final class LastModifiedTimeView extends BaseView {

    private final int lastModifiedTime;
    private final boolean centralDirectoryEncrypted;

    public LastModifiedTimeView(int lastModifiedTime, int offs, int columnWidth) {
        this(lastModifiedTime, offs, columnWidth, false);
    }

    public LastModifiedTimeView(int lastModifiedTime, int offs, int columnWidth, boolean centralDirectoryEncrypted) {
        super(offs, columnWidth);
        this.lastModifiedTime = lastModifiedTime;
        this.centralDirectoryEncrypted = centralDirectoryEncrypted;
    }

    @Override
    public boolean printTextInfo(PrintStream out) {
        if (centralDirectoryEncrypted)
            printLine(out, "file last modified on (0x0000 0x0000):", "---- ----");
        else {
            int date = lastModifiedTime >> 16;
            int time = lastModifiedTime & 0xFFFF;
            long ms = DosTimestampConverterUtils.dosToJavaTime(lastModifiedTime);

            printLine(out, String.format("file last modified on (0x%04X 0x%04X):", date, time),
                      String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", ms));
        }

        return true;
    }
}
