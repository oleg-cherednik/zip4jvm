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
package ru.olegcherednik.zip4jvm.io.ed;

import ru.olegcherednik.zip4jvm.io.in.data.DataInputNew;

import java.io.IOException;
import java.io.InputStream;

public class Deflate64CompressorInputStream extends InputStream {

    private final HuffmanDecoder decoder;
    private final byte[] oneByte = new byte[1];

    /**
     * Constructs a Deflate64CompressorInputStream.
     *
     * @param in the stream to read from
     */
    public Deflate64CompressorInputStream(DataInputNew in) {
        decoder = new HuffmanDecoder(in);
    }

    @Override
    public int read() throws IOException {
        while (true) {
            int r = read(oneByte);
            switch (r) {
                case 1:
                    return oneByte[0] & 0xFF;
                case -1:
                    return -1;
                case 0:
                    continue;
                default:
                    throw new IllegalStateException("Invalid return value from read: " + r);
            }
        }
    }

    @Override
    public int read(byte[] buf, int offs, int len) throws IOException {
        return decoder.decode(buf, offs, len);
    }

    @Override
    public void close() throws IOException {
        decoder.close();
    }

}
