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

import ru.olegcherednik.zip4jvm.utils.function.InputStreamSupplier;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * @author Oleg Cherednik
 * @since 10.11.2025
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ByteArrayUtils {

    public static InputStreamSupplier newInputStreamSupplier(byte[] buf) {
        return new InputStreamSupplier() {
            @Override
            public long getSize() {
                return ArrayUtils.getLength(buf);
            }

            @Override
            public InputStream get() {
                byte[] notNullBuf = ArrayUtils.isEmpty(buf) ? ArrayUtils.EMPTY_BYTE_ARRAY : buf;
                return new ByteArrayInputStream(notNullBuf);
            }
        };
    }

}
