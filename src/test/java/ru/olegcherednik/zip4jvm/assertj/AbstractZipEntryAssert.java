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
import org.assertj.core.api.AbstractAssert;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

/**
 * @param <S> {@link AbstractZipEntryAssert}
 * @author Oleg Cherednik
 * @since 25.03.2019
 */
public abstract class AbstractZipEntryAssert<S extends AbstractZipEntryAssert<S>>
        extends AbstractAssert<S, ZipArchiveEntry> {

    protected final ZipFileDecorator zipFile;

    protected AbstractZipEntryAssert(ZipArchiveEntry actual, Class<?> selfType, ZipFileDecorator zipFile) {
        super(actual, selfType);
        this.zipFile = zipFile;
    }

    public S exists() {
        isNotNull();
        assertThat(zipFile.containsEntry(actual.getName())).isTrue();
        return myself;
    }

    @Override
    public String toString() {
        return actual.getName();
    }

}
