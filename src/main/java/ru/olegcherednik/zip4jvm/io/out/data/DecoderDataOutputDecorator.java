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
package ru.olegcherednik.zip4jvm.io.out.data;

import lombok.RequiredArgsConstructor;
import ru.olegcherednik.zip4jvm.crypto.Encoder;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 11.02.2020
 */
@RequiredArgsConstructor
public final class DecoderDataOutputDecorator extends BaseDataOutput implements DecoderDataOutput {

    private final DataOutput delegate;
    private final Encoder encoder;

    @Override
    public void writeEncryptionHeader() throws IOException {
        encoder.writeEncryptionHeader(delegate);
    }

    @Override
    public void encodingAccomplished() throws IOException {
        encoder.close(delegate);
    }

    @Override
    public void fromLong(long val, byte[] buf, int offs, int len) {
        delegate.fromLong(val, buf, offs, len);
    }

    @Override
    public long getRelativeOffs() {
        return delegate.getRelativeOffs();
    }

    @Override
    protected void writeInternal(byte[] buf, int offs, int len) throws IOException {
        encoder.encrypt(buf, offs, len);
        delegate.write(buf, offs, len);
    }

    @Override
    public void close() throws IOException {
        delegate.close();
    }

    @Override
    public void flush() throws IOException {
        delegate.flush();
    }

    @Override
    public String toString() {
        return delegate.toString();
    }

}
