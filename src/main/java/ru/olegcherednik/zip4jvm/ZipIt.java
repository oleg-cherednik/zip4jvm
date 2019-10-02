/*
 * Copyright 2019 Cherednik Oleg
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ru.olegcherednik.zip4jvm;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import ru.olegcherednik.zip4jvm.model.settings.ZipFileSettings;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

/**
 * @author Oleg Cherednik
 * @since 14.03.2019
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ZipIt {

    private final Path zip;
    private ZipFileSettings settings = ZipFileSettings.DEFAULT;

    public static ZipIt zip(Path zip) {
        return new ZipIt(zip);
    }

    public ZipIt settings(ZipFileSettings settings) {
        this.settings = Optional.ofNullable(settings).orElse(ZipFileSettings.DEFAULT);
        return this;
    }

    public void add(@NonNull Path path) throws IOException {
        add(Collections.singleton(path));
    }

    public void add(Collection<Path> paths) throws IOException {
        // TODO check that path != zip
        try (ZipFile.Writer zipFile = ZipFile.write(zip, settings)) {
            zipFile.add(paths);
        }
    }

    public void addEntry(@NonNull ZipFile.Entry entry) throws IOException {
        addEntry(Collections.singleton(entry));
    }

    public void addEntry(Collection<ZipFile.Entry> entries) throws IOException {
        try (ZipFile.Writer zipFile = ZipFile.write(zip, settings)) {
            zipFile.addEntry(entries);
        }
    }

}
