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
import ru.olegcherednik.zip4jvm.exception.Zip4jvmException;
import ru.olegcherednik.zip4jvm.model.src.SrcZip;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 07.02.2020
 */
public final class DecoderDataInputDecorator extends BaseDataInput implements DecoderDataInput {

    private final DataInput delegate;
    private final Decoder decoder;
    private long bytesTotal;
    private final byte[] buf;
    private boolean empty = true;
    private long bytesRead;

    public DecoderDataInputDecorator(DataInput delegate, Decoder decoder, long bytesTotal) {
        this.delegate = delegate;
        this.decoder = decoder;
        this.bytesTotal = bytesTotal;

        if (bytesTotal > Integer.MAX_VALUE && decoder != Decoder.NULL)
            throw new Zip4jvmException("Big files decryption is not supported");

        // TODO temporary; should use local buffer to read block with decoder.blockSize() size
        buf = new byte[(int)bytesTotal];
    }

    @Override
    public void decodingAccomplished() throws IOException {
        decoder.close(delegate);
    }

    @Override
    public long getAbsoluteOffs() {
        return delegate.getAbsoluteOffs();
    }

    @Override
    public long convertToAbsoluteOffs(int diskNo, long relativeOffs) {
        return delegate.convertToAbsoluteOffs(diskNo, relativeOffs);
    }

    @Override
    public long getDiskRelativeOffs() {
        return delegate.getDiskRelativeOffs();
    }

    @Override
    public SrcZip getSrcZip() {
        return delegate.getSrcZip();
    }

    @Override
    public SrcZip.Disk getDisk() {
        return delegate.getDisk();
    }

    @Override
    public long size() throws IOException {
        return delegate.getAbsoluteOffs();
    }

    @Override
    public int read(byte[] buf, int offs, int len) throws IOException {
        if (decoder == Decoder.NULL) {
            int bytesAvailable = (int)Math.min(bytesTotal - bytesRead, Integer.MAX_VALUE);
            len = bytesAvailable == 0 ? IOUtils.EOF : delegate.read(buf, offs, Math.min(len, bytesAvailable));

            if (len == IOUtils.EOF)
                return IOUtils.EOF;

            bytesRead += len;
            return len == 0 ? 0 : decoder.decrypt(buf, offs, len);
        }

        if (empty) {
            int a = delegate.read(this.buf, 0, this.buf.length);
            bytesTotal = a == 0 ? 0 : decoder.decrypt(this.buf, 0, a);
            empty = false;
        }

        int bytesAvailable = (int)Math.min(bytesTotal - bytesRead, Integer.MAX_VALUE);

        if (len <= 0 || bytesAvailable == 0)
            return IOUtils.EOF;

        int bytesToRead = Math.min(len, bytesAvailable);
        System.arraycopy(this.buf, (int)bytesRead, buf, offs, bytesToRead);
        bytesRead += bytesToRead;

        return bytesToRead;
    }

    @Override
    public long toLong(byte[] buf, int offs, int len) {
        return delegate.toLong(buf, offs, len);
    }

    @Override
    public void close() throws IOException {
        delegate.close();
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
        delegate.seek(absoluteOffs);
    }

    @Override
    public void seek(int diskNo, long relativeOffs) throws IOException {
        delegate.seek(diskNo, relativeOffs);
    }

    @Override
    public String toString() {
        return delegate.toString();
    }

}
