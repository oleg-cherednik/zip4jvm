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

import ru.olegcherednik.zip4jvm.model.Version;

import java.io.PrintStream;
import java.util.Optional;

/**
 * @author Oleg Cherednik
 * @since 16.10.2019
 */
public final class VersionView extends BaseView {

    private final Version versionMadeBy;
    private final Version versionToExtract;

    public VersionView(Version versionMadeBy, Version versionToExtract, int offs, int columnWidth) {
        super(offs, columnWidth);
        this.versionMadeBy = Optional.ofNullable(versionMadeBy).orElse(Version.NULL);
        this.versionToExtract = Optional.ofNullable(versionToExtract).orElse(Version.NULL);
    }

    @Override
    public boolean printTextInfo(PrintStream out) {
        boolean res = printVersionMadeBy(out);
        res |= printVersionToExtract(out);
        return res;
    }

    private boolean printVersionMadeBy(PrintStream out) {
        if (versionMadeBy == Version.NULL)
            return false;

        Version.FileSystem fileSystem = versionMadeBy.getFileSystem();
        int zipVersion = versionMadeBy.getZipSpecificationVersion();

        printLine(out, String.format("version made by operating system (%02d):", fileSystem.getCode()), fileSystem.getTitle());
        printLine(out, String.format("version made by zip software (%02d):", zipVersion), zipVersion / 10.);

        return true;
    }

    private boolean printVersionToExtract(PrintStream out) {
        if (versionToExtract == Version.NULL)
            return false;

        Version.FileSystem fileSystem = versionToExtract.getFileSystem();
        int zipVersion = versionToExtract.getZipSpecificationVersion();

        printLine(out, String.format("operat. system version needed to extract (%02d):", fileSystem.getCode()), fileSystem.getTitle());
        printLine(out, String.format("unzip software version needed to extract (%02d):", zipVersion), zipVersion / 10.);

        return true;
    }

}
