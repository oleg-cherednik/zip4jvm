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

import ru.olegcherednik.zip4jvm.exception.SignatureNotFoundException;
import ru.olegcherednik.zip4jvm.io.in.DataInput;
import ru.olegcherednik.zip4jvm.model.DataDescriptor;
import ru.olegcherednik.zip4jvm.utils.function.Reader;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 25.07.2019
 */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class DataDescriptorReader implements Reader<DataDescriptor> {

    protected final boolean doCheckSignature;
    protected final String name;

    public static DataDescriptorReader get(boolean zip64) {
        return get(zip64, true);
    }

    public static DataDescriptorReader get(boolean zip64, boolean checkSignature) {
        return zip64 ? new Zip64(checkSignature) : new Standard(checkSignature);
    }

    protected void checkSignature(DataInput in) throws IOException {
        if (!doCheckSignature)
            return;

        long offs = in.getAbsOffs();

        if (in.readDwordSignature() != DataDescriptor.SIGNATURE)
            throw new SignatureNotFoundException(DataDescriptor.SIGNATURE, name, offs);
    }

    public static class Standard extends DataDescriptorReader {

        public Standard(boolean checkSignature) {
            super(checkSignature, "DataDescriptor");
        }

        @Override
        public DataDescriptor read(DataInput in) throws IOException {
            checkSignature(in);

            long crc32 = in.readDword();
            long compressedSize = in.readDword();
            long uncompressedSize = in.readDword();

            return new DataDescriptor(crc32, compressedSize, uncompressedSize);
        }
    }

    public static class Zip64 extends DataDescriptorReader {

        public Zip64(boolean checkSignature) {
            super(checkSignature, "Zip64.DataDescriptor");
        }

        @Override
        public DataDescriptor read(DataInput in) throws IOException {
            checkSignature(in);

            long crc32 = in.readDword();
            long compressedSize = in.readQword();
            long uncompressedSize = in.readQword();

            return new DataDescriptor(crc32, compressedSize, uncompressedSize);
        }
    }

}
