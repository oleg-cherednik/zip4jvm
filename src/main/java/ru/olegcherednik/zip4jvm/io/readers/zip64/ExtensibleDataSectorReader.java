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
package ru.olegcherednik.zip4jvm.io.readers.zip64;

import ru.olegcherednik.zip4jvm.crypto.strong.Flags;
import ru.olegcherednik.zip4jvm.io.in.data.DataInput;
import ru.olegcherednik.zip4jvm.model.CompressionMethod;
import ru.olegcherednik.zip4jvm.model.Zip64;
import ru.olegcherednik.zip4jvm.utils.function.Reader;

/**
 * @author Oleg Cherednik
 * @since 29.12.2022
 */
public class ExtensibleDataSectorReader implements Reader<Zip64.ExtensibleDataSector> {

    @Override
    public Zip64.ExtensibleDataSector read(DataInput in) {
        CompressionMethod compressionMethod = CompressionMethod.parseCode(in.readWord());
        long compressedSize = in.readQword();
        long uncompressedSize = in.readQword();
        int encryptionAlgorithmCode = in.readWord();
        int bitLength = in.readWord();
        Flags flags = Flags.parseCode(in.readWord());
        int hashAlgorithmCode = in.readWord();
        int hashLength = in.readWord();
        byte[] hashData = in.readBytes(hashLength);

        return Zip64.ExtensibleDataSector.builder()
                                         .compressionMethod(compressionMethod)
                                         .compressedSize(compressedSize)
                                         .uncompressedSize(uncompressedSize)
                                         .encryptionAlgorithm(encryptionAlgorithmCode)
                                         .bitLength(bitLength)
                                         .flags(flags)
                                         .hashAlgorithm(hashAlgorithmCode)
                                         .hashLength(hashLength)
                                         .hashData(hashData).build();
    }

}
