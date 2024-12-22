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

import ru.olegcherednik.zip4jvm.engine.info.InfoEngine;
import ru.olegcherednik.zip4jvm.engine.unzip.UnzipEngine;
import ru.olegcherednik.zip4jvm.engine.zip.ZipEngine;
import ru.olegcherednik.zip4jvm.exception.EntryNotFoundException;
import ru.olegcherednik.zip4jvm.model.CentralDirectory;
import ru.olegcherednik.zip4jvm.model.ExternalFileAttributes;
import ru.olegcherednik.zip4jvm.model.settings.UnzipSettings;
import ru.olegcherednik.zip4jvm.model.settings.ZipInfoSettings;
import ru.olegcherednik.zip4jvm.model.settings.ZipSettings;
import ru.olegcherednik.zip4jvm.model.src.SrcZip;
import ru.olegcherednik.zip4jvm.utils.EmptyInputStreamFunction;
import ru.olegcherednik.zip4jvm.utils.quitely.Quietly;
import ru.olegcherednik.zip4jvm.utils.quitely.functions.InputStreamSupplier;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.Collection;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

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
     * This is an abstraction of the single zip file entry not related to the specific zip file settings. It does not
     * matter what it is (a regular file, directory, symlink, etc.). This class is used to make client define an entry
     * that will be converted to the internal concrete entry type while zipping and unzipping.
     */
    @Getter
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Entry {

        @Getter(AccessLevel.NONE)
        private final InputStreamSupplier inputStreamSupplier;
        /**
         * Normalized file name without directory marker {@literal /}
         */
        private final String name;
        private final long lastModifiedTime;
        private final long uncompressedSize;
        private final ExternalFileAttributes externalFileAttributes;
        private final boolean dir;

        public boolean isSymlink() {
            return externalFileAttributes.isSymlink();
        }

        @Override
        public String toString() {
            return name;
        }

        public static Entry directory(String dirName,
                                      long lastModifiedTime,
                                      ExternalFileAttributes externalFileAttributes) {
            return new Entry(EmptyInputStreamFunction.INSTANCE,
                             dirName,
                             lastModifiedTime,
                             0,
                             externalFileAttributes,
                             true);
        }

        public static Entry regularFile(InputStreamSupplier inputStreamSupplier,
                                        String fileName,
                                        long lastModifiedTime,
                                        long uncompressedSize,
                                        ExternalFileAttributes externalFileAttributes) {
            return new Entry(inputStreamSupplier,
                             fileName,
                             lastModifiedTime,
                             uncompressedSize,
                             externalFileAttributes,
                             false);
        }

        public InputStream getInputStream() {
            return Quietly.doQuietly(inputStreamSupplier);
        }
    }

    public interface Writer extends Closeable {

        void add(Path path);

        void addWithRename(Path path, String name);

        void addWithMove(Path path, String dir);

        void add(ZipFile.Entry entry);

        void removeEntryByName(String entryName) throws EntryNotFoundException;

        void removeEntryByNamePrefix(String entryNamePrefix) throws EntryNotFoundException;

        void copy(Path zip) throws IOException;

        void setComment(String comment);
    }

    public interface Reader extends Iterable<ZipFile.Entry> {

        void extract(Path dstDir) throws IOException;

        void extract(Path dstDir, String fileName) throws IOException;

        void extract(Path dstDir, Collection<String> fileNames) throws IOException;

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
