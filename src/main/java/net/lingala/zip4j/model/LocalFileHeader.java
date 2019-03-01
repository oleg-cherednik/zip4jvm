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
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class LocalFileHeader {

    private int signature;
    private int versionNeededToExtract;
    private byte[] generalPurposeFlag;
    private int compressionMethod;
    private int lastModFileTime;
    private long crc32;
    private byte[] crcBuff;
    private long compressedSize;
    private long uncompressedSize;
    private int fileNameLength;
    private int extraFieldLength;
    private String fileName;
    private byte[] extraField;
    private long offsetStartOfData;
    private boolean isEncrypted;
    private int encryptionMethod;
    private char[] password;
    private List<ExtraDataRecord> extraDataRecords;
    private Zip64ExtendedInfo zip64ExtendedInfo;
    private AESExtraDataRecord aesExtraDataRecord;
    private boolean dataDescriptorExists;
    private boolean writeComprSizeInZip64ExtraRecord;
    private boolean fileNameUTF8Encoded;

    public LocalFileHeader() {
        encryptionMethod = -1;
        writeComprSizeInZip64ExtraRecord = false;
        crc32 = 0;
        uncompressedSize = 0;
    }

}
