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
package ru.olegcherednik.zip4jvm.io.in.decorators;

import ru.olegcherednik.zip4jvm.io.in.BaseDataInput;
import ru.olegcherednik.zip4jvm.io.in.BaseRealDataInput;
import ru.olegcherednik.zip4jvm.io.in.DataInput;
import ru.olegcherednik.zip4jvm.io.out.BaseDataOutput;
import ru.olegcherednik.zip4jvm.io.out.DataOutput;

import java.io.IOException;

/**
 * This decorator blocks closing the delegate {@link BaseDataInput#in}.
 *
 * @author Oleg Cherednik
 * @since 30.11.2024
 */
public class UncloseableDataInput extends BaseRealDataInput {

    public UncloseableDataInput(DataInput in) {
        super(in);
    }

    // ---------- AutoCloseable ----------

    @Override
    public long skip(long bytes) throws IOException {
        return in.skip(bytes);
    }

    @Override
    public int read(byte[] buf, int offs, int len) throws IOException {
        return in.read(buf, offs, len);
    }

    @Override
    public void close() throws IOException {
        /* nothing to close */

        int a = 0;
        a++;
    }

}
