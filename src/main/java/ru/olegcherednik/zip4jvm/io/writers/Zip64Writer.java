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

import lombok.RequiredArgsConstructor;
import ru.olegcherednik.zip4jvm.io.out.data.DataOutput;
import ru.olegcherednik.zip4jvm.model.extrafield.ExtraField;
import ru.olegcherednik.zip4jvm.model.Zip64;
import ru.olegcherednik.zip4jvm.utils.function.Writer;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 14.08.2019
 */
@RequiredArgsConstructor
final class Zip64Writer implements Writer {

    private final Zip64 zip64;

    @Override
    public void write(DataOutput out) throws IOException {
        new EndCentralDirectory(zip64.getEndCentralDirectory()).write(out);
        new EndCentralDirectoryLocator(zip64.getEndCentralDirectoryLocator()).write(out);
    }

    @RequiredArgsConstructor
    private static final class EndCentralDirectory {

        private final Zip64.EndCentralDirectory endCentralDirectory;

        public void write(DataOutput out) throws IOException {
            if (endCentralDirectory == null)
                return;

            out.writeDwordSignature(Zip64.EndCentralDirectory.SIGNATURE);
            out.writeQword(endCentralDirectory.getEndCentralDirectorySize());
            out.writeWord(endCentralDirectory.getVersionMadeBy().getData());
            out.writeWord(endCentralDirectory.getVersionToExtract().getData());
            out.writeDword(endCentralDirectory.getDiskNo());
            out.writeDword(endCentralDirectory.getMainDiskNo());
            out.writeQword(endCentralDirectory.getDiskEntries());
            out.writeQword(endCentralDirectory.getTotalEntries());
            out.writeQword(endCentralDirectory.getCentralDirectorySize());
            out.writeQword(endCentralDirectory.getCentralDirectoryRelativeOffs());
            // out.writeBytes(endCentralDirectory.getExtensibleDataSector());
        }
    }

    @RequiredArgsConstructor
    private static final class EndCentralDirectoryLocator {

        private final Zip64.EndCentralDirectoryLocator locator;

        public void write(DataOutput out) throws IOException {
            if (locator == null)
                return;

            out.writeDwordSignature(Zip64.EndCentralDirectoryLocator.SIGNATURE);
            out.writeDword(locator.getMainDiskNo());
            out.writeQword(locator.getEndCentralDirectoryRelativeOffs());
            out.writeDword(locator.getTotalDisks());
        }
    }

    @RequiredArgsConstructor
    static final class ExtendedInfo {

        private final Zip64.ExtendedInfo info;

        public void write(DataOutput out) throws IOException {
            if (info == Zip64.ExtendedInfo.NULL)
                return;

            out.writeWordSignature(Zip64.ExtendedInfo.SIGNATURE);
            out.writeWord(info.getDataSize());

            if (info.getUncompressedSize() != ExtraField.NO_DATA)
                out.writeQword(info.getUncompressedSize());
            if (info.getCompressedSize() != ExtraField.NO_DATA)
                out.writeQword(info.getCompressedSize());
            if (info.getLocalFileHeaderRelativeOffs() != ExtraField.NO_DATA)
                out.writeQword(info.getLocalFileHeaderRelativeOffs());
            if (info.getDiskNo() != ExtraField.NO_DATA)
                out.writeDword(info.getDiskNo());
        }

    }

    @RequiredArgsConstructor
    public static final class DataDescriptor {

        private final ru.olegcherednik.zip4jvm.model.DataDescriptor dataDescriptor;

        public void write(DataOutput out) throws IOException {
            out.writeDwordSignature(ru.olegcherednik.zip4jvm.model.DataDescriptor.SIGNATURE);
            out.writeDword(dataDescriptor.getCrc32());
            out.writeQword(dataDescriptor.getCompressedSize());
            out.writeQword(dataDescriptor.getUncompressedSize());
        }

    }

}
