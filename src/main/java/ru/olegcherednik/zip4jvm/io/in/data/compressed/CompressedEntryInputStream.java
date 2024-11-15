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

import ru.olegcherednik.zip4jvm.crypto.Decoder;
import ru.olegcherednik.zip4jvm.exception.CompressionNotSupportedException;
import ru.olegcherednik.zip4jvm.io.in.data.DataInput;
import ru.olegcherednik.zip4jvm.io.in.data.DecoderDataInput;
import ru.olegcherednik.zip4jvm.io.readers.LocalFileHeaderReader;
import ru.olegcherednik.zip4jvm.model.CompressionMethod;
import ru.olegcherednik.zip4jvm.model.LocalFileHeader;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.function.Function;

/**
 * This stream is responsible to read {@link ZipEntry} data. It could be encrypted; therefore all read data should be go
 * throw given {@link Decoder}.
 *
 * @author Oleg Cherednik
 * @since 04.08.2019
 */
public abstract class CompressedEntryInputStream extends EntryMetadataInputStream {

    protected final DecoderDataInput in;

    private final byte[] buf = new byte[1];

    public static DataInput create(ZipEntry zipEntry,
                                   Function<Charset, Charset> charsetCustomizer,
                                   DataInput in) {
        long absOffs = in.convertToAbsoluteOffs(zipEntry.getDiskNo(), zipEntry.getLocalFileHeaderRelativeOffs());

        LocalFileHeader localFileHeader = new LocalFileHeaderReader(absOffs, charsetCustomizer).read(in);
        zipEntry.setDataDescriptorAvailable(localFileHeader.getGeneralPurposeFlag().isDataDescriptorAvailable());
        // TODO check that localFileHeader matches fileHeader
        CompressionMethod compressionMethod = zipEntry.getCompressionMethod();

        if (compressionMethod == CompressionMethod.STORE)
            return new StoreEntryInputStream(in, zipEntry);
        if (compressionMethod == CompressionMethod.DEFLATE)
            return new InflateEntryInputStream(in, zipEntry);
        if (compressionMethod == CompressionMethod.ENHANCED_DEFLATE)
            return new EnhancedDeflateEntryInputStream(in, zipEntry);
        if (compressionMethod == CompressionMethod.BZIP2)
            return new Bzip2EntryInputStream(in, zipEntry);
        if (compressionMethod == CompressionMethod.LZMA)
            return new LzmaEntryInputStream(in, zipEntry);
        if (compressionMethod == CompressionMethod.ZSTD)
            return new ZstdEntryInputStream(in, zipEntry);

        throw new CompressionNotSupportedException(compressionMethod);
    }

    protected CompressedEntryInputStream(DataInput in, ZipEntry zipEntry) {
        super(in, zipEntry);
        Decoder decoder = zipEntry.createDecoder(in);
        long compressedSize = decoder == Decoder.NULL ? zipEntry.getCompressedSize() : decoder.getCompressedSize();
        this.in = new DecoderDataInput(in, decoder, compressedSize);
    }

    @Override
    public final int read() throws IOException {
        int len = read(buf, 0, 1);
        return len == IOUtils.EOF ? IOUtils.EOF : buf[0] & 0xFF;
    }

    @Override
    public void close() throws IOException {
        in.decodingAccomplished();
        super.close();
    }

}
