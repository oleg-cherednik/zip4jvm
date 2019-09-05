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
package ru.olegcherednik.zip4jvm;

import lombok.Builder;
import lombok.NonNull;
import org.apache.commons.lang.ArrayUtils;
import ru.olegcherednik.zip4jvm.engine.ZipEngine;
import ru.olegcherednik.zip4jvm.exception.Zip4jEmptyPasswordException;
import ru.olegcherednik.zip4jvm.exception.Zip4jPathNotExistsException;
import ru.olegcherednik.zip4jvm.model.Encryption;
import ru.olegcherednik.zip4jvm.model.ZipEntrySettings;
import ru.olegcherednik.zip4jvm.model.ZipModel;
import ru.olegcherednik.zip4jvm.model.ZipParameters;
import ru.olegcherednik.zip4jvm.model.builders.ZipModelBuilder;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntryBuilder;
import ru.olegcherednik.zip4jvm.utils.ZipUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author Oleg Cherednik
 * @since 14.03.2019
 */
@Builder
public final class ZipIt {

    @NonNull
    private final Path zipFile;

    public void add(@NonNull Path path, @NonNull ZipParameters parameters) throws IOException {
        add(Collections.singleton(path), parameters);
    }

    public void add(@NonNull Collection<Path> paths, @NonNull ZipParameters parameters) throws IOException {
        checkParameters(parameters);

        if (paths.size() == 1) {
            Path path = paths.iterator().next();

            if (Files.isDirectory(path) && parameters.getDefaultFolderPath() == null)
                parameters.setDefaultFolderPath(path);
        }

        ZipModel zipModel = ZipModelBuilder.readOrCreate(zipFile).noSplitOnly();
        zipModel.setSplitSize(parameters.getSplitLength());
        zipModel.setComment(ZipUtils.normalizeComment.apply(parameters.getComment()));
        zipModel.setZip64(parameters.isZip64());

        List<ZipEntry> entries = createEntries(withExistedEntries(paths), parameters);
        // TODO if at least one fileName is null then defaultRootPath is not correct
        new ZipEngine(zipModel).addEntries(entries);
    }

    public static Collection<Path> withExistedEntries(Collection<Path> paths) {
        for (Path path : paths)
            if (!Files.exists(path))
                throw new Zip4jPathNotExistsException(path);

        return paths;
    }

    @NonNull
    public static List<ZipEntry> createEntries(Collection<Path> paths, ZipEntrySettings settings) {
        paths = getUniqueRecursivePaths(paths);
        Set<Path> emptyDirectories = getEmptyDirectories(paths);

        return paths.parallelStream()
                    .filter(path -> Files.isRegularFile(path) || emptyDirectories.contains(path))
                    .sorted()
                    .map(path -> ZipEntryBuilder.create(path, settings))
                    .collect(Collectors.toList());
    }

    @NonNull
    public static List<ZipEntry> createEntries(Collection<Path> paths, ZipParameters parameters) {
        paths = getUniqueRecursivePaths(paths);
        Set<Path> emptyDirectories = getEmptyDirectories(paths);

        return paths.parallelStream()
                    .filter(path -> Files.isRegularFile(path) || emptyDirectories.contains(path))
                    .sorted()
                    .map(path -> ZipEntryBuilder.create(path, parameters))
                    .collect(Collectors.toList());
    }

    private static Set<Path> getUniqueRecursivePaths(Collection<Path> paths) {
        return paths.stream()
                    .filter(path -> Files.isRegularFile(path) || Files.isDirectory(path))
                    .map(path -> Files.isDirectory(path) ? getDirectoryEntries(path) : Collections.singleton(path))
                    .flatMap(Collection::stream)
                    .collect(Collectors.toSet());
    }

    private static Set<Path> getEmptyDirectories(Collection<Path> paths) {
        final Predicate<Path> emptyDirectory = path -> {
            int pathLength = path.toAbsolutePath().toString().length();
            return paths.stream().noneMatch(p -> p.startsWith(path) && p.toAbsolutePath().toString().length() > pathLength);
        };

        return paths.stream()
                    .filter(Files::isDirectory)
                    .filter(emptyDirectory)
                    .collect(Collectors.toSet());
    }

    @NonNull
    private static List<Path> getDirectoryEntries(@NonNull Path dir) {
        assert Files.isDirectory(dir);

        try {
            return Files.walk(dir)
                        .filter(path -> Files.isRegularFile(path) || Files.isDirectory(path))
                        .collect(Collectors.toList());
        } catch(IOException e) {
            return Collections.emptyList();
        }
    }

    private static void checkParameters(ZipParameters parameters) {
        Encryption encryption = parameters.getEncryption();
        boolean passwordEmpty = ArrayUtils.isEmpty(parameters.getPassword());

        if (encryption != Encryption.OFF && passwordEmpty)
            throw new Zip4jEmptyPasswordException();
    }

}
