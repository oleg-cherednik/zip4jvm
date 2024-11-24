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
package ru.olegcherednik.zip4jvm.io.in.data;

import org.apache.commons.io.IOUtils;

import java.io.IOException;

/**
 * This decorator apply a given {@code size} as a maximum amount of byte, that
 * can be read from the {@link DataInput}.
 *
 * @author Oleg Cherednik
 * @since 23.11.2024
 */
public class LimitSizeDataInput extends BaseDecoratorDataInput {

    protected long available;

    public static LimitSizeDataInput create(long size, DataInput in) {
        return new LimitSizeDataInput(size, in);
    }

    protected LimitSizeDataInput(long size, DataInput in) {
        super(in);
        available = size;
    }

    // ---------- ReadBuffer ----------

    @Override
    public int read(byte[] buf, int offs, int len) throws IOException {
        if (available == 0)
            return IOUtils.EOF;

        int readNow = super.read(buf, offs, (int) Math.min(available, len));

        if (readNow == IOUtils.EOF || readNow == 0)
            return readNow;

        available -= readNow;
        return readNow;
    }

}
