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
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import ru.olegcherednik.zip4jvm.exception.EntryNotFoundException;
import ru.olegcherednik.zip4jvm.model.settings.ZipSettings;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import static ru.olegcherednik.zip4jvm.utils.ValidationUtils.requireExists;
import static ru.olegcherednik.zip4jvm.utils.ValidationUtils.requireNotBlank;
import static ru.olegcherednik.zip4jvm.utils.ValidationUtils.requireNotEmpty;
import static ru.olegcherednik.zip4jvm.utils.ValidationUtils.requireNotNull;
import static ru.olegcherednik.zip4jvm.utils.ValidationUtils.requireRegularFile;

/**
 * @author Oleg Cherednik
 * @since 15.03.2019
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ZipMisc {

    /** path to the zip file (new or existed) */
    private final Path zip;
    private Set<String> entryNames = Collections.emptySet();

    public static ZipMisc zip(Path zip) {
        requireNotNull(zip, "ZipMisc.zip");
        requireExists(zip, "ZipMisc.zip");
        requireRegularFile(zip, "ZipMisc.zip");

        return new ZipMisc(zip);
    }

    public ZipMisc entryName(String entryName) {
        requireNotBlank(entryName, "ZipMisc.entryName");
        entryNames = Collections.singleton(entryName);
        return this;
    }

    public ZipMisc entryName(Collection<String> entryNames) {
        requireNotNull(entryNames, "ZipMisc.entryNames");
        this.entryNames = Collections.unmodifiableSet(new HashSet<>(entryNames));
        return this;
    }

    public void setComment(String comment) throws IOException {
        try (ZipFile.Writer zipFile = ZipIt.zip(zip).open()) {
            zipFile.setComment(comment);
        }
    }

    public String getComment() throws IOException {
        return UnzipIt.zip(zip).open().getComment();
    }

    public Stream<ZipFile.Entry> getEntries() throws IOException {
        return UnzipIt.zip(zip).open().stream();
    }

    /**
     * Remove all entries from {@link #entryNames}. Exact match of the entry name is required; i.e. in case of given entry name represents a directory
     * and zip archive sub entries of this entry, then only the root entry will removed (if it's exist); all sub entries will not be removed.
     *
     * @throws IOException            in case of any problem with file access
     * @throws EntryNotFoundException in case of entry with given {@code entryName} was not found
     */
    public void removeEntryByName() throws IOException, EntryNotFoundException {
        requireNotEmpty(entryNames, "ZipMisc.entryName");

        try (ZipFile.Writer zipFile = ZipIt.zip(zip).open()) {
            for (String entryName : entryNames)
                zipFile.removeEntryByName(entryName);
        }
    }

    public void removeEntryByNamePrefix() throws IOException, EntryNotFoundException {
        requireNotEmpty(entryNames, "ZipMisc.entryName");

        try (ZipFile.Writer zipFile = ZipIt.zip(zip).open()) {
            for (String entryName : entryNames)
                zipFile.removeEntryByNamePrefix(entryName);
        }
    }

    public static boolean isSplit(Path zip) throws IOException {
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
