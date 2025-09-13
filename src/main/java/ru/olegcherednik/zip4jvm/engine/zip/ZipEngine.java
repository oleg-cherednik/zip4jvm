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
package ru.olegcherednik.zip4jvm.engine.zip;

import ru.olegcherednik.zip4jvm.ZipFile;
import ru.olegcherednik.zip4jvm.engine.np.NamedPath;
import ru.olegcherednik.zip4jvm.exception.EntryDuplicationException;
import ru.olegcherednik.zip4jvm.exception.EntryNotFoundException;
import ru.olegcherednik.zip4jvm.exception.SplitTriggerNotFoundException;
import ru.olegcherednik.zip4jvm.io.out.DataOutput;
import ru.olegcherednik.zip4jvm.io.out.file.SolidZipDataOutput;
import ru.olegcherednik.zip4jvm.io.out.file.SplitZipDataOutput;
import ru.olegcherednik.zip4jvm.io.writers.ExistedEntryWriter;
import ru.olegcherednik.zip4jvm.io.writers.entry.ZipEntryWriter;
import ru.olegcherednik.zip4jvm.model.ZipModel;
import ru.olegcherednik.zip4jvm.model.builders.ZipModelBuilder;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntryBuilder;
import ru.olegcherednik.zip4jvm.model.settings.ZipEntrySettings;
import ru.olegcherednik.zip4jvm.model.settings.ZipSettings;
import ru.olegcherednik.zip4jvm.model.src.SrcZip;
import ru.olegcherednik.zip4jvm.utils.PathUtils;
import ru.olegcherednik.zip4jvm.utils.ZipUtils;
import ru.olegcherednik.zip4jvm.utils.function.Writer;

import lombok.extern.slf4j.Slf4j;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static ru.olegcherednik.zip4jvm.utils.ValidationUtils.requireNotBlank;
import static ru.olegcherednik.zip4jvm.utils.ValidationUtils.requireNotNull;
import static ru.olegcherednik.zip4jvm.utils.ValidationUtils.requireValidEntryName;

/**
 * @author Oleg Cherednik
 * @since 09.09.2019
 */
@Slf4j
public final class ZipEngine implements ZipFile.Writer {

    private final Path zip;
    private final ZipModel tempZipModel;
    private final ZipSymlinkEngine zipSymlinkEngine;
    private final ZipSettings settings;
    private final FileNameWriter fileNameWriter = new FileNameWriter();

    private boolean success;

    public ZipEngine(Path zip, ZipSettings settings) {
        this.zip = requireNotNull(zip, "ZipEngine.zip");
        this.settings = requireNotNull(settings, "ZipEngine.settings");
        tempZipModel = createTempZipModel(zip, settings, fileNameWriter);
        zipSymlinkEngine = new ZipSymlinkEngine(settings.getZipSymlink());
    }

    @Override
    public void add(Path path, String entryName) {
        if (!Files.exists(path))
            return;

        requireNotBlank(entryName, "entryName");
        requireValidEntryName(entryName);

        if (Files.isSymbolicLink(path))
            path = ZipSymlinkEngine.getSymlinkTarget(path);

        for (NamedPath namedPath : getNamedPaths(path, entryName)) {
            ZipEntrySettings entrySettings = settings.getEntrySettings(namedPath.getEntryName());
            add(namedPath.createZipEntry(entrySettings));
        }
    }

    private List<NamedPath> getNamedPaths(Path path, String entryName) {
        if (Files.isDirectory(path))
            return zipSymlinkEngine.list(getDirectoryNamedPaths(path, entryName));

        if (Files.isRegularFile(path))
            return Collections.singletonList(NamedPath.create(path, entryName));

        log.warn("Unknown path type '{}'; ignore it", path);
        return Collections.emptyList();
    }

    private List<NamedPath> getDirectoryNamedPaths(Path path, String entryName) {
        if (settings.isRemoveRootDir())
            return PathUtils.list(path).stream()
                            .map(p -> NamedPath.create(p, entryName + '/' + path.relativize(p)))
                            .sorted(NamedPath.SORT_BY_NAME_ASC)
                            .collect(Collectors.toList());

        return Collections.singletonList(NamedPath.create(path, entryName));
    }

    @Override
    public void add(ZipFile.Entry entry) {
        ZipEntrySettings entrySettings = settings.getEntrySettings(entry.getName());
        ZipEntry zipEntry = ZipEntryBuilder.build(entry, entrySettings);
        add(zipEntry);
    }

    private void add(ZipEntry entry) {
        fileNameWriter.put(entry.getFileName(), ZipEntryWriter.create(entry, tempZipModel.getTempDir()));
        tempZipModel.addZipEntry(entry);
    }

