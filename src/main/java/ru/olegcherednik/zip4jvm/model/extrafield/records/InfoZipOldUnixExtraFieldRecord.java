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
import org.apache.commons.lang3.NotImplementedException;

import java.io.IOException;

import static ru.olegcherednik.zip4jvm.model.extrafield.PkwareExtraField.NO_DATA;

/**
 * Added under MacOS
 *
 * @author Oleg Cherednik
 * @since 25.10.2019
 */
@Getter
@Builder
public final class InfoZipOldUnixExtraFieldRecord implements PkwareExtraField.Record {

    public static final InfoZipOldUnixExtraFieldRecord NULL = builder().build();

    public static final int SIGNATURE = 0x5855;
    public static final int SIZE_FIELD = 2 + 2; // 4 bytes: signature + size

    // size:2 - attribute tag value #1 (0x5855)
    // size:2 - total data size for this block
    private final int dataSize;
    // size:4 - file last access time
    private final long lastAccessTime;
    // size:4 - file last modification time
    private final long lastModificationTime;
    // size:2 - unix user ID (optional, LocalFileHeader only)
    @Builder.Default
    @SuppressWarnings("FieldMayBeStatic")
    private final int uid = NO_DATA;
    // size:2 - unix group ID (optional, LocalFileHeader only)
    @Builder.Default
    @SuppressWarnings("FieldMayBeStatic")
    private final int gid = NO_DATA;

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
        return "old InfoZIP Unix/OS2/NT";
    }

    // ---------- Writer ----------

    @Override
    public void write(DataOutput out) throws IOException {
        throw new NotImplementedException();
    }

}
