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
package ru.olegcherednik.zip4jvm.model;

import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Oleg Cherednik
 * @since 02.01.2020
 */
@Test
public class VersionTest {

    public void shouldParseFileSystemFromCodeWhenCodeKnown() {
        for (Version.FileSystem fileSystem : Version.FileSystem.values())
            assertThat(Version.FileSystem.parseCode(fileSystem.getCode())).isSameAs(fileSystem);
    }

    public void shouldRetrieveUnknownWhenCodeUnknown() {
        assertThat(Version.FileSystem.parseCode(-1)).isSameAs(Version.FileSystem.UNKNOWN);
    }

    public void shouldRetrieveStringWhenNull() {
        assertThat(Version.NULL.toString()).isEqualTo("<null>");
    }

    public void shouldRetrieveStringWhenNotNull() {
        assertThat(Version.of(Version.FileSystem.NTFS, 20).toString()).isEqualTo("NTFS / 2.0");
    }

    public void shouldRetrieveNullWhenBuildWithNullFileSystem() {
        assertThat(Version.of(null, 20)).isSameAs(Version.NULL);
    }

    public void shouldRetrieveSeparateObjectWhenBuildWithKnownFileSystem() {
        Version one = Version.of(Version.FileSystem.AMIGA, 20);
        Version two = Version.of(Version.FileSystem.AMIGA, 20);
        assertThat(one).isNotSameAs(two);
        assertThat(one).isEqualTo(two);
    }

}
