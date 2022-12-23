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
package ru.olegcherednik.zip4jvm.io.readers;

import lombok.RequiredArgsConstructor;
import ru.olegcherednik.zip4jvm.io.in.data.DataInputFile;
import ru.olegcherednik.zip4jvm.model.Charsets;
import ru.olegcherednik.zip4jvm.model.EndCentralDirectory;
import ru.olegcherednik.zip4jvm.utils.function.FileReader;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.function.Function;

/**
 * @author Oleg Cherednik
 * @since 04.03.2019
 */
@RequiredArgsConstructor
public class EndCentralDirectoryReader implements FileReader<EndCentralDirectory> {

    private final Function<Charset, Charset> customizeCharset;

    @Override
    public EndCentralDirectory read(DataInputFile in) throws IOException {
        in.skip(in.dwordSignatureSize());

        EndCentralDirectory endCentralDirectory = new EndCentralDirectory();
        endCentralDirectory.setTotalDisks(in.readWord());
        endCentralDirectory.setMainDiskNo(in.readWord());
        endCentralDirectory.setDiskEntries(in.readWord());
        endCentralDirectory.setTotalEntries(in.readWord());
        endCentralDirectory.setCentralDirectorySize(in.readDword());
        endCentralDirectory.setCentralDirectoryRelativeOffs(in.readDword());
        int commentLength = in.readWord();
        endCentralDirectory.setComment(in.readString(commentLength, customizeCharset.apply(Charsets.IBM437)));
        return endCentralDirectory;
    }

}
