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
import ru.olegcherednik.zip4jvm.io.in.data.DataInputNew;
import ru.olegcherednik.zip4jvm.model.extrafield.ExecutableJarMarkerExtraFieldRecord;
import ru.olegcherednik.zip4jvm.utils.function.Reader;
import ru.olegcherednik.zip4jvm.utils.function.ReaderNew;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 11.04.2020
 */
@RequiredArgsConstructor
public final class ExecutableJarMarkerExtraFieldRecordReader implements ReaderNew<ExecutableJarMarkerExtraFieldRecord> {

    private final int size;

    @Override
    public ExecutableJarMarkerExtraFieldRecord read(DataInputNew in) throws IOException {
        return new ExecutableJarMarkerExtraFieldRecord(size);
    }

}
