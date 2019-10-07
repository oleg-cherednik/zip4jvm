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
import lombok.RequiredArgsConstructor;
import ru.olegcherednik.zip4jvm.model.settings.ZipEntrySettings;
import ru.olegcherednik.zip4jvm.model.settings.ZipSettings;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.function.Function;

import static ru.olegcherednik.zip4jvm.utils.ValidationUtils.requireExists;
import static ru.olegcherednik.zip4jvm.utils.ValidationUtils.requireNotNull;
import static ru.olegcherednik.zip4jvm.utils.ValidationUtils.requireRegularFile;

/**
 * Add regular files and/or directories to the zip archive
 *
 * @author Oleg Cherednik
 * @since 14.03.2019
 */
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public final class ZipIt {

    /** path to the zip file (new or existed) */
    private final Path zip;
    /** zip file scope settings */
    private ZipSettings settings = ZipSettings.DEFAULT;

    /**
     * Create {@link ZipIt} instance with given {@code zip} path to the new or existed zip archive.
     *
     * @param zip zip file path
     * @return not {@literal null} {@link ZipIt} instance
     */
    public static ZipIt zip(Path zip) {
        requireNotNull(zip, "zipit.zip");
        requireRegularFile(zip, "zipit.zip");

        return new ZipIt(zip);
    }

    /**
     * Set custom settings for zip archive. If it's {@literal null}, then {@link ZipSettings#DEFAULT} will be used.
     *
     * @param settings custom zip file settings
     * @return not {@literal null} {@link ZipIt} instance
     */
    public ZipIt settings(ZipSettings settings) {
        this.settings = Optional.ofNullable(settings).orElse(ZipSettings.DEFAULT);
        return this;
    }

    /**
     * Set same {@link ZipEntrySettings} for all new entries in the zip archive. If {@literal null}, then {@link ZipEntrySettings#DEFAULT} will be
     * used.
     *
     * @param entrySettings entry settings
     * @return not {@literal null} {@link ZipIt} instance
     */
    public ZipIt entrySettings(ZipEntrySettings entrySettings) {
        return entrySettings == null ? entrySettings(ZipEntrySettings.DEFAULT_PROVIDER) : entrySettings(fileName -> entrySettings);
    }

    /**
     * Set provider of {@link ZipEntrySettings} for the given file name. Each entry could have separate settings. If {@literal null}, then {@link
     * ZipEntrySettings#DEFAULT_PROVIDER} will be used.
     *
     * @param entrySettingsProvider entry settings provider with fileName as a key
     * @return not {@literal null} {@link ZipIt} instance
     */
    public ZipIt entrySettings(Function<String, ZipEntrySettings> entrySettingsProvider) {
        requireNotNull(entrySettingsProvider, "zipit.entrySettingsProvider");
        settings = settings.toBuilder().entrySettingsProvider(entrySettingsProvider).build();
        return this;
    }

    /**
     * Add regular file or directory (keeping initial structure) to the new or existed zip archive.
     *
     * @param path not {@literal null} path to the regular file or directory
     * @throws IOException in case of any problem
     */
    public void add(Path path) throws IOException {
        requireNotNull(path, "zipit.path");
        requireExists(path, "zipit.path");

        add(Collections.singleton(path));
    }

    /**
     * Add regular files and/or directories (keeping initial structure) to the new or existed zip archive.
     *
     * @param paths path to the regular files and/or directories
     * @throws IOException in case of any problem
     */
    public void add(Collection<Path> paths) throws IOException {
        // TODO check that path != zip
        try (ZipFile.Writer zipFile = ZipFile.writer(zip, settings)) {
            zipFile.add(paths);
        }
    }

    /**
     * Add zip entry to the new or existed zip archive. The source of the {@link ZipFile.Entry} is representing with input stream supplier.
     *
     * @param entry zip file entry description
     * @throws IOException in case of any problem
     */
    public void addEntry(ZipFile.Entry entry) throws IOException {
        requireNotNull(entry, "zipit.entry");
        addEntry(Collections.singleton(entry));
    }

    /**
     * Add zip entries to the new or existed zip archive. The source of the {@link ZipFile.Entry} is representing with input stream supplier.
     *
     * @param entries zip file entries description
     * @throws IOException in case of any problem
     */
    public void addEntry(Collection<ZipFile.Entry> entries) throws IOException {
        try (ZipFile.Writer zipFile = zip(zip).settings(settings).open()) {
            zipFile.addEntry(entries);
        }
    }

    /**
     * Creates instance of zip file stream. It could be used to add multiple entries to the zip archive. It should be correctly closed to flush all
     * data.
     *
     * @return not {@literal null} instance of {@link ZipFile.Writer}
     * @throws IOException in case of any problem
     */
    public ZipFile.Writer open() throws IOException {
        return ZipFile.writer(zip, settings);
    }

}
