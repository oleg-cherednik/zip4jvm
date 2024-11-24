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

import ru.olegcherednik.zip4jvm.io.out.DataOutput;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 09.08.2019
 */
final class NullEncoder implements Encoder {

    @Override
    public void writeEncryptionHeader(DataOutput out) throws IOException {
        /* nothing to write */
    }

    @Override
    public byte encrypt(byte b) {
        return b;
    }

    @Override
    public String toString() {
        return "<null>";
    }

}
