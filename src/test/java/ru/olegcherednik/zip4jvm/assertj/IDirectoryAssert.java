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

import java.util.function.Consumer;

/**
 * @param <S> {@link IDirectoryAssert}
 * @author Oleg Cherednik
 * @since 03.10.2019
 */
@SuppressWarnings("AbbreviationAsWordInName")
public interface IDirectoryAssert<S extends IDirectoryAssert<S>> {

    String SLASH = "/";
    char SLASH_CHAR = '/';

    S exists();

    S hasEntries(int expected);

    S hasDirectories(int expected);

    S hasRegularFiles(int expected);

    default S hasOnlyRegularFiles(int expected) {
        return hasEntries(expected).hasRegularFiles(expected);
    }

    default S hasOnlyDirectories(int expected) {
        return hasEntries(expected).hasDirectories(expected);
    }

    default S hasOnlyDirectoriesRegularFiles(int expectedDirectories, int expectedRegularFiles) {
        return hasEntries(expectedDirectories + expectedRegularFiles)
                .hasDirectories(expectedDirectories)
                .hasRegularFiles(expectedRegularFiles);
    }

    default S hasOnlyDirectoriesRegularFilesSymlinks(int expectedDirectories,
                                                     int expectedRegularFiles,
                                                     int expectedSymlinks) {
        return hasEntries(expectedDirectories + expectedRegularFiles + expectedSymlinks)
                .hasDirectories(expectedDirectories)
                .hasRegularFiles(expectedRegularFiles)
                .hasSymlinks(expectedSymlinks);
    }

    S hasSymlinks(int expected);

    S isEmpty();

    S directory(String name);

    IRegularFileAssert<?> regularFile(String name);

    S withDirectory(String name, Consumer<IDirectoryAssert<?>> consumer);

    S withRegularFile(String name, Consumer<IRegularFileAssert<?>> consumer);

    ISymlinkAssert<?> symlink(String name);

    S matches(Consumer<IDirectoryAssert<?>> consumer);

}
