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
package ru.olegcherednik.zip4jvm.model.extrafield.records;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ArrayUtils;
import ru.olegcherednik.zip4jvm.io.out.data.DataOutput;
import ru.olegcherednik.zip4jvm.model.extrafield.PkwareExtraField;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 05.01.2023
 */
@RequiredArgsConstructor
public class UnknownExtraFieldRecord implements PkwareExtraField.Record {

    @Getter
    private final int signature;
    private final byte[] data;

    public byte[] getData() {
        return ArrayUtils.clone(data);
    }

    @Override
    public int getBlockSize() {
        return data.length;
    }

    @Override
    public boolean isNull() {
        return false;
    }

    @Override
    public String getTitle() {
        return "Unknown";
    }

    @Override
    public void write(DataOutput out) throws IOException {
        out.writeWordSignature(signature);
        out.writeWord(data.length);
        out.write(data, 0, data.length);
    }

}
