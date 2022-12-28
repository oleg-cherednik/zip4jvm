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
import ru.olegcherednik.zip4jvm.crypto.strong.DecryptionHeaderDecoder;
import ru.olegcherednik.zip4jvm.exception.IncorrectPasswordException;
import ru.olegcherednik.zip4jvm.exception.Zip4jvmException;
import ru.olegcherednik.zip4jvm.io.Endianness;
import ru.olegcherednik.zip4jvm.io.in.buf.MetadataByteArrayDataInput;
import ru.olegcherednik.zip4jvm.io.in.buf.SimpleDataInputLocation;
import ru.olegcherednik.zip4jvm.io.in.data.DataInput;
import ru.olegcherednik.zip4jvm.io.in.data.DataInputFile;
import ru.olegcherednik.zip4jvm.model.CentralDirectory;
import ru.olegcherednik.zip4jvm.model.Compression;
import ru.olegcherednik.zip4jvm.model.CompressionMethod;
import ru.olegcherednik.zip4jvm.model.Zip64;
import ru.olegcherednik.zip4jvm.io.in.data.DataInputLocation;
import ru.olegcherednik.zip4jvm.model.password.PasswordProvider;
import ru.olegcherednik.zip4jvm.utils.ValidationUtils;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
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
        try {
            char[] password = passwordProvider.getCentralDirectoryPassword();
            DecryptionHeaderReader decryptionHeaderReader = getDecryptionHeaderReader();
            DecryptionHeader decryptionHeader = decryptionHeaderReader.read(in);
            Cipher cipher = new DecryptionHeaderDecoder(password).readAndCreateCipher(in, decryptionHeader);
            DataInputLocation dataInputLocation = new SimpleDataInputLocation((DataInputFile)in);

            byte[] encrypted = in.readBytes((int)extensibleDataSector.getCompressedSize());

            byte[] buf = cipher.update(encrypted);
            CentralDirectoryReader centralDirectoryReader = getCentralDirectoryReader();
            CentralDirectory centralDirectory = centralDirectoryReader.read(createDataInput(buf, in.getEndianness(), dataInputLocation));
            centralDirectory.setDecryptionHeader(decryptionHeader);
            return centralDirectory;
        } catch(IncorrectPasswordException | BadPaddingException e) {
            throw new IncorrectPasswordException("Central Directory");
        } catch(Zip4jvmException e) {
            throw e;
        } catch(Exception e) {
            throw new Zip4jvmException(e);
        }
    }

    private DataInput createDataInput(byte[] buf, Endianness endianness, DataInputLocation dataInputLocation) throws Exception {
        ValidationUtils.requireLessOrEqual(extensibleDataSector.getUncompressedSize(), Integer.MAX_VALUE, "extensibleDataSector.uncompressedSize");

        MetadataByteArrayDataInput in = new MetadataByteArrayDataInput(buf, endianness, dataInputLocation);
        CompressionMethod compressionMethod = extensibleDataSector.getCompressionMethod();
        Compression compression = Compression.parseCompressionMethod(compressionMethod);
        return compression.createDataInput(in, (int)extensibleDataSector.getUncompressedSize(), in.getDataInputLocation());
    }

    protected DecryptionHeaderReader getDecryptionHeaderReader() {
        return new DecryptionHeaderReader();
    }

    protected CentralDirectoryReader getCentralDirectoryReader() {
        return new CentralDirectoryReader(totalEntries, customizeCharset);
    }
}