    @Override
    public void removeEntryByName(String entryName) {
        requireNotBlank(entryName, "ZipEngine.entryName");

        entryName = ZipUtils.getFileNameNoDirectoryMarker(entryName);
        entryName = ZipUtils.normalizeFileName(entryName);

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

        Set<String> entryNames = fileNameWriter.getEntryNames().stream()
                                               .filter(entryName -> entryName.startsWith(normalizedPrefixEntryName))
                                               .collect(Collectors.toSet());

        if (entryNames.isEmpty())
            throw new EntryNotFoundException(entryNamePrefix);

        entryNames.forEach(fileNameWriter::remove);
    }

    @Override
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public void copy(Path zip) {
        requireNotNull(zip, "ZipEngine.zip");

        ZipModel srcZipModel = ZipModelBuilder.read(SrcZip.of(zip));

        for (String fileName : srcZipModel.getEntryNames()) {
            char[] password = settings.getEntrySettings(fileName).getPassword();
            fileNameWriter.put(fileName, new ExistedEntryWriter(srcZipModel, fileName, tempZipModel, password));
        }
    }

    @Override
    public void setComment(String comment) {
        tempZipModel.setComment(comment);
    }

    /**
     * This method should be called at the end of the <tt>try...catch</tt>
     * to correctly call <tt>close()</tt> method.
     */
    public void markSuccess() {
        success = true;
    }

    @Override
    public void close() {
        if (success && (tempZipModel.isChanged() || fileNameWriter.isChanged())) {
            createTempZipFiles();
            removeOriginalZipFiles();
            moveTempZipFiles();
        }
    }

    private void createTempZipFiles() {
        try (DataOutput out = creatDataOutput(tempZipModel)) {
            for (Writer writer : fileNameWriter.getWriters())
                writer.write(out);
        }
    }

    private void removeOriginalZipFiles() {
        if (!Files.exists(zip))
            return;

        SrcZip srcZip = SrcZip.of(zip);

        for (int diskNo = 0; diskNo < srcZip.getTotalDisks(); diskNo++)
            PathUtils.deleteIfExists(srcZip.getDiskByNo(diskNo).getPath());
    }

    private void moveTempZipFiles() {
        for (int diskNo = 0; diskNo <= tempZipModel.getTotalDisks(); diskNo++) {
            Path src = tempZipModel.getDisk(diskNo);
            Path dst = zip.getParent().resolve(src.getFileName());
            PathUtils.move(src, dst);
        }

        PathUtils.deleteIfExists(tempZipModel.getSrcZip().getPath().getParent());
    }

    private static ZipModel createTempZipModel(Path zip, ZipSettings settings, FileNameWriter fileNameWriter) {
        Map<String, Writer> map = new LinkedHashMap<>();
        Path tempZip = createTempZip(zip);
        ZipModel tempZipModel = ZipModelBuilder.build(tempZip, settings);
        tempZipModel.setTempDir(tempZip.getParent());

        if (Files.exists(zip)) {
            ZipModel zipModel = ZipModelBuilder.read(SrcZip.of(zip));
            zipModel.getSplitTriggers().forEach(tempZipModel::addSplitTrigger);

            if (zipModel.isSplit() && tempZipModel.getSplitTriggers().isEmpty())
                throw new SplitTriggerNotFoundException();

            if (zipModel.getComment() != null)
                tempZipModel.setComment(zipModel.getComment());

            tempZipModel.setZip64(zipModel.isZip64());

            zipModel.getEntryNames().forEach(entryName -> {
                char[] password = settings.getEntrySettings(entryName).getPassword();
                map.put(entryName, new ExistedEntryWriter(zipModel, entryName, tempZipModel, password));
            });
        }

        fileNameWriter.init(map);
        tempZipModel.finishInit();

        return tempZipModel;
    }

    private static Path createTempZip(Path zip) {
        Path dir = zip.getParent().resolve("tmp_" + UUID.randomUUID());
        return dir.resolve(zip.getFileName());
    }

    private static DataOutput creatDataOutput(ZipModel zipModel) {
        return zipModel.isSplit() ? new SplitZipDataOutput(zipModel) : new SolidZipDataOutput(zipModel);
    }

    private static final class FileNameWriter {

        private final Map<String, Writer> map = new LinkedHashMap<>();
        private int initSize;

        public void init(Map<String, Writer> map) {
            this.map.clear();
            this.map.putAll(map);
            initSize = map.size();
        }

        public void put(String entryName, Writer writer) {
            requireNotNull(writer, "fileNameWriter");

            if (map.containsKey(entryName))
                throw new EntryDuplicationException(entryName);

            map.put(entryName, writer);
        }

        public Writer remove(String entryName) {
            return map.remove(entryName);
        }

        public Set<String> getEntryNames() {
            return map.keySet();
        }

        public Collection<Writer> getWriters() {
            return map.values();
        }

        public boolean isChanged() {
            return initSize != map.size();
        }

    }

}
