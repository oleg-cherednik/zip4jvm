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

import ru.olegcherednik.zip4jvm.model.Charsets;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

/**
 * @author Oleg Cherednik
 * @since 17.03.2023
 */
public class ZipEntrySymlinkAssert extends AbstractZipEntryAssert<ZipEntrySymlinkAssert>
        implements ISymlinkAssert<ZipEntrySymlinkAssert> {

    public ZipEntrySymlinkAssert(ZipArchiveEntry actual, ZipFileDecorator zipFile) {
        super(actual, ZipEntrySymlinkAssert.class, zipFile);
    }

    @Override
    public ZipEntrySymlinkAssert hasTarget(String expectedTarget) {
        try (InputStream in = zipFile.getInputStream(actual)) {
            String actualTarget = IOUtils.toString(in, Charsets.UTF_8);
            assertThat(actualTarget).isEqualTo(expectedTarget);
        } catch (IOException e) {
            assertThatThrownBy(() -> {
                throw e;
            }).doesNotThrowAnyException();
        }

        return myself;
    }

}
