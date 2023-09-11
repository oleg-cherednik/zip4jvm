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

import lombok.Getter;
import ru.olegcherednik.zip4jvm.io.in.data.DataInput;
import ru.olegcherednik.zip4jvm.io.readers.LocalFileHeaderReader;
import ru.olegcherednik.zip4jvm.io.readers.extrafield.ExtraFieldReader;
import ru.olegcherednik.zip4jvm.model.CustomizeCharset;
import ru.olegcherednik.zip4jvm.model.LocalFileHeader;
import ru.olegcherednik.zip4jvm.model.block.ZipEntryBlock;
import ru.olegcherednik.zip4jvm.model.extrafield.ExtraField;

import java.nio.charset.Charset;
import java.util.function.Function;

/**
 * @author Oleg Cherednik
 * @since 20.10.2019
 */
@Getter
public class BlockLocalFileHeaderReader extends LocalFileHeaderReader {

    private final ZipEntryBlock.LocalFileHeaderBlock block = new ZipEntryBlock.LocalFileHeaderBlock();

    public BlockLocalFileHeaderReader(long absoluteOffs, CustomizeCharset customizeCharset) {
        super(absoluteOffs, customizeCharset);
    }

    @Override
    protected LocalFileHeader readLocalFileHeader(DataInput in) {
        return block.getContent().calcSize(in, () -> super.readLocalFileHeader(in));
    }

    @Override
    protected ExtraField readExtraFiled(int size, LocalFileHeader localFileHeader, DataInput in) {
        block.getContent().calcSize(in);
        return new BlockExtraFieldReader(size, ExtraFieldReader.getReaders(localFileHeader), block.getExtraFieldBlock()).read(in);
    }

}
