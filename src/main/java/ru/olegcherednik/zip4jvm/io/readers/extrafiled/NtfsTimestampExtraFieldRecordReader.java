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
import ru.olegcherednik.zip4jvm.model.extrafield.records.NtfsTimestampExtraFieldRecord;
import ru.olegcherednik.zip4jvm.utils.function.Reader;
import ru.olegcherednik.zip4jvm.utils.time.NtfsTimestampConverterUtils;

import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Oleg Cherednik
 * @since 16.10.2019
 */
@RequiredArgsConstructor
public final class NtfsTimestampExtraFieldRecordReader implements Reader<NtfsTimestampExtraFieldRecord> {

    private final int size;

    @Override
    public NtfsTimestampExtraFieldRecord read(DataInput in) {
        long offs = in.getAbsoluteOffs();

        in.skip(4);

        List<NtfsTimestampExtraFieldRecord.Tag> tags = readTags(offs, in);

        return NtfsTimestampExtraFieldRecord.builder()
                                            .dataSize(size)
                                            .tags(tags).build();
    }

    private List<NtfsTimestampExtraFieldRecord.Tag> readTags(long offs, DataInput in) {
        List<NtfsTimestampExtraFieldRecord.Tag> tags = new ArrayList<>();

        while (in.getAbsoluteOffs() < offs + size) {
            int tag = in.readWord();
            tags.add(tag == NtfsTimestampExtraFieldRecord.OneTag.SIGNATURE ? readOneTag(in) : readUnknownTag(tag, in));
        }

        return tags.isEmpty() ? Collections.emptyList() : Collections.unmodifiableList(tags);
    }

    private static NtfsTimestampExtraFieldRecord.OneTag readOneTag(DataInput in) {
        int size = in.readWord();
        assert size == 8 * 3;

        long lastModificationTime = NtfsTimestampConverterUtils.ntfsToJavaTime(in.readQword());
        long lastAccessTime = NtfsTimestampConverterUtils.ntfsToJavaTime(in.readQword());
        long creationTime = NtfsTimestampConverterUtils.ntfsToJavaTime(in.readQword());

        return NtfsTimestampExtraFieldRecord.OneTag.builder()
                                                   .lastModificationTime(lastModificationTime)
                                                   .lastAccessTime(lastAccessTime)
                                                   .creationTime(creationTime).build();
    }

    private static NtfsTimestampExtraFieldRecord.UnknownTag readUnknownTag(int tag, DataInput in) {
        int size = in.readWord();
        byte[] data = in.readBytes(size);
        return NtfsTimestampExtraFieldRecord.UnknownTag.builder()
                                                       .signature(tag)
                                                       .data(data).build();
    }

    @Override
    public String toString() {
        return String.format("NTFS Timestamps (0x%04X)", NtfsTimestampExtraFieldRecord.SIGNATURE);
    }

}
