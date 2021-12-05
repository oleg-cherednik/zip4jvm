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
import ru.olegcherednik.zip4jvm.model.EndCentralDirectory;
import ru.olegcherednik.zip4jvm.model.Zip64;
import ru.olegcherednik.zip4jvm.model.ZipModel;

/**
 * @author Oleg Cherednik
 * @since 31.08.2019
 */
@RequiredArgsConstructor
public final class EndCentralDirectoryBuilder {

    private final ZipModel zipModel;

    public EndCentralDirectory build() {
        EndCentralDirectory endCentralDirectory = new EndCentralDirectory();
        endCentralDirectory.setTotalDisks(getTotalDisks());
        endCentralDirectory.setMainDiskNo(getTotalDisks());
        endCentralDirectory.setDiskEntries(getDiskEntries());
        endCentralDirectory.setTotalEntries(getTotalEntries());
        endCentralDirectory.setCentralDirectorySize(getCentralDirectorySize());
        endCentralDirectory.setCentralDirectoryRelativeOffs(getCentralDirectoryRelativeOffs());
        endCentralDirectory.setComment(zipModel.getComment());
        return endCentralDirectory;
    }

    private int getTotalDisks() {
        return zipModel.isZip64() ? ZipModel.MAX_TOTAL_DISKS : zipModel.getTotalDisks();
    }

    private int getDiskEntries() {
        return zipModel.isZip64() ? ZipModel.MAX_TOTAL_ENTRIES : zipModel.getTotalEntries();
    }

    private int getTotalEntries() {
        return zipModel.isZip64() ? ZipModel.MAX_TOTAL_ENTRIES : zipModel.getTotalEntries();
    }

    private long getCentralDirectorySize() {
        return zipModel.isZip64() ? Zip64.LIMIT_DWORD : zipModel.getCentralDirectorySize();
    }

    private long getCentralDirectoryRelativeOffs() {
        return zipModel.isZip64() ? Zip64.LIMIT_DWORD : zipModel.getCentralDirectoryRelativeOffs();
    }

}
