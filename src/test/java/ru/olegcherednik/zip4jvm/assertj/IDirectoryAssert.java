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

import java.util.function.Consumer;

/**
 * @param <S> {@link IDirectoryAssert}
 * @author Oleg Cherednik
 * @since 03.10.2019
 */
@SuppressWarnings("AbbreviationAsWordInName")
public interface IDirectoryAssert<S extends IDirectoryAssert<S>> {

    String SLASH = "/";

    S exists();

    S hasEntries(int expected);

    S hasDirectories(int expected);

    S hasRegularFiles(int expected);

    default S hasOnlyRegularFiles(int expected) {
        return hasEntries(expected).hasRegularFiles(expected);
    }

    S hasSymlinks(int expected);

    S isEmpty();

    S directory(String name);

    IRegularFileAssert<?> regularFile(String name);

    ISymlinkAssert<?> symlink(String name);

    S matches(Consumer<IDirectoryAssert<?>> consumer);

}
