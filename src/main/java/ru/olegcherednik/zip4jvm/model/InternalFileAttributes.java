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

import ru.olegcherednik.zip4jvm.utils.BitUtils;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.ArrayUtils;

import static ru.olegcherednik.zip4jvm.utils.BitUtils.BIT0;

/**
 * @author Oleg Cherednik
 * @since 16.08.2019
 */
@NoArgsConstructor
public final class InternalFileAttributes {

    public static final int SIZE = 2;

    @Getter
    private ApparentFileType apparentFileType = ApparentFileType.BINARY;

    private final byte[] data = new byte[SIZE];

    public InternalFileAttributes(byte[] data) {
        readFrom(data);
    }

    public InternalFileAttributes readFrom(InternalFileAttributes internalFileAttributes) {
        return readFrom(internalFileAttributes.data);
    }

    private InternalFileAttributes readFrom(byte[] data) {
        System.arraycopy(data, 0, this.data, 0, SIZE);
        apparentFileType = BitUtils.isBitSet(data[0], BIT0) ? ApparentFileType.TEXT : ApparentFileType.BINARY;
        return this;
    }

    public byte[] getData() {
        byte[] buf = ArrayUtils.clone(data);
        buf[0] = BitUtils.updateBits((byte) 0x0, BIT0, apparentFileType == ApparentFileType.TEXT);
        return buf;
    }

    @Override
    public String toString() {
        return "internal";
    }

}
