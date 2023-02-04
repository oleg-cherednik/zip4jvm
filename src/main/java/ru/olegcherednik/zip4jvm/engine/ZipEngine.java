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
import ru.olegcherednik.zip4jvm.exception.EntryDuplicationException;
import ru.olegcherednik.zip4jvm.exception.EntryNotFoundException;
import ru.olegcherednik.zip4jvm.io.out.data.DataOutput;
import ru.olegcherednik.zip4jvm.io.out.data.SolidZipOutputStream;
import ru.olegcherednik.zip4jvm.io.out.data.SplitZipOutputStream;
import ru.olegcherednik.zip4jvm.io.writers.ExistedEntryWriter;
import ru.olegcherednik.zip4jvm.io.writers.ZipFileEntryWriter;
import ru.olegcherednik.zip4jvm.model.ZipModel;
import ru.olegcherednik.zip4jvm.model.ZipSymlink;
import ru.olegcherednik.zip4jvm.model.builders.ZipModelBuilder;
import ru.olegcherednik.zip4jvm.model.settings.ZipEntrySettings;
import ru.olegcherednik.zip4jvm.model.settings.ZipSettings;
import ru.olegcherednik.zip4jvm.model.src.SrcZip;
import ru.olegcherednik.zip4jvm.utils.PathUtils;
import ru.olegcherednik.zip4jvm.utils.ZipUtils;
import ru.olegcherednik.zip4jvm.utils.function.Writer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static ru.olegcherednik.zip4jvm.utils.ValidationUtils.requireNotBlank;
import static ru.olegcherednik.zip4jvm.utils.ValidationUtils.requireNotNull;

/**
 * @author Oleg Cherednik
 * @since 09.09.2019
 */
public final class ZipEngine implements ZipFile.Writer {

    private final Path zip;
    private final ZipModel tempZipModel;
    private final ZipSettings settings;
    private final Map<String, Writer> fileNameWriter = new LinkedHashMap<>();

    public ZipEngine(Path zip, ZipSettings settings) throws IOException {
        this.zip = zip;
        tempZipModel = createTempZipModel(zip, settings, fileNameWriter);
        this.settings = settings;
    }

    @Override
    public void add(Path path) throws IOException {
        for (Path child : removeRootDir(path)) {
            boolean symlink = Files.isSymbolicLink(child);
            ZipSymlink zipSymlink = settings.getZipSymlink();

            if (symlink && zipSymlink == ZipSymlink.IGNORE_SYMLINK)
                continue;

            for (Map.Entry<Path, String> entry : PathUtils.getRelativeContent(child).entrySet()) {
                add(entry.getKey(), entry.getValue());
            }
        }
    }

    private List<Path> removeRootDir(Path path) throws IOException {
        if (Files.isDirectory(path) && settings.isRemoveRootDir())
            return PathUtils.getDirectoryContent(path);
        return Collections.singletonList(path);
    }

    @Override
    public void add(Path path, String fileName) throws IOException {
        boolean symlink = Files.isSymbolicLink(path);

        if (symlink) {
            ZipSymlink zipSymlink = settings.getZipSymlink();

            if (zipSymlink == ZipSymlink.IGNORE_SYMLINK)
                return;
            if (zipSymlink != ZipSymlink.REPLACE_SYMLINK_WITH_TARGET)
                throw new RuntimeException("not implemented symlink option");
            if (Files.isDirectory(path))
                throw new RuntimeException("symlink to folder not supported");
            if (!Files.exists(path))
                return;

            path = PathUtils.getSymbolicLinkTarget(path);
        }

        add(ZipFile.Entry.of(path, fileName));
    }

    @Override
    public void add(ZipFile.Entry entry) {
        ZipEntrySettings entrySettings = settings.getEntrySettingsProvider().apply(entry.getFileName());
        String fileName = ZipUtils.getFileName(entry);
        Writer writer = new ZipFileEntryWriter(entry, entrySettings, tempZipModel);

        if (fileNameWriter.put(fileName, writer) != null)
            throw new EntryDuplicationException(entry.getFileName());
    }

