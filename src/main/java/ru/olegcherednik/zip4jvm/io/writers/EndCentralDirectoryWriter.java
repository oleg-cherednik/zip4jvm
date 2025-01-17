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

import ru.olegcherednik.zip4jvm.io.out.DataOutput;
import ru.olegcherednik.zip4jvm.model.Charsets;
import ru.olegcherednik.zip4jvm.model.EndCentralDirectory;
import ru.olegcherednik.zip4jvm.utils.function.Writer;

import lombok.RequiredArgsConstructor;

import java.io.IOException;

/**
 * @author Oleg Cherednik
 * @since 10.04.2019
 */
@RequiredArgsConstructor
final class EndCentralDirectoryWriter implements Writer {

    private final EndCentralDirectory endCentralDirectory;

    // ---------- Writer ----------

    @Override
    public void write(DataOutput out) throws IOException {
        byte[] comment = endCentralDirectory.getComment(Charsets.UTF_8);

        out.writeDwordSignature(EndCentralDirectory.SIGNATURE);
        out.writeWord(endCentralDirectory.getTotalDisks());
        out.writeWord(endCentralDirectory.getMainDiskNo());
        out.writeWord(endCentralDirectory.getDiskEntries());
        out.writeWord(endCentralDirectory.getTotalEntries());
        out.writeDword(endCentralDirectory.getCentralDirectorySize());
        out.writeDword(endCentralDirectory.getCentralDirectoryRelativeOffs());
        out.writeWord(comment.length);
        out.writeBytes(comment);
    }

}
