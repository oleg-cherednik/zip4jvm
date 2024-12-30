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

import ru.olegcherednik.zip4jvm.io.out.DataOutput;
import ru.olegcherednik.zip4jvm.model.extrafield.PkwareExtraField;

import lombok.Builder;
import lombok.Getter;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.NotImplementedException;

import java.io.IOException;
import java.util.List;

/**
 * see 4.5.5
 *
 * @author Oleg Cherednik
 * @since 16.10.2019
 */
@Getter
@Builder
public final class NtfsTimestampExtraFieldRecord implements PkwareExtraField.Record {

    public static final NtfsTimestampExtraFieldRecord NULL = builder().build();

    public static final int SIGNATURE = 0x000A;
    public static final int SIZE_FIELD = 2 + 2; // 4 bytes: signature + size

    // size:2 - tag for this "extra" block type (0x000A)
    // size:2 - size of total "extra" block
    private final int dataSize;
    // size:4 - reserved for future use
    private final List<Tag> tags;

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
        return "NTFS Timestamp";
    }

    // ---------- Writer ----------

    @Override
    public void write(DataOutput out) throws IOException {
        throw new NotImplementedException();
    }

    public interface Tag {

        int getSignature();

        int getSize();
    }

    @Getter
    @Builder
    public static final class OneTag implements Tag {

        public static final int SIGNATURE = 0x0001;
        public static final int SIZE = 8 + 8 + 8;

        // size:2 - attribute tag value #1 (0x0001)
        // size:2 - size of attribute #i (24)
        // size:8 - file last modification time
        private final long lastModificationTime;
        // size:8 - file last access time
        private final long lastAccessTime;
        // size:8 - file creation time
        private final long creationTime;

        @Override
        public int getSignature() {
            return SIGNATURE;
        }

        @Override
        public int getSize() {
            return SIZE;
        }
    }

    @Getter
    @Builder
    public static final class UnknownTag implements Tag {

        private final int signature;
        private final byte[] data;

        @Override
        public int getSize() {
            return ArrayUtils.getLength(data);
        }
    }

}
