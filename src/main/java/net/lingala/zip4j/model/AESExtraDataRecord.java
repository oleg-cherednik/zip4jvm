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

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import net.lingala.zip4j.exception.ZipException;
import org.apache.commons.lang.ArrayUtils;

import java.nio.charset.Charset;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class AESExtraDataRecord {

    public static final short SIGNATURE = (short)0x9901;
    public static final int SIZE = 2 + 2 + 2 + 2 + 1 + 2;   // size:11
    public static final int SIZE_FIELD = 2 + 2; // 4 bytes: signature + size

    // size:2 - signature (0x9901)
    private final short signature = SIGNATURE;
    // size:2
    @Builder.Default
    private int dataSize = ExtraField.NO_DATA;
    // size:2
    @Builder.Default
    private int versionNumber = ExtraField.NO_DATA;
    // size:2
    private String vendor;
    // size:1
    @NonNull
    @Builder.Default
    private AESStrength aesStrength = AESStrength.NONE;
    // size:2
    @NonNull
    @Builder.Default
    private CompressionMethod compressionMethod = CompressionMethod.STORE;

    // TODO should be checked on set
    public byte[] getVendor(@NonNull Charset charset) {
        byte[] buf = vendor != null ? vendor.getBytes(charset) : null;

        if (ArrayUtils.getLength(buf) > 2)
            throw new ZipException("AESExtraDataRecord.vendor should be maximum 2 characters");

        return buf;
    }

    public int getLength() {
        return SIZE;
    }

    public static final AESExtraDataRecord NULL = new AESExtraDataRecord() {
        @Override
        public void setDataSize(int dataSize) {
            throw new NullPointerException("Null object modification: " + getClass().getSimpleName());
        }

        @Override
        public void setVersionNumber(int versionNumber) {
            throw new NullPointerException("Null object modification: " + getClass().getSimpleName());
        }

        @Override
        public void setVendor(String vendor) {
            throw new NullPointerException("Null object modification: " + getClass().getSimpleName());
        }

        @Override
        public void setAesStrength(AESStrength aesStrength) {
            throw new NullPointerException("Null object modification: " + getClass().getSimpleName());
        }

        @Override
        public void setCompressionMethod(CompressionMethod compressionMethod) {
            throw new NullPointerException("Null object modification: " + getClass().getSimpleName());
        }

        @Override
        public int getLength() {
            return 0;
        }
    };

}
