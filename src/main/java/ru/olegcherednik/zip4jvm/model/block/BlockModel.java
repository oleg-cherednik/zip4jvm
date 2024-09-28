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
package ru.olegcherednik.zip4jvm.model.block;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.MapUtils;
import ru.olegcherednik.zip4jvm.model.CentralDirectory;
import ru.olegcherednik.zip4jvm.model.EndCentralDirectory;
import ru.olegcherednik.zip4jvm.model.Zip64;
import ru.olegcherednik.zip4jvm.model.ZipModel;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

/**
 * @author Oleg Cherednik
 * @since 12.10.2019
 */
@Getter
public final class BlockModel {

    private final ZipModel zipModel;
    private final EndCentralDirectory endCentralDirectory;
    private final Zip64 zip64;
    private final CentralDirectory centralDirectory;

    private final Block endCentralDirectoryBlock;
    private final Zip64Block zip64Block;
    private final BaseCentralDirectoryBlock centralDirectoryBlock;

    private final Map<String, ZipEntryBlock> fileNameZipEntryBlock;

    public static Builder builder() {
        return new Builder();
    }

    private BlockModel(Builder builder) {
        zipModel = builder.zipModel;

        endCentralDirectory = builder.endCentralDirectory;
        zip64 = builder.zip64;
        centralDirectory = builder.centralDirectory;

        endCentralDirectoryBlock = builder.endCentralDirectoryBlock;
        zip64Block = builder.zip64Block;
        centralDirectoryBlock = builder.centralDirectoryBlock;

        fileNameZipEntryBlock = builder.zipEntries;
    }

    public ZipEntryBlock getZipEntryBlock(String fileName) {
        return fileNameZipEntryBlock.get(fileName);
    }

    public boolean isEmpty() {
        return fileNameZipEntryBlock.isEmpty();
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Builder {

        private ZipModel zipModel;

        private EndCentralDirectory endCentralDirectory;
        private Zip64 zip64;
        private CentralDirectory centralDirectory;

        private Block endCentralDirectoryBlock;
        private Zip64Block zip64Block;
        private BaseCentralDirectoryBlock centralDirectoryBlock;
        private Map<String, ZipEntryBlock> zipEntries = Collections.emptyMap();

        public BlockModel build() {
            return new BlockModel(this);
        }

        public Builder zipModel(ZipModel zipModel) {
            this.zipModel = zipModel;
            return this;
        }

        public Builder endCentralDirectory(EndCentralDirectory endCentralDirectory, Block block) {
            this.endCentralDirectory = endCentralDirectory;
            endCentralDirectoryBlock = block;
            return this;
        }

        public Builder zip64(Zip64 zip64, Zip64Block block) {
            this.zip64 = Optional.ofNullable(zip64).orElse(Zip64.NULL);
            zip64Block = block;
            return this;
        }

        public Builder centralDirectory(CentralDirectory centralDirectory, BaseCentralDirectoryBlock centralDirectoryBlock) {
            this.centralDirectory = centralDirectory;
            this.centralDirectoryBlock = centralDirectoryBlock;
            return this;
        }

        public Builder zipEntries(Map<String, ZipEntryBlock> zipEntries) {
            this.zipEntries = MapUtils.isEmpty(zipEntries) ? Collections.emptyMap() : Collections.unmodifiableMap(zipEntries);
            return this;
        }
    }
}


