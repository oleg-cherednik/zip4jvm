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

import ru.olegcherednik.zip4jvm.exception.Zip4jvmException;
import ru.olegcherednik.zip4jvm.io.in.data.DataInput;
import ru.olegcherednik.zip4jvm.io.in.file.DataInputFile;
import ru.olegcherednik.zip4jvm.model.Zip64;
import ru.olegcherednik.zip4jvm.utils.function.FileReader;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 22.08.2019
 */
public class Zip64Reader implements FileReader<Zip64> {

    @Override
    public final Zip64 read(DataInputFile in) throws IOException {
        return read(in, false);
    }

    public final Zip64 findAndReadEndCentralDirectoryLocator(DataInputFile in) throws IOException {
        return read(in, true);
    }

    private Zip64 read(DataInputFile in, boolean locatorOnly) throws IOException {
        if (findCentralDirectoryLocatorSignature(in)) {
            Zip64.EndCentralDirectoryLocator locator = getEndCentralDirectoryLocatorReader().read(in);
            Zip64.EndCentralDirectory endCentralDirectory = null;
            Zip64.ExtensibleDataSector extensibleDataSector = null;

            if (!locatorOnly) {
                findEndCentralDirectorySignature(locator, in);

                endCentralDirectory = getEndCentralDirectoryReader().read(in);
                extensibleDataSector = readExtensibleDataSector(endCentralDirectory, in);
            }

            return Zip64.of(locator, endCentralDirectory, extensibleDataSector);
        }

        return Zip64.NULL;
    }

    private Zip64.ExtensibleDataSector readExtensibleDataSector(Zip64.EndCentralDirectory endCentralDirectory,
                                                                DataInputFile in) {
        long size = endCentralDirectory.getEndCentralDirectorySize() - Zip64.EndCentralDirectory.SIZE;

        if (size == 0)
            return null;

        long offs = in.getAbsoluteOffs();

        Zip64.ExtensibleDataSector extensibleDataSector = getExtensibleDataSectorReader().read(in);

        if (in.getAbsoluteOffs() - offs != size)
            throw new Zip4jvmException("Incorrect ExtensibleDataSector");

        return extensibleDataSector;
    }

    private static void findEndCentralDirectorySignature(Zip64.EndCentralDirectoryLocator locator, DataInputFile in)
            throws IOException {
        in.seek((int) locator.getMainDiskNo(), locator.getEndCentralDirectoryRelativeOffs());

        if (in.readDwordSignature() != Zip64.EndCentralDirectory.SIGNATURE)
            throw new Zip4jvmException("invalid zip64 end of central directory");

        in.backward(in.dwordSignatureSize());
    }

    private static boolean findCentralDirectoryLocatorSignature(DataInputFile in) throws IOException {
        if (in.getAbsoluteOffs() < Zip64.EndCentralDirectoryLocator.SIZE)
            return false;

        in.backward(Zip64.EndCentralDirectoryLocator.SIZE);

        if (in.readDwordSignature() != Zip64.EndCentralDirectoryLocator.SIGNATURE)
            return false;

        in.backward(in.dwordSignatureSize());
        return true;
    }

    protected EndCentralDirectoryLocatorReader getEndCentralDirectoryLocatorReader() {
        return new EndCentralDirectoryLocatorReader();
    }

    protected EndCentralDirectoryReader getEndCentralDirectoryReader() {
        return new EndCentralDirectoryReader();
    }

    protected ExtensibleDataSectorReader getExtensibleDataSectorReader() {
        return new ExtensibleDataSectorReader();
    }

}
