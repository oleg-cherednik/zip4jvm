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

import lombok.RequiredArgsConstructor;
import ru.olegcherednik.zip4jvm.io.in.data.DataInputNew;
import ru.olegcherednik.zip4jvm.model.extrafield.InfoZipOldUnixExtraFieldRecord;
import ru.olegcherednik.zip4jvm.utils.function.ReaderNew;
import ru.olegcherednik.zip4jvm.utils.time.UnixTimestampConverterUtils;

import java.io.IOException;

import static ru.olegcherednik.zip4jvm.model.ExtraField.NO_DATA;

/**
 * @author Oleg Cherednik
 * @since 25.10.2019
 */
@RequiredArgsConstructor
public final class InfoZipOldUnixExtraFieldRecordReader implements ReaderNew<InfoZipOldUnixExtraFieldRecord> {

    private final int size;

    @Override
    public InfoZipOldUnixExtraFieldRecord read(DataInputNew in) throws IOException {
        long lastAccessTime = UnixTimestampConverterUtils.unixToJavaTime(in.readDword());
        long lastModificationTime = UnixTimestampConverterUtils.unixToJavaTime(in.readDword());
        int uid = size >= 10 ? in.readWord() : NO_DATA;
        int gid = size >= 12 ? in.readWord() : NO_DATA;

        return InfoZipOldUnixExtraFieldRecord.builder()
                                             .dataSize(size)
                                             .lastAccessTime(lastAccessTime)
                                             .lastModificationTime(lastModificationTime)
                                             .uid(uid)
                                             .gid(gid).build();
    }

}
