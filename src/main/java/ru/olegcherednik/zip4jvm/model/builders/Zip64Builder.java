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
package ru.olegcherednik.zip4jvm.model.builders;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ArrayUtils;
import ru.olegcherednik.zip4jvm.model.Version;
import ru.olegcherednik.zip4jvm.model.Zip64;
import ru.olegcherednik.zip4jvm.model.ZipModel;

/**
 * @author Oleg Cherednik
 * @since 31.08.2019
 */
@RequiredArgsConstructor
public final class Zip64Builder {

    private final ZipModel zipModel;
    private final long disk;

    public Zip64 build() {
        return zipModel.isZip64() ? Zip64.of(createLocator(), createEndCentralDirectory()) : Zip64.NULL;
    }

    private Zip64.EndCentralDirectoryLocator createLocator() {
        Zip64.EndCentralDirectoryLocator locator = new Zip64.EndCentralDirectoryLocator();
        locator.setEndCentralDirectoryRelativeOffs(zipModel.getCentralDirectoryRelativeOffs() + zipModel.getCentralDirectorySize());
        locator.setMainDiskNo(disk);
        locator.setTotalDisks(disk + 1);
        return locator;
    }

    private Zip64.EndCentralDirectory createEndCentralDirectory() {
        byte[] extensibleDataSector = getExtensibleDataSector();
        /* see 4.3.14.1 */
        long size = Zip64.EndCentralDirectory.SIZE + extensibleDataSector.length;

        Zip64.EndCentralDirectory endCentralDirectory = new Zip64.EndCentralDirectory();
        endCentralDirectory.setEndCentralDirectorySize(size);
        endCentralDirectory.setVersionMadeBy(Version.of(Version.FileSystem.MS_DOS_OS2_NT_FAT, 20));
        endCentralDirectory.setVersionToExtract(Version.of(Version.FileSystem.MS_DOS_OS2_NT_FAT, 20));
        endCentralDirectory.setDiskNo(zipModel.getTotalDisks());
        endCentralDirectory.setMainDiskNo(zipModel.getMainDiskNo());
        endCentralDirectory.setDiskEntries(countNumberOfFileHeaderEntriesOnDisk());
        endCentralDirectory.setTotalEntries(zipModel.getTotalEntries());
        endCentralDirectory.setCentralDirectorySize(zipModel.getCentralDirectorySize());
        endCentralDirectory.setCentralDirectoryRelativeOffs(zipModel.getCentralDirectoryRelativeOffs());
//        endCentralDirectory.setExtensibleDataSector(extensibleDataSector);
        return endCentralDirectory;
    }

    private int countNumberOfFileHeaderEntriesOnDisk() {
        if (zipModel.isSplit())
            return (int)zipModel.getZipEntries().stream()
                                .filter(zipEntry -> zipEntry.getDiskNo() == zipModel.getTotalDisks())
                                .count();

        return zipModel.getTotalEntries();
    }

    /** see 4.4.27 */
    private static byte[] getExtensibleDataSector() {
        return ArrayUtils.EMPTY_BYTE_ARRAY;
    }

}
