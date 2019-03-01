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

    /* size:4 - local file header signature (0x04034b50) */
    private int signature;
    /* size:2 - version needed to extract */
    private int versionNeededToExtract;
    /* size:2 - general purpose bit flag */
    private byte[] generalPurposeFlag;
    /* size:2 - compression method */
    private int compressionMethod;
    /* size:2 - last mod file time */
    /* size:2 - ast mod file date */
    private int lastModFileTime;
    /* size:4 - crc-32 */
    private long crc32;
    /* size:4 - compressed size */
    private long compressedSize;
    /* size:4 - uncompressed size */
    private long uncompressedSize;
    /* size:2 - file name length (n) */
    private int fileNameLength;
    /* size:2 - extra field length (m) */
    private int extraFieldLength;
    /* size:n - File name */
    private String fileName;
    /* size:m - extra field */
    private byte[] extraField;

    // ----

    private long offsetStartOfData;
    private boolean isEncrypted;
    private int encryptionMethod = -1;
    private char[] password;
    private List<ExtraDataRecord> extraDataRecords;
    private Zip64ExtendedInfo zip64ExtendedInfo;
    private AESExtraDataRecord aesExtraDataRecord;
    private boolean dataDescriptorExists;
    private boolean writeComprSizeInZip64ExtraRecord;
    private boolean fileNameUTF8Encoded;
    private byte[] crcBuff;

}
