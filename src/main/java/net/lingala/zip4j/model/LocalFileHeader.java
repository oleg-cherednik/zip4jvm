/*
 * Copyright 2010 Srikanth Reddy Lingala
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.lingala.zip4j.model;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import net.lingala.zip4j.util.BitUtils;
import net.lingala.zip4j.util.InternalZipConstants;

import java.util.Collections;
import java.util.Map;

import static net.lingala.zip4j.util.BitUtils.BIT0;

@Getter
@Setter
public class LocalFileHeader {

    // size:4 - signature (0x04034b50)
    private final int signature = InternalZipConstants.LOCSIG;
    // size:2 - version needed to extract
    private int versionNeededToExtract;
    // size:2 - general purpose bit flag
    private byte[] generalPurposeFlag;
    // size:2 - compression method
    @NonNull
    private CompressionMethod compressionMethod = CompressionMethod.STORE;
    // size:2 - last mod file time
    // size:2 - ast mod file date
    private int lastModFileTime;
    // size:4 - crc-32
    private long crc32;
    // size:4 - compressed size
    private long compressedSize;
    // size:4 - uncompressed size
    private long uncompressedSize;
    // size:2 - file name length (n)
    private int fileNameLength;
    // size:2 - extra field length (m)
    private int extraFieldLength;
    // size:n - file name
    private String fileName;
    // size:m - extra field
    @NonNull
    private Map<Short, ExtraDataRecord> extraDataRecords = Collections.emptyMap();

    // ----

    private long offsetStartOfData;
    @NonNull
    private Encryption encryption = Encryption.OFF;
    private char[] password;
    private Zip64ExtendedInfo zip64ExtendedInfo;
    private AESExtraDataRecord aesExtraDataRecord;
    private boolean writeComprSizeInZip64ExtraRecord;
    private boolean fileNameUTF8Encoded;
    private byte[] crcBuff;

    public short getExtraFileLength(ZipModel model) {
        short extraFieldLength = 0;

        if (model.isZip64Format())
            extraFieldLength += 20;
        if (aesExtraDataRecord != null)
            extraFieldLength += 11;

        return extraFieldLength;
    }

    public void setZip64ExtendedInfo(Zip64ExtendedInfo info) {
        zip64ExtendedInfo = info;

        if (info != null) {
            uncompressedSize = info.getUnCompressedSize() != -1 ? info.getUnCompressedSize() : uncompressedSize;
            compressedSize = info.getCompressedSize() != -1 ? info.getCompressedSize() : uncompressedSize;
        }
    }

    public void setAesExtraDataRecord(AESExtraDataRecord record) {
        aesExtraDataRecord = record;
        encryption = record != null ? Encryption.AES : Encryption.OFF;
        generalPurposeFlag = generalPurposeFlag != null ? generalPurposeFlag : new byte[2];
        generalPurposeFlag[0] = (byte)BitUtils.updateBits(generalPurposeFlag[0], BIT0, encryption != Encryption.OFF);
    }

    public ExtraDataRecord getExtraDataRecordByHeader(short header) {
        return extraDataRecords.get(header);
    }

    @Getter
    @Setter
    public static final class GeneralPurposeFlag {

        private boolean bit0;
        private boolean bit1;
        private boolean bit2;
        private boolean bit3;
    }

}
