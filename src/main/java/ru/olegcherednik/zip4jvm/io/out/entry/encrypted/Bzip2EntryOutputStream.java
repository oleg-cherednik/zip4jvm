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
package ru.olegcherednik.zip4jvm.io.out.entry.encrypted;

import ru.olegcherednik.zip4jvm.io.bzip2.Bzip2OutputStream;
import ru.olegcherednik.zip4jvm.io.out.data.DataOutput;
import ru.olegcherednik.zip4jvm.io.out.entry.EncryptedOutputStream;
import ru.olegcherednik.zip4jvm.model.CompressionLevel;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 12.04.2020
 */
final class Bzip2EntryOutputStream extends EncryptedEntryOutputStream {

    private final Bzip2OutputStream bzip2;

    Bzip2EntryOutputStream(DataOutput out, CompressionLevel compressionLevel) throws IOException {
        bzip2 = new Bzip2OutputStream(out, compressionLevel);
    }

    @Override
    public void write(byte[] buf, int offs, int len) throws IOException {
        System.out.println(Bzip2EntryOutputStream.class.getSimpleName() + ".write()");
        bzip2.write(buf, offs, len);
    }

    @Override
    public void close() throws IOException {
        bzip2.close();
    }

    @Override
    public String toString() {
        return bzip2.toString();
    }

}
