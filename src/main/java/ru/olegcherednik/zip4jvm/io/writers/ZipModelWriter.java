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
package ru.olegcherednik.zip4jvm.io.writers;

import ru.olegcherednik.zip4jvm.io.out.DataOutput;
import ru.olegcherednik.zip4jvm.model.CentralDirectory;
import ru.olegcherednik.zip4jvm.model.EndCentralDirectory;
import ru.olegcherednik.zip4jvm.model.Zip64;
import ru.olegcherednik.zip4jvm.model.ZipModel;
import ru.olegcherednik.zip4jvm.model.builders.CentralDirectoryBuilder;
import ru.olegcherednik.zip4jvm.model.builders.EndCentralDirectoryBuilder;
import ru.olegcherednik.zip4jvm.model.builders.Zip64Builder;
import ru.olegcherednik.zip4jvm.utils.function.Writer;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class ZipModelWriter implements Writer {

    private static final String CENTRAL_DIRECTORY_OFFS = "centralDirectoryOffs";

    private final ZipModel zipModel;

    private void updateZip64(long offs) {
        if (zipModel.getZipEntries().size() > ZipModel.MAX_TOTAL_ENTRIES)
            zipModel.setZip64(true);
        if (zipModel.getTotalDisks() > ZipModel.MAX_TOTAL_DISKS)
            zipModel.setZip64(true);
        if (offs > ZipModel.MAX_CENTRAL_DIRECTORY_OFFS)
            zipModel.setZip64(true);
    }

    private void writeCentralDirectoryHeaders(DataOutput out) {
        out.mark(CENTRAL_DIRECTORY_OFFS);
        CentralDirectory centralDirectory = new CentralDirectoryBuilder(zipModel.getZipEntries()).build();
        new CentralDirectoryWriter(centralDirectory).write(out);
        zipModel.setCentralDirectorySize(out.getMarkSize(CENTRAL_DIRECTORY_OFFS));
    }

    private void writeZip64(DataOutput out) {
        Zip64 zip64 = new Zip64Builder(zipModel, out.getDiskNo()).build();
        new Zip64Writer(zip64).write(out);
    }

    private void writeEndCentralDirectory(DataOutput out) {
        EndCentralDirectory endCentralDirectory = new EndCentralDirectoryBuilder(zipModel).build();
        new EndCentralDirectoryWriter(endCentralDirectory).write(out);
    }

    // ---------- Writer ----------

    @Override
    public void write(DataOutput out) {
        zipModel.setTotalDisks(out.getDiskNo());
        zipModel.setCentralDirectoryRelativeOffs(out.getDiskOffs());
        zipModel.setMainDiskNo(out.getDiskNo());

        updateZip64(out.getDiskOffs());
        writeCentralDirectoryHeaders(out);
        // TODO see 4.4.1.5 - these sections must be on the same disk (probably add function to block the split)
        writeZip64(out);
        writeEndCentralDirectory(out);
    }

}
