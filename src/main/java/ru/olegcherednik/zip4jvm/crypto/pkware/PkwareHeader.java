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
package ru.olegcherednik.zip4jvm.crypto.pkware;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import ru.olegcherednik.zip4jvm.exception.IncorrectPasswordException;
import ru.olegcherednik.zip4jvm.io.in.data.DataInputNew;
import ru.olegcherednik.zip4jvm.io.out.data.DataOutput;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;

import java.io.IOException;
import java.security.SecureRandom;

/**
 * @author Oleg Cherednik
 * @since 29.07.2019
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class PkwareHeader {

    public static final int SIZE = 12;

    private final byte[] buf;

    public static PkwareHeader create(PkwareEngine engine, int checksum) {
        return new PkwareHeader(createBuf(engine, checksum));
    }

    private static byte[] createBuf(PkwareEngine engine, int checksum) {
        byte[] buf = new byte[SIZE];

        new SecureRandom().nextBytes(buf);
        buf[buf.length - 1] = low(checksum);
        buf[buf.length - 2] = high(checksum);
        engine.encrypt(buf, 0, buf.length);

        return buf;
    }

    public static PkwareHeader read(PkwareEngine engine, DataInputNew in, ZipEntry zipEntry) {
        PkwareHeader header = new PkwareHeader(in.readBytes(SIZE));
        header.requireMatchChecksum(engine, zipEntry);
        return header;
    }

    public void write(DataOutput out) throws IOException {
        out.writeBytes(buf);
    }

    /** see 6.1.6 */
    private void requireMatchChecksum(PkwareEngine engine, ZipEntry zipEntry) {
        engine.decrypt(buf, 0, buf.length);
        int lastModifiedTime = zipEntry.getLastModifiedTime();
        long checksum = zipEntry.getChecksum();

        boolean match = false;

        if (buf[buf.length - 1] == low(lastModifiedTime))
            match = true;
        if (buf[buf.length - 1] == (byte)(checksum >> 24))
            match = true;

        if (!match)
            throw new IncorrectPasswordException(zipEntry.getFileName());
    }

    private static byte low(int checksum) {
        return (byte)(checksum >> 8);
    }

    private static byte high(int checksum) {
        return (byte)checksum;
    }

}
