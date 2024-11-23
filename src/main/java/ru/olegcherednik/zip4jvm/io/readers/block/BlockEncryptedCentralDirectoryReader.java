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
import ru.olegcherednik.zip4jvm.io.in.data.RandomAccessFileBaseDataInput;
import ru.olegcherednik.zip4jvm.io.readers.DigitalSignatureReader;
import ru.olegcherednik.zip4jvm.io.readers.EncryptedCentralDirectoryReader;
import ru.olegcherednik.zip4jvm.io.readers.FileHeaderReader;
import ru.olegcherednik.zip4jvm.io.readers.block.crypto.strong.BlockDecryptionHeaderReader;
import ru.olegcherednik.zip4jvm.io.readers.crypto.strong.DecryptionHeaderReader;
import ru.olegcherednik.zip4jvm.model.CentralDirectory;
import ru.olegcherednik.zip4jvm.model.Zip64;
import ru.olegcherednik.zip4jvm.model.block.crypto.EncryptedCentralDirectoryBlock;
import ru.olegcherednik.zip4jvm.model.password.PasswordProvider;

import java.io.IOException;
import java.nio.charset.Charset;
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
    public CentralDirectory read(DataInput in) throws IOException {
        return block.calcSize((RandomAccessFileBaseDataInput) in, () -> super.read(in));
    }

    //    @Override
    //    protected CentralDirectoryDecoder createCentralDirectoryDecoder(Cipher cipher) {
    //        return new BlockCentralDirectoryDecoder(cipher, block);
    //    }

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

    //    @Override
    //    protected Reader<byte[]> getEncryptedByteArrayReader(long size) {
    //        return new BlockByteArrayReader((int) size, block.getEcdBlock());
    //    }

    //    @Override
    //    protected byte[] decompress(CompressedEcdDataInput in) throws IOException {
    //        byte[] buf = super.decompress(in);
    //        block.setDecryptedCentralDirectory(block.getDecompressedCentralDirectory());
    //        block.setDecompressedCentralDirectory(Arrays.copyOf(buf, buf.length));
    //        return buf;
    //    }

}
