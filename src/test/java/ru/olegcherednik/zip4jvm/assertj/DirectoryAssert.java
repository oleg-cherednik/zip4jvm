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

import ru.olegcherednik.zip4jvm.Zip4jvmSuite;
import ru.olegcherednik.zip4jvm.exception.Zip4jvmException;
import ru.olegcherednik.zip4jvm.utils.PathUtils;

import org.apache.commons.io.FilenameUtils;
import org.assertj.core.api.AbstractFileAssert;
import org.assertj.core.internal.Failures;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.olegcherednik.zip4jvm.assertj.Zip4jvmAssertions.assertThatFile;

/**
 * @author Oleg Cherednik
 * @since 27.03.2019
 */
public class DirectoryAssert extends AbstractFileAssert<DirectoryAssert> implements IDirectoryAssert<DirectoryAssert> {

    private static final String EXT_TXT = "txt";

    public DirectoryAssert(Path actual) {
        super(actual.toFile(), DirectoryAssert.class);
    }

    @Override
    public DirectoryAssert directory(String name) {
        if (SLASH.equals(name))
            throw new Zip4jvmException("Name cannot be '%s'", SLASH);

        return new DirectoryAssert(actual.toPath().resolve(name));
    }

    @Override
    public DirectoryAssert hasEntries(int expected) {
        int actual = getEntries(this.actual.toPath()).size();

        if (actual != expected)
            throw Failures.instance().failure(
                    String.format("Directory '%s' contains illegal amount of entries: actual - '%d', expected - '%d'",
                                  this.actual.getAbsolutePath(), actual, expected));

        return myself;
    }

    @Override
    public DirectoryAssert hasDirectories(int expected) {
        int actual = getDirectories(this.actual.toPath()).size();

        if (actual != expected)
            throw Failures.instance().failure(
                    String.format(
                            "Directory '%s' contains illegal amount of directories: actual - '%d', expected - '%d'",
                            this.actual.getAbsolutePath(),
                            actual,
                            expected));

        return myself;
    }

    @Override
    public DirectoryAssert hasRegularFiles(int expected) {
        int actual = getRegularFiles(this.actual.toPath()).size();

        if (actual != expected)
            throw Failures.instance().failure(String.format(
                    "Directory '%s' contains illegal amount of files: actual - '%d', expected - '%d'",
                    this.actual.getAbsolutePath(),
                    actual,
                    expected));

        return myself;
    }

    @Override
    public DirectoryAssert hasSymlinks(int expected) {
        int actual = getSymlinks(this.actual.toPath()).size();

        if (actual != expected)
            throw Failures.instance().failure(String.format(
                    "Directory '%s' contains illegal amount of files: actual - '%d', expected - '%d'",
                    this.actual.getAbsolutePath(),
                    actual,
                    expected));

        return myself;
    }

    @Override
    public DirectoryAssert exists() {
        super.exists();
        isDirectory();
        return myself;
    }

    private static Set<String> getEntries(Path dir) {
        try (Stream<Path> stream = Files.list(dir)) {
            return stream.map(path -> path.getFileName().toString())
                         .collect(Collectors.toSet());
        } catch (IOException e) {
            throw new Zip4jvmException(e);
        }
    }

    private static Set<String> getDirectories(Path dir) {
        try (Stream<Path> stream = Files.list(dir)) {
            return stream.filter(path -> Files.isDirectory(path) && !Files.isSymbolicLink(path))
                         .map(path -> path.getFileName().toString())
                         .collect(Collectors.toSet());
        } catch (IOException e) {
            throw new Zip4jvmException(e);
        }
    }

    private static Set<String> getRegularFiles(Path dir) {
        try (Stream<Path> stream = Files.list(dir)) {
            return stream.filter(path -> Files.isRegularFile(path) && !Files.isSymbolicLink(path))
                         .filter(file -> !PathUtils.DS_STORE.equalsIgnoreCase(file.getFileName().toString()))
                         .map(path -> path.getFileName().toString())
                         .collect(Collectors.toSet());
        } catch (IOException e) {
            throw new Zip4jvmException(e);
        }
    }

    private static Set<String> getSymlinks(Path dir) {
        try (Stream<Path> stream = Files.list(dir)) {
            return stream.filter(Files::isSymbolicLink)
                         .filter(file -> !PathUtils.DS_STORE.equalsIgnoreCase(file.getFileName().toString()))
                         .map(path -> path.getFileName().toString())
                         .collect(Collectors.toSet());
        } catch (IOException e) {
            throw new Zip4jvmException(e);
        }
    }

    @Override
    public DirectoryAssert matches(Consumer<IDirectoryAssert<?>> consumer) {
        consumer.accept(this);
        return myself;
    }

    @Override
    public RegularFileAssert regularFile(String name) {
        return new RegularFileAssert(actual.toPath().resolve(name));
    }

    @Override
    public SymlinkAssert symlink(String name) {
        return new SymlinkAssert(actual.toPath().resolve(name));
    }

    public DirectoryAssert matchesResourceDirectory(String resourcePrefix) {
        matchesResourceDirectory(actual.toPath(), resourcePrefix);
        return this;
    }

    private void matchesResourceDirectory(Path dir, String resourcePrefix) {
        hasSameRegularFiles(dir, resourcePrefix);
        hasSameDirectories(dir, resourcePrefix);
    }

    public DirectoryAssert hasSameDirectories(Path dir, String resourcePrefix) {
        Set<String> actual = getDirectories(dir);
        Set<String> expected = getDirectories(Zip4jvmSuite.getResourcePath(resourcePrefix));
        assertThat(actual).isEqualTo(expected);

        for (String folderName : actual) {
            matchesResourceDirectory(dir.resolve(folderName), resourcePrefix + '/' + folderName);
        }

        return this;
    }

    @SuppressWarnings("PMD.AvoidThrowingNewInstanceOfSameException")
    public DirectoryAssert hasSameRegularFiles(Path dir, String resourcePrefix) {
        Set<String> actual = getRegularFiles(dir);
        Set<String> expected = getRegularFiles(Zip4jvmSuite.getResourcePath(resourcePrefix));

        try {
            assertThat(actual).isEqualTo(expected);
        } catch (AssertionError e) {
            throw new AssertionError(String.format("%s %s", dir.toAbsolutePath(), e.getMessage()));
        }

        for (String fileName : actual) {
            String ext = FilenameUtils.getExtension(fileName);
            Path file = dir.resolve(fileName);
            String resourcePath = resourcePrefix + '/' + fileName;

            if (EXT_TXT.equalsIgnoreCase(ext))
                assertThatFile(file).matchesTextLines(resourcePath);
            else
                assertThatFile(file).matchesData(resourcePath);
        }

        return this;
    }

    @Override
    public DirectoryAssert isEmpty() {
        hasEntries(0);
        return myself;
    }

    @Override
    public String toString() {
        return actual.toString();
    }

}
