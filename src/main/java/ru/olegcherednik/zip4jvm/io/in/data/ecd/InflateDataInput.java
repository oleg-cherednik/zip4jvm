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
package ru.olegcherednik.zip4jvm.io.in.data.ecd;

import ru.olegcherednik.zip4jvm.io.in.data.DataInput;
import ru.olegcherednik.zip4jvm.io.in.data.compressed.CompressedDataInput;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

/**
 * @author Oleg Cherednik
 * @since 21.11.2024
 */
final class InflateDataInput extends CompressedDataInput {

    private final byte[] buf1 = new byte[1024 * 4];
    private final Inflater inflater = new Inflater(true);

    InflateDataInput(DataInput in) {
        super(in);
    }

    private boolean fill() throws IOException {
        absOffs = in.getAbsOffs();
        int readNow = in.read(buf1, 0, 304);

        if (readNow == IOUtils.EOF)
            return true;

        inflater.setInput(buf1, 0, readNow);
        return false;
    }

    // ---------- ReadBuffer ----------

    @Override
    public int read(byte[] buf, int offs, int len) throws IOException {
        try {
            int readNow;

            while ((readNow = inflater.inflate(buf, offs, len)) == 0) {
                if (inflater.finished() || inflater.needsDictionary())
                    return IOUtils.EOF;

                if (inflater.needsInput())
                    if (fill())
                        return IOUtils.EOF;
            }

            return super.read(null, IOUtils.EOF, readNow);
        } catch (DataFormatException e) {
            throw new IOException(e);
        }
    }

    // ---------- AutoCloseable ----------

    @Override
    public void close() throws IOException {
        inflater.end();
        super.close();
    }

}
