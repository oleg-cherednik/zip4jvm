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
import com.cop.zip4j.exception.Zip4jAesStrengthNotSetException;
import com.cop.zip4j.exception.Zip4jEmptyPasswordException;
import com.cop.zip4j.exception.Zip4jPathNotExistsException;
import com.cop.zip4j.model.Encryption;
import com.cop.zip4j.model.ZipModel;
import com.cop.zip4j.model.ZipParameters;
import com.cop.zip4j.model.activity.PlainActivity;
import com.cop.zip4j.model.activity.Zip64Activity;
import com.cop.zip4j.model.aes.AesStrength;
import com.cop.zip4j.model.entry.PathZipEntry;
import com.cop.zip4j.model.entry.ZipEntry;
import com.cop.zip4j.utils.CreateZipModel;
import com.cop.zip4j.utils.ZipUtils;
import lombok.Builder;
import lombok.NonNull;
import org.apache.commons.lang.ArrayUtils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
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
    private final Charset charset = StandardCharsets.UTF_8;

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

        ZipModel zipModel = new CreateZipModel(zipFile, charset).get().noSplitOnly();
        zipModel.setSplitLength(parameters.getSplitLength());
        zipModel.setComment(ZipUtils.normalizeComment.apply(parameters.getComment()));

        if (parameters.isZip64())
            zipModel.zip64();

        List<PathZipEntry> entries = createEntries(withExistedEntries(paths));

        entries.forEach(entry -> {
            entry.setName(parameters.getRelativeEntryName(entry.getPath()));
            entry.setCompression(parameters.getCompression());
            entry.setEncryption(parameters.getEncryption());
            entry.setStrength(parameters.getStrength());
            entry.setPassword(parameters.getPassword());
            entry.setActivity(parameters.isZip64() ? Zip64Activity.INSTANCE : PlainActivity.INSTANCE);
            entry.setPassword(parameters.getPassword());
        });

        new ZipEngine(zipModel).addEntries(entries);
    }

    private static Collection<Path> withExistedEntries(Collection<Path> paths) {
        for (Path path : paths)
            if (!Files.exists(path))
                throw new Zip4jPathNotExistsException(path);

        return paths;
    }

    @NonNull
    private static List<PathZipEntry> createEntries(@NonNull Collection<Path> paths) {
        return paths.stream()
                    .filter(path -> Files.isRegularFile(path) || Files.isDirectory(path))
                    .map(path -> Files.isDirectory(path) ? getDirectoryEntries(path) : Collections.singleton(path))
                    .flatMap(Collection::stream)
                    .map(ZipEntry::of)
                    .collect(Collectors.toList());
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
        AesStrength strength = parameters.getStrength();
        boolean passwordEmpty = ArrayUtils.isEmpty(parameters.getPassword());

        if (encryption != Encryption.OFF && passwordEmpty)
            throw new Zip4jEmptyPasswordException();
        if (encryption == Encryption.AES && strength == AesStrength.NONE)
            throw new Zip4jAesStrengthNotSetException();
    }

}
