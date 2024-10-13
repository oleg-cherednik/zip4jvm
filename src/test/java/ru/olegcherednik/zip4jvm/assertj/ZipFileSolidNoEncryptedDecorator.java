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
import ru.olegcherednik.zip4jvm.exception.Zip4jvmException;
import ru.olegcherednik.zip4jvm.model.CentralDirectory;

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
    public InputStream getInputStream(ZipEntry entry) {
        try {
            return new InputStream() {
                private final ZipFile zipFile = new ZipFile(zip.toFile());
                private final InputStream delegate;

                {
                    ZipArchiveEntry zipEntry = zipFile.getEntry(entry.getName());

                    if (zipFile.canReadEntryData(zipEntry))
                        delegate = zipFile.getInputStream(zipEntry);
                    else if (zipEntry.getMethod() == ZipMethod.LZMA.getCode()) {
                        InputStream in = zipFile.getRawInputStream(zipEntry);
                        ByteBuffer buffer = ByteBuffer.wrap(IOUtils.readFully(in, 9)).order(ByteOrder.LITTLE_ENDIAN);

                        buffer.get();    // majorVersion
                        buffer.get();    // minorVersion
                        int size = buffer.getShort() & 0xFFFF;

                        if (size != HEADER_SIZE)
                            throw new UnsupportedOperationException(
                                    String.format("ZipEntry LZMA should have size %d in header: %s",
                                                  HEADER_SIZE, zipEntry.getName()));

                        CentralDirectory.FileHeader fileHeader = ZipInfo.zip(zip).getFileHeader(zipEntry.getName());
                        boolean lzmaEosMarker = fileHeader.getGeneralPurposeFlag().isLzmaEosMarker();
                        long uncompSize = lzmaEosMarker ? -1 : fileHeader.getUncompressedSize();
                        byte propByte = buffer.get();
                        int dictSize = buffer.getInt();
                        delegate = new LZMAInputStream(in, uncompSize, propByte, dictSize);
                    } else if (zipEntry.getMethod() == METHOD_ZSTD) {
                        InputStream in = zipFile.getRawInputStream(zipEntry);
                        byte[] compressed = IOUtils.toByteArray(in);
                        byte[] decompressed = new byte[(int) zipEntry.getSize()];
                        long total = Zstd.decompressByteArray(decompressed,
                                                              0,
                                                              decompressed.length,
                                                              compressed,
                                                              0,
                                                              compressed.length);

                        assertThat(total).isEqualTo(decompressed.length);

                        delegate = new ByteArrayInputStream(decompressed);
                    } else
                        throw new UnsupportedOperationException("ZipEntry data can't be read: " + zipEntry.getName());
                }

                @Override
                public int read() throws IOException {
                    return delegate.read();
                }

                @Override
                public void close() throws IOException {
                    delegate.close();
                    zipFile.close();
                }
            };
        } catch (Zip4jvmException e) {
            throw e;
        } catch (Exception e) {
            throw new Zip4jvmException(e);
        }
    }

}
