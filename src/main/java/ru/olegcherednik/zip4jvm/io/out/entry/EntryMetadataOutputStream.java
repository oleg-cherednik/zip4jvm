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
package ru.olegcherednik.zip4jvm.io.out.entry;

import ru.olegcherednik.zip4jvm.io.out.data.DataOutput;
import ru.olegcherednik.zip4jvm.model.DataDescriptor;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;

import java.io.IOException;
import java.io.OutputStream;

/**
 * This stream writes all {@link ZipEntry} related metadata like {@link DataDescriptor}. These data are not encrypted;
 * therefore this stream cannot be used to write {@link ZipEntry} payload (that could be encrypted).
 *
 * @author Oleg Cherednik
 * @since 26.07.2019
 */
public final class EntryMetadataOutputStream extends OutputStream {

    public static final String COMPRESSED_DATA =
            EntryMetadataOutputStream.class.getSimpleName() + ".entryCompressedDataOffs";


    private final ZipEntry zipEntry;
    private final DataOutput out;

    public EntryMetadataOutputStream(ZipEntry zipEntry, DataOutput out) {
        this.zipEntry = zipEntry;
        this.out = out;
    }

//    public void writeLocalFileHeader() throws IOException {
//        zipEntry.setLocalFileHeaderRelativeOffs(out.getRelativeOffs());
//        LocalFileHeader localFileHeader = new LocalFileHeaderBuilder(zipEntry).build();
//        new LocalFileHeaderWriter(localFileHeader).write(out);
//        out.mark(COMPRESSED_DATA);
//    }

    @Override
    public void write(int b) throws IOException {
    }

    @Override
    public void write(byte[] buf, int offs, int len) throws IOException {
    }

    @Override
    public void close() throws IOException {
        //zipEntry.setCompressedSize(out.getWrittenBytesAmount(COMPRESSED_DATA));
    }

    @Override
    public String toString() {
        return out.toString();
    }

}
