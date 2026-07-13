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
package ru.olegcherednik.zip4jvm;

import ru.olegcherednik.zip4jvm.engine.unzip.UnzipItByteArray;
import ru.olegcherednik.zip4jvm.engine.unzip.UnzipItPath;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.nio.file.Path;

/**
 * This is an entry point to unzip archives.
 *
 * @author Oleg Cherednik
 * @since 14.03.2019
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class UnzipIt {

    /**
     * Create {@link UnzipItPath} instance with given {@code zip} path to the zip archive
     *
     * @param zip not {@literal null} zip file path
     * @return not {@literal null} {@link UnzipItPath} instance
     */
    // @NotNull
    public static UnzipItPath zip(Path zip) {
        return UnzipItPath.create(zip);
    }

    @SuppressWarnings("MethodCanBeVariableArityMethod")
    // @NotNull
    public static UnzipItByteArray zip(byte[] zip) {
        return UnzipItByteArray.create(zip);
    }

}
