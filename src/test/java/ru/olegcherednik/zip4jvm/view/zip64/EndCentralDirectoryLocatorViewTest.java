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
package ru.olegcherednik.zip4jvm.view.zip64;

import ru.olegcherednik.zip4jvm.Zip4jvmSuite;
import ru.olegcherednik.zip4jvm.model.Zip64;
import ru.olegcherednik.zip4jvm.model.block.Block;

import org.testng.annotations.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Oleg Cherednik
 * @since 11.11.2019
 */
@Test
public class EndCentralDirectoryLocatorViewTest {

    public void shouldRetrieveAllLinesWhenLocatorExists() throws IOException {
        Block block = mock(Block.class);
        when(block.getSize()).thenReturn(20L);
        when(block.getDiskOffs()).thenReturn(11208273384L);

        Zip64.EndCentralDirectoryLocator locator = mock(Zip64.EndCentralDirectoryLocator.class);
        when(locator.getMainDiskNo()).thenReturn(1L);
        when(locator.getEndCentralDirectoryRelativeOffs()).thenReturn(11208273328L);
        when(locator.getTotalDisks()).thenReturn(5L);

        String[] lines = Zip4jvmSuite.execute(new EndCentralDirectoryLocatorView(locator, block, 2, 52, 0));
        assertThat(lines).hasSize(7);
        assertThat(lines[0]).isEqualTo("(PK0607) ZIP64 End of Central directory locator");
        assertThat(lines[1]).isEqualTo("===============================================");
        assertThat(lines[2])
                .isEqualTo("  - location:                                       11208273384 (0x29C10ADE8) bytes");
        assertThat(lines[3]).isEqualTo("  - size:                                           20 bytes");
        assertThat(lines[4]).isEqualTo("  part number of new-end-of-central-dir (0001):     2");
        assertThat(lines[5])
                .isEqualTo("  relative offset of new-end-of-central-dir:        11208273328 (0x29C10ADB0) bytes");
        assertThat(lines[6]).isEqualTo("  total number of parts in archive:                 5");
    }

    public void shouldRetrieveAllLineWithDiskWhenSplitZip() throws IOException {
        Block block = mock(Block.class);
        when(block.getSize()).thenReturn(20L);
        when(block.getDiskOffs()).thenReturn(11208273384L);
        when(block.getDiskNo()).thenReturn(5);
        when(block.getFileName()).thenReturn("src.zip");

        Zip64.EndCentralDirectoryLocator locator = mock(Zip64.EndCentralDirectoryLocator.class);
        when(locator.getMainDiskNo()).thenReturn(1L);
        when(locator.getEndCentralDirectoryRelativeOffs()).thenReturn(11208273328L);
        when(locator.getTotalDisks()).thenReturn(5L);

        String[] lines = Zip4jvmSuite.execute(new EndCentralDirectoryLocatorView(locator, block, 2, 52, 5));
        assertThat(lines).hasSize(8);
        assertThat(lines[0]).isEqualTo("(PK0607) ZIP64 End of Central directory locator");
        assertThat(lines[1]).isEqualTo("===============================================");
        assertThat(lines[2]).isEqualTo("  - disk (0005):                                    src.zip");
        assertThat(lines[3])
                .isEqualTo("  - location:                                       11208273384 (0x29C10ADE8) bytes");
        assertThat(lines[4]).isEqualTo("  - size:                                           20 bytes");
        assertThat(lines[5]).isEqualTo("  part number of new-end-of-central-dir (0001):     2");
        assertThat(lines[6])
                .isEqualTo("  relative offset of new-end-of-central-dir:        11208273328 (0x29C10ADB0) bytes");
        assertThat(lines[7]).isEqualTo("  total number of parts in archive:                 5");
    }

}
