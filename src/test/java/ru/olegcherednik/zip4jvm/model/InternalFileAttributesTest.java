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

import org.testng.annotations.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Oleg Cherednik
 * @since 25.09.2019
 */
@Test
// TODO commented test
public class InternalFileAttributesTest {

//    public void shouldRetrieveNullWhenDataEmpty() {
//        assertThat(InternalFileAttributes.build(null)).isSameAs(InternalFileAttributes.NULL);
//        assertThat(InternalFileAttributes.build(new byte[] { 0xA })).isSameAs(InternalFileAttributes.NULL);
//        assertThat(InternalFileAttributes.build(new byte[] { 0xA, 0xA, 0xA })).isSameAs(InternalFileAttributes.NULL);
//        assertThat(InternalFileAttributes.build(new byte[] { 0x0, 0x0 })).isSameAs(InternalFileAttributes.NULL);
//    }

    public void shouldRetrieveNotNullWhenDataNotEmpty() {
        for (byte[] data : Arrays.asList(new byte[] { 0x0, 0xA }, new byte[] { 0xA, 0x0 }, new byte[] { 0xA, 0xA })) {
            InternalFileAttributes attributes = InternalFileAttributes.build(data);
            assertThat(attributes).isNotNull();
// TODO temporary
//            assertThat(attributes).isNotSameAs(InternalFileAttributes.NULL);
            assertThat(attributes.toString()).isEqualTo("internal");
        }
    }

// TODO temporary
//    public void shouldRetrieveNullWhenToStringForNullObject() {
//        assertThat(InternalFileAttributes.NULL.toString()).isEqualTo("<null>");
//    }

}
