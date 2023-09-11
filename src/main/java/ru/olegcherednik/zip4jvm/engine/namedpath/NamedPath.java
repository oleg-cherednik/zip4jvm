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
package ru.olegcherednik.zip4jvm.engine.namedpath;

import lombok.Getter;
import ru.olegcherednik.zip4jvm.exception.Zip4jvmException;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;
import ru.olegcherednik.zip4jvm.model.settings.ZipEntrySettings;
import ru.olegcherednik.zip4jvm.utils.PathUtils;
import ru.olegcherednik.zip4jvm.utils.ZipUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

/**
 * @author Oleg Cherednik
 * @since 24.02.2023
 */
@Getter
public abstract class NamedPath {

    public static final Comparator<NamedPath> SORT_BY_NAME_ASC = (one, two) -> {
        String[] parts1 = one.name.split("/");
        String[] parts2 = two.name.split("/");

        if (parts1.length < parts2.length)
            return -1;
        if (parts1.length > parts2.length)
            return 1;

        for (int i = 0; i < parts1.length; i++) {
            int res = parts1[i].compareTo(parts2[i]);

            if (res != 0)
                return res;
        }

        return 0;
    };

    /** Normalized file name without directory marker {@literal /} */
    protected final String name;

    public static NamedPath create(Path path) {
        return create(path, PathUtils.getName(path));
    }

    public static NamedPath create(Path path, String pathName) {
        if (Files.isSymbolicLink(path))
            return new Symlink(path, pathName);
        if (Files.isDirectory(path))
            return new Directory(path, ZipUtils.getFileName(pathName, true));
        if (Files.isRegularFile(path))
            return new RegularFile(path, pathName);

        throw new Zip4jvmException(String.format("Unknown path '%s'", path));
    }

    public static NamedPath symlink(Path symlinkTarget, String symlinkTargetRelativePath, String symlinkName) {
        return new Symlink(symlinkTarget, symlinkTargetRelativePath, symlinkName);
    }

    protected NamedPath(String name) {
        this.name = name;
    }

    public String getEntryName() {
        return ZipUtils.getFileName(getName(), isDirectory());
    }

    public abstract ZipEntry createZipEntry(ZipEntrySettings entrySettings);

    public Path getPath() {
        return null;
    }

    public boolean isSymlink() {
        return false;
    }

    public boolean isDirectory() {
        return false;
    }

    public boolean isRegularFile() {
        return false;
    }

    @Override
    public String toString() {
        return name;
    }
}
