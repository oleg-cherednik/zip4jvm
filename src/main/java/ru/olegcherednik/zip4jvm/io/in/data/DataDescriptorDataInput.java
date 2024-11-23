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
package ru.olegcherednik.zip4jvm.io.in.data;

import ru.olegcherednik.zip4jvm.io.readers.DataDescriptorReader;
import ru.olegcherednik.zip4jvm.model.CentralDirectory;
import ru.olegcherednik.zip4jvm.model.DataDescriptor;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 15.11.2024
 */
public class DataDescriptorDataInput extends BaseDecoratorDataInput {

    private final ZipEntry zipEntry;

    public static DataDescriptorDataInput create(ZipEntry zipEntry, DataInput in) {
        return new DataDescriptorDataInput(zipEntry, in);
    }

    protected DataDescriptorDataInput(ZipEntry zipEntry, DataInput in) {
        super(in);
        this.zipEntry = zipEntry;
    }

    // ---------- AutoCloseable ----------

    /**
     * Just read {@link DataDescriptor} and ignore its value. We get it from
     * {@link CentralDirectory.FileHeader}
     */
    @Override
    public void close() throws IOException {
        if (zipEntry.isDataDescriptorAvailable()) {
            DataDescriptorReader reader = DataDescriptorReader.get(zipEntry.isZip64());
            /* DataDescriptor dataDescriptor = */
            reader.read(in);
        }

        super.close();
    }

}
