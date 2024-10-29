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

import ru.olegcherednik.zip4jvm.crypto.Encoder;

import lombok.RequiredArgsConstructor;

import java.io.IOException;

/**
 * This interface describes ability to write an encoded data items to the
 * given {@link DataOutput}. I.e. this is a decorator, that can encrypt
 * incoming data and write it to the given {@link DataOutput}.
 *
 * @author Oleg Cherednik
 * @since 11.02.2020
 */
@RequiredArgsConstructor
public class EncoderDataOutput extends BaseDataOutput {

    private final Encoder encoder;
    private final DataOutput out;

    public void writeEncryptionHeader() throws IOException {
        encoder.writeEncryptionHeader(out);
    }

    public void encodingAccomplished() throws IOException {
        encoder.close(out);
    }

    @Override
    public void fromLong(long val, byte[] buf, int offs, int len) {
        out.fromLong(val, buf, offs, len);
    }

    @Override
    public long getRelativeOffs() {
        return out.getRelativeOffs();
    }

    @Override
    protected void writeInternal(byte[] buf, int offs, int len) throws IOException {
        encoder.encrypt(buf, offs, len);
        out.write(buf, offs, len);
    }

    @Override
    public void close() throws IOException {
        System.out.println(EncoderDataOutput.class.getSimpleName() + ".close()");
        out.close();
    }

    @Override
    public void flush() throws IOException {
        out.flush();
    }

    @Override
    public String toString() {
        return out.toString();
    }

}
