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
package ru.olegcherednik.zip4jvm.model.settings;

import ru.olegcherednik.zip4jvm.exception.EmptyPasswordException;
import ru.olegcherednik.zip4jvm.model.Compression;
import ru.olegcherednik.zip4jvm.model.CompressionLevel;
import ru.olegcherednik.zip4jvm.model.DataDescriptorEnum;
import ru.olegcherednik.zip4jvm.model.Encryption;
import ru.olegcherednik.zip4jvm.model.ZipModel;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.ArrayUtils;

import static ru.olegcherednik.zip4jvm.utils.ValidationUtils.requireLengthLessOrEqual;
import static ru.olegcherednik.zip4jvm.utils.ValidationUtils.requireNotNull;

/**
 * @author Oleg Cherednik
 * @since 05.09.2019
 */
@Getter
@SuppressWarnings("PMD.AvoidFieldNameMatchingMethodName")
public final class ZipEntrySettings {

    public static final ZipEntrySettings DEFAULT = builder().build();

    private final Compression compression;
    private final CompressionLevel compressionLevel;
    private final Encryption encryption;
    private final char[] password;
    private final String comment;
    private final boolean zip64;
    private final boolean utf8;
    private final boolean lzmaEosMarker;
    private final DataDescriptorEnum dataDescriptor;

    public static Builder builder() {
        return new Builder();
    }

    public static ZipEntrySettings of(Compression compression) {
        if (compression == DEFAULT.getCompression())
            return DEFAULT;
        return builder().compression(compression).build();
    }

    public static ZipEntrySettings of(Compression compression, Encryption encryption, char[] password) {
        if (encryption == Encryption.OFF)
            return of(compression);
        return builder()
                .compression(compression)
                .encryption(encryption, password).build();
    }

    public static ZipEntrySettings of(Encryption encryption, char[] password) {
        if (encryption == Encryption.OFF)
            return of(DEFAULT.getCompression());
        return builder().encryption(encryption, password).build();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    private ZipEntrySettings(Builder builder) {
        compression = builder.compression;
        compressionLevel = builder.compressionLevel;
        encryption = builder.encryption;
        password = builder.password;
        comment = builder.comment;
        zip64 = builder.zip64;
        utf8 = builder.utf8;
        lzmaEosMarker = builder.lzmaEosMarker;
        dataDescriptor = builder.dataDescriptor;
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    @SuppressWarnings("PMD.UnusedAssignment")
    public static final class Builder {

        private Compression compression = Compression.DEFLATE;
        private CompressionLevel compressionLevel = CompressionLevel.NORMAL;
        private Encryption encryption = Encryption.OFF;
        private char[] password;
        private String comment;
        private boolean zip64;
        private boolean utf8 = true;
        private boolean lzmaEosMarker = true;
        private DataDescriptorEnum dataDescriptor = DataDescriptorEnum.AUTO;

        private Builder(ZipEntrySettings entrySettings) {
            compression = entrySettings.compression;
            compressionLevel = entrySettings.compressionLevel;
            encryption = entrySettings.encryption;
            password = ArrayUtils.clone(entrySettings.password);
            comment = entrySettings.comment;
            zip64 = entrySettings.zip64;
            utf8 = entrySettings.utf8;
            lzmaEosMarker = entrySettings.lzmaEosMarker;
            dataDescriptor = entrySettings.dataDescriptor;
        }

        public ZipEntrySettings build() {
            if (encryption != Encryption.OFF && ArrayUtils.isEmpty(password))
                throw new EmptyPasswordException();

            return new ZipEntrySettings(this);
        }

        public ZipEntrySettings.Builder compression(Compression compression) {
            return compression(compression, CompressionLevel.NORMAL);
        }

        public ZipEntrySettings.Builder compression(Compression compression, CompressionLevel compressionLevel) {
            this.compression = requireNotNull(compression, "ZipEntrySettings.compression");
            this.compressionLevel = requireNotNull(compressionLevel, "ZipEntrySettings.compressionLevel");
            return this;
        }

        public ZipEntrySettings.Builder encryption(Encryption encryption, char[] password) {
            this.encryption = requireNotNull(encryption, "ZipEntrySettings.encryption");

            if (encryption == Encryption.OFF)
                this.password = null;
            else {
                if (ArrayUtils.isEmpty(password))
                    throw new EmptyPasswordException();

                this.password = ArrayUtils.clone(password);
            }

            return this;
        }

        public ZipEntrySettings.Builder password(char[] password) {
            return encryption(encryption, password);
        }

        public ZipEntrySettings.Builder comment(String comment) {
            requireLengthLessOrEqual(comment, ZipModel.MAX_COMMENT_SIZE, "ZipEntry.comment");
            this.comment = comment;
            return this;
        }

        public ZipEntrySettings.Builder zip64(boolean zip64) {
            this.zip64 = zip64;
            return this;
        }

        public ZipEntrySettings.Builder utf8(boolean utf8) {
            this.utf8 = utf8;
            return this;
        }

        public ZipEntrySettings.Builder lzmaEosMarker(boolean lzmaEosMarker) {
            this.lzmaEosMarker = lzmaEosMarker;
            return this;
        }

        public ZipEntrySettings.Builder dataDescriptor(DataDescriptorEnum dataDescriptor) {
            this.dataDescriptor = dataDescriptor;
            return this;
        }

    }
}
