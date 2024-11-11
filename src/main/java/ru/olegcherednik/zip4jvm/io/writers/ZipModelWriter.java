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

import ru.olegcherednik.zip4jvm.io.out.data.DataOutput;
import ru.olegcherednik.zip4jvm.model.CentralDirectory;
import ru.olegcherednik.zip4jvm.model.EndCentralDirectory;
import ru.olegcherednik.zip4jvm.model.Zip64;
import ru.olegcherednik.zip4jvm.model.ZipModel;
import ru.olegcherednik.zip4jvm.model.builders.CentralDirectoryBuilder;
import ru.olegcherednik.zip4jvm.model.builders.EndCentralDirectoryBuilder;
import ru.olegcherednik.zip4jvm.model.builders.Zip64Builder;
import ru.olegcherednik.zip4jvm.utils.function.Writer;

import lombok.RequiredArgsConstructor;

import java.io.IOException;

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

    private void writeCentralDirectoryHeaders(DataOutput out) throws IOException {
        out.mark(CENTRAL_DIRECTORY_OFFS);
        CentralDirectory centralDirectory = new CentralDirectoryBuilder(zipModel.getZipEntries()).build();
        new CentralDirectoryWriter(centralDirectory).write(out);
        zipModel.setCentralDirectorySize(out.getSize(CENTRAL_DIRECTORY_OFFS));
    }

    private void writeZip64(DataOutput out) throws IOException {
        Zip64 zip64 = new Zip64Builder(zipModel, out.getDiskNo()).build();
        new Zip64Writer(zip64).write(out);
    }

    private void writeEndCentralDirectory(DataOutput out) throws IOException {
        EndCentralDirectory endCentralDirectory = new EndCentralDirectoryBuilder(zipModel).build();
        new EndCentralDirectoryWriter(endCentralDirectory).write(out);
    }

    // ---------- Writer ----------

    @Override
    public void write(DataOutput out) throws IOException {
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
