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
package ru.olegcherednik.zip4jvm.io.in.file;

import ru.olegcherednik.zip4jvm.io.Endianness;

/**
 * @author Oleg Cherednik
 * @since 28.09.2024
 */
public final class LittleEndian implements Endianness {

    public static final LittleEndian INSTANCE = new LittleEndian();

    @Override
    public long getLong(byte[] buf, int offs, int len) {
        long res = 0;

        for (int i = offs + len - 1; i >= offs; i--)
            res = res << 8 | buf[i] & 0xFF;

        return res;
    }

}
