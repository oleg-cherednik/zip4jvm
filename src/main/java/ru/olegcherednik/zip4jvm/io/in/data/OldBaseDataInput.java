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
package ru.olegcherednik.zip4jvm.io.in.data;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.math.BigInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author Oleg Cherednik
 * @since 20.12.2022
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class OldBaseDataInput extends MarkerDataInput {

    @Override
    public String readNumber(int bytes, int radix) throws IOException {
        if (bytes <= 0)
            return null;

        byte[] buf = readBytes(bytes);

        String hexStr = IntStream.rangeClosed(1, bytes)
                                 .map(i -> buf[buf.length - i] & 0xFF)
                                 .mapToObj(Integer::toHexString)
                                 .collect(Collectors.joining());

        return String.valueOf(new BigInteger(hexStr, radix));
    }

}
