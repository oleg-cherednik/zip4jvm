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
package ru.olegcherednik.zip4jvm;

import ru.olegcherednik.zip4jvm.engine.zip.ZipEngine;
import ru.olegcherednik.zip4jvm.exception.PathNotExistsException;
import ru.olegcherednik.zip4jvm.exception.Zip4jvmException;
import ru.olegcherednik.zip4jvm.model.settings.ZipEntrySettings;
import ru.olegcherednik.zip4jvm.model.settings.ZipEntrySettingsProvider;
import ru.olegcherednik.zip4jvm.model.settings.ZipSettings;
import ru.olegcherednik.zip4jvm.utils.function.ZipFileConsumer;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

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
@SuppressWarnings("PMD.AvoidFieldNameMatchingMethodName")
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
        requireNotNull(zip, "ZipIt.zip");
        requireRegularFile(zip, "ZipIt.zip");

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
     * Set same {@link ZipEntrySettings} for all new entries in the zip archive. If {@literal null}, then
     * {@link ZipEntrySettings#DEFAULT} will be
     * used.
     *
     * @param entrySettings entry settings
     * @return not {@literal null} {@link ZipIt} instance
     */
    public ZipIt entrySettings(ZipEntrySettings entrySettings) {
        return entrySettings == null ? entrySettings(ZipEntrySettingsProvider.DEFAULT)
                                     : entrySettings(ZipEntrySettingsProvider.of(entrySettings));
    }

    /**
     * Set provider of {@link ZipEntrySettings} for the given file name. Each entry could have separate settings. If
     * {@literal null}, then {@link
     * ZipEntrySettingsProvider#DEFAULT} will be used.
     *
     * @param entrySettingsProvider entry settings provider with fileName as a key
     * @return not {@literal null} {@link ZipIt} instance
     */
    public ZipIt entrySettings(ZipEntrySettingsProvider entrySettingsProvider) {
        requireNotNull(entrySettingsProvider, "ZipIt.entrySettingsProvider");
        settings = settings.toBuilder().entrySettingsProvider(entrySettingsProvider).build();
        return this;
    }

    /**
     * Add regular file or directory (keeping initial structure) to the new or existed zip archive.
     *
     * @param paths not {@literal empty} paths to the regular files or directories
     * @throws IOException in case of any problem with file access
     */
    public void add(Path... paths) {
        add(paths == null ? null : Arrays.stream(paths).collect(Collectors.toSet()));
    }

    /**
     * Add regular files and/or directories (keeping initial structure) to the new or existed zip archive.
     *
     * @param paths path to the regular files and/or directories
     * @throws Zip4jvmException       in case of any problem with file access
     * @throws PathNotExistsException in case of given paths not exist
     */
    public void add(Collection<Path> paths) {
        if (CollectionUtils.isEmpty(paths))
            return;

        // TODO check that path != zip
        requireExists(paths);

        execute(zipFile -> paths.forEach(zipFile::add));
    }

    /**
     * Add regular file or directory (keeping initial structure) to the new or existed zip archive under given
     * {@code entryName}. {@code entryName} can contain directory marker (`\\` or `/`), but it's not allowed to have
     * relative
     * marker (e.g. `../`).<br>
     * In case given {@code path} is a directory (or symlink to directory), then this directory will be renamed.<br>
     * In case given {@code path} is a regular file (or symlink to the file), then this file will be renamed.
     *
     * @param path      path to the regular file or directory
     * @param entryName not {@literal null} entryName to be used for the {@code path}
     * @throws Zip4jvmException in case of any problem with file access
     */
    public void add(Path path, String entryName) {
        execute(zipFile -> zipFile.add(path, entryName));
    }

    /**
     * Provides ability to execute multiple actions (e.g. add multiple files along with remove some).
     *
     * @param zipFileConsumer not {@literal null} instance of {@link ZipFileConsumer}
     * @throws Zip4jvmException in case of any problem with file access
     */
    public void execute(ZipFileConsumer zipFileConsumer) {
        try (ZipEngine zipFile = ZipFile.writer(zip, settings)) {
            zipFileConsumer.accept(zipFile);
            zipFile.markSuccess();
        } catch (IOException e) {
            throw new Zip4jvmException(e);
        }
    }

}
