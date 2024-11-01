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
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Oleg Cherednik
 * @since 14.11.2019
 */
public class CentralDirectoryBlock extends BaseCentralDirectoryBlock {

    private final Map<String, FileHeaderBlock> fileHeaders = new LinkedHashMap<>();
    @Getter
    @Setter
    @SuppressWarnings("PMD.ImmutableField")
    private Block digitalSignature = NULL;

    @Override
    public void addFileHeader(String fileName, FileHeaderBlock block) {
        fileHeaders.put(fileName, block);
    }

    @Override
    public FileHeaderBlock getFileHeader(String fileName) {
        return fileHeaders.get(fileName);
    }

    @Override
    public CentralDirectoryBlock.FileHeaderBlock createFileHeaderBlock() {
        return new CentralDirectoryBlock.FileHeaderBlock();
    }

    @Getter
    @RequiredArgsConstructor(access = AccessLevel.PROTECTED)
    public static class FileHeaderBlock extends Block {

        protected final ExtraFieldBlock extraFieldBlock;

        public FileHeaderBlock() {
            this(new ExtraFieldBlock());
        }

    }

}
