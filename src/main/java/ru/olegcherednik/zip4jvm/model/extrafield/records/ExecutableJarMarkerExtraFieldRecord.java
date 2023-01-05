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
import org.apache.commons.lang3.NotImplementedException;
import ru.olegcherednik.zip4jvm.io.out.data.DataOutput;
import ru.olegcherednik.zip4jvm.model.extrafield.ExtraField;

import java.io.IOException;

/**
 * Jar file could be treated as <b>executable</b> (but this is not mandatory marker) if this ExtraField is added as the very first extra field of the
 * archive.
 * <p>
 * <i>It's an "internal implementation detail" to support "executable" jar on Solaris platform</i>
 *
 * @author Oleg Cherednik
 * @since 25.10.2019
 */
@Getter
@RequiredArgsConstructor
public final class ExecutableJarMarkerExtraFieldRecord implements ExtraField.Record {

    public static final ExecutableJarMarkerExtraFieldRecord NULL = new ExecutableJarMarkerExtraFieldRecord(0);

    public static final int SIGNATURE = 0xCAFE;
    public static final int SIZE = 2 + 2;   // size:4

    // size:2 - signature (0x9901)
    // size:2 (should be 0)
    private final int dataSize;

    @Override
    public int getBlockSize() {
        return this == NULL ? 0 : SIZE;
    }

    @Override
    public int getSignature() {
        return SIGNATURE;
    }

    @Override
    public boolean isNull() {
        return this == NULL;
    }

    @Override
    public String getTitle() {
        return "Executable Jar Marker";
    }

    @Override
    public void write(DataOutput out) throws IOException {
        throw new NotImplementedException();
    }


}
