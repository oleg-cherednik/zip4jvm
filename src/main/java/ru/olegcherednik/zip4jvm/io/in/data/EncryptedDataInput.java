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
import ru.olegcherednik.zip4jvm.io.ByteOrder;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;
import ru.olegcherednik.zip4jvm.utils.ValidationUtils;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.apache.commons.io.IOUtils;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 07.02.2020
 */
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public class EncryptedDataInput extends DataInput {

    protected final Decoder decoder;
    protected final DataInput in;

    protected long available;

    public static EncryptedDataInput create(ZipEntry zipEntry, DataInput in) {
        Decoder decoder = zipEntry.createDecoder(in);
        int blockSize = Math.max(0, decoder.getBlockSize());
        long encryptedSize = decoder == Decoder.NULL ? zipEntry.getCompressedSize() : decoder.getCompressedSize();

        return blockSize == 0 ? new EncryptedDataInput(decoder, in, encryptedSize)
                              : new BlockRead(blockSize, encryptedSize, decoder, in);
    }

    // ---------- DataInput ----------

    @Override
    public int readByte() throws IOException {
        return getByteOrder().readByte(this);
    }

    @Override
    public int readWord() throws IOException {
        return getByteOrder().readWord(this);
    }

    @Override
    public long readDword() throws IOException {
        return getByteOrder().readDword(this);
    }

    @Override
    public long readQword() throws IOException {
        return getByteOrder().readQword(this);
    }

    @Override
    public long getAbsoluteOffs() {
        return in.getAbsoluteOffs();
    }

    @Override
    public long size() {
        return in.size();
    }

    @Override
    public ByteOrder getByteOrder() {
        return in.getByteOrder();
    }

    @Override
    public void seek(long absoluteOffs) {
        in.seek(absoluteOffs);
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

    // ---------- InputStream ----------

    @Override
    public long skip(long bytes) throws IOException {
        ValidationUtils.requireZeroOrPositive(bytes, "skip.bytes");

        int total = 0;

        for (long i = 0; i < bytes; i++)
            total += readByte();

        return total;
    }

    @Override
    public int available() throws IOException {
        return (int) Math.min(available, Integer.MAX_VALUE);
    }

    @Override
    public int read(byte[] buf, int offs, int len) throws IOException {
        if (available() == 0)
            return IOUtils.EOF;

        int readNow = in.read(buf, offs, Math.min(available(), len));

        if (readNow == IOUtils.EOF || readNow == 0)
            return readNow;

        available -= readNow;
        return decoder.decrypt(buf, offs, readNow);
    }

    // ---------- AutoCloseable ----------

    @Override
    public void close() throws IOException {
        decoder.close(in);
        in.close();
    }

    // ---------- Object ----------

    @Override
    public String toString() {
        return in.toString();
    }

    // ----------

    protected static class BlockRead extends EncryptedDataInput {

        private final byte[] localBuf;

        private int lo;
        private int hi;

        public BlockRead(int blockSize, long encryptedSize, Decoder decoder, DataInput in) {
            super(decoder, in, encryptedSize);
            localBuf = new byte[blockSize];
        }

        private int readIn(byte[] buf, int offs, int len) throws IOException {
            len = Math.min(available(), localBuf.length * (len / localBuf.length));
            int readNow = in.read(buf, offs, len);

            if (readNow > 0) {
                available -= readNow;
                return decoder.decrypt(buf, offs, readNow);
            }

            return 0;
        }

        private int readLocalBuffer(byte[] buf, int offs, int len) {
            if (lo == hi || len <= 0)
                return 0;

            int res = 0;

            for (; lo < hi && len > 0; available--, res++, len--)
                buf[offs++] = localBuf[lo++];

            return res;
        }

        private void fillLocalBuffer() throws IOException {
            lo = 0;
            int res = in.read(localBuf, lo, Math.min(available(), localBuf.length));

            if (res > 0)
                hi = decoder.decrypt(localBuf, lo, res);
        }

        // ---------- InputStream ----------

        @Override
        public int read(byte[] buf, int offs, int len) throws IOException {
            if (available() == 0)
                return IOUtils.EOF;

            int readNow = readLocalBuffer(buf, offs, len);
            readNow += readIn(buf, offs + readNow, len - readNow);

            if (len > readNow && available() > 0) {
                fillLocalBuffer();
                readNow += readLocalBuffer(buf, offs + readNow, len - readNow);
            }

            return readNow;
        }

    }

}
