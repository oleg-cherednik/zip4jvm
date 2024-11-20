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

import ru.olegcherednik.zip4jvm.io.in.buf.DiskByteArrayDataInput;
import ru.olegcherednik.zip4jvm.io.in.data.RandomAccessFileBaseDataInput;
import ru.olegcherednik.zip4jvm.io.in.data.xxx.RandomAccessDataInput;
import ru.olegcherednik.zip4jvm.io.in.data.xxx.XxxDataInput;
import ru.olegcherednik.zip4jvm.io.readers.FileHeaderReader;
import ru.olegcherednik.zip4jvm.io.readers.extrafiled.ExtraFieldReader;
import ru.olegcherednik.zip4jvm.model.CentralDirectory;
import ru.olegcherednik.zip4jvm.model.block.BaseCentralDirectoryBlock;
import ru.olegcherednik.zip4jvm.model.block.CentralDirectoryBlock;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.function.Function;

/**
 * @author Oleg Cherednik
 * @since 20.10.2019
 */
public class BlockFileHeaderReader extends FileHeaderReader {

    private final BaseCentralDirectoryBlock centralDirectoryBlock;
    private CentralDirectoryBlock.FileHeaderBlock block;

    public BlockFileHeaderReader(long totalEntries,
                                 Function<Charset, Charset> charsetCustomizer,
                                 BaseCentralDirectoryBlock centralDirectoryBlock) {
        super(totalEntries, charsetCustomizer);
        this.centralDirectoryBlock = centralDirectoryBlock;
    }

    @Override
    protected CentralDirectory.FileHeader readFileHeader(XxxDataInput in) throws IOException {
        block = centralDirectoryBlock.createFileHeaderBlock();
        CentralDirectory.FileHeader fileHeader = block.calcSize((RandomAccessDataInput) in,
                                                                () -> super.readFileHeader(in));
        centralDirectoryBlock.addFileHeader(fileHeader.getFileName(), block);
        return fileHeader;
    }

    @Override
    protected ExtraFieldReader getExtraFiledReader(int size, CentralDirectory.FileHeader fileHeader) {
        return new BlockExtraFieldReader(size, ExtraFieldReader.getReaders(fileHeader), block.getExtraFieldBlock());
    }

}
