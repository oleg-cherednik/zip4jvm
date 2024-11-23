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
package ru.olegcherednik.zip4jvm.io.in.data.compressed;

import ru.olegcherednik.zip4jvm.io.in.data.BaseRealDataInput;
import ru.olegcherednik.zip4jvm.io.in.data.DataInput;

import lombok.Getter;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Oleg Cherednik
 * @since 21.11.2024
 */
@Getter
public class CompressedDataInput extends BaseRealDataInput {

    protected final InputStream is;
    protected long absOffs;

    protected CompressedDataInput(InputStream is, DataInput in) {
        super(in);
        this.is = is;
    }

    // ---------- ReadBuffer ----------

    @Override
    public final int read(byte[] buf, int offs, int len) throws IOException {
        int readNow = is.read(buf, offs, len);

        if (readNow == IOUtils.EOF || readNow == 0)
            return IOUtils.EOF;

        absOffs += readNow;
        return readNow;
    }

}
