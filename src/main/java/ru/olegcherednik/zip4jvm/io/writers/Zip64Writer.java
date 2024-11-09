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
import ru.olegcherednik.zip4jvm.model.Zip64;
import ru.olegcherednik.zip4jvm.utils.function.Writer;

import lombok.RequiredArgsConstructor;

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

        private final Zip64.EndCentralDirectory ecd;

        public void write(DataOutput out) throws IOException {
            if (ecd == null)
                return;

            out.writeDwordSignature(Zip64.EndCentralDirectory.SIGNATURE);
            out.writeQword(ecd.getEndCentralDirectorySize());
            out.writeWord(ecd.getVersionMadeBy().getData());
            out.writeWord(ecd.getVersionToExtract().getData());
            out.writeDword(ecd.getDiskNo());
            out.writeDword(ecd.getMainDiskNo());
            out.writeQword(ecd.getDiskEntries());
            out.writeQword(ecd.getTotalEntries());
            out.writeQword(ecd.getCentralDirectorySize());
            out.writeQword(ecd.getCentralDirectoryRelativeOffs());
            // out.writeBytes(ecd.getExtensibleDataSector());
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

}
