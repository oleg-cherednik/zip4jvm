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

import org.testng.annotations.Test;
import ru.olegcherednik.zip4jvm.Zip4jvmSuite;
import ru.olegcherednik.zip4jvm.crypto.aes.AesStrength;
import ru.olegcherednik.zip4jvm.crypto.strong.EncryptionAlgorithm;
import ru.olegcherednik.zip4jvm.crypto.strong.Flags;
import ru.olegcherednik.zip4jvm.crypto.strong.HashAlgorithm;
import ru.olegcherednik.zip4jvm.model.CompressionMethod;
import ru.olegcherednik.zip4jvm.model.Version;
import ru.olegcherednik.zip4jvm.model.Zip64;
import ru.olegcherednik.zip4jvm.model.block.Block;

import java.io.IOException;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Oleg Cherednik
 * @since 11.11.2019
 */
@Test
public class EndCentralDirectoryViewTest {

    public void shouldRetrieveAllLinesWhenZip64EndCentralDirectoryExists() throws IOException {
        Block block = mock(Block.class);
        when(block.getSize()).thenReturn(56L);
        when(block.getRelativeOffs()).thenReturn(11208273328L);

        Zip64.EndCentralDirectory endCentralDirectory = createEndCentralDirectory(false);

        String[] lines = Zip4jvmSuite.execute(new EndCentralDirectoryView(endCentralDirectory, block, 2, 52, 0));
        Arrays.stream(lines).forEach(System.out::println);

        assertThat(lines).hasSize(15);
        assertThat(lines[0]).isEqualTo("(PK0606) ZIP64 End of Central directory record");
        assertThat(lines[1]).isEqualTo("==============================================");
        assertThat(lines[2]).isEqualTo("  - location:                                       11208273328 (0x29C10ADB0) bytes");
        assertThat(lines[3]).isEqualTo("  - size:                                           56 bytes");
        assertThat(lines[4]).isEqualTo("  number of bytes in rest of record:                345 bytes");
        assertThat(lines[5]).isEqualTo("  version made by operating system (00):            MS-DOS, OS/2, NT FAT");
        assertThat(lines[6]).isEqualTo("  version made by zip software (18):                1.8");
        assertThat(lines[7]).isEqualTo("  operat. system version needed to extract (01):    Amiga");
        assertThat(lines[8]).isEqualTo("  unzip software version needed to extract (52):    5.2");
        assertThat(lines[9]).isEqualTo("  part number of this part (0001):                  2");
        assertThat(lines[10]).isEqualTo("  part number of start of central dir (0002):       3");
        assertThat(lines[11]).isEqualTo("  number of entries in central dir in this part:    13");
        assertThat(lines[12]).isEqualTo("  total number of entries in central dir:           15");
        assertThat(lines[13]).isEqualTo("  size of central dir:                              115 (0x00000073) bytes");
        assertThat(lines[14]).isEqualTo("  relative offset of central dir:                   11208273213 (0x29C10AD3D) bytes");
    }

    public void shouldRetrieveAllLinesWithDiskWhenSplitZip() throws IOException {
        Block block = mock(Block.class);
        when(block.getSize()).thenReturn(56L);
        when(block.getRelativeOffs()).thenReturn(11208273328L);
        when(block.getDiskNo()).thenReturn(5);
        when(block.getFileName()).thenReturn("src.zip");

        Zip64.EndCentralDirectory endCentralDirectory = createEndCentralDirectory(false);

        String[] lines = Zip4jvmSuite.execute(new EndCentralDirectoryView(endCentralDirectory, block, 2, 52, 5));

        assertThat(lines).hasSize(16);
        assertThat(lines[0]).isEqualTo("(PK0606) ZIP64 End of Central directory record");
        assertThat(lines[1]).isEqualTo("==============================================");
        assertThat(lines[2]).isEqualTo("  - disk (0005):                                    src.zip");
        assertThat(lines[3]).isEqualTo("  - location:                                       11208273328 (0x29C10ADB0) bytes");
        assertThat(lines[4]).isEqualTo("  - size:                                           56 bytes");
        assertThat(lines[5]).isEqualTo("  number of bytes in rest of record:                345 bytes");
        assertThat(lines[6]).isEqualTo("  version made by operating system (00):            MS-DOS, OS/2, NT FAT");
        assertThat(lines[7]).isEqualTo("  version made by zip software (18):                1.8");
        assertThat(lines[8]).isEqualTo("  operat. system version needed to extract (01):    Amiga");
        assertThat(lines[9]).isEqualTo("  unzip software version needed to extract (52):    5.2");
        assertThat(lines[10]).isEqualTo("  part number of this part (0001):                  2");
        assertThat(lines[11]).isEqualTo("  part number of start of central dir (0002):       3");
        assertThat(lines[12]).isEqualTo("  number of entries in central dir in this part:    13");
        assertThat(lines[13]).isEqualTo("  total number of entries in central dir:           15");
        assertThat(lines[14]).isEqualTo("  size of central dir:                              115 (0x00000073) bytes");
        assertThat(lines[15]).isEqualTo("  relative offset of central dir:                   11208273213 (0x29C10AD3D) bytes");
    }

