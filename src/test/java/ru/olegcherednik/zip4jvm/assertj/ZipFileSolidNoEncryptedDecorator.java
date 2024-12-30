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
package ru.olegcherednik.zip4jvm.assertj;

import ru.olegcherednik.zip4jvm.ZipInfo;
import ru.olegcherednik.zip4jvm.model.CentralDirectory;
import ru.olegcherednik.zip4jvm.utils.quitely.Quietly;

import com.github.luben.zstd.Zstd;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.compress.archivers.zip.ZipMethod;
import org.apache.commons.io.IOUtils;
import org.tukaani.xz.LZMAInputStream;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Path;
import java.util.zip.ZipEntry;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Oleg Cherednik
 * @since 03.10.2019
 */
@SuppressWarnings({ "MagicConstant", "AnonInnerLength" })
class ZipFileSolidNoEncryptedDecorator extends ZipFileDecorator {

    private static final int METHOD_ZSTD = 93;
    private static final int HEADER_SIZE = 5;

    ZipFileSolidNoEncryptedDecorator(Path zip) {
        super(zip);
    }

    @Override
    @SuppressWarnings("PMD.ExceptionAsFlowControl")
    public InputStream getInputStream(ZipEntry zipEntry) {
        return Quietly.doRuntime(() -> new ZipFileInputStream(zip, zipEntry));
    }

    private static final class ZipFileInputStream extends InputStream {

        private final ZipFile zipFile;
        private final InputStream delegate;

        ZipFileInputStream(Path zip, ZipEntry zipEntry) throws IOException {
            zipFile = ZipFile.builder()
                             .setFile(zip.toFile())
                             .get();
            delegate = createDelegate(zipFile, zip, zipEntry);
        }

        private static InputStream createDelegate(ZipFile zipFile, Path zip, ZipEntry zipEntry) throws IOException {
            ZipArchiveEntry zipArchiveEntry = zipFile.getEntry(zipEntry.getName());

            if (zipFile.canReadEntryData(zipArchiveEntry))
                return zipFile.getInputStream(zipArchiveEntry);
            if (zipArchiveEntry.getMethod() == ZipMethod.LZMA.getCode())
                return createLzmaDelegate(zipArchiveEntry, zipFile, zip);
            if (zipArchiveEntry.getMethod() == METHOD_ZSTD)
                return createZstdDelegate(zipArchiveEntry, zipFile);
            if (zipArchiveEntry.getMethod() == ZipMethod.AES_ENCRYPTED.getCode())
                throw new UnsupportedOperationException(
                        "ZipEntry password id not correct: " + zipArchiveEntry.getName());

            throw new UnsupportedOperationException("ZipEntry data can't be read: " + zipArchiveEntry.getName());
        }

        private static InputStream createLzmaDelegate(ZipArchiveEntry zipArchiveEntry, ZipFile zipFile, Path zip)
                throws IOException {
            InputStream in = zipFile.getRawInputStream(zipArchiveEntry);
            ByteBuffer buffer = ByteBuffer.wrap(IOUtils.readFully(in, 9)).order(ByteOrder.LITTLE_ENDIAN);

            buffer.get();    // majorVersion
            buffer.get();    // minorVersion
            int size = buffer.getShort() & 0xFFFF;

            if (size != HEADER_SIZE)
                throw new UnsupportedOperationException(
                        String.format("ZipEntry LZMA should have size %d in header: %s",
                                      HEADER_SIZE, zipArchiveEntry.getName()));

            CentralDirectory.FileHeader fileHeader = ZipInfo.zip(zip).getFileHeader(zipArchiveEntry.getName());
            boolean lzmaEosMarker = fileHeader.getGeneralPurposeFlag().isLzmaEosMarker();
            long uncompSize = lzmaEosMarker ? -1 : fileHeader.getUncompressedSize();
            byte propByte = buffer.get();
            int dictSize = buffer.getInt();
            return new LZMAInputStream(in, uncompSize, propByte, dictSize);
        }

        private static InputStream createZstdDelegate(ZipArchiveEntry zipArchiveEntry, ZipFile zipFile)
                throws IOException {
            try (InputStream in = zipFile.getRawInputStream(zipArchiveEntry)) {
                byte[] compressed = IOUtils.toByteArray(in);
                byte[] decompressed = new byte[(int) zipArchiveEntry.getSize()];
                long total = Zstd.decompressByteArray(decompressed, 0, decompressed.length,
                                                      compressed, 0, compressed.length);

                assertThat(total).isEqualTo(decompressed.length);

                return new ByteArrayInputStream(decompressed);
            }
        }

        // ---------- InputStream ----------

        @Override
        public int read() throws IOException {
            return delegate.read();
        }

        // ---------- AutoCloseable ----------

        @Override
        public void close() throws IOException {
            delegate.close();
            zipFile.close();
        }

    }

}
