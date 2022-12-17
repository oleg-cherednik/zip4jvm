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
import ru.olegcherednik.zip4jvm.crypto.Decoder;
import ru.olegcherednik.zip4jvm.model.src.SrcZip;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 07.02.2020
 */
public final class BufferedDecoderDataInput extends BaseDataInput implements DecoderDataInput {

    private final DataInput in;
    private final Decoder decoder;
    private final long bytesTotal;

    private final int blockSize;
    private final byte[] buf;
    private int lo;
    private int hi;

    private long bytesRead;

    public BufferedDecoderDataInput(DataInput in, Decoder decoder, long bytesTotal) {
        this.in = in;
        this.decoder = decoder;
        this.bytesTotal = bytesTotal;
        blockSize = decoder.getBlockSize();
        buf = new byte[blockSize];
    }

    @Override
    public void decodingAccomplished() throws IOException {
        decoder.close(in);
    }

    @Override
    public long getAbsoluteOffs() {
        return in.getAbsoluteOffs();
    }

    @Override
    public long convertToAbsoluteOffs(int diskNo, long relativeOffs) {
        return in.convertToAbsoluteOffs(diskNo, relativeOffs);
    }

    @Override
    public long getDiskRelativeOffs() {
        return in.getDiskRelativeOffs();
    }

    @Override
    public SrcZip getSrcZip() {
        return in.getSrcZip();
    }

    @Override
    public SrcZip.Disk getDisk() {
        return in.getDisk();
    }

    @Override
    public long size() throws IOException {
        return in.getAbsoluteOffs();
    }

    @Override
    public int read(byte[] buf, final int offs, int len) throws IOException {
        long bytesAvailable = bytesTotal - bytesRead;
        len = (int)Math.min(len, bytesAvailable);

        if (len <= 0)
            return IOUtils.EOF;

        int res = readFromLocalBuf(buf, offs, len);
        len = readFromIn(buf, offs + res, len + res);

        if (len == IOUtils.EOF)
            return IOUtils.EOF;

        bytesRead += len;
        return len == 0 ? 0 : decoder.decrypt(buf, offs, len);
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

    private int readFromIn(byte[] buf, int offs, int len) throws IOException {
        return in.read(buf, offs, len);
    }


//    @Override
//    public int read(byte[] buf, int offs, int len) throws IOException {
//        long available = getAvailableBytes();
//
//        if (available == 0)
//            return IOUtils.EOF;
//
//        len = (int)Math.min(len, available);
//
//        if (len <= 0)
//            return len;
//
//        int j = 0;
//        int a = copyFromLocalBuffer(buf, offs, len);
//        j += a;
//        len -= a;
//
//        a = directDecrypt(buf, offs + j, len);
//        j += a;
//        len -= a;
//
//        if (len > 0) {
//            a = readNextBlockToLocalBuffer();
//
//            if (a <= 0)
//                return a;
//
//            a = copyFromLocalBuffer(buf, offs + j, len);
//            j += a;
//        }
//
//        return j;
//    }

    private long getAvailableBytes() {
        return bytesTotal - bytesRead;
    }

    private int copyFromLocalBuffer(byte[] buf, int offs, int len) {
        int available = hi - lo;

        if (available == 0)
            return 0;

        len = Math.min(len, available);
        System.arraycopy(this.buf, lo, buf, offs, len);
        lo += len;
        bytesRead += len;

        if (lo == hi) {
            lo = 0;
            hi = 0;
        }

        return len;
    }

    private int readNextBlockToLocalBuffer() throws IOException {
        int a = in.read(buf, 0, blockSize);

        if (a <= 0)
            return a;

        hi = decoder.decrypt(buf, 0, a);
        return hi;
    }

    private int directDecrypt(byte[] buf, int offs, int len) throws IOException {
        if (len < buf.length)
            return 0;

        int totalBlocks = len / blockSize;
        int a = in.read(buf, offs, totalBlocks * blockSize);

        if (a <= 0)
            return a;

        a = decoder.decrypt(buf, offs, a);
        bytesRead += a;
        return a;
    }

    @Override
    public long toLong(byte[] buf, int offs, int len) {
        return in.toLong(buf, offs, len);
    }

    @Override
    public void close() throws IOException {
        in.close();
    }

    @Override
    public long skip(long bytes) throws IOException {
        int total = 0;

        for (long i = 0; i < bytes; i++)
            total += readByte();

        return total;
    }

    @Override
    public void seek(long absoluteOffs) throws IOException {
        in.seek(absoluteOffs);
    }

    @Override
    public void seek(int diskNo, long relativeOffs) throws IOException {
        in.seek(diskNo, relativeOffs);
    }

    @Override
    public String toString() {
        return in.toString();
    }

}
