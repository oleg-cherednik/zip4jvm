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
package ru.olegcherednik.zip4jvm.io.readers.centraldirectory;

import ru.olegcherednik.zip4jvm.crypto.CentralDirectoryDecoder;
import ru.olegcherednik.zip4jvm.crypto.strong.DecryptionHeader;
import ru.olegcherednik.zip4jvm.crypto.strong.EncryptionAlgorithm;
import ru.olegcherednik.zip4jvm.io.Endianness;
import ru.olegcherednik.zip4jvm.io.in.buf.DiskByteArrayDataInput;
import ru.olegcherednik.zip4jvm.io.in.buf.MetadataByteArrayDataInput;
import ru.olegcherednik.zip4jvm.io.in.buf.SimpleDataInputLocation;
import ru.olegcherednik.zip4jvm.io.in.data.DataInput;
import ru.olegcherednik.zip4jvm.io.in.data.DataInputLocation;
import ru.olegcherednik.zip4jvm.io.in.file.DataInputFile;
import ru.olegcherednik.zip4jvm.io.readers.ByteArrayReader;
import ru.olegcherednik.zip4jvm.io.readers.DecryptionHeaderReader;
import ru.olegcherednik.zip4jvm.model.CentralDirectory;
import ru.olegcherednik.zip4jvm.model.Compression;
import ru.olegcherednik.zip4jvm.model.CustomizeCharset;
import ru.olegcherednik.zip4jvm.model.Zip64;
import ru.olegcherednik.zip4jvm.model.password.PasswordProvider;
import ru.olegcherednik.zip4jvm.utils.ValidationUtils;
import ru.olegcherednik.zip4jvm.utils.function.Reader;
import ru.olegcherednik.zip4jvm.utils.quitely.Quietly;

import java.util.Objects;

/**
 * see 7.3.4
 *
 * @author Oleg Cherednik
 * @since 10.10.2019
 */
public class EncryptedCentralDirectoryReader extends CentralDirectoryReader {

    private static final String DECRYPTION_HEADER = "EncryptedCentralDirectoryReader.DecryptionHeader";

    private final Zip64.ExtensibleDataSector extensibleDataSector;
    private final PasswordProvider passwordProvider;

    public EncryptedCentralDirectoryReader(long totalEntries,
                                           CustomizeCharset customizeCharset,
                                           Zip64.ExtensibleDataSector extensibleDataSector,
                                           PasswordProvider passwordProvider) {
        super(totalEntries, customizeCharset);
        this.extensibleDataSector = Objects.requireNonNull(extensibleDataSector);
        this.passwordProvider = passwordProvider;
    }

    @Override
    public CentralDirectory read(DataInput in) {
        return Quietly.doQuietly(() -> {
            ValidationUtils.requireLessOrEqual(extensibleDataSector.getUncompressedSize(),
                                               Integer.MAX_VALUE,
                                               "extensibleDataSector.uncompressedSize");

            in.mark(DECRYPTION_HEADER);

            DecryptionHeader decryptionHeader = getDecryptionHeaderReader().read(in);
            CentralDirectoryDecoder decoder = getCentralDirectoryDecoder(in.getEndianness(), decryptionHeader);
            DataInputLocation dataInputLocation = new SimpleDataInputLocation((DataInputFile)in);

            long decryptionHeaderSize = in.getMarkSize(DECRYPTION_HEADER);
            long compressedSize = extensibleDataSector.getCompressedSize() - decryptionHeaderSize;

            byte[] encrypted = getEncryptedByteArrayReader(compressedSize).read(in);
            byte[] decrypted = decoder.decrypt(encrypted);
            byte[] decompressed = decompressData(decrypted, in.getEndianness(), dataInputLocation);

            CentralDirectory centralDirectory =
                    super.read(new DiskByteArrayDataInput(decompressed, in.getEndianness(), dataInputLocation.getDisk()));
            centralDirectory.setDecryptionHeader(decryptionHeader);
            return centralDirectory;
        });
    }

    protected DecryptionHeaderReader getDecryptionHeaderReader() {
        return new DecryptionHeaderReader();
    }

    protected CentralDirectoryDecoder getCentralDirectoryDecoder(Endianness endianness, DecryptionHeader decryptionHeader) {
        char[] password = passwordProvider.getCentralDirectoryPassword();
        EncryptionAlgorithm encryptionAlgorithm = decryptionHeader.getEncryptionAlgorithm();
        CentralDirectoryEncryptionMethod encryptionMethod = CentralDirectoryEncryptionMethod.parseEncryptionAlgorithm(encryptionAlgorithm);
        return encryptionMethod.createDecoder(password, endianness, decryptionHeader);
    }

    protected Reader<byte[]> getEncryptedByteArrayReader(long size) {
        ValidationUtils.requireLessOrEqual(size, Integer.MAX_VALUE, "centralDirectoryEncryptedSize");
        return new ByteArrayReader((int)size);
    }

    private byte[] decompressData(byte[] compressed, Endianness endianness, DataInputLocation dataInputLocation) {
        Compression compression = Compression.parseCompressionMethod(extensibleDataSector.getCompressionMethod());

        if (compression == Compression.STORE)
            return compressed;

        DataInput in = new MetadataByteArrayDataInput(compressed, endianness, dataInputLocation);
        in = compression.createDataInput(in, (int)extensibleDataSector.getUncompressedSize(), dataInputLocation);

        return decompress(in);
    }

    protected byte[] decompress(DataInput in) {
        return in.readBytes((int)extensibleDataSector.getUncompressedSize());
    }

}