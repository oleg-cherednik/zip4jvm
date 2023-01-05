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
package ru.olegcherednik.zip4jvm.io.readers;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ArrayUtils;
import ru.olegcherednik.zip4jvm.io.in.data.DataInput;
import ru.olegcherednik.zip4jvm.model.extrafield.ExtraField;
import ru.olegcherednik.zip4jvm.utils.function.Reader;

import java.util.Map;
import java.util.function.Function;

/**
 * @author Oleg Cherednik
 * @since 20.10.2019
 */
@RequiredArgsConstructor
public class ExtraFieldRecordReader implements Reader<ExtraField.Record> {

    private final Map<Integer, Function<Integer, Reader<? extends ExtraField.Record>>> readers;

    @Override
    public ExtraField.Record read(DataInput in) {
        int signature = in.readWordSignature();
        int size = in.readWord();

        if (readers.containsKey(signature))
            return readers.get(signature).apply(size).read(in);

        byte[] data = in.readBytes(size);
        return ExtraField.Record.Unknown.builder()
                                        .signature(signature)
                                        .data(data == null ? ArrayUtils.EMPTY_BYTE_ARRAY : data).build();
    }

    public static int getHeaderSize(DataInput in) {
        return in.wordSignatureSize() + in.wordSize();
    }
}
