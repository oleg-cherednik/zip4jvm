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

import ru.olegcherednik.zip4jvm.engine.UnzipEngine;
import ru.olegcherednik.zip4jvm.io.in.file.random.RandomAccessDataInput;
import ru.olegcherednik.zip4jvm.io.readers.BaseZipModelReader;
import ru.olegcherednik.zip4jvm.io.readers.CentralDirectoryReader;
import ru.olegcherednik.zip4jvm.io.readers.EndCentralDirectoryReader;
import ru.olegcherednik.zip4jvm.io.readers.block.zip64.BlockZip64Reader;
import ru.olegcherednik.zip4jvm.io.readers.zip64.Zip64Reader;
import ru.olegcherednik.zip4jvm.model.ZipModel;
import ru.olegcherednik.zip4jvm.model.block.BaseCentralDirectoryBlock;
import ru.olegcherednik.zip4jvm.model.block.Block;
import ru.olegcherednik.zip4jvm.model.block.BlockModel;
import ru.olegcherednik.zip4jvm.model.block.CentralDirectoryBlock;
import ru.olegcherednik.zip4jvm.model.block.Zip64Block;
import ru.olegcherednik.zip4jvm.model.block.crypto.EncryptedCentralDirectoryBlock;
import ru.olegcherednik.zip4jvm.model.builders.ZipModelBuilder;
import ru.olegcherednik.zip4jvm.model.password.PasswordProvider;
import ru.olegcherednik.zip4jvm.model.src.SrcZip;
import ru.olegcherednik.zip4jvm.utils.quitely.Quietly;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.function.Function;

/**
 * @author Oleg Cherednik
 * @since 19.10.2019
 */
public final class BlockZipModelReader extends BaseZipModelReader {

    private final Block endCentralDirectoryBlock = new Block();
    private final Zip64Block zip64Block = new Zip64Block();

    private BaseCentralDirectoryBlock centralDirectoryBlock;

    public BlockZipModelReader(SrcZip srcZip,
                               Function<Charset, Charset> customizeCharset,
                               PasswordProvider passwordProvider) {
        super(srcZip, customizeCharset, passwordProvider);
    }

    public BlockModel read() throws IOException {
        return read(false);
    }

    public BlockModel readWithEntries() throws IOException {
        return read(true);
    }

    private BlockModel read(boolean readEntries) throws IOException {
        readCentralData();

        ZipModel zipModel = new ZipModelBuilder(srcZip,
                                                endCentralDirectory,
                                                zip64,
                                                centralDirectory,
                                                customizeCharset,
                                                false).build();

        return BlockModel.builder()
                         .zipModel(zipModel)
                         .zipEntries(readEntries ? new BlockZipEntryReader(zipModel, customizeCharset).read()
                                                 : Collections.emptyMap())
                         .endCentralDirectory(endCentralDirectory, endCentralDirectoryBlock)
                         .zip64(zip64, zip64Block)
                         .centralDirectory(centralDirectory, centralDirectoryBlock).build();
    }

    @Override
    protected RandomAccessDataInput createDataInput() {
        return Quietly.doQuietly(() -> UnzipEngine.createDataInput(srcZip));
    }

    @Override
    protected EndCentralDirectoryReader getEndCentralDirectoryReader() {
        return new BlockEndCentralDirectoryReader(customizeCharset, endCentralDirectoryBlock);
    }

    @Override
    protected Zip64Reader getZip64Reader() {
        return new BlockZip64Reader(zip64Block);
    }

    @Override
    protected CentralDirectoryReader getCentralDirectoryReader(long totalEntries) {
        if (zip64.isCentralDirectoryEncrypted())
            return getEncryptedCentralDirectoryReader(totalEntries);

        centralDirectoryBlock = new CentralDirectoryBlock();
        return new BlockCentralDirectoryReader(totalEntries, customizeCharset, centralDirectoryBlock);
    }

    private CentralDirectoryReader getEncryptedCentralDirectoryReader(long totalEntries) {
        centralDirectoryBlock = new EncryptedCentralDirectoryBlock();

        return new BlockEncryptedCentralDirectoryReader(totalEntries,
                                                        customizeCharset,
                                                        zip64.getExtensibleDataSector(),
                                                        passwordProvider,
                                                        (EncryptedCentralDirectoryBlock) centralDirectoryBlock);
    }

}

