/*
 * Copyright 2019 Oleg Cherednik (oleg.cherednik@gmail.com)
 *
 * Licensed under The Apache Software License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

import ru.olegcherednik.zip4jvm.exception.SignatureNotFoundException;
import ru.olegcherednik.zip4jvm.exception.Zip4jvmException;
import ru.olegcherednik.zip4jvm.io.in.DataInput;
import ru.olegcherednik.zip4jvm.io.in.file.random.RandomAccessDataInput;
import ru.olegcherednik.zip4jvm.model.Zip64;
import ru.olegcherednik.zip4jvm.model.src.SrcZip;

import lombok.RequiredArgsConstructor;

import static ru.olegcherednik.zip4jvm.utils.ValidationUtils.requireLessOrEqual;

/**
 * @author Oleg Cherednik
 * @since 22.08.2019
 */
@RequiredArgsConstructor
public class Zip64Reader {

    protected final SrcZip srcZip;

    public final Zip64 read(RandomAccessDataInput in) {
        return read(in, false);
    }

    public final Zip64 findAndReadEndCentralDirectoryLocator(RandomAccessDataInput in) {
        return read(in, true);
    }

    private Zip64 read(RandomAccessDataInput in, boolean locatorOnly) {
        if (findCentralDirectoryLocatorSignature(in)) {
            Zip64.EndCentralDirectoryLocator locator = getEndCentralDirectoryLocatorReader().read(in);
            Zip64.EndCentralDirectory ecd = null;
            Zip64.ExtensibleDataSector eds = null;

            if (!locatorOnly) {
                findEndCentralDirectorySignature(locator, in);

                ecd = getEndCentralDirectoryReader().read(in);
                eds = readExtensibleDataSector(ecd, in);
            }

            return Zip64.of(locator, ecd, eds);
        }

        return Zip64.NULL;
    }

    private Zip64.ExtensibleDataSector readExtensibleDataSector(Zip64.EndCentralDirectory ecd, DataInput in) {
        long size = ecd.getEndCentralDirectorySize() - Zip64.EndCentralDirectory.SIZE;

        if (size == 0)
            return null;

        long offs = in.getAbsOffs();

        Zip64.ExtensibleDataSector eds = getExtensibleDataSectorReader().read(in);

        if (in.getAbsOffs() - offs != size)
            throw new Zip4jvmException("Incorrect ExtensibleDataSector");

        return eds;
    }

    private void findEndCentralDirectorySignature(Zip64.EndCentralDirectoryLocator locator,
                                                  RandomAccessDataInput in) {
        requireLessOrEqual(locator.getMainDiskNo(), Integer.MAX_VALUE, "zip64.locator.mainDisk");
        in.seek(srcZip.getAbsOffs((int) locator.getMainDiskNo(), locator.getEndCentralDirectoryRelativeOffs()));
        long absOffs = in.getAbsOffs();

        if (!in.isDwordSignature(Zip64.EndCentralDirectory.SIGNATURE))
            throw new SignatureNotFoundException(Zip64.EndCentralDirectory.SIGNATURE,
                                                 "Zip64.EndCentralDirectory",
                                                 absOffs);
    }

    private static boolean findCentralDirectoryLocatorSignature(RandomAccessDataInput in) {
        if (in.getAbsOffs() < Zip64.EndCentralDirectoryLocator.SIZE)
            return false;

        in.backward(Zip64.EndCentralDirectoryLocator.SIZE);
        return in.isDwordSignature(Zip64.EndCentralDirectoryLocator.SIGNATURE);
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
