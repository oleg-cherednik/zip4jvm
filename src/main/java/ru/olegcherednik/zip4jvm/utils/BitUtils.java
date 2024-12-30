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

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * @author Oleg Cherednik
 * @since 06.03.2019
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class BitUtils {

    public static final int BIT0 = 0b00000001;
    public static final int BIT1 = 0b00000010;
    public static final int BIT2 = 0b00000100;
    public static final int BIT3 = 0b00001000;
    public static final int BIT4 = 0b00010000;
    public static final int BIT5 = 0b00100000;
    public static final int BIT6 = 0b01000000;
    public static final int BIT7 = 0b10000000;

    public static final int BIT8 = BIT0 << 8;
    public static final int BIT9 = BIT1 << 8;
    public static final int BIT10 = BIT2 << 8;
    public static final int BIT11 = BIT3 << 8;
    public static final int BIT12 = BIT4 << 8;
    public static final int BIT13 = BIT5 << 8;
    public static final int BIT14 = BIT6 << 8;
    public static final int BIT15 = BIT7 << 8;

    /**
     * Checks if all bits of giving bit set are set or not
     *
     * @param val  checked val
     * @param bits checked bit or bit set
     * @return {@literal true} if all selected bit(s) are set
     */
    public static boolean isBitSet(int val, int bits) {
        return (val & bits) == bits;
    }

    /**
     * Checks if all bits of giving bit set are clear or not
     *
     * @param val  checked value
     * @param bits checked bit or bit set
     * @return <code>true</code> if all selected bit(s) are clear
     */
    public static boolean isBitClear(int val, int bits) {
        return (val & bits) == 0;
    }

    /**
     * Set selected bit(s) in giving val
     *
     * @param val  val
     * @param bits bit or bit set to set in the val
     * @return {@literal val} with set selected bits
     */
    public static int setBits(int val, int bits) {
        return val | bits;
    }

    /**
     * Clear selected bit(s) in giving val
     *
     * @param val  val
     * @param bits bit or bit set to clear in the val
     * @return {@literal val} with cleared selected bits
     */
    public static int clearBits(int val, int bits) {
        return val & ~bits;
    }

    public static int updateBits(int val, int bits, boolean value) {
        return value ? setBits(val, bits) : clearBits(val, bits);
    }

    public static byte updateBits(byte val, int bits, boolean value) {
        return (byte) updateBits((int) val, bits, value);
    }

}
