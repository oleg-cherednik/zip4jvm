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
package ru.olegcherednik.zip4jvm.io.out.entry.compressed;

import ru.olegcherednik.zip4jvm.io.out.data.DataOutput;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 04.08.2019
 */
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
final class StoreEntryOutputStream extends CompressedEntryOutputStream {

    private final DataOutput out;

    @Override
    public void write(int b) throws IOException {
        out.write(b);
    }

    @Override
    public String toString() {
        return out.toString();
    }

}