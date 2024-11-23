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
package ru.olegcherednik.zip4jvm.io.in.data.compressed;

import ru.olegcherednik.zip4jvm.io.in.data.DataInput;
import ru.olegcherednik.zip4jvm.io.in.data.ReadBufferInputStream;

import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

/**
 * @author Oleg Cherednik
 * @since 21.11.2024
 */
public final class InflateDataInput extends CompressedDataInput {

    public static InflateDataInput create(DataInput in) {
        InflaterInputStream inf = new InflaterInputStream(new ReadBufferInputStream(in), new Inflater(true));
        return new InflateDataInput(inf, in);
    }

    private InflateDataInput(InflaterInputStream inf, DataInput in) {
        super(inf, in);
    }

}
