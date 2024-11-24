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

import ru.olegcherednik.zip4jvm.io.out.DataOutput;
import ru.olegcherednik.zip4jvm.model.DataDescriptor;
import ru.olegcherednik.zip4jvm.utils.function.Writer;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 25.07.2019
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class DataDescriptorWriter implements Writer {

    public static DataDescriptorWriter get(boolean zip64, DataDescriptor dataDescriptor) {
        return zip64 ? new Zip64(dataDescriptor) : new Standard(dataDescriptor);
    }

    @RequiredArgsConstructor
    private static final class Standard extends DataDescriptorWriter {

        private final DataDescriptor dataDescriptor;

        // ---------- Writer ----------

        @Override
        public void write(DataOutput out) throws IOException {
            out.writeDwordSignature(DataDescriptor.SIGNATURE);
            out.writeDword(dataDescriptor.getCrc32());
            out.writeDword(dataDescriptor.getCompressedSize());
            out.writeDword(dataDescriptor.getUncompressedSize());
        }
    }

    @RequiredArgsConstructor
    private static final class Zip64 extends DataDescriptorWriter {

        private final DataDescriptor dataDescriptor;

        @Override
        public void write(DataOutput out) throws IOException {
            out.writeDwordSignature(DataDescriptor.SIGNATURE);
            out.writeDword(dataDescriptor.getCrc32());
            out.writeQword(dataDescriptor.getCompressedSize());
            out.writeQword(dataDescriptor.getUncompressedSize());
        }
    }

}

