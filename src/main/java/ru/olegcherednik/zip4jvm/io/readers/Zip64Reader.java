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

import lombok.AllArgsConstructor;
import ru.olegcherednik.zip4jvm.exception.Zip4jvmException;
import ru.olegcherednik.zip4jvm.io.in.data.DataInput;
import ru.olegcherednik.zip4jvm.model.ExtraField;
import ru.olegcherednik.zip4jvm.model.Version;
import ru.olegcherednik.zip4jvm.model.Zip64;
import ru.olegcherednik.zip4jvm.utils.function.Reader;

import java.io.IOException;

import static ru.olegcherednik.zip4jvm.utils.ValidationUtils.realBigZip64;

/**
 * @author Oleg Cherednik
 * @since 22.08.2019
 */
public class Zip64Reader implements Reader<Zip64> {

    @Override
    public final Zip64 read(DataInput in) throws IOException {
        if (findCentralDirectoryLocatorSignature(in)) {
            Zip64.EndCentralDirectoryLocator locator = readEndCentralDirectoryLocator(in);
            findEndCentralDirectorySignature(locator, in);
            return Zip64.of(locator, readEndCentralDirectory(in));
        }

        return Zip64.NULL;
    }

    protected final Zip64 findAndReadEndCentralDirectoryLocator(DataInput in) throws IOException {
        return findCentralDirectoryLocatorSignature(in) ? Zip64.of(readEndCentralDirectoryLocator(in), null) : Zip64.NULL;
    }

    private static boolean findCentralDirectoryLocatorSignature(DataInput in) throws IOException {
        if (in.getAbsoluteOffs() < Zip64.EndCentralDirectoryLocator.SIZE)
            return false;

        in.backward(Zip64.EndCentralDirectoryLocator.SIZE);

        if (in.readDwordSignature() != Zip64.EndCentralDirectoryLocator.SIGNATURE)
            return false;

        in.backward(in.dwordSignatureSize());
        return true;
    }

    private static void findEndCentralDirectorySignature(Zip64.EndCentralDirectoryLocator locator, DataInput in) throws IOException {
        in.seek((int)locator.getMainDiskNo(), locator.getEndCentralDirectoryRelativeOffs());

        if (in.readDwordSignature() != Zip64.EndCentralDirectory.SIGNATURE)
            throw new Zip4jvmException("invalid zip64 end of central directory");

        in.backward(in.dwordSignatureSize());
    }

    protected Zip64.EndCentralDirectoryLocator readEndCentralDirectoryLocator(DataInput in) throws IOException {
        return new Zip64Reader.EndCentralDirectoryLocator().read(in);
    }

    protected Zip64.EndCentralDirectory readEndCentralDirectory(DataInput in) throws IOException {
        return new Zip64Reader.EndCentralDirectory().read(in);
    }

    private static final class EndCentralDirectoryLocator {

        public Zip64.EndCentralDirectoryLocator read(DataInput in) throws IOException {
            in.skip(in.dwordSignatureSize());

            Zip64.EndCentralDirectoryLocator locator = new Zip64.EndCentralDirectoryLocator();
            locator.setMainDiskNo(in.readDword());
            locator.setEndCentralDirectoryRelativeOffs(in.readQword());
            locator.setTotalDisks(in.readDword());

            realBigZip64(locator.getMainDiskNo(), "zip64.locator.mainDisk");
            realBigZip64(locator.getMainDiskNo(), "zip64.locator.totalDisks");
            realBigZip64(locator.getEndCentralDirectoryRelativeOffs(), "zip64.locator.centralDirectoryOffs");

            return locator;
        }

    }

    private static final class EndCentralDirectory {

        public Zip64.EndCentralDirectory read(DataInput in) throws IOException {
            in.skip(in.dwordSignatureSize());

            Zip64.EndCentralDirectory dir = new Zip64.EndCentralDirectory();
            long endCentralDirectorySize = in.readQword();
            dir.setEndCentralDirectorySize(endCentralDirectorySize);
            dir.setVersionMadeBy(Version.of(in.readWord()));
            dir.setVersionToExtract(Version.of(in.readWord()));
            dir.setDiskNo(in.readDword());
            dir.setMainDiskNo(in.readDword());
            dir.setDiskEntries(in.readQword());
            dir.setTotalEntries(in.readQword());
            dir.setCentralDirectorySize(in.readQword());
            dir.setCentralDirectoryRelativeOffs(in.readQword());
            dir.setExtensibleDataSector(in.readBytes((int)endCentralDirectorySize - Zip64.EndCentralDirectory.SIZE));

            realBigZip64(dir.getCentralDirectoryRelativeOffs(), "zip64.endCentralDirectory.centralDirectoryOffs");
            realBigZip64(dir.getTotalEntries(), "zip64.endCentralDirectory.totalEntries");

            return dir;
        }

    }

    @AllArgsConstructor
    public static final class ExtendedInfo implements Reader<Zip64.ExtendedInfo> {

        private final int size;
        private boolean uncompressedSizeExists;
        private boolean compressedSizeExists;
        private boolean offsLocalHeaderRelativeExists;
        private boolean diskExists;

        private void updateFlags(DataInput in) {
            if (uncompressedSizeExists || compressedSizeExists || offsLocalHeaderRelativeExists || diskExists)
                return;

            uncompressedSizeExists = size >= in.qwordSize();
            compressedSizeExists = size >= in.qwordSize() * 2;
            offsLocalHeaderRelativeExists = size >= in.qwordSize() * 3;
            diskExists = size >= in.qwordSize() * 3 + in.dwordSize();
        }

        @Override
        public Zip64.ExtendedInfo read(DataInput in) throws IOException {
            long offs = in.getAbsoluteOffs();
            updateFlags(in);

            Zip64.ExtendedInfo extendedInfo = readExtendedInfo(in);

            if (in.getAbsoluteOffs() - offs != size) {
                // section exists, but not need to read it; all data is in FileHeader
                extendedInfo = Zip64.ExtendedInfo.NULL;
                in.seek(offs + size);
            }

            if (extendedInfo.getDiskNo() != ExtraField.NO_DATA)
                realBigZip64(extendedInfo.getDiskNo(), "zip64.extendedInfo.disk");

            return extendedInfo;
        }

        private Zip64.ExtendedInfo readExtendedInfo(DataInput in) throws IOException {
            return Zip64.ExtendedInfo.builder()
                                     .uncompressedSize(uncompressedSizeExists ? in.readQword() : ExtraField.NO_DATA)
                                     .compressedSize(compressedSizeExists ? in.readQword() : ExtraField.NO_DATA)
                                     .localFileHeaderRelativeOffs(offsLocalHeaderRelativeExists ? in.readQword() : ExtraField.NO_DATA)
                                     .diskNo(diskExists ? in.readDword() : ExtraField.NO_DATA)
                                     .build();
        }

        @Override
        public String toString() {
            return "ZIP64";
        }

    }

}
