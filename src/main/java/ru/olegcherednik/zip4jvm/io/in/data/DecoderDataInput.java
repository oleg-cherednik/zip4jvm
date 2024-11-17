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
import ru.olegcherednik.zip4jvm.utils.quitely.Quietly;

import org.apache.commons.io.IOUtils;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 07.02.2020
 */
public abstract class DecoderDataInput extends FooDataInput {

    private static final int OFFS_BYTE = 0;
    private static final int OFFS_WORD = 1;
    private static final int OFFS_DWORD = 3;
    private static final int OFFS_QWORD = 7;

    private static final ThreadLocal<byte[]> THREAD_LOCAL_BUF = ThreadLocal.withInitial(() -> new byte[15]);

    protected final Decoder decoder;

    public static DataInput create(Decoder decoder, long encryptedSize, DataInput in) {
        int blockSize = Math.max(0, decoder.getBlockSize());
        return blockSize == 0 ? new PlainDecoderDataInput(decoder, encryptedSize, in)
                              : new BlockDecoderDataInput(decoder, blockSize, encryptedSize, in);
    }

    protected DecoderDataInput(Decoder decoder, DataInput in) {
        super(in);
        this.decoder = decoder;
    }

    public void decodingAccomplished() throws IOException {
        decoder.close(in);
    }

    private long readAndToLong(int offs, int len) {
        return Quietly.doQuietly(() -> {
            byte[] buf1 = THREAD_LOCAL_BUF.get();
            read(buf1, offs, len);
            return getByteOrder().getLong(buf1, offs, len);
        });
    }

    // ---------- DataInput ----------

    @Override
    public int readByte() {
        return (int) readAndToLong(OFFS_BYTE, BYTE_SIZE);
    }

    @Override
    public int readWord() {
        return (int) readAndToLong(OFFS_WORD, WORD_SIZE);
    }

    @Override
    public long readDword() {
        return readAndToLong(OFFS_DWORD, DWORD_SIZE);
    }

    @Override
    public long readQword() {
        return readAndToLong(OFFS_QWORD, QWORD_SIZE);
    }

    // ---------- RandomAccess ----------

    @Override
    public long skip(long bytes) {
        ValidationUtils.requireZeroOrPositive(bytes, "skip.bytes");

        int total = 0;

        for (long i = 0; i < bytes; i++)
            total += readByte();

        return total;
    }

}

class PlainDecoderDataInput extends DecoderDataInput {

    private long available;

    public PlainDecoderDataInput(Decoder decoder, long encryptedSize, DataInput in) {
        super(decoder, in);
        available = encryptedSize;
    }

    // ---------- InputStream ----------

    @Override
    public int available() throws IOException {
        return (int) Math.min(available, Integer.MAX_VALUE);
    }

    // ---------- ReadBuffer ----------

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

}

class BlockDecoderDataInput extends DecoderDataInput {

    private final byte[] localBuf;

    private int lo;
    private int hi;
    private boolean eof;

    private long available;

    public BlockDecoderDataInput(Decoder decoder, int blockSize, long encryptedSize, DataInput in) {
        super(decoder, in);
        localBuf = new byte[blockSize];
        available = encryptedSize;
    }

    private int readLocalBuffer(byte[] buf, int offs, int len) {
        if (lo == hi || len == 0)
            return 0;

        int res = 0;

        for (; lo < hi && len > 0; lo++, offs++, available--, res++, len--)
            buf[offs] = localBuf[lo];

        if (lo == hi) {
            lo = 0;
            hi = 0;
        }

        return res;
    }

    private int readFromIn(byte[] buf, int offs, int len) throws IOException {
        int res = readFromInToBuf(buf, offs, localBuf.length * (len / localBuf.length));

        if (eof)
            return res;

        available -= res;
        return res == 0 ? 0 : decoder.decrypt(buf, offs, res);
    }

    private int readFromInToBuf(byte[] buf, int offs, int len) throws IOException {
        len = Math.min(available(), len);
        int res = in.read(buf, offs, len);

        if (res == IOUtils.EOF)
            eof = true;

        return eof ? len : res;
    }

    private void fillLocalBuffer() throws IOException {
        assert lo == hi;
        assert lo == 0;

        int res = readFromInToBuf(localBuf, 0, localBuf.length);

        if (!eof && res > 0)
            hi = decoder.decrypt(localBuf, 0, res);
    }

    private int foo(byte[] buf, final int offs, int len) throws IOException {
        fillLocalBuffer();
        return readLocalBuffer(buf, offs, len);
    }

    // ---------- InputStream ----------

    @Override
    public int available() throws IOException {
        return (int) Math.min(available, Integer.MAX_VALUE);
    }

    // ---------- ReadBuffer ----------

    @Override
    public int read(byte[] buf, final int offs, int len) throws IOException {
        if (available() == 0)
            return IOUtils.EOF;

        len = Math.min(available(), len);

        int readNow = readLocalBuffer(buf, offs, len);
        readNow += readFromIn(buf, offs + readNow, len - readNow);

        if (len > readNow)
            readNow += foo(buf, offs + readNow, len - readNow);

        return readNow;
    }

}
