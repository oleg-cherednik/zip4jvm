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
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.commons.io.IOUtils;
import ru.olegcherednik.zip4jvm.engine.InfoEngine;
import ru.olegcherednik.zip4jvm.engine.UnzipEngine;
import ru.olegcherednik.zip4jvm.engine.ZipEngine;
import ru.olegcherednik.zip4jvm.exception.EntryNotFoundException;
import ru.olegcherednik.zip4jvm.model.CentralDirectory;
import ru.olegcherednik.zip4jvm.model.Charsets;
import ru.olegcherednik.zip4jvm.model.ExternalFileAttributes;
import ru.olegcherednik.zip4jvm.model.settings.UnzipSettings;
import ru.olegcherednik.zip4jvm.model.settings.ZipInfoSettings;
import ru.olegcherednik.zip4jvm.model.settings.ZipSettings;
import ru.olegcherednik.zip4jvm.model.src.SrcZip;
import ru.olegcherednik.zip4jvm.utils.EmptyInputStreamSupplier;
import ru.olegcherednik.zip4jvm.utils.PathUtils;
import ru.olegcherednik.zip4jvm.utils.ZipUtils;
import ru.olegcherednik.zip4jvm.utils.function.InputStreamSupplier;

import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static ru.olegcherednik.zip4jvm.model.ExternalFileAttributes.PROP_OS_NAME;

/**
 * @author Oleg Cherednik
 * @since 01.09.2019
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ZipFile {

    static Writer writer(Path zip, ZipSettings settings) throws IOException {
        return new ZipEngine(zip, settings);
    }

    static Reader reader(SrcZip srcZip, UnzipSettings settings) {
        return new UnzipEngine(srcZip, settings);
    }

    static Info info(SrcZip srcZip, ZipInfoSettings settings) throws IOException {
        return new InfoEngine(srcZip, settings);
    }

    /**
     * This is an abstraction of the single zip file entry. It does not matter what it is (a regular file, directory,
     * symlink, etc). This class is used to make client define an entry that will be converted to the internal concrete
     * entry type while zipping and unzipping.
     */
    @Getter
    @Builder
    public static final class Entry {

        @Getter(AccessLevel.NONE)
        private final InputStreamSupplier inputStreamSupplier;
        /** Normalized file name without directory marker {@literal /} */
        private final String fileName;
        private final long lastModifiedTime;
        private final long uncompressedSize;
        private final ExternalFileAttributes externalFileAttributes;
        private final boolean directory;

        public boolean isSymlink() {
            return externalFileAttributes.isSymlink();
        }

        @Override
        public String toString() {
            return fileName;
        }

        public static Entry symlink(Path symlinkTarget, String symlinkTargetRelativePath, String symlinkName) {
            byte[] buf = symlinkTargetRelativePath.getBytes(Charsets.UTF_8);

            return builder()
                    .lastModifiedTime(System.currentTimeMillis())
                    .externalFileAttributes(ExternalFileAttributes.build(PROP_OS_NAME)
                                                                  .readFrom(symlinkTarget)
                                                                  .symlink())
                    .fileName(ZipUtils.getFileNameNoDirectoryMarker(symlinkName))
                    .inputStreamSupplier(() -> new ByteArrayInputStream(buf))
                    .uncompressedSize(buf.length)
                    .build();
        }

        public static Entry directory(Path dir, String dirName) {
            return ZipUtils.readQuietly(() -> builder()
                    .lastModifiedTime(Files.getLastModifiedTime(dir).toMillis())
                    .externalFileAttributes(ExternalFileAttributes.build(PROP_OS_NAME).readFrom(dir).directory())
                    .fileName(ZipUtils.getFileNameNoDirectoryMarker(dirName))
                    .directory(true)
                    .inputStreamSupplier(EmptyInputStreamSupplier.INSTANCE)
                    .build());
        }

        public static Entry regularFile(InputStreamSupplier inputStreamSupplier, String fileName) {
            return builder().inputStreamSupplier(() -> IOUtils.toInputStream(fileName, Charsets.UTF_8))
                            .externalFileAttributes(ExternalFileAttributes.NULL)
                            .fileName(fileName)
                            .build();
        }

        public static Entry regularFile(Path file, String fileName) {
            return ZipUtils.readQuietly(() -> builder()
                    .lastModifiedTime(Files.getLastModifiedTime(file).toMillis())
                    .externalFileAttributes(ExternalFileAttributes.build(PROP_OS_NAME).readFrom(file).regularFile())
                    .fileName(ZipUtils.getFileNameNoDirectoryMarker(fileName))
                    .inputStreamSupplier(() -> Files.newInputStream(file))
                    .uncompressedSize(Files.size(file))
                    .build());
        }

        public InputStream getInputStream() {
            return ZipUtils.readQuietly(inputStreamSupplier);
        }
    }

    public interface Writer extends Closeable {

        default void add(Path path) {
            add(path, PathUtils.getName(path));
        }

        void add(Path path, String name);

        void add(ZipFile.Entry entry);

        void removeEntryByName(String entryName) throws EntryNotFoundException;

        void removeEntryByNamePrefix(String entryNamePrefix) throws EntryNotFoundException;

        void copy(Path zip) throws IOException;

        void setComment(String comment);
    }

    public interface Reader extends Iterable<ZipFile.Entry> {

        void extract(Path destDir) throws IOException;

        void extract(Path destDir, String fileName) throws IOException;

        ZipFile.Entry extract(String fileName) throws IOException;

        default Stream<Entry> stream() {
            return StreamSupport.stream(spliterator(), false);
        }

        String getComment();

        boolean isSplit();

        boolean isZip64();
    }

    public interface Info {

        void printTextInfo(PrintStream out) throws IOException;

        void decompose(Path dir) throws IOException;

        CentralDirectory.FileHeader getFileHeader(String entryName) throws IOException;
    }

}
