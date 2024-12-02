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
package ru.olegcherednik.zip4jvm.io.in.decorators;

import ru.olegcherednik.zip4jvm.exception.Zip4jvmException;
import ru.olegcherednik.zip4jvm.io.in.DataInput;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;

import lombok.Setter;
import org.apache.commons.io.IOUtils;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 17.11.2024
 */
public class SizeCheckDataInput extends BaseDecoratorDataInput {

    private final String fileName;
    @Setter
    private long expectedSize;
    private long size;

    public static SizeCheckDataInput uncompressedSize(ZipEntry zipEntry, DataInput in) {
        return new SizeCheckDataInput(zipEntry.getUncompressedSize(), zipEntry.getFileName(), in);
    }

    public static SizeCheckDataInput compressedSize(String fileName, DataInput in) {
        return new SizeCheckDataInput(0, fileName, in);
    }

    protected SizeCheckDataInput(long expectedSize, String fileName, DataInput in) {
        super(in);
        this.expectedSize = expectedSize;
        this.fileName = fileName;
    }

    // ---------- ReadBuffer ----------

    @Override
    public int read(byte[] buf, int offs, int len) throws IOException {
        int readNow = super.read(buf, offs, len);

        if (readNow != IOUtils.EOF)
            size += readNow;

        return readNow;
    }

    // ---------- AutoCloseable ----------

    @Override
    public void close() throws IOException {
        if (size != Math.max(0, expectedSize))
            throw new Zip4jvmException("UncompressedSize is not matched: " + fileName);

        super.close();
    }

}
