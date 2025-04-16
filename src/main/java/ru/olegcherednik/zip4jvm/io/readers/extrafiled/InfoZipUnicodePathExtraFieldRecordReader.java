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
package ru.olegcherednik.zip4jvm.io.readers.extrafiled;

import ru.olegcherednik.zip4jvm.io.in.DataInput;
import ru.olegcherednik.zip4jvm.model.charset.Charsets;
import ru.olegcherednik.zip4jvm.model.extrafield.records.InfoZipUnicodePathExtraFieldRecord;
import ru.olegcherednik.zip4jvm.utils.ByteUtils;
import ru.olegcherednik.zip4jvm.utils.ChecksumUtils;
import ru.olegcherednik.zip4jvm.utils.function.Reader;

import lombok.RequiredArgsConstructor;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 16.04.2025
 */
@RequiredArgsConstructor
public final class InfoZipUnicodePathExtraFieldRecordReader implements Reader<InfoZipUnicodePathExtraFieldRecord> {

    private final int size;

    @Override
    public InfoZipUnicodePathExtraFieldRecord read(DataInput in) throws IOException {
        int version = in.readByte();

        InfoZipUnicodePathExtraFieldRecord.Payload payload = version == 1 ? readVersionOnePayload(in)
                                                                          : readUnknownPayload(version, in);

        return InfoZipUnicodePathExtraFieldRecord.builder()
                                                 .dataSize(size)
                                                 .payload(payload).build();
    }

    private InfoZipUnicodePathExtraFieldRecord.VersionOnePayload readVersionOnePayload(DataInput in)
            throws IOException {
        long crc32 = in.readDword();
        String name = in.readString(size - InfoZipUnicodePathExtraFieldRecord.SIZE_FIELD, Charsets.UTF_8);
        boolean checksumCorrect = crc32 == ChecksumUtils.crc32(name);

        return InfoZipUnicodePathExtraFieldRecord.VersionOnePayload.builder()
                                                                   .crc32(crc32)
                                                                   .name(name)
                                                                   .checksumCorrect(checksumCorrect)
                                                                   .build();
    }

    private InfoZipUnicodePathExtraFieldRecord.UnknownPayload readUnknownPayload(int version, DataInput in)
            throws IOException {
        byte[] data = in.readBytes(size - ByteUtils.BYTE_SIZE);
        return InfoZipUnicodePathExtraFieldRecord.UnknownPayload.builder()
                                                                .version(version)
                                                                .data(data).build();
    }
}
