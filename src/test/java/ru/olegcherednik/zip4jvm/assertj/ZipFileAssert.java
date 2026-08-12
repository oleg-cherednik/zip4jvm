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
package ru.olegcherednik.zip4jvm.assertj;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.internal.Failures;

import java.nio.file.Files;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.olegcherednik.zip4jvm.assertj.IDirectoryAssert.SLASH;
import static ru.olegcherednik.zip4jvm.assertj.IDirectoryAssert.SLASH_CHAR;

/**
 * @author Oleg Cherednik
 * @since 25.03.2019
 */
public class ZipFileAssert extends AbstractAssert<ZipFileAssert, ZipFileDecorator> {

    public ZipFileAssert(ZipFileDecorator actual) {
        super(actual, ZipFileAssert.class);
    }

    public ZipEntryDirectoryAssert root() {
        return directory(SLASH);
    }

    public ZipEntryDirectoryAssert directory(String name) {
        if (!name.endsWith(SLASH))
            name += SLASH_CHAR;

        ZipArchiveEntry entry = new ZipArchiveEntry(name);

        if (!entry.isDirectory())
            throw Failures.instance().failure(
                    String.format("Zip file does not contain directory entry '%s'"
                                          + " (directory entry should end with '%s'", name, SLASH_CHAR));

        return new ZipEntryDirectoryAssert(entry, actual);
    }

    public ZipEntryRegularFileAssert regularFile(String name) {
        ZipArchiveEntry entry = actual.getEntry(name);

        if (entry == null || !ZipEntryUtils.isRegularFile(entry))
            throw Failures.instance().failure(
                    String.format("Zip file does not contain file entry '%s'", name));

        if (entry.isDirectory())
            throw Failures.instance().failure(
                    String.format("Zip file does not contain file entry '%s'"
                                          + " (file entry should not end with '%s'", name, SLASH_CHAR));

        return new ZipEntryRegularFileAssert(entry, actual);
    }

    public ZipEntrySymlinkAssert symlink(String name) {
        ZipArchiveEntry entry = actual.getEntry(name);

        if (entry == null || !ZipEntryUtils.isSymlink(entry))
            throw Failures.instance().failure(
                    String.format("Zip file does not contain symlink entry '%s'", name));

        return new ZipEntrySymlinkAssert(entry, actual);
    }

    public ZipFileAssert exists() {
        isNotNull();
        assertThat(Files.exists(actual.getZip())).isTrue();
        assertThat(Files.isRegularFile(actual.getZip())).isTrue();
        return myself;
    }

    public DirectoryAssert parent() {
        return new DirectoryAssert(actual.zip.getParent());
    }

    public ZipFileAssert withParent(Consumer<DirectoryAssert> consumer) {
        consumer.accept(parent());
        return myself;
    }

    public ZipFileAssert hasCommentSize(int size) {
        if (size == 0)
            assertThat(actual.getComment()).isNull();
        else
            assertThat(actual.getComment()).hasSize(size);

        return myself;
    }

    public ZipFileAssert hasComment(String comment) {
        assertThat(actual.getComment()).isEqualTo(comment);
        return myself;
    }

    public ZipFileAssert hasSize(long expectedSizeInBytes) {
        assertThat(actual.zip).hasSize(expectedSizeInBytes);
        return myself;
    }

}
