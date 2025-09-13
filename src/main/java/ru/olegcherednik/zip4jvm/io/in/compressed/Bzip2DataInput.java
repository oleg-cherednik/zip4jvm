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
package ru.olegcherednik.zip4jvm.io.in.compressed;

import ru.olegcherednik.zip4jvm.io.in.DataInput;
import ru.olegcherednik.zip4jvm.io.in.ReadBufferInputStream;
import ru.olegcherednik.zip4jvm.utils.quitely.Quietly;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;

/**
 * @author Oleg Cherednik
 * @since 12.04.2020
 */
public final class Bzip2DataInput extends CompressedDataInput {

    public static Bzip2DataInput create(DataInput in) {
        return Quietly.doRuntime(() -> new Bzip2DataInput(createInputStream(in), in));
    }

    private Bzip2DataInput(BZip2CompressorInputStream bzip, DataInput in) {
        super(bzip, in);
    }

    // ---------- static ----------

    private static BZip2CompressorInputStream createInputStream(DataInput in) {
        return Quietly.doRuntime(() -> new BZip2CompressorInputStream(new ReadBufferInputStream(in)));
    }

}
