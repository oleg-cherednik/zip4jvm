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
import ru.olegcherednik.zip4jvm.model.extrafield.records.AlignmentExtraFieldRecord;
import ru.olegcherednik.zip4jvm.model.extrafield.records.ExecutableJarMarkerExtraFieldRecord;
import ru.olegcherednik.zip4jvm.model.extrafield.records.UnknownExtraFieldRecord;
import ru.olegcherednik.zip4jvm.utils.function.Reader;

/**
 * @author Oleg Cherednik
 * @since 05.01.2023
 */
@RequiredArgsConstructor
public final class AlignmentExtraFieldRecordReader implements Reader<AlignmentExtraFieldRecord> {

    private final int size;

    @Override
    public AlignmentExtraFieldRecord read(DataInput in) {
        byte[] data = in.readBytes(size);
        return AlignmentExtraFieldRecord.builder()
                                        .dataSize(size)
                                        .data(data)
                                        .build();
    }

}
