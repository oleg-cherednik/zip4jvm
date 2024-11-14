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
package ru.olegcherednik.zip4jvm.io.in.data.compressed;

import ru.olegcherednik.zip4jvm.exception.Zip4jvmException;
import ru.olegcherednik.zip4jvm.io.in.data.DataInput;
import ru.olegcherednik.zip4jvm.io.readers.DataDescriptorReader;
import ru.olegcherednik.zip4jvm.model.DataDescriptor;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

/**
 * This stream reads all {@link ZipEntry} related metadata like {@link DataDescriptor}. These data are not encrypted;
 * therefore this stream cannot be used to read {@link ZipEntry} payload (that could be encrypted).
 *
 * @author Oleg Cherednik
 * @since 08.02.2020
 */
abstract class EntryMetadataInputStream extends InputStream {

    private final DataInput in;

    protected final ZipEntry zipEntry;
    protected final long uncompressedSize;

    private final Checksum checksum = new CRC32();

    protected long writtenUncompressedBytes;

    protected EntryMetadataInputStream(DataInput in, ZipEntry zipEntry) {
        this.in = in;
        this.zipEntry = zipEntry;
        uncompressedSize = Math.max(0, zipEntry.getUncompressedSize());
    }

    protected final void updateChecksum(byte[] buf, int offs, int len) {
        checksum.update(buf, offs, len);
    }

    @Override
    public int available() {
        return (int) Math.max(0, uncompressedSize - writtenUncompressedBytes);
    }

    @Override
    @SuppressWarnings("PMD.UseTryWithResources")
    public void close() throws IOException {
        try {
            readDataDescriptor();
            checkChecksum();
            checkUncompressedSize();
        } finally {
            if (in instanceof Closeable)
                ((Closeable) in).close();
        }
    }

    /**
     * Just read {@link DataDescriptor} and ignore its value. We got it from
     * {@link ru.olegcherednik.zip4jvm.model.CentralDirectory.FileHeader}
     */
    @SuppressWarnings("UnnecessaryFullyQualifiedName")
    private void readDataDescriptor() throws IOException {
        if (zipEntry.isDataDescriptorAvailable())
            DataDescriptorReader.get(zipEntry.isZip64()).read(in);
    }

    private void checkChecksum() {
        long expected = zipEntry.getChecksum();
        long actual = checksum.getValue();

        if (expected > 0 && expected != actual)
            throw new Zip4jvmException("Checksum is not matched: " + zipEntry.getFileName());
    }

    private void checkUncompressedSize() {
        if (uncompressedSize != writtenUncompressedBytes)
            throw new Zip4jvmException("UncompressedSize is not matched: " + zipEntry.getFileName());
    }

    @Override
    public String toString() {
        return in.toString();
    }

}
