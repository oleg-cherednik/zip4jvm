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

import ru.olegcherednik.zip4jvm.crypto.Decoder;
import ru.olegcherednik.zip4jvm.utils.ValidationUtils;

import org.apache.commons.io.IOUtils;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 07.02.2020
 */
public class EncryptedDataInput extends BaseRealDataInput {

    protected final Decoder decoder;
    protected long available;

    public static DataInput create(Decoder decoder, DataInput in) throws IOException {
        if (decoder == Decoder.NULL || decoder == null)
            return in;

        int batchSize = Math.max(0, decoder.getBlockSize());
        long encryptedSize = decoder.getCompressedSize();

        return batchSize == 0 ? new EncryptedDataInput(decoder, in, encryptedSize)
                              : new BatchRead(batchSize, encryptedSize, decoder, in);
    }

    protected EncryptedDataInput(Decoder decoder, DataInput in, long encryptedSize) {
        super(in);
        this.decoder = decoder;
        available = encryptedSize;
    }

    // ---------- DataInput ----------

    @Override
    public long skip(long bytes) throws IOException {
        ValidationUtils.requireZeroOrPositive(bytes, "skip.bytes");
        return in.skip(bytes);
    }

    // ---------- ReadBuffer ----------

    @Override
    public int read(byte[] buf, int offs, int len) throws IOException {
        if (available == 0)
            return IOUtils.EOF;

        int readNow = in.read(buf, offs, (int) Math.min(available, len));

        if (readNow == IOUtils.EOF || readNow == 0)
            return readNow;

        available -= readNow;
        return decoder.decrypt(buf, offs, readNow);
    }

    // ---------- AutoCloseable ----------

    @Override
    public void close() throws IOException {
        decoder.close(in);
        super.close();
    }

    // ---------- Object ----------

    @Override
    public String toString() {
        return "[" + decoder + "] " + super.toString();
    }

    // ----------

    protected static class BatchRead extends EncryptedDataInput {

        private final byte[] batch;

        private int lo;
        private int hi;

        public BatchRead(int batchSize, long encryptedSize, Decoder decoder, DataInput in) {
            super(decoder, in, encryptedSize);
            batch = new byte[batchSize];
        }

        private int readIn(byte[] buf, int offs, int len) throws IOException {
            int readNow = in.read(buf, offs, (int) Math.min(available, batch.length * (len / batch.length)));

            if (readNow > 0) {
                available -= readNow;
                return decoder.decrypt(buf, offs, readNow);
            }

            return 0;
        }

        private int readBatch(byte[] buf, int offs, int len) {
            if (lo == hi || len <= 0)
                return 0;

            int res = 0;

            for (; lo < hi && len > 0; available--, res++, len--)
                buf[offs++] = batch[lo++];

            return res;
        }

        private void fillBatch() throws IOException {
            lo = 0;
            int res = in.read(batch, lo, (int) Math.min(available, batch.length));

            if (res > 0)
                hi = decoder.decrypt(batch, lo, res);
        }

        // ---------- DataInput ----------

        @Override
        public long skip(long bytes) throws IOException {
            ValidationUtils.requireZeroOrPositive(bytes, "skip.bytes");

            int skipped = 0;

            for (long i = 0; i < bytes; i++, skipped++)
                readByte();

            return skipped;
        }

        // ---------- ReadBuffer ----------

        @Override
        public int read(byte[] buf, int offs, int len) throws IOException {
            if (available == 0)
                return IOUtils.EOF;

            int readNow = readBatch(buf, offs, len);
            readNow += readIn(buf, offs + readNow, len - readNow);

            if (len > readNow && available > 0) {
                fillBatch();
                readNow += readBatch(buf, offs + readNow, len - readNow);
            }

            return readNow;
        }

    }

}
