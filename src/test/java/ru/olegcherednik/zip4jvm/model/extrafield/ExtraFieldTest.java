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
package ru.olegcherednik.zip4jvm.model.extrafield;

import org.mockito.InOrder;
import org.testng.annotations.Test;
import ru.olegcherednik.zip4jvm.io.out.data.DataOutput;
import ru.olegcherednik.zip4jvm.model.Zip64;
import ru.olegcherednik.zip4jvm.model.extrafield.ExtraField;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Oleg Cherednik
 * @since 01.10.2019
 */
@Test
public class ExtraFieldTest {

    public void shouldRetrieveNullObjectWhenBuildEmptyExtraField() {
        assertThat(ExtraField.builder().build()).isSameAs(ExtraField.NULL);
    }

    public void shouldIgnoreNullOrNullObjectRecordWhenBuildExtraField() {
        assertThat(ExtraField.builder().addRecord((ExtraField.Record)null).build()).isSameAs(ExtraField.NULL);
        assertThat(ExtraField.builder().build()).isSameAs(ExtraField.NULL);
        assertThat(ExtraField.builder().addRecord(Zip64.ExtendedInfo.NULL).build()).isSameAs(ExtraField.NULL);
    }

    public void shouldRetrieveCorrectStringWhenToString() {
        ExtraField.Record record = mock(ExtraField.Record.class);
        when(record.isNull()).thenReturn(false);
        when(record.getSignature()).thenReturn(666);

        ExtraField extraField = ExtraField.builder().addRecord(record).build();
        assertThat(extraField).isNotSameAs(ExtraField.NULL);

        assertThat(ExtraField.NULL.toString()).isEqualTo("<null>");
        assertThat(extraField.toString()).isEqualTo("total: 1");
    }

    public void shouldRetrievePredefinedBlockSizeWhenUnknownRecord() {
        ExtraField.Record record = new ExtraField.Record.Unknown(777, new byte[] { 0xA, 0xB, 0xC });
        assertThat(record.getBlockSize()).isEqualTo(3);
    }

    public void shouldWritePredefinedDataWhenUnknownRecord() throws IOException {
        DataOutput out = mock(DataOutput.class);
        InOrder order = inOrder(out);

        ExtraField.Record record = new ExtraField.Record.Unknown(777, new byte[] { 0xA, 0xB, 0xC });
        record.write(out);

        order.verify(out).writeWordSignature(eq(777));
        order.verify(out).writeWord(eq(3));
        order.verify(out).write(eq(new byte[] { 0xA, 0xB, 0xC }), eq(0), eq(3));
    }

}
