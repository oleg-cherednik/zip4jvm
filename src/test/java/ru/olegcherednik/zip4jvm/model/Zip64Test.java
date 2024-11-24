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
package ru.olegcherednik.zip4jvm.model;

import ru.olegcherednik.zip4jvm.io.out.DataOutput;

import org.testng.annotations.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * @author Oleg Cherednik
 * @since 01.10.2019
 */
@Test
@SuppressWarnings("NewClassNamingConvention")
public class Zip64Test {

    private static final String NULL = "<null>";

    public void shouldRetrieveNullObjectWhenAllArgumentsAreNull() {
        Zip64.EndCentralDirectoryLocator endCentralDirectoryLocator = new Zip64.EndCentralDirectoryLocator();
        Zip64.EndCentralDirectory endCentralDirectory = new Zip64.EndCentralDirectory();

        assertThat(Zip64.of(null, null, null)).isSameAs(Zip64.NULL);
        assertThat(Zip64.of(endCentralDirectoryLocator, null, null)).isNotSameAs(Zip64.NULL);
        assertThat(Zip64.of(null, endCentralDirectory, null)).isSameAs(Zip64.NULL);
        assertThat(Zip64.of(endCentralDirectoryLocator, endCentralDirectory, null)).isNotSameAs(Zip64.NULL);
    }

    public void shouldRetrieveSpecialStringForNullObjectWhenToString() {
        Zip64 zip64 = Zip64.of(new Zip64.EndCentralDirectoryLocator(),
                               new Zip64.EndCentralDirectory(),
                               Zip64.ExtensibleDataSector.builder().build());
        assertThat(zip64.toString()).isNotEqualTo(NULL);
        assertThat(Zip64.NULL.toString()).isEqualTo(NULL);
    }

    public void shouldRetrieveZeroWhenGetSizeNullObject() {
        assertThat(Zip64.ExtendedInfo.NULL.getDataSize()).isZero();
        assertThat(Zip64.ExtendedInfo.NULL.getBlockSize()).isZero();
    }

    public void shouldRetrieveCorrectStringWhenToString() {
        assertThat(Zip64.ExtendedInfo.NULL.toString()).isEqualTo(NULL);
        assertThat(Zip64.ExtendedInfo.builder().diskNo(1).build().toString()).isNotEqualTo(NULL);
    }

    public void shouldRetrieveNullObjectWhenAllDataInExtendedInfoNoExist() {
        assertThat(Zip64.ExtendedInfo.builder().build()).isSameAs(Zip64.ExtendedInfo.NULL);
    }

    public void shouldIgnoreWriteOutputWhenNullObject() throws IOException {
        try (DataOutput out = mock(DataOutput.class)) {
            Zip64.ExtendedInfo.NULL.write(out);
            verify(out, never()).writeWordSignature(any(int.class));
        }
    }

    public void shouldIgnoreDataWhenNotExists() throws IOException {
        try (DataOutput out = mock(DataOutput.class)) {
            Zip64.ExtendedInfo.builder().uncompressedSize(1).build().write(out);
            verify(out, times(1)).writeQword(eq(1L));
            verify(out, never()).writeDword(any(long.class));
            reset(out);

            Zip64.ExtendedInfo.builder().compressedSize(2).build().write(out);
            verify(out, times(1)).writeQword(eq(2L));
            verify(out, never()).writeDword(any(long.class));
            reset(out);

            Zip64.ExtendedInfo.builder().localFileHeaderRelativeOffs(3).build().write(out);
            verify(out, times(1)).writeQword(eq(3L));
            verify(out, never()).writeDword(any(long.class));
            reset(out);

            Zip64.ExtendedInfo.builder().diskNo(4).build().write(out);
            verify(out, never()).writeQword(any(long.class));
            verify(out, times(1)).writeDword(eq(4L));
            reset(out);
        }
    }
}
