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
package ru.olegcherednik.zip4jvm.model;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.ArrayUtils;
import ru.olegcherednik.zip4jvm.crypto.strong.DecryptionHeader;

import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;

/**
 * see 4.3.12
 *
 * @author Oleg Cherednik
 * @since 05.03.2019
 */
@Getter
@Setter
public class CentralDirectory {

    private List<FileHeader> fileHeaders = Collections.emptyList();
    private DecryptionHeader decryptionHeader;
    private DigitalSignature digitalSignature;

    /** see 4.3.12 */
    @Getter
    @Setter
    public static class FileHeader {

        public static final int SIGNATURE = 0x02014B50;

        // size:4 - signature (0x02014b50)
        // size:2 - version made by
        private Version versionMadeBy = Version.NULL;
        // size:2 - version needed to extractEntries
        private Version versionToExtract = Version.NULL;
        // size:2 - general purpose bit flag
        private GeneralPurposeFlag generalPurposeFlag = new GeneralPurposeFlag();
        // size:2 - compression method
        private CompressionMethod compressionMethod = CompressionMethod.STORE;
        // size:2 - last mod file time
        // size:2 - last mod file date
        private int lastModifiedTime;
        // size:4 - checksum
        private long crc32;
        // size:4 - compressed size
        private long compressedSize;
        // size:4 - uncompressed size
        private long uncompressedSize;
        // size:2 - file name length (n)
        // size:2 - extra field length (m)
        // size:2 - comment length (k)
        private int commentLength;
        // size:2 - disk number start
        private int diskNo;
        // size:2 - internal file attributes
        private final InternalFileAttributes internalFileAttributes = new InternalFileAttributes();
        // size:4 - external file attributes
        private ExternalFileAttributes externalFileAttributes = ExternalFileAttributes.NULL;
        // size:4 - relative offset of local header
        private long localFileHeaderRelativeOffs;
        // size:n - file name
        private String fileName;
        // size:m - extra field
        private ExtraField extraField = ExtraField.NULL;
        // size:k - comment
        private String comment;

        public byte[] getFileName(Charset charset) {
            return fileName == null ? ArrayUtils.EMPTY_BYTE_ARRAY : fileName.getBytes(charset);
        }

        public byte[] getComment(Charset charset) {
            return comment == null ? ArrayUtils.EMPTY_BYTE_ARRAY : comment.getBytes(charset);
        }

        public CompressionMethod getOriginalCompressionMethod() {
            return compressionMethod == CompressionMethod.AES ? extraField.getAesRecord().getCompressionMethod() : compressionMethod;
        }

        public boolean isZip64() {
            return extraField.getExtendedInfo() != Zip64.ExtendedInfo.NULL;
        }

        public void setInternalFileAttributes(InternalFileAttributes internalFileAttributes) {
            this.internalFileAttributes.readFrom(internalFileAttributes);
        }

        public void setExtraField(ExtraField extraField) {
            this.extraField = ExtraField.builder().addRecord(extraField).build();
            generalPurposeFlag.setEncrypted(isEncrypted());
        }

        public void setGeneralPurposeFlagData(int data) {
            generalPurposeFlag.read(data);
            generalPurposeFlag.setEncrypted(isEncrypted());
        }

        public boolean isEncrypted() {
            return generalPurposeFlag.isEncrypted();
        }

        public EncryptionMethod getEncryptionMethod() {
            return EncryptionMethod.get(extraField, generalPurposeFlag);
        }

        public boolean isWriteZip64OffsetLocalHeader() {
            return localFileHeaderRelativeOffs > Zip64.LIMIT_DWORD;
        }

        @Override
        public String toString() {
            return fileName;
        }

    }

    /** see 4.3.13 */
    @Getter
    @Setter
    public static class DigitalSignature {

        public static final int SIGNATURE = 0x05054B50;

        // size:4 - header signature (0x06054b50)
        // size:2 - size of data (n)
        // size:n - signature data
        private byte[] signatureData;

    }

}
