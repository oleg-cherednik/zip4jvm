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
package ru.olegcherednik.zip4jvm.view;

import org.testng.annotations.Test;
import ru.olegcherednik.zip4jvm.Zip4jvmSuite;
import ru.olegcherednik.zip4jvm.model.Version;

import java.io.IOException;
import java.io.PrintStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * @author Oleg Cherednik
 * @since 08.11.2019
 */
@Test
public class VersionViewTest {

    public void shouldRetrieveVersionMadeByOnlyAndVersionToExtractWhenBothVersionsSet() throws IOException {
        String[] lines = Zip4jvmSuite.execute(new VersionView(Version.of(0x12), Version.of(0x134), 0, 52));

        assertThat(lines).hasSize(4);
        assertThat(lines[0]).isEqualTo("version made by operating system (00):              MS-DOS, OS/2, NT FAT");
        assertThat(lines[1]).isEqualTo("version made by zip software (18):                  1.8");
        assertThat(lines[2]).isEqualTo("operat. system version needed to extract (01):      Amiga");
        assertThat(lines[3]).isEqualTo("unzip software version needed to extract (52):      5.2");
    }

    public void shouldRetrieveVersionMadeByOnlyWhenOnlyItSet() throws IOException {
        String[] lines = Zip4jvmSuite.execute(new VersionView(Version.of(0x12), null, 0, 52));

        assertThat(lines).hasSize(2);
        assertThat(lines[0]).isEqualTo("version made by operating system (00):              MS-DOS, OS/2, NT FAT");
        assertThat(lines[1]).isEqualTo("version made by zip software (18):                  1.8");
    }

    public void shouldRetrieveVersionToExtractOnlyWhenOnlyItSet() throws IOException {
        String[] lines = Zip4jvmSuite.execute(new VersionView(null, Version.of(0x134), 0, 52));

        assertThat(lines).hasSize(2);
        assertThat(lines[0]).isEqualTo("operat. system version needed to extract (01):      Amiga");
        assertThat(lines[1]).isEqualTo("unzip software version needed to extract (52):      5.2");
    }

    public void shouldRetrieveEmptyStringWhenVersionNull() throws IOException {
        PrintStream out = mock(PrintStream.class);
        VersionView view = new VersionView(null, null, 0, 52);
        assertThat(view.print(out)).isFalse();
    }
}
