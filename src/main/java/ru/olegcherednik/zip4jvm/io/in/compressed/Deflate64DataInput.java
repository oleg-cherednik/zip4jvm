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

import org.apache.commons.compress.compressors.deflate64.Deflate64CompressorInputStream;

import java.io.InputStream;

/**
 * @author Oleg Cherednik
 * @since 15.04.2020
 */
public final class Deflate64DataInput extends CompressedDataInput {

    public static Deflate64DataInput create(DataInput in) {
        return new Deflate64DataInput(createInputStream(in), in);
    }

    private Deflate64DataInput(InputStream d64, DataInput in) {
        super(d64, in);
    }

    // ---------- static ----------

    private static Deflate64CompressorInputStream createInputStream(DataInput in) {
        return new Deflate64CompressorInputStream(new ReadBufferInputStream(in));
    }

}
