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
package ru.olegcherednik.zip4jvm.io.in.compressed;

import ru.olegcherednik.zip4jvm.io.in.DataInput;
import ru.olegcherednik.zip4jvm.io.in.ReadBufferInputStream;

import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

/**
 * @author Oleg Cherednik
 * @since 21.11.2024
 */
public final class DeflateDataInput extends CompressedDataInput {

    public static DeflateDataInput create(DataInput in) {
        return new DeflateDataInput(createInputStream(in), in);
    }

    private DeflateDataInput(InflaterInputStream inf, DataInput in) {
        super(inf, in);
    }

    // ---------- static ----------

    private static InflaterInputStream createInputStream(DataInput in) {
        return new InflaterInputStream(new ReadBufferInputStream(in), new Inflater(true));
    }

}
