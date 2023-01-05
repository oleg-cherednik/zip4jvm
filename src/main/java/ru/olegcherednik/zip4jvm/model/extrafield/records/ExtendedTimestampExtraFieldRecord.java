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
package ru.olegcherednik.zip4jvm.model.extrafield.records;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;
import ru.olegcherednik.zip4jvm.io.out.data.DataOutput;
import ru.olegcherednik.zip4jvm.model.extrafield.PkwareExtraField;
import ru.olegcherednik.zip4jvm.utils.BitUtils;

import java.io.IOException;
import java.util.function.IntSupplier;

import static ru.olegcherednik.zip4jvm.utils.BitUtils.BIT0;
import static ru.olegcherednik.zip4jvm.utils.BitUtils.BIT1;
import static ru.olegcherednik.zip4jvm.utils.BitUtils.BIT2;

/**
 * Added under Ubuntu
 *
 * @author Oleg Cherednik
 * @since 25.10.2019
 */
@Getter
@Builder
public final class ExtendedTimestampExtraFieldRecord implements PkwareExtraField.Record {

    public static final ExtendedTimestampExtraFieldRecord NULL = builder().build();

    public static final int SIGNATURE = 0x5455;
    public static final int SIZE_FIELD = 2 + 2; // 4 bytes: signature + size

    // size:2 - attribute tag value #1 (0x5455)
    // size:2 - total data size for this block
    private final int dataSize;
    // size:1 - bit flag (refers to local header!)
    private final Flag flag;
    // size:4 - file last modification time (must present in central header if present in local header)
    private final long lastModificationTime;
    // size:4 - file last access time (only in local header)
    private final long lastAccessTime;
    // size:4 - file creation time (only in local header)
    private final long creationTime;

    @Override
    public int getSignature() {
        return SIGNATURE;
    }

    @Override
    public int getBlockSize() {
        return this == NULL ? 0 : dataSize + SIZE_FIELD;
    }

    @Override
    public boolean isNull() {
        return this == NULL;
    }

    @Override
    public String getTitle() {
        return "Universal time";
    }

    @Override
    public void write(DataOutput out) throws IOException {
        throw new NotImplementedException();
    }

    @Override
    public String toString() {
        return isNull() ? "<null>" : StringUtils.leftPad(Integer.toBinaryString(flag.getAsInt()), 3, '0');
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class Flag implements IntSupplier {

        private boolean lastModificationTime;
        private boolean lastAccessTime;
        private boolean creationTime;

        public Flag(int data) {
            read(data);
        }

        public void read(int data) {
            lastModificationTime = BitUtils.isBitSet(data, BIT0);
            lastAccessTime = BitUtils.isBitSet(data, BIT1);
            creationTime = BitUtils.isBitSet(data, BIT2);
        }

        @Override
        public int getAsInt() {
            int data = BitUtils.updateBits(0, BIT0, lastModificationTime);
            data = BitUtils.updateBits(data, BIT1, lastAccessTime);
            data = BitUtils.updateBits(data, BIT2, creationTime);
            return data;
        }
    }

}
