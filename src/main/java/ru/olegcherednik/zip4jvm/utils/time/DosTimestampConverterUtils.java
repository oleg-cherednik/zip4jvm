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
package ru.olegcherednik.zip4jvm.utils.time;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @author Oleg Cherednik
 * @since 17.10.2019
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DosTimestampConverterUtils {

    private static final int YEAR_1980 = 1980;
    private static final int DOS_TIME_BEFORE_1980 = (1 << 21) | (1 << 16);

    /* @see {@link java.util.zip.ZipUtils#dosToJavaTime(long)} */
    @SuppressWarnings({ "deprecation", "MagicConstant" })
    public static long dosToJavaTime(int dtime) {
        return new Date(((dtime >> 25) & 0x7F) + 80,
                        ((dtime >> 21) & 0x0F) - 1,
                        (dtime >> 16) & 0x1F,
                        (dtime >> 11) & 0x1F,
                        (dtime >> 5) & 0x3F,
                        (dtime << 1) & 0x3E).getTime();
    }

    /* @see {@link java.util.zip.ZipUtils#javaToDosTime(long)} */
    @SuppressWarnings("deprecation")
    public static int javaToDosTime(long time) {
        Date date = new Date(time);
        int year = date.getYear() + 1900;

        if (year < YEAR_1980)
            return DOS_TIME_BEFORE_1980;

        return (year - 1980) << 25 | (date.getMonth() + 1) << 21 | date.getDate() << 16 | date.getHours() << 11
                | date.getMinutes() << 5 | date.getSeconds() >> 1;
    }

}
