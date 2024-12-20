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
package ru.olegcherednik.zip4jvm.view.entry;

import ru.olegcherednik.zip4jvm.Zip4jvmSuite;
import ru.olegcherednik.zip4jvm.model.DataDescriptor;
import ru.olegcherednik.zip4jvm.model.block.Block;

import org.testng.annotations.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Oleg Cherednik
 * @since 09.11.2019
 */
@Test
public class DataDescriptorViewTest {

    public void shouldRetrieveAllLinesWhenDataDescriptorExists() throws IOException {
        Block block = mock(Block.class);
        when(block.getSize()).thenReturn(16L);
        when(block.getDiskOffs()).thenReturn(255496L);

        DataDescriptor dataDescriptor = new DataDescriptor(3992319659L, 255436L, 293823L);

        String[] lines = Zip4jvmSuite.execute(new DataDescriptorView(dataDescriptor, block, 1, 2, 52, 0));

        assertThat(lines).hasSize(7);
        assertThat(lines[0]).isEqualTo("#2 (PK0708) Data descriptor");
        assertThat(lines[1]).isEqualTo("---------------------------");
        assertThat(lines[2]).isEqualTo("  - location:                                       255496 (0x0003E608) bytes");
        assertThat(lines[3]).isEqualTo("  - size:                                           16 bytes");
        assertThat(lines[4]).isEqualTo("  32-bit CRC value:                                 0xEDF5F6AB");
        assertThat(lines[5]).isEqualTo("  compressed size:                                  255436 bytes");
        assertThat(lines[6]).isEqualTo("  uncompressed size:                                293823 bytes");
    }

    public void shouldRetrieveAllLinesWithDiskWhenSplitZip() throws IOException {
        Block block = mock(Block.class);
        when(block.getSize()).thenReturn(16L);
        when(block.getDiskOffs()).thenReturn(255496L);
        when(block.getDiskNo()).thenReturn(5);
        when(block.getFileName()).thenReturn("src.zip");

        DataDescriptor dataDescriptor = new DataDescriptor(3992319659L, 255436L, 293823L);

        String[] lines = Zip4jvmSuite.execute(new DataDescriptorView(dataDescriptor, block, 1, 2, 52, 5));

        assertThat(lines).hasSize(8);
        assertThat(lines[0]).isEqualTo("#2 (PK0708) Data descriptor");
        assertThat(lines[1]).isEqualTo("---------------------------");
        assertThat(lines[2]).isEqualTo("  - disk (0005):                                    src.zip");
        assertThat(lines[3]).isEqualTo("  - location:                                       255496 (0x0003E608) bytes");
        assertThat(lines[4]).isEqualTo("  - size:                                           16 bytes");
        assertThat(lines[5]).isEqualTo("  32-bit CRC value:                                 0xEDF5F6AB");
        assertThat(lines[6]).isEqualTo("  compressed size:                                  255436 bytes");
        assertThat(lines[7]).isEqualTo("  uncompressed size:                                293823 bytes");
    }
}
