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
import lombok.NoArgsConstructor;
import lombok.NonNull;
import ru.olegcherednik.zip4jvm.model.settings.ZipFileSettings;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;

/**
 * @author Oleg Cherednik
 * @since 14.03.2019
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ZipIt {

    public static void add(@NonNull Path zip, @NonNull Path path) throws IOException {
        add(zip, path, ZipFileSettings.builder().build());
    }

    public static void add(@NonNull Path zip, @NonNull Path path, @NonNull ZipFileSettings settings) throws IOException {
        add(zip, Collections.singleton(path), settings);
    }

    public static void add(@NonNull Path zip, @NonNull Collection<Path> paths) throws IOException {
        add(zip, paths, ZipFileSettings.builder().build());
    }

    public static void add(@NonNull Path zip, @NonNull Collection<Path> paths, @NonNull ZipFileSettings settings) throws IOException {
        try (ZipFile.Writer zipFile = ZipFile.write(zip, settings)) {
            zipFile.add(paths);
        }
    }

}
