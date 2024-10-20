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
package ru.olegcherednik.zip4jvm.io.readers;

import ru.olegcherednik.zip4jvm.crypto.strong.DecryptionHeader;
import ru.olegcherednik.zip4jvm.crypto.strong.cd.CentralDirectoryCipherCreator;
import ru.olegcherednik.zip4jvm.crypto.strong.cd.CentralDirectoryDecoder;
import ru.olegcherednik.zip4jvm.exception.IncorrectCentralDirectoryPasswordException;
import ru.olegcherednik.zip4jvm.exception.IncorrectPasswordException;
import ru.olegcherednik.zip4jvm.io.Endianness;
import ru.olegcherednik.zip4jvm.io.in.buf.DiskByteArrayDataInput;
import ru.olegcherednik.zip4jvm.io.in.buf.MetadataByteArrayDataInput;
import ru.olegcherednik.zip4jvm.io.in.buf.SimpleDataInputLocation;
import ru.olegcherednik.zip4jvm.io.in.data.DataInput;
import ru.olegcherednik.zip4jvm.io.in.data.DataInputLocation;
import ru.olegcherednik.zip4jvm.io.in.file.DataInputFile;
import ru.olegcherednik.zip4jvm.io.readers.crypto.strong.DecryptionHeaderReader;
import ru.olegcherednik.zip4jvm.model.CentralDirectory;
import ru.olegcherednik.zip4jvm.model.Compression;
import ru.olegcherednik.zip4jvm.model.Zip64;
import ru.olegcherednik.zip4jvm.model.password.PasswordProvider;
import ru.olegcherednik.zip4jvm.utils.ValidationUtils;
import ru.olegcherednik.zip4jvm.utils.function.Reader;

import java.nio.charset.Charset;
import java.util.Objects;
import java.util.function.Function;
import javax.crypto.Cipher;

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
                                           Function<Charset, Charset> customizeCharset,
                                           Zip64.ExtensibleDataSector extensibleDataSector,
                                           PasswordProvider passwordProvider) {
        super(totalEntries, customizeCharset);
        this.extensibleDataSector = Objects.requireNonNull(extensibleDataSector);
        this.passwordProvider = passwordProvider;
    }

    @Override
    public CentralDirectory read(DataInput in) {
        ValidationUtils.requireLessOrEqual(extensibleDataSector.getUncompressedSize(),
                                           Integer.MAX_VALUE,
                                           "extensibleDataSector.uncompressedSize");

        in.mark(DECRYPTION_HEADER);
        DecryptionHeader decryptionHeader = getDecryptionHeaderReader().read(in);
        Cipher cipher = createCipher(in.getEndianness(), decryptionHeader);
        CentralDirectoryDecoder centralDirectoryDecoder = createCentralDirectoryDecoder(cipher);
        DataInputLocation dataInputLocation = new SimpleDataInputLocation((DataInputFile) in);

        long decryptionHeaderSize = in.getMarkSize(DECRYPTION_HEADER);
        long compressedSize = extensibleDataSector.getCompressedSize() - decryptionHeaderSize;

        byte[] encrypted = getEncryptedByteArrayReader(compressedSize).read(in);
        byte[] decrypted = centralDirectoryDecoder.decrypt(encrypted, 0, encrypted.length);
        byte[] decompressed = decompressData(decrypted, in.getEndianness(), dataInputLocation);

        CentralDirectory centralDirectory =
                super.read(new DiskByteArrayDataInput(decompressed,
                                                      in.getEndianness(),
                                                      dataInputLocation.getDisk()));
        centralDirectory.setDecryptionHeader(decryptionHeader);
        return centralDirectory;
    }

    protected CentralDirectoryDecoder createCentralDirectoryDecoder(Cipher cipher) {
        return new CentralDirectoryDecoder(cipher);
    }

    private Cipher createCipher(Endianness endianness, DecryptionHeader decryptionHeader) {
        try {
            char[] password = passwordProvider.getCentralDirectoryPassword();
            CentralDirectoryCipherCreator centralDirectoryDecoder =
                    decryptionHeader.getEncryptionAlgorithm().createCentralDirectoryCipherCreator(password);

            return centralDirectoryDecoder.createCipher(endianness, decryptionHeader);
        } catch (IncorrectPasswordException e) {
            throw new IncorrectCentralDirectoryPasswordException();
        }
    }

    protected DecryptionHeaderReader getDecryptionHeaderReader() {
        return new DecryptionHeaderReader();
    }

    protected Reader<byte[]> getEncryptedByteArrayReader(long size) {
        ValidationUtils.requireLessOrEqual(size, Integer.MAX_VALUE, "centralDirectoryEncryptedSize");
        return new ByteArrayReader((int) size);
    }

    private byte[] decompressData(byte[] compressed, Endianness endianness, DataInputLocation dataInputLocation) {
        Compression compression = Compression.parseCompressionMethod(extensibleDataSector.getCompressionMethod());

        if (compression == Compression.STORE)
            return compressed;

        DataInput in = new MetadataByteArrayDataInput(compressed, endianness, dataInputLocation);
        in = compression.createDataInput(in, (int) extensibleDataSector.getUncompressedSize(), dataInputLocation);

        return decompress(in);
    }

    protected byte[] decompress(DataInput in) {
        return in.readBytes((int) extensibleDataSector.getUncompressedSize());
    }

}
