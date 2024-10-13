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
package ru.olegcherednik.zip4jvm.io.readers.block;

import ru.olegcherednik.zip4jvm.io.in.data.DataInput;
import ru.olegcherednik.zip4jvm.io.readers.CentralDirectoryReader;
import ru.olegcherednik.zip4jvm.io.readers.DigitalSignatureReader;
import ru.olegcherednik.zip4jvm.io.readers.FileHeaderReader;
import ru.olegcherednik.zip4jvm.model.CentralDirectory;
import ru.olegcherednik.zip4jvm.model.block.BaseCentralDirectoryBlock;

import java.nio.charset.Charset;
import java.util.function.Function;

/**
 * @author Oleg Cherednik
 * @since 20.10.2019
 */
public class BlockCentralDirectoryReader extends CentralDirectoryReader {

    private final BaseCentralDirectoryBlock block;

    public BlockCentralDirectoryReader(long totalEntries,
                                       Function<Charset, Charset> customizeCharset,
                                       BaseCentralDirectoryBlock block) {
        super(totalEntries, customizeCharset);
        this.block = block;
    }

    @Override
    public CentralDirectory read(DataInput in) {
        return block.calcSize(in, () -> super.read(in));
    }

    @Override
    protected FileHeaderReader getFileHeaderReader() {
        return new BlockFileHeaderReader(totalEntries, customizeCharset, block);
    }

    @Override
    protected DigitalSignatureReader getDigitalSignatureReader() {
        return new BlockDigitalSignatureReader(block);
    }

}
