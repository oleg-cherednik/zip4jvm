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

import ru.olegcherednik.zip4jvm.utils.ZipUtils;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.assertj.core.internal.Failures;

import java.util.function.Consumer;
import java.util.function.Predicate;

import static ru.olegcherednik.zip4jvm.utils.PathUtils.SLASH;

/**
 * @author Oleg Cherednik
 * @since 25.03.2019
 */
public class ZipEntryDirectoryAssert extends AbstractZipEntryAssert<ZipEntryDirectoryAssert>
        implements IDirectoryAssert<ZipEntryDirectoryAssert> {

    public ZipEntryDirectoryAssert(ZipArchiveEntry actual, ZipFileDecorator zipFile) {
        super(actual, ZipEntryDirectoryAssert.class, zipFile);
    }

    public ZipEntryDirectoryAssert withRegularFileEncrypted(
            String name, char[] password, Consumer<IRegularFileAssert<?>> consumer) {
        regularFileEncrypted(name, password).matches(consumer);
        return myself;
    }

    public ZipEntryRegularFileAssert regularFileEncrypted(String name, char[] password) {
        ZipArchiveEntry entry = getEntry(name);
        ZipFileDecorator zipFileDecorator = Zip4jvmAssertions.createZipFileDecorator(zipFile.getZip(), password);
        return new ZipEntryRegularFileAssert(entry, zipFileDecorator);
    }

    // ---------- IDirectoryAssert ----------

    @Override
    public ZipEntryDirectoryAssert hasEntries(int expected) {
        long actual = getEntriesAmount();

        if (actual != expected)
            throw Failures.instance().failure(
                    String.format(
                            "Zip directory '%s' contains illegal amount of entries: actual - '%d', expected - '%d'",
                            this.actual, actual, expected));

        return myself;
    }

    @Override
    public ZipEntryDirectoryAssert hasDirectories(int expected) {
        long actual = getZipEntriesAmount(ZipEntryUtils::isDirectory);

        if (actual != expected)
            throw Failures.instance().failure(
                    String.format(
                            "Zip directory '%s' contains illegal amount of directories: actual - '%d', expected - '%d'",
                            this.actual, actual, expected));

        return myself;
    }

    @Override
    public ZipEntryDirectoryAssert hasRegularFiles(int expected) {
        long actual = getZipEntriesAmount(ZipEntryUtils::isRegularFile);

        if (actual != expected)
            throw Failures.instance().failure(String.format(
                    "Zip directory '%s' contains illegal amount of files: actual - '%d', expected - '%d'",
                    this.actual, actual, expected));

        return myself;
    }

    @Override
    public ZipEntryDirectoryAssert hasSymlinks(int expected) {
        long actual = getZipEntriesAmount(ZipEntryUtils::isSymlink);

        if (actual != expected)
            throw Failures.instance().failure(String.format(
                    "Zip directory '%s' contains illegal amount of symlinks: actual - '%d', expected - '%d'",
                    this.actual, actual, expected));

        return myself;
    }

    @Override
    public ZipEntryDirectoryAssert directory(String entryName) {
        if (!entryName.endsWith(SLASH_STR))
            entryName += SLASH;
        entryName = SLASH_STR.equals(actual.getName()) ? entryName : actual.getName() + entryName;
        return new ZipEntryDirectoryAssert(new ZipArchiveEntry(entryName), zipFile);
    }

    @Override
    public ZipEntryRegularFileAssert regularFile(String name) {
        return new ZipEntryRegularFileAssert(getEntry(name), zipFile);
    }

    @Override
    public ZipEntryDirectoryAssert withDirectory(String name, Consumer<IDirectoryAssert<?>> consumer) {
        directory(name).matches(consumer);
        return myself;
    }

    @Override
    public ZipEntryDirectoryAssert withRegularFile(String name, Consumer<IRegularFileAssert<?>> consumer) {
        regularFile(name).matches(consumer);
        return myself;
    }

    @Override
    public ISymlinkAssert<?> symlink(String name) {
        return new ZipEntrySymlinkAssert(getEntry(name), zipFile);
    }

    @Override
    public ZipEntryDirectoryAssert matches(Consumer<IDirectoryAssert<?>> consumer) {
        consumer.accept(this);
        return myself;
    }

    @Override
    public ZipEntryDirectoryAssert isEmpty() {
        hasEntries(0);
        return myself;
    }

    // ----------

    private ZipArchiveEntry getEntry(String entryName) {
        entryName = SLASH_STR.equals(actual.getName()) ? entryName : actual.getName() + entryName;
        return zipFile.getEntry(entryName);
    }

    private int getEntriesAmount() {
        return zipFile.getSubEntries(actual.getName()).size();
    }

    private int getZipEntriesAmount(Predicate<ZipArchiveEntry> predicate) {
        return (int) zipFile.getSubEntries(actual.getName()).stream()
                            .map(ZipUtils::getFileNameNoDirectoryMarker)
                            .map(entryName -> {
                                String parent = SLASH_STR.equals(actual.getName()) ? "" : actual.getName();
                                return zipFile.getEntry(parent + entryName);
                            })
                            .filter(predicate)
                            .count();
    }

}