    @Override
    public void removeEntryByName(String entryName) {
        requireNotBlank(entryName, "ZipEngine.entryName");

        entryName = ZipUtils.getFileNameNoDirectoryMarker(entryName);

        if (fileNameWriter.remove(entryName) != null)
            return;
        if (fileNameWriter.remove(entryName + '/') != null)
            return;

        throw new EntryNotFoundException(entryName);
    }

    @Override
    public void removeEntryByNamePrefix(String entryNamePrefix) {
        requireNotBlank(entryNamePrefix, "ZipEngine.entryNamePrefix");

        String normalizedPrefixEntryName = ZipUtils.normalizeFileName(entryNamePrefix);

        Set<String> entryNames = fileNameWriter.keySet().stream()
                                               .filter(entryName -> entryName.startsWith(normalizedPrefixEntryName))
                                               .collect(Collectors.toSet());

        if (entryNames.isEmpty())
            throw new EntryNotFoundException(entryNamePrefix);

        entryNames.forEach(fileNameWriter::remove);
    }

    @Override
    public void copy(Path zip) throws IOException {
        requireNotNull(zip, "ZipEngine.zip");

        ZipModel srcZipModel = ZipModelBuilder.read(SrcZip.of(zip));

        for (String fileName : srcZipModel.getEntryNames()) {
            if (fileNameWriter.containsKey(fileName))
                throw new EntryDuplicationException(fileName);

            char[] password = settings.getEntrySettingsProvider().apply(fileName).getPassword();
            fileNameWriter.put(fileName, new ExistedEntryWriter(srcZipModel, fileName, tempZipModel, password));
        }
    }

    @Override
    public void setComment(String comment) {
        tempZipModel.setComment(comment);
    }

    @Override
    public void close() throws IOException {
        createTempZipFiles();
        removeOriginalZipFiles();
        moveTempZipFiles();
    }

    private void createTempZipFiles() throws IOException {
        try (DataOutput out = creatDataOutput(tempZipModel)) {
            for (Writer writer : fileNameWriter.values())
                writer.write(out);
        }
    }

    private void removeOriginalZipFiles() throws IOException {
        if (!Files.exists(zip))
            return;

        SrcZip srcZip = SrcZip.of(zip);

        for (int diskNo = 0; diskNo < srcZip.getTotalDisks(); diskNo++)
            Files.deleteIfExists(srcZip.getDiskByNo(diskNo).getPath());
    }

    private void moveTempZipFiles() throws IOException {
        for (int diskNo = 0; diskNo <= tempZipModel.getTotalDisks(); diskNo++) {
            Path src = tempZipModel.getDiskPath(diskNo);
            Path dest = zip.getParent().resolve(src.getFileName());
            Files.move(src, dest);
        }

        Files.deleteIfExists(tempZipModel.getSrcZip().getPath().getParent());
    }

    private static ZipModel createTempZipModel(Path zip, ZipSettings settings, Map<String, Writer> fileNameWriter) throws IOException {
        Path tempZip = createTempZip(zip);
        ZipModel tempZipModel = ZipModelBuilder.build(tempZip, settings);

        if (Files.exists(zip)) {
            ZipModel zipModel = ZipModelBuilder.read(SrcZip.of(zip));

            if (zipModel.isSplit())
                tempZipModel.setSplitSize(zipModel.getSplitSize());
            if (zipModel.getComment() != null)
                tempZipModel.setComment(zipModel.getComment());

            tempZipModel.setZip64(zipModel.isZip64());

            zipModel.getEntryNames().forEach(entryName -> {
                char[] password = settings.getEntrySettingsProvider().apply(entryName).getPassword();
                fileNameWriter.put(entryName, new ExistedEntryWriter(zipModel, entryName, tempZipModel, password));
            });
        }

        return tempZipModel;
    }

    private static Path createTempZip(Path zip) throws IOException {
        Path dir = zip.getParent().resolve("tmp");
        Files.createDirectories(dir);
        return dir.resolve(zip.getFileName());
    }

    private static DataOutput creatDataOutput(ZipModel zipModel) throws IOException {
        return zipModel.isSplit() ? new SplitZipOutputStream(zipModel) : new SolidZipOutputStream(zipModel);
    }

}
