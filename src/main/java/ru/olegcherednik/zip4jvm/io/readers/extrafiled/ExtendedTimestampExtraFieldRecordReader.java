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
import ru.olegcherednik.zip4jvm.io.in.data.DataInput;
import ru.olegcherednik.zip4jvm.model.extrafield.ExtendedTimestampExtraFieldRecord;
import ru.olegcherednik.zip4jvm.utils.function.Reader;
import ru.olegcherednik.zip4jvm.utils.time.UnixTimestampConverterUtils;

/**
 * @author Oleg Cherednik
 * @since 25.10.2019
 */
@RequiredArgsConstructor
public final class ExtendedTimestampExtraFieldRecordReader implements Reader<ExtendedTimestampExtraFieldRecord> {

    private final int size;

    @Override
    public ExtendedTimestampExtraFieldRecord read(DataInput in) {
        ExtendedTimestampExtraFieldRecord.Flag flag = new ExtendedTimestampExtraFieldRecord.Flag(in.readByte());
        long lastModificationTime = -1;
        long lastAccessTime = -1;
        long creationTime = -1;

        if (flag.isLastModificationTime())
            lastModificationTime = UnixTimestampConverterUtils.unixToJavaTime(in.readDword());
        if (flag.isLastAccessTime() && size > 5)
            lastAccessTime = UnixTimestampConverterUtils.unixToJavaTime(in.readDword());
        if (flag.isCreationTime() && size > 5)
            creationTime = UnixTimestampConverterUtils.unixToJavaTime(in.readDword());

        return ExtendedTimestampExtraFieldRecord.builder()
                                                .dataSize(size)
                                                .flag(flag)
                                                .lastModificationTime(lastModificationTime)
                                                .lastAccessTime(lastAccessTime)
                                                .creationTime(creationTime).build();
    }

}
