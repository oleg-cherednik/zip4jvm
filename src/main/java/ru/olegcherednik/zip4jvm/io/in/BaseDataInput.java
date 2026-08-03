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
package ru.olegcherednik.zip4jvm.io.in;

import ru.olegcherednik.zip4jvm.utils.ThreadLocalBuffer;

import org.apache.commons.io.IOUtils;

/**
 * @author Oleg Cherednik
 * @since 24.11.2024
 */
public abstract class BaseDataInput implements DataInput {

    // ---------- DataInput ----------

    @Override
    public int readByte() {
        return getByteOrder().readByte(this);
    }

    @Override
    public int readWord() {
        return getByteOrder().readWord(this);
    }

    @Override
    public long readDword() {
        return getByteOrder().readDword(this);
    }

    @Override
    public long readQword() {
        return getByteOrder().readQword(this);
    }

    // ---------- ReadBuffer ----------

    @Override
    public final int read() {
        byte[] buf = ThreadLocalBuffer.getOne();
        int readNow = read(buf, 0, buf.length);
        return readNow == IOUtils.EOF ? IOUtils.EOF : buf[0] & 0xFF;
    }

}
