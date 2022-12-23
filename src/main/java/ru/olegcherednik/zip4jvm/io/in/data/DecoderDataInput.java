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
import org.apache.commons.lang3.ArrayUtils;
import ru.olegcherednik.zip4jvm.crypto.Decoder;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 07.02.2020
 */
public final class DecoderDataInput extends CommonBaseDataInput {

    private final Decoder decoder;
    private final long bytesTotal;

    private final int blockSize;
    private final byte[] buf;
    private int lo;
    private int hi;

    private long bytesRead;
    private boolean eof;

    public DecoderDataInput(DataInput in, Decoder decoder, long bytesTotal) {
        super(in);
        this.decoder = decoder;
        this.bytesTotal = bytesTotal;
        blockSize = Math.max(0, decoder.getBlockSize());
        buf = blockSize == 0 ? ArrayUtils.EMPTY_BYTE_ARRAY : new byte[blockSize];
    }

    public void decodingAccomplished() throws IOException {
        decoder.close(in);
    }

    @Override
    public int read(byte[] buf, final int offs, int len) {
        len = getAvailableBytes(len);
        int res = readFromLocalBuf(buf, offs, len);
        res += readFromIn(buf, offs + res, eof ? 0 : len - res);
        readBlockToLocalBuf(eof ? 0 : len - res);
        res += readFromLocalBuf(buf, offs + res, eof ? 0 : len - res);
        return eof ? IOUtils.EOF : res;
    }

    private int readFromLocalBuf(byte[] buf, int offs, int len) {
        int res = 0;

        for (; lo < hi && len > 0; lo++, offs++, bytesRead++, res++, len--)
            buf[offs] = this.buf[lo];

        if (lo == hi) {
            lo = 0;
            hi = 0;
        }

        return res;
    }

    private int readFromIn(byte[] buf, int offs, int len) {
        len = blockSize == 0 ? len : blockSize * (len / blockSize);
        int res = readFromInToBuf(buf, offs, len);

        if (eof)
            return res;

        bytesRead += res;
        return res == 0 ? 0 : decoder.decrypt(buf, offs, res);
    }

    private void readBlockToLocalBuf(int len) {
        if (len == 0)
            return;

        assert lo == hi;
        assert lo == 0;

        int res = readFromInToBuf(buf, 0, blockSize);

        if (!eof && res > 0)
            hi = decoder.decrypt(buf, 0, res);
    }

    private int readFromInToBuf(byte[] buf, int offs, int len) {
        len = getAvailableBytes(len);
        int res = in.read(buf, offs, len);

        if (res == IOUtils.EOF)
            eof = true;

        return eof ? len : res;
    }

    private int getAvailableBytes(int len) {
        long bytesAvailable = bytesTotal - bytesRead;
        return eof || len <= 0 ? 0 : (int)Math.min(len, bytesAvailable);
    }

    // ---------- RandomAccess ----------

    @Override
    public long skip(long bytes) {
        int total = 0;

        for (long i = 0; i < bytes; i++)
            total += readByte();

        return total;
    }

}
