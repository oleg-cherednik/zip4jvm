/*
 * Copyright © 2019 Cherednik Oleg
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
import ru.olegcherednik.zip4jvm.model.settings.ZipSettings;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

/**
 * @author Oleg Cherednik
 * @since 15.03.2019
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ZipMisc {

    public static void setComment(@NonNull Path zip, String comment) throws IOException {
        try (ZipFile.Writer zipFile = ZipIt.zip(zip).open()) {
            zipFile.setComment(comment);
        }
    }

    public static String getComment(@NonNull Path zip) throws IOException {
        return UnzipIt.zip(zip).open().getComment();
    }

    public static Set<String> getEntryNames(@NonNull Path zip) throws IOException {
        return UnzipIt.zip(zip).open().getEntryNames();
    }

    public static void removeEntry(@NonNull Path zip, @NonNull String entryName) throws IOException {
        removeEntry(zip, Collections.singleton(entryName));
    }

    public static void removeEntry(@NonNull Path zip, @NonNull Collection<String> entryNames) throws IOException {
        try (ZipFile.Writer zipFile = ZipIt.zip(zip).open()) {
            zipFile.remove(entryNames);
        }
    }

    public static boolean isSplit(@NonNull Path zip) throws IOException {
        return UnzipIt.zip(zip).open().isSplit();
    }

    // TODO refactoring; it's not clear where is source and destination
    public static void merge(@NonNull Path dest, @NonNull Path src) throws IOException {
        ZipFile.Reader reader = UnzipIt.zip(src).open();

        ZipSettings settings = ZipSettings.builder()
                                          .comment(reader.getComment())
                                          .zip64(reader.isZip64()).build();

        try (ZipFile.Writer zipFile = ZipIt.zip(dest).settings(settings).open()) {
            zipFile.copy(src);
        }
    }

}
