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
package ru.olegcherednik.zip4jvm.io.readers;

import ru.olegcherednik.zip4jvm.io.in.DataInput;
import ru.olegcherednik.zip4jvm.model.extrafield.PkwareExtraField;
import ru.olegcherednik.zip4jvm.model.extrafield.records.UnknownExtraFieldRecord;
import ru.olegcherednik.zip4jvm.utils.function.Reader;

import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.function.Function;

/**
 * @author Oleg Cherednik
 * @since 20.10.2019
 */
@RequiredArgsConstructor
public class ExtraFieldRecordReader implements Reader<PkwareExtraField.Record> {

    private final Map<Integer, Function<Integer, Reader<? extends PkwareExtraField.Record>>> readers;

    @Override
    public PkwareExtraField.Record read(DataInput in) {
        int sig = in.readWordSignature();
        int size = in.readWord();

        if (readers.containsKey(sig))
            return readers.get(sig).apply(size).read(in);

        byte[] data = in.readBytes(size);
        return new UnknownExtraFieldRecord(sig, data);
    }

}
