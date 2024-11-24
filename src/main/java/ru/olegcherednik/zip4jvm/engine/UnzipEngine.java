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
package ru.olegcherednik.zip4jvm.engine;

import ru.olegcherednik.zip4jvm.ZipFile;
import ru.olegcherednik.zip4jvm.exception.Zip4jvmException;
import ru.olegcherednik.zip4jvm.io.in.RandomAccessDataInput;
import ru.olegcherednik.zip4jvm.io.in.file.SolidRandomAccessDataInputFile;
import ru.olegcherednik.zip4jvm.io.in.file.SplitRandomAccessDataInputFile;
import ru.olegcherednik.zip4jvm.model.AesVersion;
import ru.olegcherednik.zip4jvm.model.Charsets;
import ru.olegcherednik.zip4jvm.model.ZipModel;
import ru.olegcherednik.zip4jvm.model.builders.ZipModelBuilder;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;
import ru.olegcherednik.zip4jvm.model.password.PasswordProvider;
import ru.olegcherednik.zip4jvm.model.settings.UnzipSettings;
import ru.olegcherednik.zip4jvm.model.src.SrcZip;
import ru.olegcherednik.zip4jvm.utils.ZipUtils;
import ru.olegcherednik.zip4jvm.utils.quitely.Quietly;
import ru.olegcherednik.zip4jvm.utils.time.DosTimestampConverterUtils;

import org.apache.commons.codec.digest.PureJavaCrc32;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.ChecksumInputStream;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Oleg Cherednik
 * @since 07.09.2019
 */
public final class UnzipEngine implements ZipFile.Reader {

    private static final int ONE = 1;

    private final ZipModel zipModel;
    private final PasswordProvider passwordProvider;

    public UnzipEngine(SrcZip srcZip, UnzipSettings settings) {
        passwordProvider = settings.getPasswordProvider();
        zipModel = ZipModelBuilder.read(srcZip, settings.getCharsetCustomizer(), passwordProvider);
    }

    @Override
    public void extract(Path destDir) throws IOException {
        for (ZipEntry zipEntry : zipModel.getZipEntries())
            extractEntry(destDir, zipEntry, ZipEntry::getFileName);
    }

    @Override
    @SuppressWarnings("PMD.AvoidReassigningParameters")
    public void extract(Path destDir, String fileName) throws IOException {
        fileName = ZipUtils.getFileNameNoDirectoryMarker(fileName);

        if (zipModel.hasEntry(fileName))
            extractEntry(destDir,
                         zipModel.getZipEntryByFileName(fileName),
                         e -> FilenameUtils.getName(e.getFileName()));
        else {
            List<ZipEntry> subEntries = getEntriesWithFileNamePrefix(fileName + '/');

            if (subEntries.isEmpty())
                throw new Zip4jvmException("Zip entry not found: " + fileName);

            for (ZipEntry zipEntry : subEntries)
                extractEntry(destDir, zipEntry, ZipEntry::getFileName);
        }
    }

    private List<ZipEntry> getEntriesWithFileNamePrefix(String fileNamePrefix) {
        return zipModel.getZipEntries().stream()
                       .filter(entry -> entry.getFileName().startsWith(fileNamePrefix))
                       .collect(Collectors.toList());
    }

    @Override
    public ZipFile.Entry extract(String fileName) {
        return Quietly.doQuietly(() -> {
            ZipEntry zipEntry = zipModel.getZipEntryByFileName(ZipUtils.normalizeFileName(fileName));

            if (zipEntry == null)
                throw new FileNotFoundException("Entry '" + fileName + "' was not found");

            zipEntry.setPassword(passwordProvider.getFilePassword(zipEntry.getFileName()));
            return zipEntry.createImmutableEntry();
        });
    }

    @Override
    public String getComment() {
        return zipModel.getComment();
    }

    @Override
    public boolean isSplit() {
        return zipModel.isSplit();
    }

    @Override
    public boolean isZip64() {
        return zipModel.isZip64();
    }

    @Override
    @SuppressWarnings("PMD.UseDiamondOperator")
    public Iterator<ZipFile.Entry> iterator() {
        return new Iterator<ZipFile.Entry>() {
            private final Iterator<String> it = zipModel.getEntryNames().iterator();

            @Override
            public boolean hasNext() {
                return it.hasNext();
            }

            @Override
            public ZipFile.Entry next() {
                return zipModel.getZipEntryByFileName(it.next()).createImmutableEntry();
            }
        };
    }

    private void extractEntry(Path destDir,
                              ZipEntry zipEntry,
                              Function<ZipEntry, String> getFileName) throws IOException {
        Path file = destDir.resolve(getFileName.apply(zipEntry));

        if (zipEntry.isSymlink())
            extractSymlink(file, zipEntry);
        else if (zipEntry.isDirectory())
            extractEmptyDirectory(file);
        else
            extractRegularFile(file, zipEntry);

        // TODO attributes for directory should be set at the end (under Posix, it could have less privelegies)
        setFileAttributes(file, zipEntry);
        setFileLastModifiedTime(file, zipEntry);
    }

    private static void extractSymlink(Path symlink, ZipEntry zipEntry) throws IOException {
        String target = IOUtils.toString(zipEntry.createInputStream(), Charsets.UTF_8);

        if (target.startsWith("/"))
            ZipSymlinkEngine.createAbsoluteSymlink(symlink, Paths.get(target));
        else if (target.contains(":"))
            // TODO absolute windows symlink
            throw new Zip4jvmException("windows absolute symlink not supported");
        else
            ZipSymlinkEngine.createRelativeSymlink(symlink, symlink.getParent().resolve(target));
    }

    private static void extractEmptyDirectory(Path dir) throws IOException {
        Files.createDirectories(dir);
    }

    @SuppressWarnings("PMD.CloseResource")
    private void extractRegularFile(Path file, ZipEntry zipEntry) throws IOException {
        String fileName = ZipUtils.getFileNameNoDirectoryMarker(zipEntry.getFileName());
        zipEntry.setPassword(passwordProvider.getFilePassword(fileName));

        InputStream in = zipEntry.createInputStream();

        if (zipEntry.getAesVersion() != AesVersion.AE_2) {
            in = ChecksumInputStream.builder()
                                    .setExpectedChecksumValue(zipEntry.getChecksum())
                                    .setChecksum(new PureJavaCrc32())
                                    .setInputStream(zipEntry.createInputStream())
                                    .get();
        }

        ZipUtils.copyLarge(in, getOutputStream(file));
    }

    private static void setFileLastModifiedTime(Path path, ZipEntry zipEntry) {
        try {
            long lastModifiedTime = DosTimestampConverterUtils.dosToJavaTime(zipEntry.getLastModifiedTime());
            Files.setLastModifiedTime(path, FileTime.fromMillis(lastModifiedTime));
        } catch (IOException ignored) {
        }
    }

    private static void setFileAttributes(Path path, ZipEntry zipEntry) {
        try {
            zipEntry.getExternalFileAttributes().apply(path);
        } catch (IOException ignored) {
        }
    }

    private static OutputStream getOutputStream(Path file) throws IOException {
        Path parent = file.getParent();

        if (!Files.exists(parent))
            Files.createDirectories(parent);

        Files.deleteIfExists(file);
        return Files.newOutputStream(file);
    }

    public static RandomAccessDataInput createDataInput(SrcZip srcZip) throws FileNotFoundException {
        if (srcZip.getTotalDisks() > ONE)
            return new SplitRandomAccessDataInputFile(srcZip);
        return new SolidRandomAccessDataInputFile(srcZip);
    }

}
