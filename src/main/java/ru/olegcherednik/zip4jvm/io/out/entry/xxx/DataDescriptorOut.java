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
package ru.olegcherednik.zip4jvm.io.out.entry.xxx;

import ru.olegcherednik.zip4jvm.io.out.data.DataOutput;
import ru.olegcherednik.zip4jvm.io.writers.DataDescriptorWriter;
import ru.olegcherednik.zip4jvm.model.DataDescriptor;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 29.10.2024
 */
public final class DataDescriptorOut {

    public void write(ZipEntry zipEntry, DataOutput out) throws IOException {
        if (!zipEntry.isDataDescriptorAvailable())
            return;

        DataDescriptor dataDescriptor = new DataDescriptor(zipEntry.getChecksum(),
                                                           zipEntry.getCompressedSize(),
                                                           zipEntry.getUncompressedSize());
        DataDescriptorWriter.get(zipEntry.isZip64(), dataDescriptor).write(out);
    }

}
