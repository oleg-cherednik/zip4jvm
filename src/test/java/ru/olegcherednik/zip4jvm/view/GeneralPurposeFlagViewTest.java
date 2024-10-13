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

import ru.olegcherednik.zip4jvm.Zip4jvmSuite;
import ru.olegcherednik.zip4jvm.model.CompressionLevel;
import ru.olegcherednik.zip4jvm.model.CompressionMethod;
import ru.olegcherednik.zip4jvm.model.GeneralPurposeFlag;

import org.testng.annotations.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Oleg Cherednik
 * @since 06.11.2019
 */
@Test
public class GeneralPurposeFlagViewTest {

    private static final String FSS_BIT_3_NE = "  file security status  (bit 0):                    not encrypted";
    private static final String DESCRIPTOR_BIT_3_NO = "  data descriptor       (bit 3):                    no";
    private static final String STRONG_BIT_6_NO = "  strong encryption     (bit 6):                    no";
    private static final String UTF8_BIT_11_NO = "  UTF-8 names          (bit 11):                    no";

    public void shouldRetrieveViewWithMultipleLinesWhenNotEncrypted() throws IOException {
        String[] lines = Zip4jvmSuite.execute(new GeneralPurposeFlagView(new GeneralPurposeFlag(),
                                                                         CompressionMethod.STORE,
                                                                         0,
                                                                         52));

        assertThat(lines).hasSize(5);
        assertThat(lines[0]).isEqualTo("general purpose bit flag (0x0000) (bit 15..0):      0000.0000 0000.0000");
        assertThat(lines[1]).isEqualTo(FSS_BIT_3_NE);
        assertThat(lines[2]).isEqualTo(DESCRIPTOR_BIT_3_NO);
        assertThat(lines[3]).isEqualTo(STRONG_BIT_6_NO);
        assertThat(lines[4]).isEqualTo(UTF8_BIT_11_NO);
    }

    public void shouldRetrieveViewWithMultipleLinesWhenEncrypted() throws IOException {
        GeneralPurposeFlag generalPurposeFlag = new GeneralPurposeFlag();
        generalPurposeFlag.setEncrypted(true);

        String[] lines = Zip4jvmSuite.execute(new GeneralPurposeFlagView(generalPurposeFlag,
                                                                         CompressionMethod.STORE,
                                                                         0,
                                                                         52));

        assertThat(lines).hasSize(5);
        assertThat(lines[0]).isEqualTo("general purpose bit flag (0x0001) (bit 15..0):      0000.0000 0000.0001");
        assertThat(lines[1]).isEqualTo("  file security status  (bit 0):                    encrypted");
        assertThat(lines[2]).isEqualTo(DESCRIPTOR_BIT_3_NO);
        assertThat(lines[3]).isEqualTo(STRONG_BIT_6_NO);
        assertThat(lines[4]).isEqualTo(UTF8_BIT_11_NO);
    }

    public void shouldRetrieveViewWithMultipleLinesWhenDataDescriptorAvailable() throws IOException {
        GeneralPurposeFlag generalPurposeFlag = new GeneralPurposeFlag();
        generalPurposeFlag.setDataDescriptorAvailable(true);

        String[] lines = Zip4jvmSuite.execute(new GeneralPurposeFlagView(generalPurposeFlag,
                                                                         CompressionMethod.STORE,
                                                                         0,
                                                                         52));

        assertThat(lines).hasSize(5);
        assertThat(lines[0]).isEqualTo("general purpose bit flag (0x0008) (bit 15..0):      0000.0000 0000.1000");
        assertThat(lines[1]).isEqualTo(FSS_BIT_3_NE);
        assertThat(lines[2]).isEqualTo("  data descriptor       (bit 3):                    yes");
        assertThat(lines[3]).isEqualTo(STRONG_BIT_6_NO);
        assertThat(lines[4]).isEqualTo(UTF8_BIT_11_NO);
    }

    public void shouldRetrieveViewWithMultipleLinesWhenStringEncryption() throws IOException {
        GeneralPurposeFlag generalPurposeFlag = new GeneralPurposeFlag();
        generalPurposeFlag.setStrongEncryption(true);

        String[] lines = Zip4jvmSuite.execute(new GeneralPurposeFlagView(generalPurposeFlag,
                                                                         CompressionMethod.STORE,
                                                                         0,
                                                                         52));

        assertThat(lines).hasSize(5);
        assertThat(lines[0]).isEqualTo("general purpose bit flag (0x0040) (bit 15..0):      0000.0000 0100.0000");
        assertThat(lines[1]).isEqualTo(FSS_BIT_3_NE);
        assertThat(lines[2]).isEqualTo(DESCRIPTOR_BIT_3_NO);
        assertThat(lines[3]).isEqualTo("  strong encryption     (bit 6):                    yes");
        assertThat(lines[4]).isEqualTo(UTF8_BIT_11_NO);
    }

    public void shouldRetrieveViewWithMultipleLinesWhenUnicode() throws IOException {
        GeneralPurposeFlag generalPurposeFlag = new GeneralPurposeFlag();
        generalPurposeFlag.setUtf8(true);

        String[] lines = Zip4jvmSuite.execute(new GeneralPurposeFlagView(generalPurposeFlag,
                                                                         CompressionMethod.STORE,
                                                                         0,
                                                                         52));

        assertThat(lines).hasSize(5);
        assertThat(lines[0]).isEqualTo("general purpose bit flag (0x0800) (bit 15..0):      0000.1000 0000.0000");
        assertThat(lines[1]).isEqualTo(FSS_BIT_3_NE);
        assertThat(lines[2]).isEqualTo(DESCRIPTOR_BIT_3_NO);
        assertThat(lines[3]).isEqualTo(STRONG_BIT_6_NO);
        assertThat(lines[4]).isEqualTo("  UTF-8 names          (bit 11):                    yes");
    }

    public void shouldRetrieveViewWithMultipleLinesWhenDeflateSuperFast() throws IOException {
        GeneralPurposeFlag generalPurposeFlag = new GeneralPurposeFlag();
        generalPurposeFlag.setCompressionLevel(CompressionLevel.SUPER_FAST);

        String[] lines = Zip4jvmSuite.execute(new GeneralPurposeFlagView(generalPurposeFlag,
                                                                         CompressionMethod.DEFLATE,
                                                                         0,
                                                                         52));

        assertThat(lines).hasSize(5);
        assertThat(lines[0]).isEqualTo("general purpose bit flag (0x0006) (bit 15..0):      0000.0000 0000.0110");
        assertThat(lines[1]).isEqualTo(FSS_BIT_3_NE);
        assertThat(lines[2]).isEqualTo(DESCRIPTOR_BIT_3_NO);
        assertThat(lines[3]).isEqualTo(STRONG_BIT_6_NO);
        assertThat(lines[4]).isEqualTo(UTF8_BIT_11_NO);
    }
}
