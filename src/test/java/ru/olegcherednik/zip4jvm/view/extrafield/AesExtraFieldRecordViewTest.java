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
package ru.olegcherednik.zip4jvm.view.extrafield;

import ru.olegcherednik.zip4jvm.Zip4jvmSuite;
import ru.olegcherednik.zip4jvm.crypto.aes.AesStrength;
import ru.olegcherednik.zip4jvm.model.AesVersion;
import ru.olegcherednik.zip4jvm.model.CompressionMethod;
import ru.olegcherednik.zip4jvm.model.GeneralPurposeFlag;
import ru.olegcherednik.zip4jvm.model.block.Block;
import ru.olegcherednik.zip4jvm.model.extrafield.records.AesExtraFieldRecord;

import org.testng.annotations.Test;

import java.io.IOException;
import java.io.PrintStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Oleg Cherednik
 * @since 09.11.2019
 */
@Test
public class AesExtraFieldRecordViewTest {

    public void shouldRetrieveMultipleLinesWhenViewAesRecord() throws IOException {
        Block block = mock(Block.class);
        when(block.getSize()).thenReturn(11L);
        when(block.getRelativeOffs()).thenReturn(255603L);

        AesExtraFieldRecord record = AesExtraFieldRecord.builder()
                                                        .dataSize(7)
                                                        .version(AesVersion.AE_2)
                                                        .vendor(AesExtraFieldRecord.VENDOR_AE)
                                                        .strength(AesStrength.S256)
                                                        .compressionMethod(CompressionMethod.DEFLATE).build();

        String[] lines = Zip4jvmSuite.execute(AesExtraFieldRecordView.builder()
                                                                     .record(record)
                                                                     .generalPurposeFlag(new GeneralPurposeFlag(2057))
                                                                     .block(block)
                                                                     .position(0, 52, 0).build());
        assertThat(lines).hasSize(6);
        assertThat(lines[0]).isEqualTo("(0x9901) AES Encryption Tag:                        255603 (0x0003E673) bytes");
        assertThat(lines[1]).isEqualTo("  - size:                                           11 bytes");
        assertThat(lines[2]).isEqualTo("  Encryption Tag Version:                           AE-2");
        assertThat(lines[3]).isEqualTo("  Encryption Key Bits:                              256");
        assertThat(lines[4]).isEqualTo("  compression method (08):                          deflate");
        assertThat(lines[5]).isEqualTo("    compression sub-type (deflation):               normal");
    }

    public void shouldRetrieveEmptyStringWhenRecordNull() throws IOException {
        try (PrintStream out = mock(PrintStream.class)) {
            AesExtraFieldRecordView view = AesExtraFieldRecordView.builder()
                                                                  .record(AesExtraFieldRecord.NULL)
                                                                  .generalPurposeFlag(mock(GeneralPurposeFlag.class))
                                                                  .block(mock(Block.class))
                                                                  .position(0, 52, 0).build();
            assertThat(view.printTextInfo(out)).isFalse();
        }
    }

    public void shouldRetrieveMultipleLinesWithDiskWhenSplit() throws IOException {
        Block block = mock(Block.class);
        when(block.getSize()).thenReturn(11L);
        when(block.getRelativeOffs()).thenReturn(255603L);
        when(block.getDiskNo()).thenReturn(5);
        when(block.getFileName()).thenReturn("src.zip");

        AesExtraFieldRecord record = AesExtraFieldRecord.builder()
                                                        .dataSize(7)
                                                        .version(AesVersion.AE_2)
                                                        .vendor(AesExtraFieldRecord.VENDOR_AE)
                                                        .strength(AesStrength.S256)
                                                        .compressionMethod(CompressionMethod.DEFLATE).build();

        String[] lines = Zip4jvmSuite.execute(AesExtraFieldRecordView.builder()
                                                                     .record(record)
                                                                     .generalPurposeFlag(new GeneralPurposeFlag(2057))
                                                                     .block(block)
                                                                     .position(0, 52, 5).build());
        assertThat(lines).hasSize(7);
        assertThat(lines[0]).isEqualTo("(0x9901) AES Encryption Tag:                        255603 (0x0003E673) bytes");
        assertThat(lines[1]).isEqualTo("  - disk (0005):                                    src.zip");
        assertThat(lines[2]).isEqualTo("  - size:                                           11 bytes");
        assertThat(lines[3]).isEqualTo("  Encryption Tag Version:                           AE-2");
        assertThat(lines[4]).isEqualTo("  Encryption Key Bits:                              256");
        assertThat(lines[5]).isEqualTo("  compression method (08):                          deflate");
        assertThat(lines[6]).isEqualTo("    compression sub-type (deflation):               normal");
    }

}
