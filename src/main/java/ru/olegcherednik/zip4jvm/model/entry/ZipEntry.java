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
package ru.olegcherednik.zip4jvm.model.entry;

import ru.olegcherednik.zip4jvm.ZipFile;
import ru.olegcherednik.zip4jvm.crypto.Decoder;
import ru.olegcherednik.zip4jvm.crypto.Encoder;
import ru.olegcherednik.zip4jvm.io.in.data.DataInput;
import ru.olegcherednik.zip4jvm.model.CompressionLevel;
import ru.olegcherednik.zip4jvm.model.CompressionMethod;
import ru.olegcherednik.zip4jvm.model.EncryptionMethod;
import ru.olegcherednik.zip4jvm.model.ExternalFileAttributes;
import ru.olegcherednik.zip4jvm.model.InternalFileAttributes;
import ru.olegcherednik.zip4jvm.model.LocalFileHeader;
import ru.olegcherednik.zip4jvm.utils.function.ZipEntryInputStreamSupplier;
import ru.olegcherednik.zip4jvm.utils.quitely.Quietly;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.io.IOException;
import java.io.InputStream;
import java.util.Comparator;

/**
 * Represents one single entry in zip archive, i.e. one instance of {@link LocalFileHeader} and related to
 * {@link ru.olegcherednik.zip4jvm.model.CentralDirectory.FileHeader}. This entry belongs uses zip file's settings.
 *
 * @author Oleg Cherednik
 * @since 26.07.2019
 */
@Getter
@Setter
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@SuppressWarnings("UnnecessaryFullyQualifiedName")
public class ZipEntry {

    public static final Comparator<ZipEntry> SORT_BY_DISC_LOCAL_FILE_HEADER_OFFS =
            Comparator.comparingLong(ZipEntry::getDiskNo).thenComparing(ZipEntry::getLocalFileHeaderRelativeOffs);

    protected final String fileName;
    protected final int lastModifiedTime;
    protected final ExternalFileAttributes externalFileAttributes;

    protected final CompressionMethod compressionMethod;
    private final CompressionLevel compressionLevel;
    protected final EncryptionMethod encryptionMethod;
    @Getter(AccessLevel.NONE)
    private final ZipEntryInputStreamSupplier inputStreamSup;

    /**
     * {@literal true} only if section {@link ru.olegcherednik.zip4jvm.model.Zip64.ExtendedInfo} exists in
     * {@link LocalFileHeader} and
     * {@link ru.olegcherednik.zip4jvm.model.CentralDirectory.FileHeader}. In other words, do set this to {@code true},
     * to write given entry in
     * ZIP64 format.
     */
    private boolean zip64;

    private char[] password;
    private int diskNo;
    private long localFileHeaderRelativeOffs;
    private boolean dataDescriptorAvailable;
    private long uncompressedSize;
    private long compressedSize;
    private boolean lzmaEosMarker = true;

    private String comment;
    private boolean utf8;
    private boolean strongEncryption;

    public boolean isSymlink() {
        return externalFileAttributes.isSymlink();
    }

    public boolean isDirectory() {
        return false;
    }

    public boolean isRegularFile() {
        return false;
    }

    public final boolean isEncrypted() {
        return encryptionMethod != EncryptionMethod.OFF;
    }

    public InputStream getInputStream() {
        return Quietly.doQuietly(() -> inputStreamSup.get(this));
    }

    public CompressionMethod getCompressionMethodForBuilder() {
        return encryptionMethod.isAes() ? CompressionMethod.AES : compressionMethod;
    }

    @Override
    public String toString() {
        return fileName;
    }

    public InternalFileAttributes getInternalFileAttributes() throws IOException {
        return new InternalFileAttributes();
    }

    public long getChecksum() {
        return 0;
    }

    public void setChecksum(long checksum) {
        /* nothing to set */
    }

    public ZipFile.Entry createImmutableEntry() {
        if (isDirectory())
            return ZipFile.Entry.directory(fileName, lastModifiedTime, externalFileAttributes);

        return ZipFile.Entry.regularFile(this::getInputStream,
                                         fileName,
                                         lastModifiedTime,
                                         uncompressedSize,
                                         externalFileAttributes);
    }

    public Decoder createDecoder(DataInput in) {
        return Decoder.NULL;
    }

    public Encoder createEncoder() {
        return Encoder.NULL;
    }

}
