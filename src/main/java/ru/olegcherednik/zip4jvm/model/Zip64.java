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

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.ArrayUtils;
import ru.olegcherednik.zip4jvm.crypto.strong.EncryptionAlgorithm;
import ru.olegcherednik.zip4jvm.crypto.strong.Flags;
import ru.olegcherednik.zip4jvm.crypto.strong.HashAlgorithm;
import ru.olegcherednik.zip4jvm.io.out.data.DataOutput;
import ru.olegcherednik.zip4jvm.model.extrafield.PkwareExtraField;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 26.04.2019
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class Zip64 {

    public static final Zip64 NULL = new Zip64(null, null, null);
    public static final int LIMIT_WORD = 0xFFFF;
    public static final long LIMIT_DWORD = 0xFFFF_FFFFL;

    private final EndCentralDirectoryLocator endCentralDirectoryLocator;
    private final EndCentralDirectory endCentralDirectory;
    private final ExtensibleDataSector extensibleDataSector;

    public static Zip64 of(EndCentralDirectoryLocator endCentralDirectoryLocator,
                           EndCentralDirectory endCentralDirectory,
                           ExtensibleDataSector extensibleDataSector) {
        if (endCentralDirectoryLocator == null)
            return NULL;

        return new Zip64(endCentralDirectoryLocator, endCentralDirectory, extensibleDataSector);
    }

    public boolean isCentralDirectoryEncrypted() {
        return extensibleDataSector != null;
    }

    @Override
    public String toString() {
        return this == NULL ? "<null>" : super.toString();
    }

    /** see 4.3.15 */
    @Getter
    @Setter
    public static class EndCentralDirectoryLocator {

        public static final int SIGNATURE = 0x07064B50;

        // size (20) with comment length = 0
        public static final int SIZE = 4 + 4 + 8 + 4;

        // size:4 - signature (0x06054b50)
        // size:4 - number of the disk with the start of the zip64 end of central directory
        private long mainDiskNo;
        // size:8 - relative offset of the Zip64.EndCentralDirectory
        private long endCentralDirectoryRelativeOffs;
        // size:4 - total number of disks (=1 - single zip; >1 - split zip (e.g. 5 means 5 total parts)
        private long totalDisks;

    }

    /** see 4.3.14 */
    @Getter
    @Setter
    public static class EndCentralDirectory {

        public static final int SIGNATURE = 0x06064B50;
        // size (44) with extensibleDataSector length = 0
        public static final int SIZE = 2 + 2 + 4 + 4 + 8 + 8 + 8 + 8;

        // size:4 - signature (0x06064b50)
        // size:8 - directory record (n)
        private long endCentralDirectorySize;
        // size:2 - version made by
        private Version versionMadeBy = Version.NULL;
        // size:2 - version needed to extractEntries
        private Version versionToExtract = Version.NULL;
        // size:4 - number of this disk
        private long diskNo;
        // size:4 - number of the disk with the start of the central directory
        private long mainDiskNo;
        // size:8 - total number of entries in the central directory on this disk
        private long diskEntries;
        // size:8 - total number of entries in the central directory
        private long totalEntries;
        // size:8 - size of the central directory
        private long centralDirectorySize;
        // size:8 - offs of CentralDirectory in startDiskNumber
        private long centralDirectoryRelativeOffs;

    }

    /** see 4.5.3 */
    @Getter
    public static final class ExtendedInfo implements PkwareExtraField.Record {

        public static final ExtendedInfo NULL = new ExtendedInfo(new Builder());

        public static final int SIGNATURE = 0x0001;
        public static final int SIZE_FIELD = 2 + 2; // 4 bytes: signature + size

        // size:2 - tag for this "extra" block type (ZIP64 = 0x001)
        // size:2 - size of this "extra" block
        // size:8 - original uncompressed file size
        private final long uncompressedSize;
        // size:8 - size of compressed data
        private final long compressedSize;
        // size:8 - offset of local header record
        private final long localFileHeaderRelativeOffs;
        // size:4 - number of the disk on which  this file starts
        private final long diskNo;

        public static Builder builder() {
            return new Builder();
        }

        private ExtendedInfo(Builder builder) {
            uncompressedSize = builder.uncompressedSize;
            compressedSize = builder.compressedSize;
            localFileHeaderRelativeOffs = builder.localFileHeaderRelativeOffs;
            diskNo = builder.diskNo;
        }

        public int getDataSize() {
            int size = 0;

            if (uncompressedSize != PkwareExtraField.NO_DATA)
                size += 8;
            if (compressedSize != PkwareExtraField.NO_DATA)
                size += 8;
            if (localFileHeaderRelativeOffs != PkwareExtraField.NO_DATA)
                size += 8;
            if (diskNo != PkwareExtraField.NO_DATA)
                size += 4;

            return size;
        }

        @Override
        public int getBlockSize() {
            return isNull() ? 0 : getDataSize() + SIZE_FIELD;
        }

        @Override
        public int getSignature() {
            return SIGNATURE;
        }

        @Override
        public boolean isNull() {
            return this == NULL;
        }

        @Override
        public String getTitle() {
            return "Zip64 Extended Information";
        }

        @Override
        public String toString() {
            return isNull() ? "<null>" : super.toString();
        }

        @Override
        public void write(DataOutput out) throws IOException {
            if (isNull())
                return;

            out.writeWordSignature(SIGNATURE);
            out.writeWord(getDataSize());

            if (getUncompressedSize() != PkwareExtraField.NO_DATA)
                out.writeQword(getUncompressedSize());
            if (getCompressedSize() != PkwareExtraField.NO_DATA)
                out.writeQword(getCompressedSize());
            if (getLocalFileHeaderRelativeOffs() != PkwareExtraField.NO_DATA)
                out.writeQword(getLocalFileHeaderRelativeOffs());
            if (getDiskNo() != PkwareExtraField.NO_DATA)
                out.writeDword(getDiskNo());
        }

        @NoArgsConstructor(access = AccessLevel.PRIVATE)
        public static final class Builder {

            private long uncompressedSize = PkwareExtraField.NO_DATA;
            private long compressedSize = PkwareExtraField.NO_DATA;
            private long localFileHeaderRelativeOffs = PkwareExtraField.NO_DATA;
            private long diskNo = PkwareExtraField.NO_DATA;

            public ExtendedInfo build() {
                if (uncompressedSize == PkwareExtraField.NO_DATA && compressedSize == PkwareExtraField.NO_DATA
                        && localFileHeaderRelativeOffs == PkwareExtraField.NO_DATA && diskNo == PkwareExtraField.NO_DATA)
                    return NULL;
                return new ExtendedInfo(this);
            }

            public Builder uncompressedSize(long uncompressedSize) {
                this.uncompressedSize = uncompressedSize;
                return this;
            }

            public Builder compressedSize(long compressedSize) {
                this.compressedSize = compressedSize;
                return this;
            }

            public Builder localFileHeaderRelativeOffs(long localFileHeaderRelativeOffs) {
                this.localFileHeaderRelativeOffs = localFileHeaderRelativeOffs;
                return this;
            }

            public Builder diskNo(long diskNo) {
                this.diskNo = diskNo;
                return this;
            }

        }

    }

    /** see 7.3.4 */
    @Getter
    public static final class ExtensibleDataSector {

        // size:2 - compression method
        private final CompressionMethod compressionMethod;
        // size:8 - size of compressed data
        private final long compressedSize;
        // size:8 - original uncompressed file size
        private final long uncompressedSize;
        // size:2 - encryption algorithm
        private final int encryptionAlgorithmCode;
        private final EncryptionAlgorithm encryptionAlgorithm;
        // size:2 - encryption key length
        private final int bitLength;
        // size:2 - encryption flags
        private final Flags flags;
        // size:2 - hash algorithm identifier
        private final int hashAlgorithmCode;
        private final HashAlgorithm hashAlgorithm;
        // size:2 - length of hash data (m)
        private final int hashLength;
        // size:m - hash data
        private final byte[] hashData;

        public static Builder builder() {
            return new Builder();
        }

        private ExtensibleDataSector(Builder builder) {
            compressionMethod = builder.compressionMethod;
            compressedSize = builder.compressedSize;
            uncompressedSize = builder.uncompressedSize;
            encryptionAlgorithmCode = builder.encryptionAlgorithmCode;
            encryptionAlgorithm = builder.encryptionAlgorithm;
            bitLength = builder.bitLength;
            flags = builder.flags;
            hashAlgorithmCode = builder.hashAlgorithmCode;
            hashAlgorithm = builder.hashAlgorithm;
            hashLength = builder.hashLength;
            hashData = builder.hashData;
        }

        @SuppressWarnings("MethodCanBeVariableArityMethod")
        @NoArgsConstructor(access = AccessLevel.PRIVATE)
        public static final class Builder {

            private CompressionMethod compressionMethod = CompressionMethod.STORE;
            private long compressedSize;
            private long uncompressedSize;
            private int encryptionAlgorithmCode = EncryptionAlgorithm.UNKNOWN.getCode();
            private EncryptionAlgorithm encryptionAlgorithm = EncryptionAlgorithm.UNKNOWN;
            private int bitLength;
            private Flags flags;
            private int hashAlgorithmCode = HashAlgorithm.UNKNOWN.getCode();
            private HashAlgorithm hashAlgorithm = HashAlgorithm.UNKNOWN;
            private int hashLength;
            private byte[] hashData;

            public ExtensibleDataSector build() {
                return new ExtensibleDataSector(this);
            }

            public Builder compressionMethod(CompressionMethod compressionMethod) {
                this.compressionMethod = compressionMethod;
                return this;
            }

            public Builder compressedSize(long compressedSize) {
                this.compressedSize = compressedSize;
                return this;
            }

            public Builder uncompressedSize(long uncompressedSize) {
                this.uncompressedSize = uncompressedSize;
                return this;
            }

            public Builder encryptionAlgorithm(int code) {
                encryptionAlgorithmCode = code;
                encryptionAlgorithm = EncryptionAlgorithm.parseCode(code);
                return this;
            }

            public Builder bitLength(int bitLength) {
                this.bitLength = bitLength;
                return this;
            }

            public Builder flags(Flags flags) {
                this.flags = flags;
                return this;
            }

            public Builder hashAlgorithm(int code) {
                hashAlgorithmCode = code;
                hashAlgorithm = HashAlgorithm.parseCode(code);
                return this;
            }

            public Builder hashLength(int hashLength) {
                this.hashLength = hashLength;
                return this;
            }

            public Builder hashData(byte[] hashData) {
                this.hashData = ArrayUtils.clone(hashData);
                return this;
            }
        }
    }

}