    public void shouldRetrieveAllLinesWhenZip64ExtensibleDataSectorExists() throws IOException {
        Block block = mock(Block.class);
        when(block.getSize()).thenReturn(56L);
        when(block.getRelativeOffs()).thenReturn(11208273328L);

        Zip64.EndCentralDirectory endCentralDirectory = createEndCentralDirectory(true);

        String[] lines = Zip4jvmSuite.execute(new EndCentralDirectoryView(endCentralDirectory, block, 2, 52, 0));

        assertThat(lines).hasSize(25);
        assertThat(lines[0]).isEqualTo("(PK0606) ZIP64 End of Central directory record");
        assertThat(lines[1]).isEqualTo("==============================================");
        assertThat(lines[2]).isEqualTo("  - location:                                       11208273328 (0x29C10ADB0) bytes");
        assertThat(lines[3]).isEqualTo("  - size:                                           56 bytes");
        assertThat(lines[4]).isEqualTo("  number of bytes in rest of record:                345 bytes");
        assertThat(lines[5]).isEqualTo("  version made by operating system (00):            MS-DOS, OS/2, NT FAT");
        assertThat(lines[6]).isEqualTo("  version made by zip software (18):                1.8");
        assertThat(lines[7]).isEqualTo("  operat. system version needed to extract (01):    Amiga");
        assertThat(lines[8]).isEqualTo("  unzip software version needed to extract (52):    5.2");
        assertThat(lines[9]).isEqualTo("  part number of this part (0001):                  2");
        assertThat(lines[10]).isEqualTo("  part number of start of central dir (0002):       3");
        assertThat(lines[11]).isEqualTo("  number of entries in central dir in this part:    13");
        assertThat(lines[12]).isEqualTo("  total number of entries in central dir:           15");
        assertThat(lines[13]).isEqualTo("  size of central dir:                              115 (0x00000073) bytes");
        assertThat(lines[14]).isEqualTo("  relative offset of central dir:                   11208273213 (0x29C10AD3D) bytes");
        assertThat(lines[15]).isEqualTo("  extensible data sector:                           56 bytes");
        assertThat(lines[16]).isEqualTo("    compression method (99):                        AES encryption");
        assertThat(lines[17]).isEqualTo("    compressed size:                                438 bytes");
        assertThat(lines[18]).isEqualTo("    uncompressed size:                              120 bytes");
        assertThat(lines[19]).isEqualTo("    encryption algorithm (0x6610):                  AES");
        assertThat(lines[20]).isEqualTo("    encryption key bits:                            256");
        assertThat(lines[21]).isEqualTo("    flags (0x01):                                   password");
        assertThat(lines[22]).isEqualTo("    hash algorithm (0x8004):                        SHA1");
        assertThat(lines[23]).isEqualTo("    hashData:                                       4 bytes");
        assertThat(lines[24]).isEqualTo("      0A 0B 0C 0D");
    }

    private static Zip64.EndCentralDirectory createEndCentralDirectory(boolean extensibleDataSector) {
        Zip64.EndCentralDirectory endCentralDirectory = new Zip64.EndCentralDirectory();
        endCentralDirectory.setEndCentralDirectorySize(345);
        endCentralDirectory.setVersionMadeBy(Version.of(0x12));
        endCentralDirectory.setVersionToExtract(Version.of(0x134));
        endCentralDirectory.setDiskNo(1);
        endCentralDirectory.setMainDiskNo(2);
        endCentralDirectory.setDiskEntries(13);
        endCentralDirectory.setTotalEntries(15);
        endCentralDirectory.setCentralDirectorySize(115);
        endCentralDirectory.setCentralDirectoryRelativeOffs(11208273213L);

        if (extensibleDataSector) {
            Zip64.ExtensibleDataSector.Builder builder = Zip64.ExtensibleDataSector.builder();

            builder.compressionMethod(CompressionMethod.AES)
                   .compressedSize(438)
                   .uncompressedSize(120)
                   .encryptionAlgorithm(EncryptionAlgorithm.AES_256.getCode())
                   .bitLength(AesStrength.S256.getSize())
                   .flags(Flags.PASSWORD_KEY)
                   .hashAlgorithm(HashAlgorithm.SHA1.getCode())
                   .hashLength(4)
                   .hashData(new byte[] { 0xA, 0xB, 0xC, 0xD });

            throw new RuntimeException();

//            endCentralDirectory.setExtensibleDataSector(builder.build());
//            private Zip64.ExtensibleDataSector extensibleDataSector = Zip64.ExtensibleDataSector.NULL;
        }

        return endCentralDirectory;
    }
}
