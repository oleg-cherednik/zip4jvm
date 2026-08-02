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
package ru.olegcherednik.zip4jvm.io.out;

import ru.olegcherednik.zip4jvm.io.ByteOrder;
import ru.olegcherednik.zip4jvm.io.Marker;
import ru.olegcherednik.zip4jvm.utils.quitely.Quietly;

import org.apache.commons.lang3.ArrayUtils;

/**
 * This interface describes an abstract resource where we can write data
 * consecutively. It does not support a random data access at this level.
 *
 * @author Oleg Cherednik
 * @since 03.08.2019
 */
public interface DataOutput extends Marker, WriteBuffer {

    ByteOrder getByteOrder();

    long getDiskOffs();

    void writeByte(int val);

    default void writeWordSignature(int sig) {
        writeWord(sig);
    }

    default void writeDwordSignature(int sig) {
        writeDword(sig);
    }

    void writeWord(int val);

    void writeDword(long val);

    void writeQword(long val);

    default void writeBytes(byte... buf) {
        if (ArrayUtils.isNotEmpty(buf))
            Quietly.doRuntime(() -> write(buf, 0, buf.length));
    }

    default int getDiskNo() {
        return 0;
    }

    default void flush() {
        // avoid checked exception
    }

    // ---------- AutoCloseable ----------

    @Override
    default void close() {
        /* nothing to close */
    }

}
