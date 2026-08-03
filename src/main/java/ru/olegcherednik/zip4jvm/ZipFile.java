/*
 * Copyright 2019 Oleg Cherednik (oleg.cherednik@gmail.com)
 *
 * Licensed under The Apache Software License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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
import ru.olegcherednik.zip4jvm.model.charset.Charsets;
import ru.olegcherednik.zip4jvm.model.settings.UnzipSettings;
import ru.olegcherednik.zip4jvm.model.settings.ZipInfoSettings;
import ru.olegcherednik.zip4jvm.model.settings.ZipSettings;
import ru.olegcherednik.zip4jvm.model.src.SrcZip;
import ru.olegcherednik.zip4jvm.utils.ByteArrayUtils;
import ru.olegcherednik.zip4jvm.utils.EmptyInputStreamSupplier;
import ru.olegcherednik.zip4jvm.utils.PathUtils;
import ru.olegcherednik.zip4jvm.utils.function.InputStreamSupplier;
import ru.olegcherednik.zip4jvm.view.out.Out;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.io.Closeable;
import java.io.InputStream;
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

    static ZipEngine writer(Path zip, ZipSettings settings) {
        return new ZipEngine(zip, settings);
    }

    static Reader reader(SrcZip srcZip, UnzipSettings settings) {
        return new UnzipEngine(srcZip, settings);
    }

    static Info info(SrcZip srcZip, ZipInfoSettings settings) {
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
        private final ExternalFileAttributes externalFileAttributes;
        private final boolean dir;

        public boolean isSymlink() {
            return externalFileAttributes.isSymlink();
        }

        public long getUncompressedSize() {
            return inputStreamSupplier.getSize();
        }

        @Override
        public String toString() {
            return name;
        }

        public static Entry directory(String dirName,
                                      long lastModifiedTime,
                                      ExternalFileAttributes externalFileAttributes) {
            return new Entry(EmptyInputStreamSupplier.INSTANCE,
                             dirName,
                             lastModifiedTime,
                             externalFileAttributes,
                             true);
        }

        public static Entry regularFile(InputStreamSupplier inputStreamSupplier, String fileName) {
            return regularFile(inputStreamSupplier, fileName, System.currentTimeMillis(), new ExternalFileAttributes());
        }

        public static Entry regularFile(InputStreamSupplier inputStreamSupplier,
                                        String fileName,
                                        long lastModifiedTime,
                                        ExternalFileAttributes externalFileAttributes) {
            return new Entry(inputStreamSupplier,
                             fileName,
                             lastModifiedTime,
                             externalFileAttributes,
                             false);
        }

        public InputStream getInputStream() {
            return inputStreamSupplier.get();
        }
    }

    public interface Writer extends Closeable {

        default void add(Path path) {
            add(path, PathUtils.getName(path));
        }

        void add(Path path, String entryName);

        default void add(String content, String entryName) {
            add(content == null ? null : content.getBytes(Charsets.UTF_8), entryName);
        }

        default void add(byte[] content, String entryName) {
            add(ByteArrayUtils.newInputStreamSupplier(content), entryName);
        }

        void add(InputStreamSupplier inputStreamSupplier, String entryName);

        void add(ZipFile.Entry entry);

        void removeEntryByName(String entryName) throws EntryNotFoundException;

        void removeEntryByNamePrefix(String entryNamePrefix) throws EntryNotFoundException;

        void copy(Path zip);

        void setComment(String comment);

    }

    public interface Reader extends Iterable<ZipFile.Entry> {

        void extract(Path dstDir);

        void extract(Path dstDir, String fileName);

        void extract(Path dstDir, Collection<String> fileNamePrefixes);

        ZipFile.Entry extract(String fileName);

        default Stream<Entry> stream() {
            return StreamSupport.stream(spliterator(), false);
        }

        String getComment();

        boolean isSplit();

        boolean isZip64();
    }

    public interface Info {

        void printTextInfo(Out out);

        void decompose(Path dir);

        CentralDirectory.FileHeader getFileHeader(String entryName);
    }

}
