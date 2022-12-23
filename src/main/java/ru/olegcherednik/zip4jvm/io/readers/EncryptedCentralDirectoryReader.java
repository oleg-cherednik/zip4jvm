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

import ru.olegcherednik.zip4jvm.crypto.strong.DecryptionHeaderDecoder;
import ru.olegcherednik.zip4jvm.exception.IncorrectPasswordException;
import ru.olegcherednik.zip4jvm.exception.Zip4jvmException;
import ru.olegcherednik.zip4jvm.io.in.buf.ByteArrayDataInput;
import ru.olegcherednik.zip4jvm.io.in.data.DataInput;
import ru.olegcherednik.zip4jvm.model.CentralDirectory;
import ru.olegcherednik.zip4jvm.model.Compression;
import ru.olegcherednik.zip4jvm.model.CompressionMethod;
import ru.olegcherednik.zip4jvm.model.Zip64;
import ru.olegcherednik.zip4jvm.model.password.PasswordProvider;
import ru.olegcherednik.zip4jvm.utils.ValidationUtils;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Objects;
import java.util.function.Function;
import java.util.zip.DataFormatException;

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
    public CentralDirectory read(DataInput in) throws IOException {
        try {
            char[] password = passwordProvider.getCentralDirectoryPassword();
            Cipher cipher = new DecryptionHeaderDecoder(password).readAndCreateCipher(in);
            byte[] buf = cipher.update(in.readBytes((int)extensibleDataSector.getCompressedSize()));
            return getCentralDirectoryReader().read(createReader(new ByteArrayDataInput(buf, in.getEndianness())));
        } catch(IncorrectPasswordException | BadPaddingException e) {
            throw new IncorrectPasswordException("Central Directory");
        } catch(Zip4jvmException | IOException e) {
            throw e;
        } catch(Exception e) {
            throw new Zip4jvmException(e);
        }
    }

    private DataInput createReader(DataInput in) throws IOException, DataFormatException {
        ValidationUtils.requireLessOrEqual(extensibleDataSector.getUncompressedSize(), Integer.MAX_VALUE, "extensibleDataSector.uncompressedSize");

        CompressionMethod compressionMethod = extensibleDataSector.getCompressionMethod();
        Compression compression = Compression.parseCompressionMethod(compressionMethod);
        return compression.createDataInput(in, (int)extensibleDataSector.getUncompressedSize());
    }

    protected CentralDirectoryReader getCentralDirectoryReader() {
        return new CentralDirectoryReader(totalEntries, customizeCharset);
    }
}
