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
package ru.olegcherednik.zip4jvm.decompose;

import ru.olegcherednik.zip4jvm.exception.Zip4jvmException;
import ru.olegcherednik.zip4jvm.model.ZipModel;
import ru.olegcherednik.zip4jvm.model.block.Block;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;
import ru.olegcherednik.zip4jvm.utils.PathUtils;
import ru.olegcherednik.zip4jvm.utils.quitely.Quietly;
import ru.olegcherednik.zip4jvm.view.out.Out;
import ru.olegcherednik.zip4jvm.view.out.PrintStreamOut;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

/**
 * @author Oleg Cherednik
 * @since 07.12.2019
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Utils {

    public static void print(Path file, Consumer<Out> consumer) {
        try (Out out = new PrintStreamOut(new PrintStream(file.toFile(), StandardCharsets.UTF_8.name()))) {
            consumer.accept(out);
        } catch (IOException e) {
            throw new Zip4jvmException(e);
        }
    }

    public static void copyLarge(ZipModel zipModel, Path out, Block block) {
        copyLarge(zipModel, out, block.getDiskOffs(), block.getAbsOffs(), block.getSize());
    }

    public static void copyLarge(ZipModel zipModel, Path out, long diskOffs, long absOffs, long size) {
        Path file = zipModel.getSrcZip().getDiskByAbsOffs(absOffs).getPath();

        try (InputStream fis = PathUtils.newInputStream(file);
             OutputStream fos = PathUtils.newOutputStream(out)) {
            long skipBytes = fis.skip(diskOffs);
            assert skipBytes == diskOffs;

            IOUtils.copyLarge(fis, fos, 0, size);
        } catch (IOException e) {
            throw new Zip4jvmException(e);
        }
    }

    public static Path createSubDir(Path dir, ZipEntry zipEntry, long pos) {
        String fileName = zipEntry.getFileName();

        if (zipEntry.isDirectory())
            fileName = fileName.substring(0, fileName.length() - 1);

        fileName = "#" + (pos + 1) + " - " + fileName.replaceAll("[\\/]", "_-_");
        return PathUtils.createDirectories(dir.resolve(fileName));
    }

    public static Path createDirectories(Path dir) {
        return Quietly.doRuntime(() -> Files.createDirectories(dir.resolve("extra_fields")));
    }

}
