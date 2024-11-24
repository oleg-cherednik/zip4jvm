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
import ru.olegcherednik.zip4jvm.model.extrafield.records.InfoZipNewUnixExtraFieldRecord;
import ru.olegcherednik.zip4jvm.utils.BitUtils;
import ru.olegcherednik.zip4jvm.utils.function.Reader;

import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.math.BigInteger;

/**
 * @author Oleg Cherednik
 * @since 25.10.2019
 */
@RequiredArgsConstructor
public final class InfoZipNewUnixExtraFieldRecordReader implements Reader<InfoZipNewUnixExtraFieldRecord> {

    private final int size;

    @Override
    public InfoZipNewUnixExtraFieldRecord read(DataInput in) throws IOException {
        int version = in.readByte();

        InfoZipNewUnixExtraFieldRecord.Payload payload = version == 1 ? readVersionOnePayload(in)
                                                                      : readVersionUnknown(version, in);

        return InfoZipNewUnixExtraFieldRecord.builder()
                                             .dataSize(size)
                                             .payload(payload).build();
    }

    private static InfoZipNewUnixExtraFieldRecord.VersionOnePayload readVersionOnePayload(DataInput in)
            throws IOException {
        BigInteger uid = in.readBigInteger(in.readByte());
        BigInteger gid = in.readBigInteger(in.readByte());

        return InfoZipNewUnixExtraFieldRecord.VersionOnePayload.builder()
                                                               .uid(String.valueOf(uid))
                                                               .gid(String.valueOf(gid)).build();
    }

    private InfoZipNewUnixExtraFieldRecord.VersionUnknownPayload readVersionUnknown(int version, DataInput in)
            throws IOException {
        byte[] data = in.readBytes(size - BitUtils.BYTE_SIZE);
        return InfoZipNewUnixExtraFieldRecord.VersionUnknownPayload.builder()
                                                                   .version(version)
                                                                   .data(data).build();
    }
}
