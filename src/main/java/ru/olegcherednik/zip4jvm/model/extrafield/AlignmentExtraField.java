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
package ru.olegcherednik.zip4jvm.model.extrafield;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ru.olegcherednik.zip4jvm.io.in.data.DataInput;
import ru.olegcherednik.zip4jvm.io.readers.ExtraFieldRecordReader;

/**
 * It was faced in <tt>apk</tt> file. This is not a PKWARE standard extra field.
 * Store it as simple byte array. It should not be greater than
 * {@link  ExtraFieldRecordReader#getHeaderSize(DataInput)}}.
 *
 * @author Oleg Cherednik
 * @since 05.01.2023
 */
@Getter
@RequiredArgsConstructor
public class AlignmentExtraField implements ExtraField {

    private final byte[] data;

    @Override
    public int getSize() {
        return data.length;
    }

}
