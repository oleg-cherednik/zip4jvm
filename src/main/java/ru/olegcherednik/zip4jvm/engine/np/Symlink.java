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
package ru.olegcherednik.zip4jvm.engine.np;

import ru.olegcherednik.zip4jvm.ZipFile;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntryBuilder;
import ru.olegcherednik.zip4jvm.model.settings.ZipEntrySettings;

import java.nio.file.Path;

/**
 * @author Oleg Cherednik
 * @since 24.02.2023
 */
final class Symlink extends NamedPath {

    private final Path symlinkTarget;
    private final String symlinkTargetRelativePath;

    public Symlink(Path symlinkTarget, String symlinkName) {
        this(symlinkTarget, null, symlinkName);
    }

    public Symlink(Path symlinkTarget, String symlinkTargetRelativePath, String symlinkName) {
        super(symlinkName);
        this.symlinkTarget = symlinkTarget;
        this.symlinkTargetRelativePath = symlinkTargetRelativePath;
    }

    @Override
    public ZipEntry createZipEntry(ZipEntrySettings entrySettings) {
        return ZipEntryBuilder.symlink(symlinkTarget, symlinkTargetRelativePath, name, entrySettings);
    }

    @Override
    public Path getPath() {
        return symlinkTarget;
    }

    @Override
    public boolean isSymlink() {
        return true;
    }

    @Override
    public String toString() {
        return name + " (symlink)";
    }
}
