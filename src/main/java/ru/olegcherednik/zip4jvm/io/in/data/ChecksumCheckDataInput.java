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

import ru.olegcherednik.zip4jvm.exception.Zip4jvmException;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;

import org.apache.commons.codec.digest.PureJavaCrc32;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.util.zip.Checksum;

/**
 * @author Oleg Cherednik
 * @since 15.11.2024
 */
public class ChecksumCheckDataInput extends BaseDataInput {

    private final long expectedCrc32;
    private final String fileName;
    private final Checksum crc32 = new PureJavaCrc32();

    public static ChecksumCheckDataInput checksum(ZipEntry zipEntry, DataInput in) {
        return new ChecksumCheckDataInput(zipEntry.getChecksum(), zipEntry.getFileName(), in);
    }

    protected ChecksumCheckDataInput(long expectedCrc32, String fileName, DataInput in) {
        super(in);
        this.expectedCrc32 = expectedCrc32;
        this.fileName = fileName;
    }

    // ---------- ReadBuffer ----------

    @Override
    public int read(byte[] buf, int offs, int len) throws IOException {
        int readNow = super.read(buf, offs, len);

        if (readNow != IOUtils.EOF)
            crc32.update(buf, offs, readNow);

        return readNow;
    }

    // ---------- AutoCloseable ----------

    @Override
    public void close() throws IOException {
        long actual = crc32.getValue();

        if (expectedCrc32 > 0 && expectedCrc32 != actual)
            throw new Zip4jvmException("Checksum is not matched: " + fileName);

        super.close();
    }

}
