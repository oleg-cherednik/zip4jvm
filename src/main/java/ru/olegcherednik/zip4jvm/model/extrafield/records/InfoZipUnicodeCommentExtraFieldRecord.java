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

/**
 * see 4.6.8
 *
 * @author Oleg Cherednik
 * @since 20.04.2025
 */
@Getter
@Builder
public class InfoZipUnicodeCommentExtraFieldRecord implements PkwareExtraField.Record {

    public static final InfoZipUnicodeCommentExtraFieldRecord NULL = builder().build();

    public static final int SIGNATURE = 0x6375;
    public static final int SIZE_FIELD = 2 + 2 + 1; // 5 bytes: signature + size + version

    // size:2 - attribute tag value #1 (0x6375)
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
        return "InfoZIP Unicode Comment";
    }

    // ---------- Writer ----------

    @Override
    public void write(DataOutput out) throws IOException {
        throw new NotImplementedException();
    }

    // ---------- Object ----------

    @Override
    public String toString() {
        return isNull() ? "<null>" : String.format("%s (version: %d)", getTitle(), payload.getVersion());
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
        // size:4 - crc32
        private final long crc32;
        // size:1 - UTF-8 version of the entry comment
        private final String comment;
        private final boolean checksumCorrect;
    }

    @Getter
    @Builder
    public static final class UnknownPayload implements Payload {

        // size:1 - version of this extra field
        private final int version;
        private final byte[] data;
    }

}
