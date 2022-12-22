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
package ru.olegcherednik.zip4jvm.io.in.file;

import ru.olegcherednik.zip4jvm.io.Endianness;
import ru.olegcherednik.zip4jvm.io.in.RandomAccess;
import ru.olegcherednik.zip4jvm.io.in.data.ReadBuffer;
import ru.olegcherednik.zip4jvm.model.src.SrcZip;

import java.io.Closeable;
import java.io.IOException;

/**
 * Represent a virtual file with data. The file can be as single file as a set
 * of multiple files treated as a single one.
 *
 * @author Oleg Cherednik
 * @since 28.09.2019
 */
public interface DataInputFile extends Closeable, RandomAccess, ReadBuffer {

    /**
     * Absolute offs starting from the beginning of the first disk
     */
    long getAbsoluteOffs();

    long convertToAbsoluteOffs(int diskNo, long relativeOffs);

    /**
     * Relative offs starting from the beginning of the current disk
     */
    long getDiskRelativeOffs();

    long size();

    Endianness getEndiannes();

    void seek(int diskNo, long relativeOffs);

    SrcZip getSrcZip();

    SrcZip.Disk getDisk();

    // ---------- RandomAccess ----------

    @Override
    default void backward(int bytes) {
        seek(getAbsoluteOffs() - bytes);
    }

}
