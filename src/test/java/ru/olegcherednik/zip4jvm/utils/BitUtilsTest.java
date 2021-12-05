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
package ru.olegcherednik.zip4jvm.utils;

import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.olegcherednik.zip4jvm.utils.BitUtils.BIT2;
import static ru.olegcherednik.zip4jvm.utils.BitUtils.BIT3;
import static ru.olegcherednik.zip4jvm.utils.BitUtils.BIT4;
import static ru.olegcherednik.zip4jvm.utils.BitUtils.BIT5;

/**
 * @author Oleg Cherednik
 * @since 01.10.2019
 */
@Test
public class BitUtilsTest {

    public void shouldRetrieveTrueWhenAllRequiredBitsAreSet() {
        assertThat(BitUtils.isBitSet(BIT3 | BIT5, BIT3)).isTrue();
        assertThat(BitUtils.isBitSet(BIT3 | BIT5, BIT3 | BIT5)).isTrue();
        assertThat(BitUtils.isBitSet(BIT3 | BIT5, BIT2)).isFalse();
        assertThat(BitUtils.isBitSet(BIT3 | BIT5, BIT3 | BIT4)).isFalse();
    }

    public void shouldRetrieveTrueWhenAllRequiredBitsAreCleared() {
        assertThat(BitUtils.isBitClear(BIT3 | BIT5, BIT2)).isTrue();
        assertThat(BitUtils.isBitClear(BIT3 | BIT5, BIT2 | BIT4)).isTrue();
        assertThat(BitUtils.isBitClear(BIT3 | BIT5, BIT3)).isFalse();
        assertThat(BitUtils.isBitClear(BIT3 | BIT5, BIT2 | BIT5)).isFalse();
    }

    public void shouldRetrieveValueWithSetBitsWhenRequiredBitsToSet() {
        assertThat(BitUtils.setBits(BIT3, BIT3)).isEqualTo(BIT3);
        assertThat(BitUtils.setBits(BIT3, BIT4)).isEqualTo(BIT3 | BIT4);
    }

    public void shouldRetrieveValueWithClearedBitsWhenRequiredBitsToClear() {
        assertThat(BitUtils.clearBits(BIT3, BIT3)).isZero();
        assertThat(BitUtils.clearBits(BIT3 | BIT4, BIT4)).isEqualTo(BIT3);
    }
}
