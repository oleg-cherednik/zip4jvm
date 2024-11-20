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

import ru.olegcherednik.zip4jvm.io.in.data.xxx.DataInput;
import ru.olegcherednik.zip4jvm.model.extrafield.PkwareExtraField;
import ru.olegcherednik.zip4jvm.model.extrafield.records.UnknownExtraFieldRecord;
import ru.olegcherednik.zip4jvm.utils.function.XxxReader;

import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.util.Map;
import java.util.function.Function;

/**
 * @author Oleg Cherednik
 * @since 20.10.2019
 */
@RequiredArgsConstructor
public class ExtraFieldRecordReader implements XxxReader<PkwareExtraField.Record> {

    private final Map<Integer, Function<Integer, XxxReader<? extends PkwareExtraField.Record>>> readers;

    @Override
    public PkwareExtraField.Record read(DataInput in) throws IOException {
        int sig = in.readWordSignature();
        int size = in.readWord();

        if (readers.containsKey(sig))
            return readers.get(sig).apply(size).read(in);

        byte[] data = in.readBytes(size);
        return new UnknownExtraFieldRecord(sig, data);
    }

}
