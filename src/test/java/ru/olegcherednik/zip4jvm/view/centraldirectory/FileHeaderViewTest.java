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

import org.testng.annotations.Test;
import ru.olegcherednik.zip4jvm.Zip4jvmSuite;
import ru.olegcherednik.zip4jvm.crypto.aes.AesStrength;
import ru.olegcherednik.zip4jvm.model.extrafield.AesExtraFieldRecord;
import ru.olegcherednik.zip4jvm.model.CentralDirectory;
import ru.olegcherednik.zip4jvm.model.Charsets;
import ru.olegcherednik.zip4jvm.model.CompressionMethod;
import ru.olegcherednik.zip4jvm.model.ExternalFileAttributes;
import ru.olegcherednik.zip4jvm.model.ExtraField;
import ru.olegcherednik.zip4jvm.model.GeneralPurposeFlag;
import ru.olegcherednik.zip4jvm.model.Version;
import ru.olegcherednik.zip4jvm.model.block.Block;
import ru.olegcherednik.zip4jvm.model.block.CentralDirectoryBlock;
import ru.olegcherednik.zip4jvm.model.block.ExtraFieldBlock;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Oleg Cherednik
 * @since 15.12.2019
 */
@Test
public class FileHeaderViewTest {

    public void shouldRetrieveAllLinesWhenFileHeader() throws IOException {
        CentralDirectoryBlock.FileHeaderBlock block = mock(CentralDirectoryBlock.FileHeaderBlock.class);
        when(block.getSize()).thenReturn(81L);
        when(block.getRelativeOffs()).thenReturn(255533L);

        String[] lines = Zip4jvmSuite.execute(FileHeaderView.builder()
                                                            .fileHeader(createFileHeader(false, null))
                                                            .block(block)
                                                            .pos(0)
                                                            .charset(Charsets.UTF_8)
                                                            .position(4, 52, 0).build());

        assertThat(lines).hasSize(30);
        assertThat(lines[0]).isEqualTo("#1 (PK0102) [UTF-8] ducati-panigale-1199.jpg");
        assertThat(lines[1]).isEqualTo("--------------------------------------------");
        assertThat(lines[2]).isEqualTo("    - location:                                     255533 (0x0003E62D) bytes");
        assertThat(lines[3]).isEqualTo("    - size:                                         81 bytes");
        assertThat(lines[4]).isEqualTo("    part number of this part (0001):                2");
        assertThat(lines[5]).isEqualTo("    relative offset of local header:                293899 (0x00047C0B) bytes");
        assertThat(lines[6]).isEqualTo("    version made by operating system (00):          MS-DOS, OS/2, NT FAT");
        assertThat(lines[7]).isEqualTo("    version made by zip software (18):              1.8");
        assertThat(lines[8]).isEqualTo("    operat. system version needed to extract (00):  MS-DOS, OS/2, NT FAT");
        assertThat(lines[9]).isEqualTo("    unzip software version needed to extract (69):  6.9");
        assertThat(lines[10]).isEqualTo("    general purpose bit flag (0x0809) (bit 15..0):  0000.1000 0000.1001");
        assertThat(lines[11]).isEqualTo("      file security status  (bit 0):                encrypted");
        assertThat(lines[12]).isEqualTo("      data descriptor       (bit 3):                yes");
        assertThat(lines[13]).isEqualTo("      strong encryption     (bit 6):                no");
        assertThat(lines[14]).isEqualTo("      UTF-8 names          (bit 11):                yes");
        assertThat(lines[15]).isEqualTo("    compression method (99):                        AES encryption");
        assertThat(lines[16]).isEqualTo("    file last modified on (0x4F3C 0x5AE0):          2019-09-28 11:23:00");
        assertThat(lines[17]).isEqualTo("    32-bit CRC value:                               0x0002539F");
        assertThat(lines[18]).isEqualTo("    compressed size:                                255452 bytes");
        assertThat(lines[19]).isEqualTo("    uncompressed size:                              293823 bytes");
        assertThat(lines[20]).isEqualTo("    length of filename:                             24");
        assertThat(lines[21]).isEqualTo("                                                    UTF-8");
        assertThat(lines[22]).isEqualTo("    64 75 63 61 74 69 2D 70 61 6E 69 67 61 6C 65 2D ducati-panigale-");
        assertThat(lines[23]).isEqualTo("    31 31 39 39 2E 6A 70 67                         1199.jpg");
        assertThat(lines[24]).isEqualTo("    length of file comment:                         0 bytes");
        assertThat(lines[25]).isEqualTo("    internal file attributes:                       0x0000");
        assertThat(lines[26]).isEqualTo("      apparent file type:                           binary");
        assertThat(lines[27]).isEqualTo("    external file attributes:                       0x00000020");
        assertThat(lines[28]).isEqualTo("      WINDOWS   (0x20):                             arc");
        assertThat(lines[29]).isEqualTo("      POSIX (0x000000):                             ?---------");
    }

