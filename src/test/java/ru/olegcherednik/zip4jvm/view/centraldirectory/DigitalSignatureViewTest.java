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
package ru.olegcherednik.zip4jvm.view.centraldirectory;

import ru.olegcherednik.zip4jvm.Zip4jvmSuite;
import ru.olegcherednik.zip4jvm.model.CentralDirectory;
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
public class DigitalSignatureViewTest {

    public void shouldRetrieveAllLinesWhenDigitalSignatureExists() throws IOException {
        Block block = mock(Block.class);
        when(block.getSize()).thenReturn(33L);
        when(block.getRelativeOffs()).thenReturn(255614L);

        CentralDirectory.DigitalSignature digitalSignature = new CentralDirectory.DigitalSignature();
        digitalSignature.setSignatureData(new byte[] { 0x0, 0x1, 0x2, 0x3 });

        String[] lines = Zip4jvmSuite.execute(new DigitalSignatureView(digitalSignature, block, 4, 52, 0));

        assertThat(lines).hasSize(5);
        assertThat(lines[0]).isEqualTo("(PK0505) Digital signature");
        assertThat(lines[1]).isEqualTo("==========================");
        assertThat(lines[2]).isEqualTo("    - location:                                     255614 (0x0003E67E) bytes");
        assertThat(lines[3]).isEqualTo("    - size:                                         33 bytes");
        assertThat(lines[4]).isEqualTo("    00 01 02 03");
    }

    public void shouldRetrieveAllLinesWithDiskWhenSplitZip() throws IOException {
        Block block = mock(Block.class);
        when(block.getSize()).thenReturn(33L);
        when(block.getRelativeOffs()).thenReturn(255614L);
        when(block.getDiskNo()).thenReturn(5);
        when(block.getFileName()).thenReturn("src.zip");

        CentralDirectory.DigitalSignature digitalSignature = new CentralDirectory.DigitalSignature();
        digitalSignature.setSignatureData(new byte[] { 0x0, 0x1, 0x2, 0x3 });

        String[] lines = Zip4jvmSuite.execute(new DigitalSignatureView(digitalSignature, block, 4, 52, 5));

        assertThat(lines).hasSize(6);
        assertThat(lines[0]).isEqualTo("(PK0505) Digital signature");
        assertThat(lines[1]).isEqualTo("==========================");
        assertThat(lines[2]).isEqualTo("    - disk (0005):                                  src.zip");
        assertThat(lines[3]).isEqualTo("    - location:                                     255614 (0x0003E67E) bytes");
        assertThat(lines[4]).isEqualTo("    - size:                                         33 bytes");
        assertThat(lines[5]).isEqualTo("    00 01 02 03");
    }
}
