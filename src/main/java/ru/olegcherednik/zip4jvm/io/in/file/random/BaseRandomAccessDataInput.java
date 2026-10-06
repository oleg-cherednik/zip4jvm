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
package ru.olegcherednik.zip4jvm.io.in.file.random;

import ru.olegcherednik.zip4jvm.io.ByteOrder;
import ru.olegcherednik.zip4jvm.io.in.MarkerDataInput;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * The abstraction of random access data. This data is not huge and have a
 * finite {@code size}.
 *
 * @author Oleg Cherednik
 * @since 11.11.2024
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class BaseRandomAccessDataInput extends MarkerDataInput implements RandomAccessDataInput {

    protected final long size;
    protected final ByteOrder byteOrder;

    // ---------- RandomAccessDataInput ----------

    @Override
    public void seek(String id) {
        seek(getMark(id));
    }

    @Override
    public long available() {
        return size - getAbsOffs();
    }

}
