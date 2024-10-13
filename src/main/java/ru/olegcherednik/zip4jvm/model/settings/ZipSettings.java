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

import ru.olegcherednik.zip4jvm.model.ZipModel;
import ru.olegcherednik.zip4jvm.model.ZipSymlink;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;
import java.util.function.Function;

/**
 * @author Oleg Cherednik
 * @since 05.09.2019
 */
@Getter
public final class ZipSettings {

    public static final ZipSettings DEFAULT = builder().build();

    private final long splitSize;
    private final String comment;
    private final boolean zip64;
    private final Function<String, ZipEntrySettings> entrySettingsProvider;
    private final ZipSymlink zipSymlink;
    private final boolean removeRootDir;

    public static Builder builder() {
        return new Builder();
    }

    private ZipSettings(Builder builder) {
        splitSize = builder.splitSize;
        comment = builder.comment;
        zip64 = builder.zip64;
        entrySettingsProvider = builder.entrySettingsProvider;
        zipSymlink = builder.zipSymlink;
        removeRootDir = builder.removeRootDir;
    }

    public Builder toBuilder() {
        return builder().splitSize(splitSize).comment(comment).zip64(zip64)
                        .entrySettingsProvider(entrySettingsProvider);
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    @SuppressWarnings("PMD.AvoidFieldNameMatchingMethodName")
    public static final class Builder {

        private long splitSize = ZipModel.NO_SPLIT;
        private String comment;
        private boolean zip64;
        private Function<String, ZipEntrySettings> entrySettingsProvider = ZipEntrySettings.DEFAULT_PROVIDER;
        private ZipSymlink zipSymlink = ZipSymlink.IGNORE_SYMLINK;
        private boolean removeRootDir;

        public ZipSettings build() {
            return new ZipSettings(this);
        }

        public Builder splitSize(long splitSize) {
            if (splitSize > 0 && splitSize < ZipModel.MIN_SPLIT_SIZE)
                throw new IllegalArgumentException(
                        "Zip split size should be <= 0 (no split) or >= " + ZipModel.MIN_SPLIT_SIZE);

            this.splitSize = splitSize;
            return this;
        }

        public Builder comment(String comment) {
            if (StringUtils.length(comment) > ZipModel.MAX_COMMENT_SIZE)
                throw new IllegalArgumentException(
                        "File comment should be " + ZipModel.MAX_COMMENT_SIZE + " characters maximum");

            this.comment = StringUtils.isEmpty(comment) ? null : comment;
            return this;
        }

        public Builder zip64(boolean zip64) {
            this.zip64 = zip64;
            return this;
        }

        public Builder entrySettingsProvider(Function<String, ZipEntrySettings> entrySettingsProvider) {
            this.entrySettingsProvider = Optional.ofNullable(entrySettingsProvider)
                                                 .orElse(ZipEntrySettings.DEFAULT_PROVIDER);
            return this;
        }

        public Builder zipSymlink(ZipSymlink zipSymlink) {
            this.zipSymlink = Optional.ofNullable(zipSymlink).orElse(ZipSymlink.IGNORE_SYMLINK);
            return this;
        }

        public Builder removeRootDir(boolean removeRootDir) {
            this.removeRootDir = removeRootDir;
            return this;
        }

    }

}
