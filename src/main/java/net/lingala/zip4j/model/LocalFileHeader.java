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
import org.apache.commons.lang.ArrayUtils;

import java.nio.charset.Charset;

/**
 * see 4.3.7
 *
 * @author Oleg Cherednik
 * @since 12.03.2019
 */
@Getter
@Setter
public class LocalFileHeader {

    public static final int SIGNATURE = 0x04034B50;

    // size:4 - signature (0x04034b50)
    private final int signature = SIGNATURE;
    // size:2 - version needed to extractEntries
    private int versionToExtract;
    // size:2 - general purpose bit flag
    private final GeneralPurposeFlag generalPurposeFlag = new GeneralPurposeFlag();
    // size:2 - compression method
    @NonNull
    private CompressionMethod compressionMethod = CompressionMethod.STORE;
    // size:2 - last mod file time
    // size:2 - ast mod file date
    private int lastModifiedTime;
    // size:4 - crc-32
    private long crc32;
    // size:4 - compressed size
    private long compressedSize;
    // size:4 - uncompressed size
    private long uncompressedSize;
    // size:2 - file name length (n)
//    private int fileNameLength;
    // size:2 - extra field length (m)
//    private int extraFieldLength;
    // size:n - file name
    private String fileName;
    // size:m - extra field
    @NonNull
    private ExtraField extraField = ExtraField.NULL;

    private long offs;

    @NonNull
    public byte[] getFileName(@NonNull Charset charset) {
        return fileName != null ? fileName.getBytes(charset) : ArrayUtils.EMPTY_BYTE_ARRAY;
    }

    public void setExtraField(@NonNull ExtraField extraField) {
        this.extraField = extraField;
        generalPurposeFlag.setEncrypted(isEncrypted());
    }

    public void setGeneralPurposeFlag(short data) {
        generalPurposeFlag.setData(data);
        generalPurposeFlag.setEncrypted(getEncryption() != Encryption.OFF);
    }

    public boolean isEncrypted() {
        return getEncryption() != Encryption.OFF;
    }

    public Encryption getEncryption() {
        if (extraField.getAesExtraDataRecord() != AESExtraDataRecord.NULL)
            return Encryption.AES;
        if (generalPurposeFlag.isStrongEncryption())
            return Encryption.STRONG;
        return generalPurposeFlag.isEncrypted() ? Encryption.STANDARD : Encryption.OFF;
    }

}
