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
package ru.olegcherednik.zip4jvm.io.in.data.ecd;

import ru.olegcherednik.zip4jvm.io.in.data.DataInput;
import ru.olegcherednik.zip4jvm.utils.quitely.Quietly;

import java.util.zip.Inflater;

/**
 * @author Oleg Cherednik
 * @since 18.12.2022
 */
final class InflateDataInput extends CompressedEcdDataInput {

    InflateDataInput(DataInput in, int uncompressedSize) {
        super(read(in, uncompressedSize), in.getByteOrder());
    }

    private static byte[] read(DataInput in, int uncompressedSize) {
        return Quietly.doQuietly(() -> {
            Inflater inflater = new Inflater(true);
            inflater.setInput(in.readBytes((int) in.size()));

            byte[] buf = new byte[uncompressedSize];
            inflater.inflate(buf, 0, buf.length);
            return buf;
        });
    }
}
