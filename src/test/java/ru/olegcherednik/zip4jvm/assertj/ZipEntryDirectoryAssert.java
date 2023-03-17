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
package ru.olegcherednik.zip4jvm.assertj;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.assertj.core.internal.Failures;
import ru.olegcherednik.zip4jvm.utils.ZipUtils;

import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * @author Oleg Cherednik
 * @since 25.03.2019
 */
public class ZipEntryDirectoryAssert extends AbstractZipEntryAssert<ZipEntryDirectoryAssert> implements IDirectoryAssert<ZipEntryDirectoryAssert> {

    public ZipEntryDirectoryAssert(ZipArchiveEntry actual, ZipFileDecorator zipFile) {
        super(actual, ZipEntryDirectoryAssert.class, zipFile);
    }

    @Override
    public ZipEntryDirectoryAssert hasEntries(int expected) {
        long actual = getEntriesAmount();

        if (actual != expected)
            throw Failures.instance().failure(
                    String.format("Zip directory '%s' contains illegal amount of entries: actual - '%d', expected - '%d'",
                                  this.actual, actual, expected));

        return myself;
    }

    @Override
    public ZipEntryDirectoryAssert hasDirectories(int expected) {
        long actual = getZipEntriesAmount(ZipEntryUtils::isDirectory);

        if (actual != expected)
            throw Failures.instance().failure(
                    String.format("Zip directory '%s' contains illegal amount of directories: actual - '%d', expected - '%d'",
                                  this.actual, actual, expected));

        return myself;
    }

    @Override
    public ZipEntryDirectoryAssert hasRegularFiles(int expected) {
        long actual = getZipEntriesAmount(ZipEntryUtils::isRegularFile);

        if (actual != expected)
            throw Failures.instance().failure(String.format("Zip directory '%s' contains illegal amount of files: actual - '%d', expected - '%d'",
                                                            this.actual, actual, expected));

        return myself;
    }

    @Override
    public ZipEntryDirectoryAssert hasSymlinks(int expected) {
        long actual = getZipEntriesAmount(ZipEntryUtils::isSymlink);

        if (actual != expected)
            throw Failures.instance().failure(String.format("Zip directory '%s' contains illegal amount of symlinks: actual - '%d', expected - '%d'",
                                                            this.actual, actual, expected));

        return myself;
    }

    @Override
    public ZipEntryFileAssert file(String name) {
        return new ZipEntryFileAssert(getEntry(name), zipFile);
    }

    @Override
    public ZipEntryDirectoryAssert directory(String name) {
        return new ZipEntryDirectoryAssert(getEntry(name), zipFile);
    }

    @SuppressWarnings("PMD.AvoidReassigningParameters")
    private ZipArchiveEntry getEntry(String name) {
        name = "/".equals(actual.getName()) ? name : actual.getName() + name;
        return new ZipArchiveEntry(name);
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

    private int getEntriesAmount() {
        return zipFile.getSubEntries(actual.getName()).size();
    }

    private int getZipEntriesAmount(Predicate<ZipArchiveEntry> predicate) {
        return (int)zipFile.getSubEntries(actual.getName()).stream()
                           .map(ZipUtils::getFileNameNoDirectoryMarker)
                           .map(zipFile::getEntry)
                           .filter(predicate)
                           .count();
    }

}