    public void shouldRetrieveExtraFieldLocationAndSizeWhenFileHeaderWithExtraField() throws IOException {
        CentralDirectoryBlock.FileHeaderBlock block = mock(CentralDirectoryBlock.FileHeaderBlock.class);
        ExtraFieldBlock extraFieldBlock = mock(ExtraFieldBlock.class);
        Block recordBlock = mock(Block.class);

        when(block.getSize()).thenReturn(81L);
        when(block.getRelativeOffs()).thenReturn(255533L);
        when(block.getExtraFieldBlock()).thenReturn(extraFieldBlock);

        when(extraFieldBlock.getSize()).thenReturn(11L);
        when(extraFieldBlock.getRelativeOffs()).thenReturn(255603L);
        when(extraFieldBlock.getRecord(eq(AesExtraFieldRecord.SIGNATURE))).thenReturn(recordBlock);

        when(recordBlock.getSize()).thenReturn(11L);
        when(recordBlock.getRelativeOffs()).thenReturn(255603L);
        when(recordBlock.getRelativeOffs()).thenReturn(255603L);

        String[] lines = Zip4jvmSuite.execute(FileHeaderView.builder()
                                                            .fileHeader(createFileHeader(true, null))
                                                            .block(block)
                                                            .pos(0)
                                                            .charset(Charsets.UTF_8)
                                                            .position(4, 52, 0).build());

        assertThat(lines).hasSize(32);
        assertThat(lines[0]).isEqualTo("#1 (PK0102) [UTF-8] ducati-panigale-1199.jpg");
        assertThat(lines[1]).isEqualTo("--------------------------------------------");
        assertThat(lines[2]).isEqualTo("    - location:                                     255533 (0x0003E62D) bytes");
        assertThat(lines[3]).isEqualTo("    - size:                                         81 bytes");
        assertThat(lines[4]).isEqualTo("    part number of this part (0001):                2");
        assertThat(lines[5]).isEqualTo("    relative offset of local header:                293899 (0x00047C0B) bytes");
        assertThat(lines[6]).isEqualTo("    version made by operating system (00):          MS-DOS, OS/2, NT FAT");
        assertThat(lines[7]).isEqualTo("    version made by zip software (18):              1.8");
        assertThat(lines[8]).isEqualTo("    operat. system version needed to extract (00):  MS-DOS, OS/2, NT FAT");
        assertThat(lines[9]).isEqualTo("    unzip software version needed to extract (69):  6.9");
        assertThat(lines[10]).isEqualTo("    general purpose bit flag (0x0809) (bit 15..0):  0000.1000 0000.1001");
        assertThat(lines[11]).isEqualTo("      file security status  (bit 0):                encrypted");
        assertThat(lines[12]).isEqualTo("      data descriptor       (bit 3):                yes");
        assertThat(lines[13]).isEqualTo("      strong encryption     (bit 6):                no");
        assertThat(lines[14]).isEqualTo("      UTF-8 names          (bit 11):                yes");
        assertThat(lines[15]).isEqualTo("    compression method (99):                        AES encryption");
        assertThat(lines[16]).isEqualTo("    file last modified on (0x4F3C 0x5AE0):          2019-09-28 11:23:00");
        assertThat(lines[17]).isEqualTo("    32-bit CRC value:                               0x0002539F");
        assertThat(lines[18]).isEqualTo("    compressed size:                                255452 bytes");
        assertThat(lines[19]).isEqualTo("    uncompressed size:                              293823 bytes");
        assertThat(lines[20]).isEqualTo("    length of filename:                             24");
        assertThat(lines[21]).isEqualTo("                                                    UTF-8");
        assertThat(lines[22]).isEqualTo("    64 75 63 61 74 69 2D 70 61 6E 69 67 61 6C 65 2D ducati-panigale-");
        assertThat(lines[23]).isEqualTo("    31 31 39 39 2E 6A 70 67                         1199.jpg");
        assertThat(lines[24]).isEqualTo("    length of file comment:                         0 bytes");
        assertThat(lines[25]).isEqualTo("    internal file attributes:                       0x0000");
        assertThat(lines[26]).isEqualTo("      apparent file type:                           binary");
        assertThat(lines[27]).isEqualTo("    external file attributes:                       0x00000020");
        assertThat(lines[28]).isEqualTo("      WINDOWS   (0x20):                             arc");
        assertThat(lines[29]).isEqualTo("      POSIX (0x000000):                             ?---------");
        assertThat(lines[30]).isEqualTo("    extra field:                                    255603 (0x0003E673) bytes");
        assertThat(lines[31]).isEqualTo("      - size:                                       11 bytes (1 record)");
    }

