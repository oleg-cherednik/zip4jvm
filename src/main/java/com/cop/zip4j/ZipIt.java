/*
 * Copyright 2019 Cherednik Oleg
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
package com.cop.zip4j;

import com.cop.zip4j.engine.ZipEngine;
import com.cop.zip4j.exception.ZipException;
import com.cop.zip4j.model.InputStreamMeta;
import com.cop.zip4j.model.ZipModel;
import com.cop.zip4j.model.ZipParameters;
import com.cop.zip4j.utils.CreateZipModelSup;
import com.cop.zip4j.utils.ZipUtils;
import lombok.Builder;
import lombok.NonNull;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Oleg Cherednik
 * @since 14.03.2019
 */
@Builder
public final class ZipIt {

    @NonNull
    private final Path zipFile;
    @NonNull
    @Builder.Default
    private final Charset charset = Charset.defaultCharset();

    public void add(@NonNull Collection<Path> paths, @NonNull ZipParameters parameters) throws IOException {
        if (paths.stream().anyMatch(path -> !Files.isDirectory(path) && !Files.isRegularFile(path)))
            throw new ZipException("Cannot add neither directory nor regular file to zip");

        paths = getRegularFilesAndDirectoryEntries(paths);
        addRegularFiles(paths, parameters);
    }

    public void add(@NonNull Path path, @NonNull ZipParameters parameters) {
        if (Files.isDirectory(path))
            addDirectory(path, parameters);
        else if (Files.isRegularFile(path))
            addRegularFiles(Collections.singleton(path), parameters);
        else
            throw new ZipException("Cannot add neither directory nor regular file to zip: " + path);
    }

    public void add(@NonNull InputStreamMeta file, @NonNull ZipParameters parameters) throws ZipException {
        addStream(Collections.singletonList(file), parameters);
    }

    public void addStream(@NonNull Collection<InputStreamMeta> files, @NonNull ZipParameters parameters) throws ZipException {
        ZipModel zipModel = new CreateZipModelSup(zipFile, charset).get().noSplitOnly();
        parameters.setSourceExternalStream(true);

        new ZipEngine(zipModel).addStreamToZip(files, parameters);
    }

    // TODO addDirectory and addRegularFile are same
    private void addDirectory(Path dir, ZipParameters parameters) {
        assert Files.isDirectory(dir);

        if (Files.isDirectory(dir) && parameters.getDefaultFolderPath() == null)
            parameters.setDefaultFolderPath(dir);

        ZipModel zipModel = new CreateZipModelSup(zipFile, charset).get().noSplitOnly();
        zipModel.setSplitLength(parameters.getSplitLength());
        zipModel.getEndCentralDirectory().setComment(ZipUtils.normalizeComment.apply(parameters.getComment()));

        if (parameters.isZip64())
            zipModel.zip64();

        new ZipEngine(zipModel).addEntries(getDirectoryEntries(dir), parameters);
    }

    private void addRegularFiles(@NonNull Collection<Path> files, @NonNull ZipParameters parameters) {
        assert files.stream().allMatch(file -> Files.isRegularFile(file));

        ZipModel zipModel = new CreateZipModelSup(zipFile, charset).get().noSplitOnly();
        zipModel.setSplitLength(parameters.getSplitLength());
        zipModel.getEndCentralDirectory().setComment(ZipUtils.normalizeComment.apply(parameters.getComment()));

        new ZipEngine(zipModel).addEntries(files, parameters);
    }

    @NonNull
    private static List<Path> getRegularFilesAndDirectoryEntries(@NonNull Collection<Path> paths) {
        return paths.stream()
                    .filter(path -> Files.isRegularFile(path) || Files.isDirectory(path))
                    .map(path -> Files.isDirectory(path) ? getDirectoryEntries(path) : Collections.singleton(path))
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());
    }

    @NonNull
    public static List<Path> getDirectoryEntries(@NonNull Path dir) {
        assert Files.isDirectory(dir);

        try {
            return Files.walk(dir)
                        .filter(path -> Files.isRegularFile(path) || Files.isDirectory(path))
                        .collect(Collectors.toList());
        } catch(IOException e) {
            return Collections.emptyList();
        }
    }

}
