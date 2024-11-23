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

import ru.olegcherednik.zip4jvm.exception.SignatureNotFoundException;
import ru.olegcherednik.zip4jvm.io.in.data.DataInput;
import ru.olegcherednik.zip4jvm.model.Version;
import ru.olegcherednik.zip4jvm.model.Zip64;
import ru.olegcherednik.zip4jvm.utils.function.Reader;

import java.io.IOException;

import static ru.olegcherednik.zip4jvm.utils.ValidationUtils.realBigZip64;

/**
 * @author Oleg Cherednik
 * @since 29.12.2022
 */
public class EndCentralDirectoryReader implements Reader<Zip64.EndCentralDirectory> {

    @Override
    public Zip64.EndCentralDirectory read(DataInput in) throws IOException {
        checkSignature(in);

        Zip64.EndCentralDirectory ecd = new Zip64.EndCentralDirectory();
        ecd.setEndCentralDirectorySize(in.readQword());
        ecd.setVersionMadeBy(Version.of(in.readWord()));
        ecd.setVersionToExtract(Version.of(in.readWord()));
        ecd.setDiskNo(in.readDword());
        ecd.setMainDiskNo(in.readDword());
        ecd.setDiskEntries(in.readQword());
        ecd.setTotalEntries(in.readQword());
        ecd.setCentralDirectorySize(in.readQword());
        ecd.setCentralDirectoryRelativeOffs(in.readQword());

        realBigZip64(ecd.getCentralDirectoryRelativeOffs(), "zip64.endCentralDirectory.centralDirectoryOffs");
        realBigZip64(ecd.getTotalEntries(), "zip64.endCentralDirectory.totalEntries");

        return ecd;
    }

    private static void checkSignature(DataInput in) throws IOException {
        long offs = in.getAbsOffs();

        if (in.readDwordSignature() != Zip64.EndCentralDirectory.SIGNATURE)
            throw new SignatureNotFoundException(Zip64.EndCentralDirectory.SIGNATURE,
                                                 "Zip64.EndCentralDirectory",
                                                 offs);
    }

}
