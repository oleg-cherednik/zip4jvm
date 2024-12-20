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
package ru.olegcherednik.zip4jvm.io.in.file.random;

import ru.olegcherednik.zip4jvm.io.in.DataInput;

import java.io.IOException;

import static ru.olegcherednik.zip4jvm.utils.ValidationUtils.requireZeroOrPositive;

/**
 * This interface extends {@link DataInput} with adding ability random data
 * access.
 *
 * @author Oleg Cherednik
 * @since 18.11.2024
 */
public interface RandomAccessDataInput extends DataInput {

    void seek(int diskNo, long diskOffs) throws IOException;

    void seek(long absOffs) throws IOException;

    void seek(String id) throws IOException;

    long availableLong() throws IOException;

    default boolean isDwordSignature(int expected) throws IOException {
        long offs = getAbsOffs();
        int actual = readDwordSignature();
        backward((int) (getAbsOffs() - offs));
        return actual == expected;
    }

    default void backward(int bytes) throws IOException {
        requireZeroOrPositive(bytes, "backward.bytes");
        seek(getAbsOffs() - bytes);
    }

}