    public void shouldRetrieveCommentWhenFileHeaderWithComment() throws IOException {
        CentralDirectoryBlock.FileHeaderBlock block = mock(CentralDirectoryBlock.FileHeaderBlock.class);
        when(block.getSize()).thenReturn(81L);
        when(block.getRelativeOffs()).thenReturn(255533L);

        String[] lines = Zip4jvmSuite.execute(FileHeaderView.builder()
                                                            .fileHeader(createFileHeader(false, "This is comment"))
                                                            .block(block)
                                                            .pos(0)
                                                            .charset(Charsets.UTF_8)
                                                            .position(4, 52, 0).build());

        assertThat(lines).hasSize(32);
        assertThat(lines[0]).isEqualTo("#1 (PK0102) [UTF-8] ducati-panigale-1199.jpg");
        assertThat(lines[1]).isEqualTo("--------------------------------------------");
        assertThat(lines[2]).isEqualTo("    - location:                                     255533 (0x0003E62D) bytes");
        assertThat(lines[3]).isEqualTo("    - size:                                         81 bytes");
        assertThat(lines[4]).isEqualTo("    part number of this part (0001):                2");
        assertThat(lines[5]).isEqualTo("    relative offset of local header:                293899 (0x00047C0B) bytes");
        assertThat(lines[6]).isEqualTo("    version made by operating system (00):          MS-DOS, OS/2, NT FAT");
        assertThat(lines[7]).isEqualTo("    version made by zip software (18):              1.8");
        assertThat(lines[8]).isEqualTo("    operat. system version needed to extract (00):  MS-DOS, OS/2, NT FAT");
        assertThat(lines[9]).isEqualTo("    unzip software version needed to extract (69):  6.9");
        assertThat(lines[10]).isEqualTo("    general purpose bit flag (0x0809) (bit 15..0):  0000.1000 0000.1001");
        assertThat(lines[11]).isEqualTo("      file security status  (bit 0):                encrypted");
        assertThat(lines[12]).isEqualTo("      data descriptor       (bit 3):                yes");
        assertThat(lines[13]).isEqualTo("      strong encryption     (bit 6):                no");
        assertThat(lines[14]).isEqualTo("      UTF-8 names          (bit 11):                yes");
        assertThat(lines[15]).isEqualTo("    compression method (99):                        AES encryption");
        assertThat(lines[16]).isEqualTo("    file last modified on (0x4F3C 0x5AE0):          2019-09-28 11:23:00");
        assertThat(lines[17]).isEqualTo("    32-bit CRC value:                               0x0002539F");
        assertThat(lines[18]).isEqualTo("    compressed size:                                255452 bytes");
        assertThat(lines[19]).isEqualTo("    uncompressed size:                              293823 bytes");
        assertThat(lines[20]).isEqualTo("    length of filename:                             24");
        assertThat(lines[21]).isEqualTo("                                                    UTF-8");
        assertThat(lines[22]).isEqualTo("    64 75 63 61 74 69 2D 70 61 6E 69 67 61 6C 65 2D ducati-panigale-");
        assertThat(lines[23]).isEqualTo("    31 31 39 39 2E 6A 70 67                         1199.jpg");
        assertThat(lines[24]).isEqualTo("    length of file comment:                         15 bytes");
        assertThat(lines[25]).isEqualTo("                                                    UTF-8");
        assertThat(lines[26]).isEqualTo("    54 68 69 73 20 69 73 20 63 6F 6D 6D 65 6E 74    This is comment");
        assertThat(lines[27]).isEqualTo("    internal file attributes:                       0x0000");
        assertThat(lines[28]).isEqualTo("      apparent file type:                           binary");
        assertThat(lines[29]).isEqualTo("    external file attributes:                       0x00000020");
        assertThat(lines[30]).isEqualTo("      WINDOWS   (0x20):                             arc");
        assertThat(lines[31]).isEqualTo("      POSIX (0x000000):                             ?---------");
    }

    private static CentralDirectory.FileHeader createFileHeader(boolean extraField, String comment) {
        ExternalFileAttributes externalFileAttributes = mock(ExternalFileAttributes.class);
        when(externalFileAttributes.getData()).thenReturn(new byte[] { 0x20, 0x0, 0x0, 0x0 });

        CentralDirectory.FileHeader fileHeader = new CentralDirectory.FileHeader();

        fileHeader.setVersionMadeBy(Version.of(0x12));
        fileHeader.setVersionToExtract(Version.of(0x45));
        fileHeader.setGeneralPurposeFlag(new GeneralPurposeFlag(0x0809));
        fileHeader.setCompressionMethod(CompressionMethod.AES);
        fileHeader.setLastModifiedTime(1329355488);
        fileHeader.setCrc32(152479);
        fileHeader.setCompressedSize(255452);
        fileHeader.setUncompressedSize(293823);
        fileHeader.setCommentLength(comment == null ? 0 : comment.length());
        fileHeader.setDiskNo(1);
        fileHeader.setExternalFileAttributes(externalFileAttributes);
        fileHeader.setLocalFileHeaderRelativeOffs(293899);
        fileHeader.setFileName("ducati-panigale-1199.jpg");

        if (extraField) {
            fileHeader.setExtraField(ExtraField.builder()
                                               .addRecord(AesExtraFieldRecord.builder()
                                                                             .dataSize(1)
                                                                             .versionNumber(2)
                                                                             .vendor("AE")
                                                                             .strength(AesStrength.AES_256)
                                                                             .compressionMethod(CompressionMethod.DEFLATE).build())
                                               .build());
        }

        fileHeader.setComment(comment);

        return fileHeader;
    }

}
