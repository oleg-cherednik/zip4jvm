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
package ru.olegcherednik.zip4jvm.crypto;

/**
 * @author Oleg Cherednik
 * @since 11.08.2019
 */
final class NullDecoder implements Decoder {

    @Override
    public int decrypt(byte[] buf, int offs, int len) {
        return len;
    }

    @Override
    public long getCompressedSize() {
        return Long.MAX_VALUE;
    }

    @Override
    public long getDataCompressedSize(long compressedSize) {
        return compressedSize;
    }

    @Override
    public String toString() {
        return "<null>";
    }
}
