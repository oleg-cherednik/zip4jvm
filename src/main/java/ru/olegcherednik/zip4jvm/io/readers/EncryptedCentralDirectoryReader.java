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

import ru.olegcherednik.zip4jvm.crypto.Decoder;
import ru.olegcherednik.zip4jvm.crypto.strong.DecryptionHeader;
import ru.olegcherednik.zip4jvm.crypto.strong.EncryptionAlgorithm;
import ru.olegcherednik.zip4jvm.io.in.DataInput;
import ru.olegcherednik.zip4jvm.io.in.decorators.LimitSizeDataInput;
import ru.olegcherednik.zip4jvm.io.in.encrypted.EncryptedDataInput;
import ru.olegcherednik.zip4jvm.io.readers.crypto.strong.DecryptionHeaderReader;
import ru.olegcherednik.zip4jvm.model.CentralDirectory;
import ru.olegcherednik.zip4jvm.model.Compression;
import ru.olegcherednik.zip4jvm.model.Zip64;
import ru.olegcherednik.zip4jvm.model.password.PasswordProvider;
import ru.olegcherednik.zip4jvm.utils.ValidationUtils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Objects;
import java.util.function.Function;

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
    public CentralDirectory read(DataInput in) throws IOException {
        ValidationUtils.requireLessOrEqual(extensibleDataSector.getUncompressedSize(),
                                           Integer.MAX_VALUE,
                                           "extensibleDataSector.uncompressedSize");

        in.mark(DECRYPTION_HEADER);
        DecryptionHeader decryptionHeader = getDecryptionHeaderReader().read(in);

        long decryptionHeaderSize = in.getMarkSize(DECRYPTION_HEADER);
        long compressedSize = extensibleDataSector.getCompressedSize() - decryptionHeaderSize;

        EncryptionAlgorithm encryptionAlgorithm = decryptionHeader.getEncryptionAlgorithm();
        Decoder decoder = encryptionAlgorithm.createEcdDecoder(decryptionHeader,
                                                               passwordProvider.getCentralDirectoryPassword(),
                                                               compressedSize,
                                                               in.getByteOrder());

        in = LimitSizeDataInput.create(compressedSize, in);
        in = EncryptedDataInput.create(decoder, in);
        in = Compression.of(extensibleDataSector.getCompressionMethod()).addCompressionDecorator(in);

        CentralDirectory centralDirectory = super.read(in);
        centralDirectory.setDecryptionHeader(decryptionHeader);
        return centralDirectory;
    }

    protected DecryptionHeaderReader getDecryptionHeaderReader() {
        return new DecryptionHeaderReader();
    }

}
