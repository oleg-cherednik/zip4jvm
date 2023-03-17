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
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.internal.Failures;
import ru.olegcherednik.zip4jvm.exception.Zip4jvmException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Oleg Cherednik
 * @since 25.03.2019
 */
@SuppressWarnings("MethodCanBeVariableArityMethod")
public class ZipFileAssert extends AbstractAssert<ZipFileAssert, ZipFileDecorator> {

    public ZipFileAssert(ZipFileDecorator actual) {
        super(actual, ZipFileAssert.class);
    }

    public ZipEntryDirectoryAssert root() {
        return directory("/");
    }

    public ZipEntryDirectoryAssert directory(String name) {
        ZipArchiveEntry entry = new ZipArchiveEntry(name);

        if (!entry.isDirectory())
            throw Failures.instance().failure(
                    String.format("Zip file does not contain directory entry '%s' (directory entry should end with '/'", name));

        return new ZipEntryDirectoryAssert(entry, actual);
    }

    public ZipEntryFileAssert file(String name) {
        ZipArchiveEntry entry = actual.getEntry(name);

        if (entry == null || !ZipEntryUtils.isRegularFile(entry))
            throw Failures.instance().failure(
                    String.format("Zip file does not contain file entry '%s'", name));

        if (entry.isDirectory())
            throw Failures.instance().failure(
                    String.format("Zip file does not contain file entry '%s' (file entry should not end with '/'", name));

        return new ZipEntryFileAssert(entry, actual);
    }

    public ZipEntryDirectoryAssert symlinkDirectory(String name) {
        ZipArchiveEntry entry = actual.getEntry(name);

        if (entry == null || !ZipEntryUtils.isSymlink(entry))
            throw Failures.instance().failure(
                    String.format("Zip file does not contain symlink entry '%s'", name));

        String target = getSymlinkTarget(entry);
        return directory(FilenameUtils.getPath(name) + target);
    }

    public ZipEntryFileAssert symlinkFile(String name) throws IOException {
        ZipArchiveEntry entry = actual.getEntry(name);

        if (entry == null || !ZipEntryUtils.isSymlink(entry))
            throw Failures.instance().failure(
                    String.format("Zip file does not contain symlink entry '%s'", name));

        String target = getSymlinkTarget(entry);
        return file(FilenameUtils.getPath(name) + target);
    }

    private String getSymlinkTarget(ZipArchiveEntry entry) {
        try (InputStream in = actual.getInputStream(entry)) {
            return IOUtils.toString(in, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new Zip4jvmException(e);
        }
    }

    public ZipFileAssert exists() {
        isNotNull();
        assertThat(Files.exists(actual.getZip())).isTrue();
        assertThat(Files.isRegularFile(actual.getZip())).isTrue();
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

}
