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
package ru.olegcherednik.zip4jvm.io.in;

import ru.olegcherednik.zip4jvm.io.ByteOrder;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.io.IOException;

/**
 * This is a base implementation of the <tt>real</tt> {@link DataInput}.
 * <tt>Real</tt> means <tt>not decorator</tt>.
 *
 * @author Oleg Cherednik
 * @since 19.11.2024
 */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class BaseRealDataInput extends BaseDataInput {

    protected final DataInput in;

    // ---------- DataInput ----------

    @Override
    public ByteOrder getByteOrder() {
        return in.getByteOrder();
    }

    @Override
    public long getAbsOffs() {
        return in.getAbsOffs();
    }

    // ---------- Marker ----------

    @Override
    public void mark(String id) {
        in.mark(id);
    }

    @Override
    public long getMark(String id) {
        return in.getMark(id);
    }

    @Override
    public long getMarkSize(String id) {
        return in.getMarkSize(id);
    }

    // ---------- AutoCloseable ----------

    @Override
    public void close() throws IOException {
        in.close();
        super.close();
    }

    // ---------- Object ----------

    @Override
    public String toString() {
        long offs = getAbsOffs();
        return String.format("offs: %s (0x%s)", offs, Long.toHexString(offs));
    }

}
