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
package ru.olegcherednik.zip4jvm.io.readers.centraldirectory;

import lombok.RequiredArgsConstructor;
import ru.olegcherednik.zip4jvm.io.in.data.DataInput;
import ru.olegcherednik.zip4jvm.io.readers.DigitalSignatureReader;
import ru.olegcherednik.zip4jvm.io.readers.FileHeaderReader;
import ru.olegcherednik.zip4jvm.model.CentralDirectory;
import ru.olegcherednik.zip4jvm.model.CustomizeCharset;
import ru.olegcherednik.zip4jvm.utils.function.Reader;

/**
 * @author Oleg Cherednik
 * @since 05.03.2019
 */
@RequiredArgsConstructor
public class CentralDirectoryReader implements Reader<CentralDirectory> {

    protected final long totalEntries;
    protected final CustomizeCharset customizeCharset;

    @Override
    public CentralDirectory read(DataInput in) {
        CentralDirectory centralDirectory = new CentralDirectory();
        centralDirectory.setFileHeaders(getFileHeaderReader().read(in));
        centralDirectory.setDigitalSignature(getDigitalSignatureReader().read(in));
        return centralDirectory;
    }

    protected FileHeaderReader getFileHeaderReader() {
        return new FileHeaderReader(totalEntries, customizeCharset);
    }

    protected DigitalSignatureReader getDigitalSignatureReader() {
        return new DigitalSignatureReader();
    }
}
