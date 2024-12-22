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
import ru.olegcherednik.zip4jvm.crypto.strong.EncryptionAlgorithm;
import ru.olegcherednik.zip4jvm.model.CentralDirectory;
import ru.olegcherednik.zip4jvm.model.CompressionMethod;
import ru.olegcherednik.zip4jvm.model.Zip64;
import ru.olegcherednik.zip4jvm.model.block.CentralDirectoryBlock;

import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Oleg Cherednik
 * @since 09.04.2023
 */
@Test
public class EncryptedCentralDirectoryViewTest {

    public void shouldRetrieveAllLinesWhenFileHeader() throws IOException {
        CentralDirectoryBlock.FileHeaderBlock block = mock(CentralDirectoryBlock.FileHeaderBlock.class);
        when(block.getSize()).thenReturn(81L);
        when(block.getDiskOffs()).thenReturn(255533L);

        String[] lines = Zip4jvmSuite.execute(new EncryptedCentralDirectoryView(createCentralDirectory(),
                                                                                null,
                                                                                block,
                                                                                4,
                                                                                52,
                                                                                0));

        assertThat(lines).hasSize(5);
        assertThat(lines[0]).isEqualTo("(PK0102) Central directory");
        assertThat(lines[1]).isEqualTo("==========================");
        assertThat(lines[2]).isEqualTo("    - location:                                     255533 (0x0003E62D) bytes");
        assertThat(lines[3]).isEqualTo("    - size:                                         81 bytes");
        assertThat(lines[4]).isEqualTo("    total entries:                                  1");
    }

    public void shouldPrintExtensibleDataSectorWhenAvailable() throws IOException {
        CentralDirectoryBlock.FileHeaderBlock block = mock(CentralDirectoryBlock.FileHeaderBlock.class);
        when(block.getSize()).thenReturn(81L);
        when(block.getDiskOffs()).thenReturn(255533L);

        String[] lines = Zip4jvmSuite.execute(new EncryptedCentralDirectoryView(createCentralDirectory(),
                                                                                createExtensibleDataSector(),
                                                                                block,
                                                                                4,
                                                                                52,
                                                                                0));

        assertThat(lines).hasSize(7);
        assertThat(lines[0]).isEqualTo("(PK0102) Central directory");
        assertThat(lines[1]).isEqualTo("==========================");
        assertThat(lines[2]).isEqualTo("    - location:                                     255533 (0x0003E62D) bytes");
        assertThat(lines[3]).isEqualTo("    - size:                                         81 bytes");
        assertThat(lines[4]).isEqualTo("    total entries:                                  1");
        assertThat(lines[5]).isEqualTo("    compression method (99):                        AES encryption");
        assertThat(lines[6]).isEqualTo("    encryption algorithm (0x6610):                  AES-256");
    }


    private static CentralDirectory createCentralDirectory() {
        CentralDirectory centralDirectory = new CentralDirectory();
        centralDirectory.setFileHeaders(Collections.singletonList(new CentralDirectory.FileHeader()));

        return centralDirectory;
    }

    private static Zip64.ExtensibleDataSector createExtensibleDataSector() {
        return Zip64.ExtensibleDataSector.builder()
                                         .compressionMethod(CompressionMethod.AES)
                                         .encryptionAlgorithm(EncryptionAlgorithm.AES_256.getCode())
                                         .build();
    }

}
