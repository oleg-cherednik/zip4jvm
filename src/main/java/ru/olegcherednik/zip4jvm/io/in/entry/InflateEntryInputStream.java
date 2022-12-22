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
import ru.olegcherednik.zip4jvm.io.in.data.DataInput;
import ru.olegcherednik.zip4jvm.io.in.data.DataInputNew;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;

import java.io.IOException;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

/**
 * @author Oleg Cherednik
 * @since 04.08.2019
 */
final class InflateEntryInputStream extends EntryInputStream {

    private final byte[] buf = new byte[1024 * 4];
    private final Inflater inflater = new Inflater(true);

    public InflateEntryInputStream(ZipEntry zipEntry, DataInputNew in) throws IOException {
        super(zipEntry, in);
    }

    @Override
    public int available() {
        int bytes = super.available();

        if (bytes == 0)
            return inflater.finished() ? 0 : 1;

        return bytes;
    }

    @Override
    public int read(byte[] buf, int offs, int len) throws IOException {
        try {
            int bytes;

            while ((bytes = inflater.inflate(buf, offs, len)) == 0) {
                if (inflater.finished() || inflater.needsDictionary())
                    return IOUtils.EOF;

                if (inflater.needsInput())
                    if (fill())
                        return IOUtils.EOF;
            }

            updateChecksum(buf, offs, bytes);
            writtenUncompressedBytes += bytes;
            return bytes;
        } catch(DataFormatException e) {
            throw new IOException(e);
        }
    }

    private boolean fill() throws IOException {
        int len = in.read(buf, 0, buf.length);

        if (len == IOUtils.EOF)
            return true;

        inflater.setInput(buf, 0, len);
        return false;
    }

    @Override
    public void close() throws IOException {
        inflater.end();
        super.close();
    }

}
