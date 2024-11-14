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
package ru.olegcherednik.zip4jvm.io.in.data.ecd;

import ru.olegcherednik.zip4jvm.exception.CompressionNotSupportedException;
import ru.olegcherednik.zip4jvm.io.ByteOrder;
import ru.olegcherednik.zip4jvm.io.in.buf.ByteArrayDataInput;
import ru.olegcherednik.zip4jvm.model.CompressionMethod;
import ru.olegcherednik.zip4jvm.model.Zip64;

import lombok.Getter;

/**
 * @author Oleg Cherednik
 * @since 24.12.2022
 */
@Getter
public class CompressedEcdDataInput extends ByteArrayDataInput {

    public static CompressedEcdDataInput create(Zip64.ExtensibleDataSector extensibleDataSector,
                                                byte[] compressed,
                                                ByteOrder byteOrder) {
        CompressionMethod compressionMethod = extensibleDataSector.getCompressionMethod();
        int uncompressedSize = (int) extensibleDataSector.getUncompressedSize();

        if (compressionMethod == CompressionMethod.STORE)
            return new StoreDataInput(new ByteArrayDataInput(compressed, byteOrder), uncompressedSize);
        if (compressionMethod == CompressionMethod.DEFLATE)
            return new InflateDataInput(new ByteArrayDataInput(compressed, byteOrder), uncompressedSize);
        if (compressionMethod == CompressionMethod.ENHANCED_DEFLATE)
            return new EnhancedDeflateDataInput(new ByteArrayDataInput(compressed, byteOrder), uncompressedSize);
        if (compressionMethod == CompressionMethod.BZIP2)
            return new Bzip2DataInput(new ByteArrayDataInput(compressed, byteOrder), uncompressedSize);

        throw new CompressionNotSupportedException(compressionMethod);
    }

    protected CompressedEcdDataInput(byte[] buf, ByteOrder byteOrder) {
        super(buf, byteOrder);
    }

}
