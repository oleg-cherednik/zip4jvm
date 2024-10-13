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
package ru.olegcherednik.zip4jvm.model.extrafield.records;

import ru.olegcherednik.zip4jvm.crypto.aes.AesStrength;
import ru.olegcherednik.zip4jvm.io.out.data.DataOutput;
import ru.olegcherednik.zip4jvm.model.Charsets;
import ru.olegcherednik.zip4jvm.model.CompressionMethod;
import ru.olegcherednik.zip4jvm.model.extrafield.PkwareExtraField;
import ru.olegcherednik.zip4jvm.utils.ValidationUtils;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Optional;

/**
 * @author Oleg Cherednik
 * @since 25.10.2019
 */
@Getter
public final class AesExtraFieldRecord implements PkwareExtraField.Record {

    public static final AesExtraFieldRecord NULL = builder().build();

    public static final int SIGNATURE = 0x9901;
    public static final int SIZE = 2 + 2 + 2 + 2 + 1 + 2;   // size:11

    // size:2 - signature (0x9901)
    // size:2
    private final int dataSize;
    // size:2
    private final int versionNumber;
    // size:2
    private final String vendor;
    // size:1
    private final AesStrength strength;
    // size:2
    private final CompressionMethod compressionMethod;

    public static Builder builder() {
        return new Builder();
    }

    private AesExtraFieldRecord(Builder builder) {
        dataSize = builder.dataSize;
        versionNumber = builder.versionNumber;
        vendor = builder.vendor;
        strength = builder.strength;
        compressionMethod = builder.compressionMethod;
    }

    public byte[] getVendor(Charset charset) {
        return vendor == null ? null : vendor.getBytes(charset);
    }

    @Override
    public int getBlockSize() {
        return this == NULL ? 0 : SIZE;
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
        return "AES Encryption Tag";
    }

    @Override
    public String toString() {
        return isNull() ? "<null>" : "strength:" + strength.getSize() + ", compression:" + compressionMethod.name();
    }

    @Override
    public void write(DataOutput out) throws IOException {
        if (this == NULL)
            return;

        out.writeWordSignature(SIGNATURE);
        out.writeWord(dataSize);
        out.writeWord(versionNumber);
        out.writeBytes(getVendor(Charsets.UTF_8));
        out.writeBytes((byte) strength.getCode());
        out.writeWord(compressionMethod.getCode());
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    @SuppressWarnings("PMD.AvoidFieldNameMatchingMethodName")
    public static final class Builder {

        private static final int MAX_VENDOR_SIZE = 2;

        private int dataSize = PkwareExtraField.NO_DATA;
        private int versionNumber = PkwareExtraField.NO_DATA;
        private String vendor;
        private AesStrength strength = AesStrength.NULL;
        private CompressionMethod compressionMethod = CompressionMethod.DEFLATE;

        public AesExtraFieldRecord build() {
            return new AesExtraFieldRecord(this);
        }

        public Builder dataSize(int dataSize) {
            this.dataSize = dataSize;
            return this;
        }

        public Builder versionNumber(int versionNumber) {
            this.versionNumber = versionNumber;
            return this;
        }

        public Builder vendor(String vendor) {
            this.vendor = ValidationUtils.requireLengthLessOrEqual(vendor,
                                                                   MAX_VENDOR_SIZE,
                                                                   "AESExtraDataRecord.vendor");
            return this;
        }

        public Builder strength(AesStrength strength) {
            this.strength = Optional.ofNullable(strength).orElse(AesStrength.NULL);
            return this;
        }

        public Builder compressionMethod(CompressionMethod compressionMethod) {
            this.compressionMethod = Optional.ofNullable(compressionMethod).orElse(CompressionMethod.DEFLATE);
            return this;
        }
    }

}
