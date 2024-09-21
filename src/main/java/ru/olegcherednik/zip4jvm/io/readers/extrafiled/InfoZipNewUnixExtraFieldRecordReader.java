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

import ru.olegcherednik.zip4jvm.io.in.data.DataInput;
import ru.olegcherednik.zip4jvm.model.extrafield.records.InfoZipNewUnixExtraFieldRecord;
import ru.olegcherednik.zip4jvm.utils.function.ReaderWithSize;

/**
 * @author Oleg Cherednik
 * @since 25.10.2019
 */
final class InfoZipNewUnixExtraFieldRecordReader implements ReaderWithSize<InfoZipNewUnixExtraFieldRecord> {

    @Override
    public InfoZipNewUnixExtraFieldRecord read(DataInput in, int size) {
        int version = in.readByte();

        InfoZipNewUnixExtraFieldRecord.Payload payload =
                version == 1 ? readVersionOnePayload(in) : readVersionUnknown(version, in, size);

        return InfoZipNewUnixExtraFieldRecord.builder()
                                             .dataSize(size)
                                             .payload(payload).build();
    }

    private static InfoZipNewUnixExtraFieldRecord.VersionOnePayload readVersionOnePayload(DataInput in) {
        String uid = in.readNumber(in.readByte(), 16);
        String gid = in.readNumber(in.readByte(), 16);

        return InfoZipNewUnixExtraFieldRecord.VersionOnePayload.builder()
                                                               .uid(uid)
                                                               .gid(gid).build();
    }

    private static InfoZipNewUnixExtraFieldRecord.VersionUnknownPayload readVersionUnknown(int version,
                                                                                           DataInput in,
                                                                                           int size) {
        byte[] data = in.readBytes(size - in.byteSize());
        return InfoZipNewUnixExtraFieldRecord.VersionUnknownPayload.builder()
                                                                   .version(version)
                                                                   .data(data).build();
    }
}
