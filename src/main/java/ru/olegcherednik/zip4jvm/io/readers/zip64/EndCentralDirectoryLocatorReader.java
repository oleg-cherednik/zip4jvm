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

import ru.olegcherednik.zip4jvm.io.in.data.DataInput;
import ru.olegcherednik.zip4jvm.model.Zip64;
import ru.olegcherednik.zip4jvm.utils.quitely.Quietly;

import java.io.IOException;

import static ru.olegcherednik.zip4jvm.utils.ValidationUtils.realBigZip64;

/**
 * @author Oleg Cherednik
 * @since 29.12.2022
 */
public class EndCentralDirectoryLocatorReader {

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
