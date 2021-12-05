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

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import ru.olegcherednik.zip4jvm.exception.EntryNotFoundException;
import ru.olegcherednik.zip4jvm.model.settings.ZipSettings;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.function.BiConsumer;
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

    /**
     * Create {@link ZipMisc} instance with given {@code zip} path to the existed zip archive.
     *
     * @param zip zip file path
     * @return not {@literal null} {@link ZipMisc} instance
     */
    public static ZipMisc zip(Path zip) {
        requireNotNull(zip, "ZipMisc.zip");
        requireExists(zip);
        requireRegularFile(zip, "ZipMisc.zip");

        return new ZipMisc(zip);
    }

    /**
     * Set comment to the existed {@link #zip} archive. If it's {@literal null} or empty, then comment will be removed.
     *
     * @param comment new comment or {@literal null} to remove comment
     * @throws IOException in case of any problem with file access
     */
    public void setComment(String comment) throws IOException {
        try (ZipFile.Writer zipFile = ZipIt.zip(zip).open()) {
            zipFile.setComment(comment);
        }
    }

    /**
     * Retrieve actual zip comment or {@literal null} if no comment.
     *
     * @return actual comment or {@literal null} if no comment
     * @throws IOException in case of any problem with file access
     */
    public String getComment() throws IOException {
        return UnzipIt.zip(zip).open().getComment();
    }

    /**
     * Retrieve not {@literal null} {@link Stream} with existed entries as {@link ZipFile.Entry} objects.
     *
     * @return not {@literal null} stream of entries
     * @throws IOException in case of any problem with file access
     */
    public Stream<ZipFile.Entry> getEntries() throws IOException {
        return UnzipIt.zip(zip).open().stream();
    }

    /**
     * Remove entry with given {@code entryName}. Exact match of the entry name is required; i.e. in case of given entry name represents a directory
     * and zip archive has sub entries of this entry, then only the root entry will be removed (if it's exist); all sub entries will not be removed.
     *
     * @param entryName not blank entry name
     * @throws IOException            in case of any problem with file access
     * @throws EntryNotFoundException in case of entry with given {@code entryName} was not found
     */
    public void removeEntryByName(String entryName) throws IOException, EntryNotFoundException {
        requireNotBlank(entryName, "ZipMisc.entryName");
        removeEntry(Collections.singleton(entryName), ZipFile.Writer::removeEntryByName);
    }

    /**
     * Remove all entries with given {@code entryNames}. Exact match of the entry name is required; i.e. in case of given entry name represents a
     * directory and zip archive sub entries of this entry, then only the root entry will removed (if it's exist); all sub entries will not be
     * removed.
     *
     * @param entryNames not empty entry names
     * @throws IOException            in case of any problem with file access
     * @throws EntryNotFoundException in case of entry with given {@code entryName} was not found
     */
    public void removeEntryByName(Collection<String> entryNames) throws IOException, EntryNotFoundException {
        requireNotEmpty(entryNames, "ZipMisc.entryName");
        removeEntry(entryNames, ZipFile.Writer::removeEntryByName);
    }

    /**
     * Remove all entries with given {@code entryNamePrefix}.
     *
     * @param entryNamePrefix not blank entry name prefix
     * @throws IOException            in case of any problem with file access
     * @throws EntryNotFoundException in case of no entries with given {@code entryNamePrefix} were not found
     */
    public void removeEntryByNamePrefix(String entryNamePrefix) throws IOException, EntryNotFoundException {
        requireNotBlank(entryNamePrefix, "ZipMisc.entryName");
        removeEntry(Collections.singleton(entryNamePrefix), ZipFile.Writer::removeEntryByNamePrefix);
    }

    /**
     * Remove all entries with given {@code entryNamePrefixes}.
     *
     * @param entryNamePrefixes not empty entry name prefixes
     * @throws IOException            in case of any problem with file access
     * @throws EntryNotFoundException in case of no entries with given {@code entryNamePrefix} were not found
     */
    public void removeEntryByNamePrefix(Collection<String> entryNamePrefixes) throws IOException, EntryNotFoundException {
        requireNotEmpty(entryNamePrefixes, "ZipMisc.entryName");
        removeEntry(entryNamePrefixes, ZipFile.Writer::removeEntryByNamePrefix);
    }

    private void removeEntry(Collection<String> entryNames, BiConsumer<ZipFile.Writer, String> task)
            throws IOException, EntryNotFoundException {
        requireNotEmpty(entryNames, "ZipMisc.entryName");

        try (ZipFile.Writer zipFile = ZipIt.zip(zip).open()) {
            for (String entryName : entryNames)
                task.accept(zipFile, entryName);
        }
    }

    /**
     * Check if {@link #zip} archive is split or not.
     *
     * @return {@literal true} if zip archive is split
     * @throws IOException in case of any problem with file access
     */
    public boolean isSplit() throws IOException {
        return UnzipIt.zip(zip).open().isSplit();
    }

    /**
     * Merge split archive.
     *
     * @param dest not {@literal null} zip archive destination file
     * @throws IOException in case of any problem with file access
     */
    public void merge(Path dest) throws IOException {
        requireNotNull(dest, "ZipMis.dest");

        ZipFile.Reader reader = UnzipIt.zip(zip).open();

        if (!reader.isSplit())
            throw new IllegalArgumentException("Zip archive is not split");

        ZipSettings settings = ZipSettings.builder()
                                          .comment(reader.getComment())
                                          .zip64(reader.isZip64()).build();

        try (ZipFile.Writer zipFile = ZipIt.zip(dest).settings(settings).open()) {
            zipFile.copy(zip);
        }
    }

}
