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

import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;

import org.apache.commons.codec.digest.PureJavaCrc32;

import java.io.IOException;
import java.util.function.LongConsumer;
import java.util.zip.Checksum;

/**
 * @author Oleg Cherednik
 * @since 06.11.2024
 */
public class ChecksumCalcDataOutput extends BaseDataOutput {

    private final LongConsumer saveSize;
    private final Checksum crc32 = new PureJavaCrc32();

    public static ChecksumCalcDataOutput checksum(ZipEntry zipEntry, DataOutput out) {
        return new ChecksumCalcDataOutput(zipEntry::setChecksum, out);
    }

    protected ChecksumCalcDataOutput(LongConsumer saveSize, DataOutput out) {
        super(out);
        this.saveSize = saveSize;
    }

    // ---------- OutputStream ----------

    @Override
    public void write(int b) throws IOException {
        crc32.update(b);
        super.write(b);
    }

    // ---------- AutoCloseable ----------

    @Override
    public void close() throws IOException {
        saveSize.accept(crc32.getValue());
        out.close();
    }

}
