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
package ru.olegcherednik.zip4jvm.decompose;

import ru.olegcherednik.zip4jvm.model.ZipModel;
import ru.olegcherednik.zip4jvm.model.block.Block;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;
import ru.olegcherednik.zip4jvm.utils.ValidationUtils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.io.IOUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

/**
 * @author Oleg Cherednik
 * @since 07.12.2019
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Utils {

    public static void print(Path file, Consumer<PrintStream> consumer) throws FileNotFoundException {
        try (PrintStream out = new PrintStream(file.toFile())) {
            consumer.accept(out);
        }
    }

    public static void copyLarge(ZipModel zipModel, Path out, Block block) throws IOException {
        copyLarge(zipModel, out, block.getRelativeOffs(), block.getSize());
    }

    public static void copyLarge(ZipModel zipModel, Path out, long offs, long size) throws IOException {
        Path file = zipModel.getSrcZip().getDiskByAbsOffs(offs).getPath();

        try (InputStream fis = Files.newInputStream(file);
             OutputStream fos = Files.newOutputStream(out)) {
            long skipBytes = fis.skip(offs);
            assert skipBytes == offs;

            IOUtils.copyLarge(fis, fos, 0, size);
        }
    }

    public static void copyByteArray(Path out, byte[] buf, Block block) throws IOException {
        ValidationUtils.requireLessOrEqual(block.getAbsoluteOffs(), Integer.MAX_VALUE, "block.absoluteOffs");
        ValidationUtils.requireLessOrEqual(block.getSize(), Integer.MAX_VALUE, "block.size");

        try (OutputStream fos = Files.newOutputStream(out)) {
            fos.write(buf, (int) block.getAbsoluteOffs(), (int) block.getSize());
        }
    }

    public static void copyByteArray(Path out, byte[] buf) throws IOException {
        Files.write(out, buf);
    }

    public static Path createSubDir(Path dir, ZipEntry zipEntry, long pos) throws IOException {
        String fileName = zipEntry.getFileName();

        if (zipEntry.isDirectory())
            fileName = fileName.substring(0, fileName.length() - 1);

        fileName = "#" + (pos + 1) + " - " + fileName.replaceAll("[\\/]", "_-_");
        return Files.createDirectories(dir.resolve(fileName));
    }

}
