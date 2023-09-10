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

import ru.olegcherednik.zip4jvm.crypto.strong.DecryptionHeader;
import ru.olegcherednik.zip4jvm.io.Endianness;
import ru.olegcherednik.zip4jvm.io.in.data.DataInput;
import ru.olegcherednik.zip4jvm.io.readers.DecryptionHeaderReader;
import ru.olegcherednik.zip4jvm.io.readers.DigitalSignatureReader;
import ru.olegcherednik.zip4jvm.io.readers.cd.CentralDirectoryDecoder;
import ru.olegcherednik.zip4jvm.io.readers.cd.CentralDirectoryEncryptionMethod;
import ru.olegcherednik.zip4jvm.io.readers.cd.EncryptedCentralDirectoryReader;
import ru.olegcherednik.zip4jvm.io.readers.FileHeaderReader;
import ru.olegcherednik.zip4jvm.io.readers.block.crypto.BlockDecryptionHeaderReader;
import ru.olegcherednik.zip4jvm.model.CentralDirectory;
import ru.olegcherednik.zip4jvm.model.Zip64;
import ru.olegcherednik.zip4jvm.model.block.EncryptedCentralDirectoryBlock;
import ru.olegcherednik.zip4jvm.model.password.PasswordProvider;
import ru.olegcherednik.zip4jvm.utils.function.Reader;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.function.Function;

/**
 * @author Oleg Cherednik
 * @since 10.12.2022
 */
public class BlockEncryptedCentralDirectoryReader extends EncryptedCentralDirectoryReader {

    private final EncryptedCentralDirectoryBlock block;

    public BlockEncryptedCentralDirectoryReader(long totalEntries,
                                                Function<Charset, Charset> customizeCharset,
                                                Zip64.ExtensibleDataSector extensibleDataSector,
                                                PasswordProvider passwordProvider,
                                                EncryptedCentralDirectoryBlock block) {
        super(totalEntries, customizeCharset, extensibleDataSector, passwordProvider);
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

    @Override
    protected DecryptionHeaderReader getDecryptionHeaderReader() {
        return new BlockDecryptionHeaderReader(block.getDecryptionHeaderBlock());
    }

    @Override
    protected CentralDirectoryDecoder getCentralDirectoryDecoder(Endianness endianness, DecryptionHeader decryptionHeader) {
        CentralDirectoryDecoder decoder = super.getCentralDirectoryDecoder(endianness, decryptionHeader);
        return new BlockCentralDirectoryDecoder(decoder, block);
    }

    @Override
    protected Reader<byte[]> getEncryptedByteArrayReader(long size) {
        return new BlockByteArrayReader((int)size, block.getEncryptedCentralDirectoryBlock());
    }

    @Override
    protected byte[] decompress(DataInput in) {
        byte[] buf = super.decompress(in);
        block.setDecryptedCentralDirectory(block.getDecompressedCentralDirectory());
        block.setDecompressedCentralDirectory(Arrays.copyOf(buf, buf.length));
        return buf;
    }

}
