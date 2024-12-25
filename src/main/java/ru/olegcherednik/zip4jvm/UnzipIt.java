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

import ru.olegcherednik.zip4jvm.exception.IncorrectPasswordException;
import ru.olegcherednik.zip4jvm.model.settings.UnzipSettings;
import ru.olegcherednik.zip4jvm.model.src.SrcZip;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Optional;

import static ru.olegcherednik.zip4jvm.utils.ValidationUtils.requireDirectory;
import static ru.olegcherednik.zip4jvm.utils.ValidationUtils.requireNotBlank;
import static ru.olegcherednik.zip4jvm.utils.ValidationUtils.requireNotEmpty;
import static ru.olegcherednik.zip4jvm.utils.ValidationUtils.requireNotNull;

/**
 * Extract regular files and/or directories from the zip archive
 *
 * @author Oleg Cherednik
 * @since 14.03.2019
 */
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
@SuppressWarnings("PMD.AvoidFieldNameMatchingMethodName")
public final class UnzipIt {

    /** path to the zip file (new or existed) */
    private final SrcZip srcZip;
    /**
     * Destination directory for extracted files; by default its directory where {@link #zip} archive is located
     */
    private Path dstDir;
    /** setting for unzip files */
    private UnzipSettings settings = UnzipSettings.DEFAULT;

    /**
     * Create {@link UnzipIt} instance with given {@code zip} path to the zip archive
     *
     * @param zip not {@literal null} zip file path
     * @return not {@literal null} {@link UnzipIt} instance
     */
    public static UnzipIt zip(Path zip) {
        requireNotNull(zip, "UnzipIt.zip");
        return new UnzipIt(SrcZip.of(zip)).dstDir(zip.getParent());
    }

    /**
     * Set destination directory for extracted files. By default, all files are extracted into {@link #zip} archive
     * located directory.<br>
     * If given directory is not exists, then it will be created.
     *
     * @param dstDir not {@literal null} destination directory
     * @return not {@literal null} {@link UnzipIt} instance
     */
    public UnzipIt dstDir(Path dstDir) {
        requireNotNull(dstDir, "UnzipIt.dstDir");
        requireDirectory(dstDir, "UnzipIt.dstDir");

        this.dstDir = dstDir;
        return this;
    }

    /**
     * Set custom settings or unzip operations like password or custom charset.
     *
     * @param settings custom settings; if {@literal null} then {@link UnzipSettings#DEFAULT} wil be used
     * @return not {@literal null} {@link UnzipIt} instance
     */
    public UnzipIt settings(UnzipSettings settings) {
        this.settings = Optional.ofNullable(settings).orElse(UnzipSettings.DEFAULT);
        return this;
    }

    /**
     * Set password for all entries in zip archive. It could be set with {@link #settings(UnzipSettings)} as well.
     *
     * @param password not blank password
     * @return not {@literal null} {@link UnzipIt} instance
     */
    public UnzipIt password(char[] password) {
        requireNotEmpty(password, "UnzipIt.password");
        settings = settings.toBuilder().password(password).build();
        return this;
    }

    /**
     * Extract all existed in {@link #zip} archive entries into {@link #dstDir} using {@link #settings}.
     *
     * @throws IncorrectPasswordException in case of password incorrect
     */
    public void extract() throws IncorrectPasswordException {
        open().extract(dstDir);
    }

    /**
     * Extract entry with {@code fileName} into {@link #dstDir} using {@link #settings}.<br>
     * If {@code fileName} is a regular file entry, then only single regular file will be extracted into the root of
     * {@link #dstDir}.<br>
     * If {@code fileName} is a directory, then entire directory will be extracted into the root of {@link #dstDir}
     * keeping the initial structure.
     *
     * @param fileName not blank file name
     * @throws IncorrectPasswordException in case of password incorrect
     */
    public void extract(String fileName) throws IncorrectPasswordException {
        requireNotBlank(fileName, "UnzipIt.fileName");
        open().extract(dstDir, fileName);
    }

    /**
     * Extract entries with {@code fileNames} into {@link #dstDir} using {@link #settings}. Each entry is extracted
     * separately.<br>
     * If {@code fileName} is a regular file entry, then only single regular file will be extracted into the root of
     * {@link #dstDir}.<br>
     * If {@code fileName} is a directory, then entire directory will be extracted into the root of {@link #dstDir}
     * keeping the initial structure.
     *
     * @param fileNames not {@literal null} file names
     * @throws IncorrectPasswordException in case of password incorrect
     */
    public void extract(Collection<String> fileNames) throws IncorrectPasswordException {
        requireNotNull(fileNames, "UnzipIt.fileNames");
        open().extract(dstDir, fileNames);
    }

    /**
     * Retrieve entry with given {@code fileName} as {@link InputStream}. If given {@code fileName} is directory entry,
     * then empty {@link InputStream} will be retrieved.
     *
     * @param fileName not blank file name
     * @return not {@literal null} {@link InputStream} instance; for directory entry retrieve empty {@link InputStream}
     * @throws IncorrectPasswordException in case of password incorrect
     */
    public InputStream stream(String fileName) throws IncorrectPasswordException {
        requireNotBlank(fileName, "UnzipIt.fileName");
        return ZipFile.reader(srcZip, settings).extract(fileName).getInputStream();
    }

    /**
     * Retrieves not {@literal null} instance of {@link ZipFile.Reader}. It provides all available methods to unzip an
     * archive.
     *
     * @return not {@literal null} instance of {@link ZipFile.Reader}
     */
    // @NotNull
    public ZipFile.Reader open() {
        return ZipFile.reader(srcZip, settings);
    }

}
