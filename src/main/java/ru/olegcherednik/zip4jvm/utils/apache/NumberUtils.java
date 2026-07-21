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
package ru.olegcherednik.zip4jvm.utils.apache;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * @author Oleg Cherednik
 * @since 21.07.2026
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class NumberUtils {

    public static String toBinaryString(final int val, final int size) {
        return StringUtils.leftPad(Integer.toBinaryString(val), size, '0');
    }

    /**
     * Checks whether the {@link String} contains only
     * digit characters.
     *
     * <p>{@code null} and empty String will return
     * {@code false}.</p>
     *
     * @param str the {@link String} to check
     * @return {@code true} if str contains only Unicode numeric
     */
    public static boolean isDigits(final String str) {
        if (StringUtils.isEmpty(str)) {
            return false;
        }
        final int sz = str.length();
        for (int i = 0; i < sz; i++) {
            if (!Character.isDigit(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }

}
