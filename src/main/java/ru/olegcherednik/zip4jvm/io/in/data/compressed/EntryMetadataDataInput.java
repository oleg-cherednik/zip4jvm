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
package ru.olegcherednik.zip4jvm.io.in.data.compressed;

import ru.olegcherednik.zip4jvm.io.in.data.BaseDataInput;
import ru.olegcherednik.zip4jvm.io.in.data.DataInput;
import ru.olegcherednik.zip4jvm.io.readers.DataDescriptorReader;
import ru.olegcherednik.zip4jvm.model.DataDescriptor;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;

import java.io.IOException;

/**
 * This stream reads all {@link ZipEntry} related metadata like {@link DataDescriptor}. These data are not encrypted;
 * therefore this stream cannot be used to read {@link ZipEntry} payload (that could be encrypted).
 *
 * @author Oleg Cherednik
 * @since 08.02.2020
 */
public abstract class EntryMetadataDataInput extends BaseDataInput {

    protected final ZipEntry zipEntry;

    protected EntryMetadataDataInput(DataInput in, ZipEntry zipEntry) {
        super(in);
        this.zipEntry = zipEntry;
    }

    @Override
    @SuppressWarnings("PMD.UseTryWithResources")
    public void close() throws IOException {
//        readDataDescriptor();
//        checkChecksum();
//        checkUncompressedSize();
        super.close();
    }

    /**
     * Just read {@link DataDescriptor} and ignore its value. We got it from
     * {@link ru.olegcherednik.zip4jvm.model.CentralDirectory.FileHeader}
     */
    @SuppressWarnings("UnnecessaryFullyQualifiedName")
    private void readDataDescriptor() throws IOException {
        if (zipEntry.isDataDescriptorAvailable())
            DataDescriptorReader.get(zipEntry.isZip64()).read(in);
    }

}
