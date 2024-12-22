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
import ru.olegcherednik.zip4jvm.crypto.aes.AesStrength;
import ru.olegcherednik.zip4jvm.crypto.strong.EncryptionAlgorithm;
import ru.olegcherednik.zip4jvm.crypto.strong.Flags;
import ru.olegcherednik.zip4jvm.crypto.strong.HashAlgorithm;
import ru.olegcherednik.zip4jvm.model.CompressionMethod;
import ru.olegcherednik.zip4jvm.model.Zip64;
import ru.olegcherednik.zip4jvm.model.block.Block;

import org.testng.annotations.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Oleg Cherednik
 * @since 02.01.2023
 */
@Test
public class ExtensibleDataSectorViewTest {

    public void shouldRetrieveAllLinesWhenZip64ExtensibleDataSectorExists() throws IOException {
        Block block = mock(Block.class);
        when(block.getSize()).thenReturn(56L);
        when(block.getDiskOffs()).thenReturn(11208273328L);

        Zip64.ExtensibleDataSector extensibleDataSector = createEndCentralDirectory();

        String[] lines = Zip4jvmSuite.execute(new ExtensibleDataSectorView(extensibleDataSector, block, 2, 52));
        assertThat(lines).hasSize(13);
        assertThat(lines[0]).isEqualTo("ZIP64 Extensible data sector");
        assertThat(lines[1]).isEqualTo("============================");
        assertThat(lines[2]).isEqualTo(
                "  - location:                                       11208273328 (0x29C10ADB0) bytes");
        assertThat(lines[3]).isEqualTo("  - size:                                           56 bytes");
        assertThat(lines[4]).isEqualTo("  compression method (99):                          AES encryption");
        assertThat(lines[5]).isEqualTo("  compressed size:                                  438 bytes");
        assertThat(lines[6]).isEqualTo("  uncompressed size:                                120 bytes");
        assertThat(lines[7]).isEqualTo("  encryption algorithm (0x6610):                    AES-256");
        assertThat(lines[8]).isEqualTo("  encryption key bits:                              256");
        assertThat(lines[9]).isEqualTo("  flags (0x01):                                     password");
        assertThat(lines[10]).isEqualTo("  hash algorithm (0x8004):                          SHA1");
        assertThat(lines[11]).isEqualTo("  hashData:                                         4 bytes");
        assertThat(lines[12]).isEqualTo("    0A 0B 0C 0D");
    }

    private static Zip64.ExtensibleDataSector createEndCentralDirectory() {
        return Zip64.ExtensibleDataSector.builder()
                                         .compressionMethod(CompressionMethod.AES)
                                         .compressedSize(438)
                                         .uncompressedSize(120)
                                         .encryptionAlgorithm(EncryptionAlgorithm.AES_256.getCode())
                                         .bitLength(AesStrength.S256.getSize())
                                         .flags(Flags.PASSWORD_KEY)
                                         .hashAlgorithm(HashAlgorithm.SHA1.getCode())
                                         .hashLength(4)
                                         .hashData(new byte[] { 0xA, 0xB, 0xC, 0xD })
                                         .build();
    }

}
