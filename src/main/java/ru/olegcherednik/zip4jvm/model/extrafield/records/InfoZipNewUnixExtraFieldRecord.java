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

import ru.olegcherednik.zip4jvm.io.out.data.DataOutput;
import ru.olegcherednik.zip4jvm.model.extrafield.PkwareExtraField;

import lombok.Builder;
import lombok.Getter;
import org.apache.commons.lang3.NotImplementedException;

import java.io.IOException;

/**
 * Added under Ubuntu
 *
 * @author Oleg Cherednik
 * @since 25.10.2019
 */
@Getter
@Builder
public class InfoZipNewUnixExtraFieldRecord implements PkwareExtraField.Record {

    public static final InfoZipNewUnixExtraFieldRecord NULL = builder().build();

    public static final int SIGNATURE = 0x7875;
    public static final int SIZE_FIELD = 2 + 2; // 4 bytes: signature + size

    // size:2 - attribute tag value #1 (0x5855)
    // size:2 - total data size for this block
    private final int dataSize;
    private final Payload payload;

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
        return "new InfoZIP Unix/OS2/NT";
    }

    // ---------- Writer ----------

    @Override
    public void write(DataOutput out) throws IOException {
        throw new NotImplementedException();
    }

    // ---------- Object ----------

    @Override
    public String toString() {
        return isNull() ? "<null>" : "version: " + payload.getVersion();
    }

    public <T extends Payload> T getPayload() {
        return (T) payload;
    }

    public interface Payload {

        int getVersion();
    }

    @Getter
    @Builder
    public static final class VersionOnePayload implements Payload {

        // size:1 - version of this extra field
        private final int version = 1;
        // size:1 - size of uid field (n)
        // size:n - unix user ID
        private final String uid;
        // size:1 - size of gid field (m)
        // size:m - unix group ID
        private final String gid;
    }

    @Getter
    @Builder
    public static final class VersionUnknownPayload implements Payload {

        // size:1 - version of this extra field
        private final int version;
        private final byte[] data;
    }

}
