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
package ru.olegcherednik.zip4jvm.io.out.compressed;

import ru.olegcherednik.zip4jvm.io.out.DataOutput;
import ru.olegcherednik.zip4jvm.model.CompressionLevel;

import java.io.IOException;
import java.util.zip.Deflater;

/**
 * @author Oleg Cherednik
 * @since 26.07.2019
 */
final class DeflateEntryDataOutput extends CompressedEntryDataOutput {

    private static final int FOUR = 4;

    private final byte[] buf1 = new byte[1024 * 4];
    private final byte[] buf2 = new byte[1];
    private final Deflater deflater = new Deflater();

    private boolean firstBytesRead;

    DeflateEntryDataOutput(DataOutput out, CompressionLevel compressionLevel) {
        super(out);
        deflater.setLevel(compressionLevel.getCode());
    }

    private void deflate() throws IOException {
        int len = deflater.deflate(buf1, 0, buf1.length);

        if (len <= 0)
            return;

        if (deflater.finished()) {
            if (len == FOUR)
                return;
            if (len < FOUR)
                return;
            len -= FOUR;
        }

        if (firstBytesRead)
            out.write(buf1, 0, len);
        else {
            out.write(buf1, 2, len - 2);
            firstBytesRead = true;
        }
    }

    private void finish() throws IOException {
        if (deflater.finished())
            return;

        deflater.finish();

        while (!deflater.finished()) {
            deflate();
        }
    }

    // ---------- OutputStream ----------

    @Override
    public void write(int b) throws IOException {
        buf2[0] = (byte) b;
        deflater.setInput(buf2);

        while (!deflater.needsInput()) {
            deflate();
        }
    }

    // ---------- AutoCloseable ----------

    @Override
    public void close() throws IOException {
        finish();
        super.close();
    }

}
