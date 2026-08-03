/*
 * Copyright 2019 Oleg Cherednik (oleg.cherednik@gmail.com)
 *
 * Licensed under The Apache Software License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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
import ru.olegcherednik.zip4jvm.model.extrafield.records.InfoZipUnicodeCommentExtraFieldRecord;
import ru.olegcherednik.zip4jvm.utils.ByteUtils;
import ru.olegcherednik.zip4jvm.utils.ChecksumUtils;
import ru.olegcherednik.zip4jvm.utils.function.Reader;

import lombok.RequiredArgsConstructor;

/**
 * @author Oleg Cherednik
 * @since 20.04.2025
 */
@RequiredArgsConstructor
public final class InfoZipUnicodeCommentExtraFieldRecordReader
        implements Reader<InfoZipUnicodeCommentExtraFieldRecord> {

    private final int size;

    @Override
    public InfoZipUnicodeCommentExtraFieldRecord read(DataInput in) {
        int version = in.readByte();

        InfoZipUnicodeCommentExtraFieldRecord.Payload payload = version == 1 ? readVersionOnePayload(in)
                                                                             : readUnknownPayload(version, in);

        return InfoZipUnicodeCommentExtraFieldRecord.builder()
                                                    .dataSize(size)
                                                    .payload(payload).build();
    }

    private InfoZipUnicodeCommentExtraFieldRecord.VersionOnePayload readVersionOnePayload(DataInput in) {
        long crc32 = in.readDword();
        String name = in.readString(size - InfoZipUnicodeCommentExtraFieldRecord.SIZE_FIELD, Charsets.UTF_8);
        boolean checksumCorrect = crc32 == ChecksumUtils.crc32(name);

        return InfoZipUnicodeCommentExtraFieldRecord.VersionOnePayload.builder()
                                                                      .crc32(crc32)
                                                                      .comment(name)
                                                                      .checksumCorrect(checksumCorrect)
                                                                      .build();
    }

    private InfoZipUnicodeCommentExtraFieldRecord.UnknownPayload readUnknownPayload(int version, DataInput in) {
        byte[] data = in.readBytes(size - ByteUtils.BYTE_SIZE);
        return InfoZipUnicodeCommentExtraFieldRecord.UnknownPayload.builder()
                                                                   .version(version)
                                                                   .data(data).build();
    }
}
