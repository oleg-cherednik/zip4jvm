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
package ru.olegcherednik.zip4jvm.io.writers;

import ru.olegcherednik.zip4jvm.crypto.Encoder;
import ru.olegcherednik.zip4jvm.crypto.aes.AesStrength;
import ru.olegcherednik.zip4jvm.crypto.strong.EncryptionAlgorithm;
import ru.olegcherednik.zip4jvm.crypto.strong.Flag;
import ru.olegcherednik.zip4jvm.crypto.strong.HashAlgorithm;
import ru.olegcherednik.zip4jvm.crypto.strong.cd.CentralDirectoryStrongEncoderFactory;
import ru.olegcherednik.zip4jvm.crypto.strong.desede.TripleDesStrength;
import ru.olegcherednik.zip4jvm.io.out.DataOutput;
import ru.olegcherednik.zip4jvm.io.out.compressed.CompressedEntryDataOutput;
import ru.olegcherednik.zip4jvm.io.out.decorators.UncloseableDataOutput;
import ru.olegcherednik.zip4jvm.io.out.decorators.size.SizeCalcDataOutput;
import ru.olegcherednik.zip4jvm.io.out.encrypted.EncryptedDataOutput;
import ru.olegcherednik.zip4jvm.model.CentralDirectory;
import ru.olegcherednik.zip4jvm.model.Compression;
import ru.olegcherednik.zip4jvm.model.Encryption;
import ru.olegcherednik.zip4jvm.model.Zip64;
import ru.olegcherednik.zip4jvm.model.settings.CompressionLevelEnum;

import lombok.Getter;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Objects;

/**
 * Writes the central directory encrypted with the strong encryption, i.e. this
 * is the writing counterpart of
 * {@link ru.olegcherednik.zip4jvm.io.readers.EncryptedCentralDirectoryReader}.
 * <p>
 * The central directory is first compressed and only then encrypted; the
 * decryption header (see 7.2.4) is written by the {@link Encoder} itself as a
 * clear text prefix of the encrypted data.
 * <p>
 * All details required to read this data back are stored in the
 * {@link Zip64.ExtensibleDataSector} (see 7.3.4); it's available with
 * {@link #getExtensibleDataSector()} not earlier than the central directory is
 * completely written.
 *
 * @author Oleg Cherednik
 * @since 30.07.2026
 */
@SuppressWarnings("PMD.CloseResource")
public class EncryptedCentralDirectoryWriter extends CentralDirectoryWriter {

    private static final String ENCRYPTED_CENTRAL_DIRECTORY =
            "EncryptedCentralDirectoryWriter.EncryptedCentralDirectory";

    protected final Compression compression;
    protected final CompressionLevelEnum compressionLevel;
    protected final Encryption encryption;
    protected final char[] password;

    /** see 7.3.4 */
    @Getter
    private Zip64.ExtensibleDataSector extensibleDataSector;
    private long uncompressedSize;

    public EncryptedCentralDirectoryWriter(CentralDirectory centralDirectory,
                                           Compression compression,
                                           CompressionLevelEnum compressionLevel,
                                           Encryption encryption,
                                           char[] password) {
        super(centralDirectory);
        this.compression = Objects.requireNonNull(compression);
        this.compressionLevel = Objects.requireNonNull(compressionLevel);
        this.encryption = Objects.requireNonNull(encryption);
        this.password = ArrayUtils.clone(password);
    }

    protected Encoder createEncoder() {
        return CentralDirectoryStrongEncoderFactory.INSTANCE.createEncoder(password, encryption);
    }

    private DataOutput createDataOutput(DataOutput out) {
        DataOutput res = new UncloseableDataOutput(out);
        res = EncryptedDataOutput.create(createEncoder(), res);
        res = CompressedEntryDataOutput.create(compression, compressionLevel, false, res);
        return new SizeCalcDataOutput(size -> uncompressedSize = size, res);
    }

    private Zip64.ExtensibleDataSector createExtensibleDataSector(long compressedSize) {
        return Zip64.ExtensibleDataSector.builder()
                                         .compressionMethod(compression)
                                         .compressedSize(compressedSize)
                                         .uncompressedSize(uncompressedSize)
                                         .encryptionAlgorithm(getEncryptionAlgorithm().getCode())
                                         .bitLength(getBitLength())
                                         .flags(Flag.PASSWORD_KEY)
                                         // TODO no hash data is written (see 7.3.4)
                                         .hashAlgorithm(HashAlgorithm.NONE.getCode())
                                         .hashLength(0)
                                         .hashData(ArrayUtils.EMPTY_BYTE_ARRAY).build();
    }

    private EncryptionAlgorithm getEncryptionAlgorithm() {
        return EncryptionAlgorithm.parseEncryption(encryption);
    }

    private int getBitLength() {
        AesStrength aesStrength = AesStrength.of(encryption);
        return aesStrength == AesStrength.NULL ? TripleDesStrength.of(encryption).getSize() : aesStrength.getSize();
    }

    // ---------- Writer ----------

    @Override
    public void write(DataOutput out) {
        out.mark(ENCRYPTED_CENTRAL_DIRECTORY);

        try (DataOutput dst = createDataOutput(out)) {
            super.write(dst);
        }

        extensibleDataSector = createExtensibleDataSector(out.getMarkSize(ENCRYPTED_CENTRAL_DIRECTORY));
    }

}
