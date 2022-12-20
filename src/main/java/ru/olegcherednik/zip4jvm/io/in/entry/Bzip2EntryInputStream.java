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
package ru.olegcherednik.zip4jvm.io.in.entry;

import org.apache.commons.io.IOUtils;
import ru.olegcherednik.zip4jvm.io.bzip2.Bzip2InputStream;
import ru.olegcherednik.zip4jvm.io.in.data.DataInput;
import ru.olegcherednik.zip4jvm.io.in.data.DataInputNewDecorator;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 12.04.2020
 */
final class Bzip2EntryInputStream extends EntryInputStream {

    private final Bzip2InputStream bzip;

    public Bzip2EntryInputStream(ZipEntry zipEntry, DataInput in) throws IOException {
        super(zipEntry, in);
        bzip = createInputStream();
    }

    private Bzip2InputStream createInputStream() throws IOException {
        return new Bzip2InputStream(new DataInputNewDecorator(in));
    }

    @Override
    public int read(byte[] buf, int offs, int len) throws IOException {
        len = bzip.read(buf, offs, len);

        if (len == 0 || len == IOUtils.EOF)
            return IOUtils.EOF;

        writtenUncompressedBytes += len;
        updateChecksum(buf, offs, len);
        return len;
    }
}
